package com.example.tangry.ui.profile.personal;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class FollowRequestsViewModel extends ViewModel {

    // Model class for a follow request
    public static class FollowRequest {
        public String id;
        public String from;
        public String to;
        public boolean accepted;

        public FollowRequest() {} // Required for Firestore deserialization
    }

    private final MutableLiveData<List<FollowRequest>> sentRequests = new MutableLiveData<>();
    private final MutableLiveData<List<FollowRequest>> receivedRequests = new MutableLiveData<>();
    private final MutableLiveData<String> message = new MutableLiveData<>();

    public LiveData<List<FollowRequest>> getSentRequests() {
        return sentRequests;
    }

    public LiveData<List<FollowRequest>> getReceivedRequests() {
        return receivedRequests;
    }

    public LiveData<String> getMessage() {
        return message;
    }

    public FollowRequestsViewModel() {
        loadRequests();
    }

    public void loadRequests() {
        String currentUser = FirebaseAuth.getInstance().getCurrentUser() != null ?
                FirebaseAuth.getInstance().getCurrentUser().getDisplayName() : null;
        if (currentUser == null) {
            message.setValue("Current user not logged in");
            return;
        }
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        // Query for sent requests where current user is in "from"
        db.collection("followrequests")
                .whereEqualTo("from", currentUser)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<FollowRequest> sentList = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        FollowRequest req = doc.toObject(FollowRequest.class);
                        req.id = doc.getId();
                        sentList.add(req);
                    }
                    sentRequests.setValue(sentList);
                })
                .addOnFailureListener(e ->
                        message.setValue("Failed to load sent requests: " + e.getMessage()));

        // Query for received requests where current user is in "to"
        db.collection("followrequests")
                .whereEqualTo("to", currentUser)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<FollowRequest> receivedList = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        FollowRequest req = doc.toObject(FollowRequest.class);
                        req.id = doc.getId();
                        receivedList.add(req);
                    }
                    receivedRequests.setValue(receivedList);
                })
                .addOnFailureListener(e ->
                        message.setValue("Failed to load received requests: " + e.getMessage()));
    }

    public void acceptRequest(final FollowRequest request) {
        if (request == null) return;
        String currentUser = FirebaseAuth.getInstance().getCurrentUser() != null ?
                FirebaseAuth.getInstance().getCurrentUser().getDisplayName() : null;
        if (currentUser == null) {
            message.setValue("Current user not logged in");
            return;
        }
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // First, query to find the followrequest using "from" (sender) and "to" (current user)
        db.collection("followrequests")
                .whereEqualTo("from", request.from)
                .whereEqualTo("to", currentUser)
                .limit(1)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (querySnapshot.isEmpty()) {
                        message.setValue("Follow request not found");
                        return;
                    }
                    String requestId = querySnapshot.getDocuments().get(0).getId();
                    // Mark the request as accepted
                    db.collection("followrequests").document(requestId)
                            .update("accepted", true)
                            .addOnSuccessListener(aVoid -> {
                                // Query for current user's document by username to update followers
                                db.collection("users")
                                        .whereEqualTo("username", currentUser)
                                        .limit(1)
                                        .get()
                                        .addOnSuccessListener(userSnapshot -> {
                                            if (userSnapshot.isEmpty()) {
                                                message.setValue("Current user document not found");
                                                return;
                                            }
                                            String currentUserDocId = userSnapshot.getDocuments().get(0).getId();
                                            db.collection("users").document(currentUserDocId)
                                                    .update("followers", FieldValue.arrayUnion(request.from))
                                                    .addOnSuccessListener(aVoid1 -> {
                                                        // Query for sender's document by username to update followings
                                                        db.collection("users")
                                                                .whereEqualTo("username", request.from)
                                                                .limit(1)
                                                                .get()
                                                                .addOnSuccessListener(senderSnapshot -> {
                                                                    if (senderSnapshot.isEmpty()) {
                                                                        message.setValue("Sender user document not found");
                                                                        return;
                                                                    }
                                                                    String senderDocId = senderSnapshot.getDocuments().get(0).getId();
                                                                    db.collection("users").document(senderDocId)
                                                                            .update("followings", FieldValue.arrayUnion(currentUser))
                                                                            .addOnSuccessListener(aVoid2 -> {
                                                                                message.setValue("Follow request accepted");
                                                                                loadRequests();
                                                                            })
                                                                            .addOnFailureListener(e ->
                                                                                    message.setValue("Failed to update sender followings: " + e.getMessage()));
                                                                })
                                                                .addOnFailureListener(e ->
                                                                        message.setValue("Failed to query sender document: " + e.getMessage()));
                                                    })
                                                    .addOnFailureListener(e ->
                                                            message.setValue("Failed to update current user followers: " + e.getMessage()));
                                        })
                                        .addOnFailureListener(e ->
                                                message.setValue("Failed to query current user document: " + e.getMessage()));
                            })
                            .addOnFailureListener(e ->
                                    message.setValue("Failed to update follow request: " + e.getMessage()));
                })
                .addOnFailureListener(e ->
                        message.setValue("Failed to query follow request: " + e.getMessage()));
    }

    public void denyRequest(final FollowRequest request) {
        if (request == null) return;
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("followrequests").document(request.id)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    message.setValue("Follow request denied");
                    loadRequests();
                })
                .addOnFailureListener(e ->
                        message.setValue("Failed to delete request: " + e.getMessage()));
    }
}