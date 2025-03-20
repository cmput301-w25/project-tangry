package com.example.tangry.ui.profile.others;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.tangry.repositories.UserRepository;
import com.google.firebase.auth.FirebaseAuth;

public class UserProfileViewModel extends ViewModel {

    private final MutableLiveData<String> followMessage = new MutableLiveData<>();

    public LiveData<String> getFollowMessage() {
        return followMessage;
    }

    /**
     * Sends a follow request for the profile user.
     */
    public void sendFollowRequest(String targetUsername) {
        String currentEmail = FirebaseAuth.getInstance().getCurrentUser() != null ?
                FirebaseAuth.getInstance().getCurrentUser().getEmail() : null;

        if (currentEmail == null || currentEmail.isEmpty()) {
            followMessage.setValue("Current user not logged in.");
            return;
        }

        // Retrieve current user's username from UserRepository.
        UserRepository.getInstance().getUsernameFromEmail(currentEmail, currentUsername -> {
            if (currentUsername == null || currentUsername.isEmpty()) {
                followMessage.setValue("Current user's username not found.");
            } else if (currentUsername.equals(targetUsername)) {
                followMessage.setValue("You cannot follow yourself.");
            } else {
                UserRepository.getInstance().sendFollowRequest(currentUsername, targetUsername,
                        aVoid -> followMessage.setValue("Follow request sent to " + targetUsername),
                        e -> followMessage.setValue("Follow request failed: " + e.getMessage())
                );
            }
        }, e -> followMessage.setValue("Error retrieving current user's username: " + e.getMessage()));
    }
}