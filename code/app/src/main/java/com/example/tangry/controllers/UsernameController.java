package com.example.tangry.controllers;

import com.example.tangry.repositories.UsernameRepository;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

public class UsernameController {
    private final UsernameRepository repository;

    public UsernameController() {
        this.repository = UsernameRepository.getInstance();
    }

    public void getUsername(String email,
                            OnSuccessListener<String> onSuccess,
                            OnFailureListener onFailure) {
        repository.getUsernameFromEmail(email, onSuccess, onFailure);
    }
}
