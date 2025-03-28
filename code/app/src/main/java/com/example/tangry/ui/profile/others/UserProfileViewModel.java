package com.example.tangry.ui.profile.others;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.FieldValue;

import java.util.ArrayList;
import java.util.List;

/**
 * ViewModel for handling user profile follow functionalities.
 * It manages follow status, follow requests, and updates the UI accordingly.
 */
public class UserProfileViewModel extends ViewModel {

    private final MutableLiveData<Boolean> followButtonEnabled = new MutableLiveData<>(true);
    private final MutableLiveData<String> followMessage = new MutableLiveData<>();
    private final MutableLiveData<List<String>> myFollowings = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<String>> mySentFollowRequests = new MutableLiveData<>(new ArrayList<>());
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    /**
     * Gets the LiveData for the follow button enabled state.
     *
     * @return LiveData<Boolean> representing if follow button is enabled.
     */
    public LiveData<Boolean> getFollowButtonEnabled() {
        return followButtonEnabled;
    }

    /**
     * Gets the LiveData for follow-related messages.
     *
     * @return LiveData<String> representing follow messages.
     */
    public LiveData<String> getFollowMessage() {
        return followMessage;
    }

    /**
     * Loads the current user's followings and sent follow requests from Firebase,
     * then updates the follow button state with respect to the target username.
     *
     * @param targetUsername the username of the profile being viewed.
     */
    public void loadFollowStatus(String targetUsername) {
        String currentEmail = FirebaseAuth.getInstance().getCurrentUser() != null ?
                FirebaseAuth.getInstance().getCurrentUser().getEmail() : null;
        String currentUserDisplay = FirebaseAuth.getInstance().getCurrentUser() != null ?
                FirebaseAuth.getInstance().getCurrentUser().getDisplayName() : null;
        if (currentEmail == null || currentUserDisplay == null) {
            followMessage.setValue("User not logged in");
            followButtonEnabled.setValue(false);
            return;
        }

        // Query for the current user's document to get the "followings" array.
        db.collection("users")
                .whereEqualTo("email", currentEmail)
                .limit(1)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        DocumentSnapshot doc = querySnapshot.getDocuments().get(0);
                        List<String> followingList = (List<String>) doc.get("followings");
                        myFollowings.setValue(followingList != null ? followingList : new ArrayList<>());
                        updateFollowButtonState(targetUsername);
                    }
                });

        // Query the followrequests collection for requests sent from the current user.
        db.collection("followrequests")
                .whereEqualTo("from", currentUserDisplay)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<String> reqList = new ArrayList<>();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        String toUser = doc.getString("to");
                        if (toUser != null) {
                            reqList.add(toUser);
                        }
                    }
                    mySentFollowRequests.setValue(reqList);
                    updateFollowButtonState(targetUsername);
                });
    }

    /**
     * Updates the state of the follow button based on whether the target user is already 
     * followed or if a follow request has already been sent.
     *
     * @param targetUsername the username of the target profile.
     */
    private void updateFollowButtonState(String targetUsername) {
        List<String> followings = myFollowings.getValue();
        List<String> requests = mySentFollowRequests.getValue();
        if ((followings != null && followings.contains(targetUsername)) ||
                (requests != null && requests.contains(targetUsername))) {
            followButtonEnabled.setValue(false);
        } else {
            followButtonEnabled.setValue(true);
        }
    }

    /**
     * Sends a follow request from the current user to the target username.
     * Displays messages based on success or failure.
     *
     * @param targetUsername the username of the user to follow.
     */
    public void sendFollowRequest(String targetUsername) {
        String currentUser = FirebaseAuth.getInstance().getCurrentUser() != null ?
                FirebaseAuth.getInstance().getCurrentUser().getDisplayName() : null;
        if (currentUser == null) {
            followMessage.setValue("User not logged in");
            return;
        }
        if (currentUser.equals(targetUsername)) {
            followMessage.setValue("Cannot follow yourself");
            return;
        }
        db.collection("followrequests")
                .add(new FollowRequest(currentUser, targetUsername, false))
                .addOnSuccessListener(documentReference -> {
                    followMessage.setValue("Follow request sent");
                    // Reload follow status since a new request was sent.
                    loadFollowStatus(targetUsername);
                })
                .addOnFailureListener(e ->
                        followMessage.setValue("Error sending follow request: " + e.getMessage()));
    }

    /**
     * Model representing a follow request.
     */
    public static class FollowRequest {
        public String from;
        public String to;
        public boolean accepted;

        /**
         * Default constructor required for Firebase data deserialization.
         */
        public FollowRequest() { }

        /**
         * Constructs a new FollowRequest with the specified details.
         *
         * @param from the username sending the follow request.
         * @param to the username receiving the follow request.
         * @param accepted the acceptance status of the request.
         */
        public FollowRequest(String from, String to, boolean accepted) {
            this.from = from;
            this.to = to;
            this.accepted = accepted;
        }
    }
}