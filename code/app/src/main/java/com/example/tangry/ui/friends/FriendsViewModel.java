/**
 * FriendsViewModel.java
 *
 * This ViewModel class holds the UI-related data for the FriendsFragment. It exposes a LiveData object
 * containing a text message that is observed by the fragment to update the UI. The initial text message
 * is set to a default value.
 *
 * Outstanding Issues:
 * - Additional data or complex logic for handling friend-related operations can be added in future iterations.
 */

package com.example.tangry.ui.friends;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.tangry.repositories.UserRepository;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

public class FriendsViewModel extends ViewModel {
    private final MutableLiveData<List<String>> friendsList = new MutableLiveData<>();
    private final MutableLiveData<String> message = new MutableLiveData<>();

    public FriendsViewModel() {
        loadFriendsList();
    }

    public LiveData<List<String>> getFriendsList() {
        return friendsList;
    }

    public LiveData<String> getMessage() {
        return message;
    }

    public void loadFriendsList() {
        String email = FirebaseAuth.getInstance().getCurrentUser() != null ?
                FirebaseAuth.getInstance().getCurrentUser().getEmail() : null;
        if (email == null) {
            message.setValue("User not logged in");
            return;
        }
        UserRepository.getInstance().getFriendsList(email,
                new OnSuccessListener<List<String>>() {
                    @Override
                    public void onSuccess(List<String> friends) {
                        friendsList.setValue(friends);
                    }
                },
                new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        message.setValue("Failed to load friends: " + e.getMessage());
                    }
                });
    }
}