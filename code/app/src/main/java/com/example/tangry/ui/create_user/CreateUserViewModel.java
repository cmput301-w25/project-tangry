package com.example.tangry.ui.create_user;

/**
 * CreateUserViewModel.java
 * 
 * ViewModel for the user account creation process.
 * Handles validation, Firebase authentication, and storing user data in Firestore.
 * Checks for duplicate usernames/emails and communicates results to the UI.
 */


import android.text.TextUtils;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.tangry.repositories.UserRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;

public class CreateUserViewModel extends ViewModel {

    private final MutableLiveData<String> message = new MutableLiveData<>();
    private final MutableLiveData<Boolean> accountCreated = new MutableLiveData<>();
    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();

    /**
     * Returns a LiveData instance that holds messages for user notifications.
     *
     * @return a LiveData of String messages
     */
    public LiveData<String> getMessage() {
        return message;
    }

    /**
     * Returns a LiveData instance that indicates whether an account has been successfully created.
     *
     * @return a LiveData of Boolean status for account creation
     */
    public LiveData<Boolean> getAccountCreated() {
        return accountCreated;
    }

    /**
     * Attempts to create a new user account after performing basic input validation.
     * It checks for empty fields, password requirements, and duplicate usernames or emails in Firestore.
     * If all checks pass, the method registers the user with FirebaseAuth, updates the profile with a display name,
     * and saves the username to Firestore via the UserRepository.
     *
     * @param email          the email address for the new account
     * @param password       the password for the new account
     * @param confirmPassword the confirmation of the password
     * @param username       the desired username
     */
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
        db.collection("users")
                .whereEqualTo("username", username)
                .get()
                .addOnCompleteListener(usernameTask -> {
                    if (usernameTask.isSuccessful() && !usernameTask.getResult().isEmpty()) {
                        message.setValue("Username already exists");
                    } else {
                        db.collection("users")
                                .whereEqualTo("email", email)
                                .get()
                                .addOnCompleteListener(emailTask -> {
                                    if (emailTask.isSuccessful() && !emailTask.getResult().isEmpty()) {
                                        message.setValue("Email already in use");
                                    } else {
                                        mAuth.createUserWithEmailAndPassword(email, password)
                                                .addOnCompleteListener(task -> {
                                                    if (task.isSuccessful()) {
                                                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                                                        if (firebaseUser != null) {
                                                            UserProfileChangeRequest profileUpdates =
                                                                    new UserProfileChangeRequest.Builder()
                                                                            .setDisplayName(username)
                                                                            .build();
                                                            firebaseUser.updateProfile(profileUpdates)
                                                                    .addOnCompleteListener(profileTask -> {
                                                                        if (profileTask.isSuccessful()) {
                                                                            UserRepository.getInstance().saveUsernameToFirestore(
                                                                                    username, email,
                                                                                    docRef -> {
                                                                                        message.setValue("Account created successfully. Please login");
                                                                                        accountCreated.setValue(true);
                                                                                    },
                                                                                    e -> message.setValue("Registration failed: " + e.getMessage())
                                                                            );
                                                                        } else {
                                                                            message.setValue("Failed to update profile: " + profileTask.getException().getMessage());
                                                                        }
                                                                    });
                                                        }
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