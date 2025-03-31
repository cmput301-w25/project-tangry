/**
 * FollowController.java
 * 
 * This file contains the controller implementation for managing user relationships in the Tangry
 * application. It handles the lifecycle of follow requests and connections between users.
 * 
 * Key features:
 * - Manages sending and processing of follow requests between users
 * - Handles acceptance and rejection of pending follow requests
 * - Updates follower and following lists for connected users
 * - Maintains relationship status tracking between users
 * - Interacts with Firestore to persist relationship data
 * - Provides utility classes for representing follow requests and status
 */
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

/**
 * Controller for handling follow requests and follow relationships between users.
 */
public class FollowController {

    private final FirebaseFirestore db;

    /**
     * Constructs a new FollowController and initializes the Firestore instance.
     */
    public FollowController() {
        db = FirebaseFirestore.getInstance();
    }

    /**
     * Represents a follow request document.
     */
    public static class FollowRequest {
        /**
         * The document ID of the follow request.
         */
        public String id;
        /**
         * The username of the sender.
         */
        public String from;
        /**
         * The username of the recipient.
         */
        public String to;
        /**
         * Flag indicating if the follow request has been accepted.
         */
        public boolean accepted;

        /**
         * Default constructor.
         */
        public FollowRequest() { }

        /**
         * Constructs a FollowRequest with specified parameters.
         *
         * @param id       The document ID of the follow request.
         * @param from     The username of the sender.
         * @param to       The username of the recipient.
         * @param accepted True if the request is accepted; otherwise, false.
         */
        public FollowRequest(String id, String from, String to, boolean accepted) {
            this.id = id;
            this.from = from;
            this.to = to;
            this.accepted = accepted;
        }
    }

    /**
     * Sends a follow request from one user to another.
     *
     * @param fromUser        The username of the sender.
     * @param toUser          The username of the recipient.
     * @param successListener Listener called with the DocumentReference on success.
     * @param failureListener Listener called with an Exception on failure.
     */
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

    /**
     * Accepts a follow request by marking it as accepted and updating the involved user documents.
     * First updates the follow request document, then updates the follower list of the current user,
     * and finally updates the followings list of the sender.
     *
     * @param request         The FollowRequest to accept.
     * @param successListener Listener called when the operation is successful.
     * @param failureListener Listener called with an Exception if an error occurs.
     */
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

    /**
     * Denies a follow request by deleting its document from the followrequests collection.
     *
     * @param requestId       The ID of the follow request document to delete.
     * @param successListener Listener called when deletion is successful.
     * @param failureListener Listener called with an Exception if deletion fails.
     */
    public void denyFollowRequest(String requestId,
                                  OnSuccessListener<Void> successListener,
                                  OnFailureListener failureListener) {
        db.collection("followrequests").document(requestId)
                .delete()
                .addOnSuccessListener(successListener)
                .addOnFailureListener(failureListener);
    }

    /**
     * Loads the current follow status for a user.
     * Retrieves the user's followings list by querying the user's document based on email,
     * and retrieves the list of sent follow requests by querying the followrequests collection.
     *
     * @param currentEmail    The current user's email address.
     * @param currentUsername The current user's display name.
     * @param successListener Listener called with a FollowStatus object on success.
     * @param failureListener Listener called with an Exception if any query fails.
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

    /**
     * Utility class representing the follow status of a user.
     * Contains the list of users the current user is following and the list of sent follow requests.
     */
    public static class FollowStatus {
        private List<String> followings;
        private List<String> sentFollowRequests;

        /**
         * Gets the list of users the current user is following.
         *
         * @return A list of usernames representing followings.
         */
        public List<String> getFollowings() {
            return followings;
        }

        /**
         * Sets the list of users the current user is following.
         *
         * @param followings A list of usernames.
         */
        public void setFollowings(List<String> followings) {
            this.followings = followings;
        }

        /**
         * Gets the list of usernames to which the current user has sent follow requests.
         *
         * @return A list of usernames representing sent follow requests.
         */
        public List<String> getSentFollowRequests() {
            return sentFollowRequests;
        }

        /**
         * Sets the list of usernames to which the current user has sent follow requests.
         *
         * @param sentFollowRequests A list of usernames.
         */
        public void setSentFollowRequests(List<String> sentFollowRequests) {
            this.sentFollowRequests = sentFollowRequests;
        }
    }
}