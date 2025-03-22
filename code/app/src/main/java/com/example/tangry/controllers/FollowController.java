package com.example.tangry.controllers;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FollowController {

    private final FirebaseFirestore db;

    public FollowController() {
        db = FirebaseFirestore.getInstance();
    }

    // Static inner class representing a follow request document.
    public static class FollowRequest {
        public String id;
        public String from;
        public String to;
        public boolean accepted;

        public FollowRequest() { }

        public FollowRequest(String id, String from, String to, boolean accepted) {
            this.id = id;
            this.from = from;
            this.to = to;
            this.accepted = accepted;
        }
    }

    // Sends a follow request from 'fromUser' to 'toUser'.
    public void sendFollowRequest(String fromUser, String toUser,
                                  OnSuccessListener<DocumentReference> successListener,
                                  OnFailureListener failureListener) {
        Map<String, Object> data = new HashMap<>();
        data.put("from", fromUser);
        data.put("to", toUser);
        data.put("accepted", false);
        db.collection("followrequests").add(data)
                .addOnSuccessListener(successListener)
                .addOnFailureListener(failureListener);
    }

    // Accepts a follow request.
    // Marks the follow request as accepted and updates the involved user documents.
    public void acceptFollowRequest(FollowRequest request,
                                    OnSuccessListener<Void> successListener,
                                    OnFailureListener failureListener) {
        String currentUser = FirebaseAuth.getInstance().getCurrentUser() != null ?
                FirebaseAuth.getInstance().getCurrentUser().getDisplayName() : null;
        if (currentUser == null) {
            failureListener.onFailure(new Exception("Current user not logged in"));
            return;
        }
        // First, update the follow request document.
        db.collection("followrequests").document(request.id)
                .update("accepted", true)
                .addOnSuccessListener(aVoid -> {
                    // Update current user's "followers" array.
                    db.collection("users")
                            .whereEqualTo("username", currentUser)
                            .limit(1)
                            .get()
                            .addOnSuccessListener(querySnapshot -> {
                                if (querySnapshot.isEmpty()) {
                                    failureListener.onFailure(new Exception("Current user document not found"));
                                    return;
                                }
                                String currentUserDocId = querySnapshot.getDocuments().get(0).getId();
                                db.collection("users").document(currentUserDocId)
                                        .update("followers", FieldValue.arrayUnion(request.from))
                                        .addOnSuccessListener(aVoid1 -> {
                                            // Update sender's "followings" array.
                                            db.collection("users")
                                                    .whereEqualTo("username", request.from)
                                                    .limit(1)
                                                    .get()
                                                    .addOnSuccessListener(senderQuery -> {
                                                        if (senderQuery.isEmpty()) {
                                                            failureListener.onFailure(new Exception("Sender user document not found"));
                                                            return;
                                                        }
                                                        String senderDocId = senderQuery.getDocuments().get(0).getId();
                                                        db.collection("users").document(senderDocId)
                                                                .update("followings", FieldValue.arrayUnion(currentUser))
                                                                .addOnSuccessListener(aVoid2 -> successListener.onSuccess(null))
                                                                .addOnFailureListener(failureListener);
                                                    })
                                                    .addOnFailureListener(failureListener);
                                        })
                                        .addOnFailureListener(failureListener);
                            })
                            .addOnFailureListener(failureListener);
                })
                .addOnFailureListener(failureListener);
    }

    // Denies a follow request by deleting its document.
    public void denyFollowRequest(String requestId,
                                  OnSuccessListener<Void> successListener,
                                  OnFailureListener failureListener) {
        db.collection("followrequests").document(requestId)
                .delete()
                .addOnSuccessListener(successListener)
                .addOnFailureListener(failureListener);
    }

    /**
     * Loads the current follow status.
     * Queries for the current user's document (by email) to obtain the "followings" array,
     * and queries the "followrequests" collection (where 'from' equals currentUsername)
     * to obtain a list of users to which a follow request has been sent.
     *
     * @param currentEmail    The current user's email.
     * @param currentUsername The current user's display name.
     * @param successListener Receives a FollowStatus object on success.
     * @param failureListener Called when a query fails.
     */
    public void loadFollowStatus(String currentEmail, String currentUsername,
                                 OnSuccessListener<FollowStatus> successListener,
                                 OnFailureListener failureListener) {
        FollowStatus status = new FollowStatus();
        db.collection("users")
                .whereEqualTo("email", currentEmail)
                .limit(1)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        DocumentSnapshot doc = querySnapshot.getDocuments().get(0);
                        List<String> followingList = (List<String>) doc.get("followings");
                        status.setFollowings(followingList != null ? followingList : new ArrayList<>());
                    } else {
                        status.setFollowings(new ArrayList<>());
                    }
                    // Next, get sent follow requests.
                    db.collection("followrequests")
                            .whereEqualTo("from", currentUsername)
                            .get()
                            .addOnSuccessListener(querySnapshot2 -> {
                                List<String> sentRequests = new ArrayList<>();
                                for (DocumentSnapshot doc2 : querySnapshot2.getDocuments()) {
                                    String toUser = doc2.getString("to");
                                    if (toUser != null) {
                                        sentRequests.add(toUser);
                                    }
                                }
                                status.setSentFollowRequests(sentRequests);
                                successListener.onSuccess(status);
                            })
                            .addOnFailureListener(failureListener);
                })
                .addOnFailureListener(failureListener);
    }

    // Utility class representing follow status.
    public static class FollowStatus {
        private List<String> followings;
        private List<String> sentFollowRequests;

        public List<String> getFollowings() {
            return followings;
        }

        public void setFollowings(List<String> followings) {
            this.followings = followings;
        }

        public List<String> getSentFollowRequests() {
            return sentFollowRequests;
        }

        public void setSentFollowRequests(List<String> sentFollowRequests) {
            this.sentFollowRequests = sentFollowRequests;
        }
    }
}