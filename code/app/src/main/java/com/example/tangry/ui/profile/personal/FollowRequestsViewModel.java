package com.example.tangry.ui.profile.personal;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.tangry.controllers.FollowController;
import com.example.tangry.controllers.FollowController.FollowRequest;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * ViewModel to manage follow requests data for the user.
 */
public class FollowRequestsViewModel extends ViewModel {

    private final MutableLiveData<List<FollowRequest>> sentRequests = new MutableLiveData<>();
    private final MutableLiveData<List<FollowRequest>> receivedRequests = new MutableLiveData<>();
    private final MutableLiveData<String> message = new MutableLiveData<>();

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FollowController followController = new FollowController();

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

    /**
     * Loads sent and received follow requests for the current user.
     */
    public void loadRequests() {
        String currentUser = FirebaseAuth.getInstance().getCurrentUser() != null ?
                FirebaseAuth.getInstance().getCurrentUser().getDisplayName() : null;
        if (currentUser == null) {
            message.setValue("Current user not logged in");
            return;
        }

        // Load sent requests (where current user is the sender).
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

        // Load received requests (where current user is the recipient).
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

    /**
     * Accepts a follow request by delegating to FollowController.
     *
     * @param request the FollowRequest to accept.
     */
    public void acceptRequest(final FollowRequest request) {
        if (request == null) return;
        followController.acceptFollowRequest(request,
                aVoid -> {
                    message.setValue("Follow request accepted");
                    loadRequests();
                },
                e -> message.setValue("Failed to accept follow request: " + e.getMessage())
        );
    }

    /**
     * Denies a follow request by delegating to FollowController.
     *
     * @param request the FollowRequest to deny.
     */
    public void denyRequest(final FollowRequest request) {
        if (request == null) return;
        followController.denyFollowRequest(request.id,
                aVoid -> {
                    message.setValue("Follow request denied");
                    loadRequests();
                },
                e -> message.setValue("Failed to deny follow request: " + e.getMessage())
        );
    }
}