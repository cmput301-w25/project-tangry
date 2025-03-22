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

public class AddUserViewModel extends ViewModel {

    private final MutableLiveData<List<String>> searchResults = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<String>> followings = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<String>> sentFollowRequests = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<String> message = new MutableLiveData<>();

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FollowController followController = new FollowController();

    public LiveData<List<String>> getSearchResults() {
        return searchResults;
    }

    public LiveData<List<String>> getFollowings() {
        return followings;
    }

    public LiveData<List<String>> getSentFollowRequests() {
        return sentFollowRequests;
    }

    public LiveData<String> getMessage() {
        return message;
    }

    /**
     * Loads the current user's follow status (both followed users and pending follow requests)
     * by delegating to FollowController.
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
     * Searches for a user by username.
     */
    public void searchUser(String query) {
        db.collection("users")
                .whereEqualTo("username", query)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<String> results = new ArrayList<>();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        String username = doc.getString("username");
                        if (username != null) {
                            results.add(username);
                        }
                    }
                    searchResults.setValue(results);
                    if (results.isEmpty()) {
                        message.setValue("No user found with username: " + query);
                    }
                })
                .addOnFailureListener(e -> message.setValue("Error searching user: " + e.getMessage()));
    }

    /**
     * Sends a follow request to the target user by delegating to FollowController.
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
     * Checks whether the current user is already following or has requested to follow the target user.
     */
    public boolean isAlreadyFollowingOrRequested(String targetUsername) {
        List<String> followingList = followings.getValue();
        List<String> requests = sentFollowRequests.getValue();
        return (followingList != null && followingList.contains(targetUsername)) ||
                (requests != null && requests.contains(targetUsername));
    }
}