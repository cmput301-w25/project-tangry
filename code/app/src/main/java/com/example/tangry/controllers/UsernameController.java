package com.example.tangry.controllers;

import com.example.tangry.repositories.UserRepository;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;

public class UsernameController {
    private final UserRepository repository;

    /**
     * Constructs a new UsernameController and initializes the UserRepository instance.
     */
    public UsernameController() {
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

    /**
     * Retrieves the username of the currently authenticated user.
     * It fetches the email from FirebaseAuth and then retrieves the username using the repository.
     *
     * @param onSuccess callback invoked upon successful retrieval, returning the username as a String
     * @param onFailure callback invoked if the retrieval fails or the user is not authenticated
     */
    public void getCurrentUsername(OnSuccessListener<String> onSuccess,
                                   OnFailureListener onFailure) {
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
            getUsername(email, onSuccess, onFailure);
        } else {
            onFailure.onFailure(new Exception("User not authenticated"));
        }
    }
}