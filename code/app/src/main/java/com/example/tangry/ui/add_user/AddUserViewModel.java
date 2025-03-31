package com.example.tangry.ui.add_user;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.tangry.controllers.FollowController;
import com.example.tangry.controllers.FollowController.FollowStatus;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

/**
 * ViewModel for managing user interactions in the Add User screen.
 * It handles searching users, sending follow requests, and tracking follow statuses.
 */
public class AddUserViewModel extends ViewModel {

    private final MutableLiveData<List<String>> searchResults = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<String>> followings = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<String>> sentFollowRequests = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<String> message = new MutableLiveData<>();

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FollowController followController = new FollowController();

    /**
     * Gets the LiveData for the list of usernames found from a search.
     *
     * @return LiveData list of usernames matching the search criteria.
     */
    public LiveData<List<String>> getSearchResults() {
        return searchResults;
    }

    /**
     * Gets the LiveData for the list of usernames that the current user is following.
     *
     * @return LiveData list of followed usernames.
     */
    public LiveData<List<String>> getFollowings() {
        return followings;
    }

    /**
     * Gets the LiveData for the list of usernames to whom the current user has sent follow requests.
     *
     * @return LiveData list of usernames with pending follow requests.
     */
    public LiveData<List<String>> getSentFollowRequests() {
        return sentFollowRequests;
    }

    /**
     * Gets the LiveData for messages or notifications.
     *
     * @return LiveData containing message strings for UI display.
     */
    public LiveData<String> getMessage() {
        return message;
    }

    /**
     * Loads the current user's follow status.
     * Delegates to FollowController to fetch both followed users and sent follow requests.
     * Sets an error message if the user is not logged in or if an error occurs.
     */
    public void loadFollowStatus() {
        String currentEmail = FirebaseAuth.getInstance().getCurrentUser() != null ?
                FirebaseAuth.getInstance().getCurrentUser().getEmail() : null;
        String currentUsername = FirebaseAuth.getInstance().getCurrentUser() != null ?
                FirebaseAuth.getInstance().getCurrentUser().getDisplayName() : null;
        if (currentEmail == null || currentUsername == null) {
            message.setValue("User not logged in");
            return;
        }
        followController.loadFollowStatus(currentEmail, currentUsername,
                status -> {
                    followings.setValue(status.getFollowings());
                    sentFollowRequests.setValue(status.getSentFollowRequests());
                },
                e -> message.setValue("Error loading follow status: " + e.getMessage())
        );
    }

    /**
     * Searches for a user by their username.
     * Performs substring matching rather than exact matching.
     * On success, updates the searchResults LiveData and sets a message if no user is found.
     *
     * @param query the username to search for.
     */
    public void searchUser(String query) {
        // Get all users and filter client-side for substring matches
        db.collection("users")
                .orderBy("username")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<String> results = new ArrayList<>();

                    // Perform case-insensitive substring matching
                    String lowercaseQuery = query.toLowerCase();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        String username = doc.getString("username");
                        if (username != null && username.toLowerCase().contains(lowercaseQuery)) {
                            results.add(username);
                        }
                    }

                    searchResults.setValue(results);
                    if (results.isEmpty()) {
                        message.setValue("No user found containing: " + query);
                    }
                })
                .addOnFailureListener(e -> message.setValue("Error searching user: " + e.getMessage()));
    }

    /**
     * Sends a follow request to the specified target user.
     * Validates that the current user is logged in and is not trying to follow themselves.
     * On success, notifies the UI and reloads follow status.
     *
     * @param targetUsername the username of the user to send a follow request to.
     */
    public void followUser(String targetUsername) {
        String currentUsername = FirebaseAuth.getInstance().getCurrentUser() != null ?
                FirebaseAuth.getInstance().getCurrentUser().getDisplayName() : null;
        if (currentUsername == null) {
            message.setValue("User not logged in");
            return;
        }
        if (currentUsername.equals(targetUsername)) {
            message.setValue("You cannot follow yourself");
            return;
        }
        followController.sendFollowRequest(currentUsername, targetUsername,
                documentReference -> {
                    message.setValue("Follow request sent");
                    loadFollowStatus();
                },
                e -> message.setValue("Error sending follow request: " + e.getMessage())
        );
    }

    /**
     * Checks if the current user is already following or has already sent a follow request to the target user.
     *
     * @param targetUsername the username to check against followings and sent follow requests.
     * @return true if the target username is already in the followings or sent follow requests; false otherwise.
     */
    public boolean isAlreadyFollowingOrRequested(String targetUsername) {
        List<String> followingList = followings.getValue();
        List<String> requests = sentFollowRequests.getValue();
        return (followingList != null && followingList.contains(targetUsername)) ||
                (requests != null && requests.contains(targetUsername));
    }
}