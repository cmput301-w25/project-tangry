/**
 * UsernameController.java
 *
 * This controller provides a simple interface for retrieving a username based on an email address.
 * It acts as a mediator between the view layer and the UsernameRepository, abstracting the underlying
 * logic of fetching the username data from the data source (e.g., Firestore). The controller uses the
 * singleton instance of the repository to ensure consistent access to username data.
 *
 * Outstanding Issues:
 * - Consider implementing caching mechanisms for username lookups to reduce repeated network calls.
 * - Enhance error handling and logging to provide more detailed feedback to the view layer.
 */

package com.example.tangry.controllers;

import com.example.tangry.repositories.UsernameRepository;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

public class UsernameController {
    private final UsernameRepository repository;

    /**
     * Constructs a new UsernameController and initializes the UsernameRepository instance.
     */
    public UsernameController() {
        this.repository = UsernameRepository.getInstance();
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
}
