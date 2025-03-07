package com.example.tangry.ui.add_user;

import android.text.TextUtils;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.tangry.repositories.UsernameRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class AddUserViewModel extends ViewModel {

    private final MutableLiveData<String> message = new MutableLiveData<>();
    private final MutableLiveData<Boolean> accountCreated = new MutableLiveData<>();
    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();

    public LiveData<String> getMessage() {
        return message;
    }

    public LiveData<Boolean> getAccountCreated() {
        return accountCreated;
    }

    public void createUser(String email, String password, String confirmPassword, String username) {

        // Basic validation
        if (TextUtils.isEmpty(email)) {
            message.setValue("Email is required");
            return;
        }
        if (TextUtils.isEmpty(username)) {
            message.setValue("Username is required");
            return;
        }
        if (password.length() < 8) {
            message.setValue("Password must be at least 8 characters");
            return;
        }
        if (!password.equals(confirmPassword)) {
            message.setValue("Passwords do not match");
            return;
        }
        
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("usernames")
            .whereEqualTo("username", username)
            .get()
            .addOnCompleteListener(usernameTask -> {
                if (usernameTask.isSuccessful() && !usernameTask.getResult().isEmpty()) {
                    message.setValue("Username already exists");
                } else {
                    db.collection("usernames")
                        .whereEqualTo("email", email)
                        .get()
                        .addOnCompleteListener(emailTask -> {
                            if (emailTask.isSuccessful() && !emailTask.getResult().isEmpty()) {
                                message.setValue("Email already in use");
                            } else {
                                mAuth.createUserWithEmailAndPassword(email, password)
                                    .addOnCompleteListener(task -> {
                                        if (task.isSuccessful()) {
                                            UsernameRepository.getInstance().saveUsernameToFirestore(username, email,
                                                docRef -> {
                                                    message.setValue("Account created successfully");
                                                    accountCreated.setValue(true);
                                                },
                                                e -> message.setValue("Registration failed: " + e.getMessage())
                                            );
                                        } else {
                                            message.setValue("Registration failed: " + task.getException().getMessage());
                                        }
                                    });
                            }
                        });
                }
            });
    }
}