package com.example.tangry.controllers;

import com.example.tangry.repositories.UserRepository;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.Query;

public class UserController {
    private final UserRepository repository;

    /**
     * Constructs a new UserController and initializes the UserRepository instance.
     */
    public UserController() {
        this.repository = UserRepository.getInstance();
    }

    /**
     * Retrieves the username associated with the specified email address.
     *
     * @param email     the email address for which to fetch the username
     * @param onSuccess callback invoked upon successful retrieval, returning the username as a String
     * @param onFailure callback invoked if the retrieval fails
     */
    public void getUsername(String email,
                            OnSuccessListener<String> onSuccess,
                            OnFailureListener onFailure) {
        repository.getUsernameFromEmail(email, onSuccess, onFailure);
    }

    //Increment karma using email (should be username once constraint done)
    public void incrementKarma(String email,
                               OnSuccessListener<Void> onSuccess,
                               OnFailureListener onFailure, int incrementAmount) {
        repository.incrementKarmaByEmail(email, onSuccess, onFailure, incrementAmount);
    }

    // Expose the top users query for the leaderboard.
    public Query getTopUsersQuery() {
        return repository.getTopUsersQuery();
    }
}