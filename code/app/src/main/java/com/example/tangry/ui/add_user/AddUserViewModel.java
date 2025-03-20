package com.example.tangry.ui.add_user;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.tangry.repositories.UserRepository;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class AddUserViewModel extends ViewModel {
    private final MutableLiveData<List<String>> searchResults = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<String> followSuccessMessage = new MutableLiveData<>();

    public LiveData<List<String>> getSearchResults() {
        return searchResults;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<String> getFollowSuccessMessage() {
        return followSuccessMessage;
    }

    /**
     * Search for users by prefix.
     */
    public void searchUser(String query) {
        UserRepository.getInstance().searchUsersByPrefix(query,
                querySnapshot -> {
                    List<String> results = new ArrayList<>();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        String username = doc.getString("username");
                        if (username != null) {
                            results.add(username);
                        }
                    }
                    searchResults.setValue(results);
                },
                e -> errorMessage.setValue("Search failed: " + e.getMessage())
        );
    }

    /**
     * Sends a follow request.
     * Retrieves the current user's username from Firestore ("users" collection) using their email.
     * Prevents the user from following themselves.
     */
    public void followUser(String targetUsername) {
        String currentUserEmail = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getEmail() : null;
        if (currentUserEmail == null || currentUserEmail.isEmpty()) {
            errorMessage.setValue("Current user has no email set.");
            return;
        }

        // Query the user database to get the current user's username.
        UserRepository.getInstance().getUsernameFromEmail(currentUserEmail,
                currentUsername -> {
                    if (currentUsername == null || currentUsername.isEmpty()) {
                        errorMessage.setValue("Current user's username not found.");
                    } else if (currentUsername.equals(targetUsername)) {
                        errorMessage.setValue("You cannot follow yourself.");
                    } else {
                        // Send the follow request using the retrieved username.
                        UserRepository.getInstance().sendFollowRequest(currentUsername, targetUsername,
                                documentReference -> followSuccessMessage.setValue("Follow request sent to " + targetUsername),
                                e -> errorMessage.setValue("Follow request failed: " + e.getMessage())
                        );
                    }
                },
                e -> errorMessage.setValue("Error retrieving current user's username: " + e.getMessage())
        );
    }
}