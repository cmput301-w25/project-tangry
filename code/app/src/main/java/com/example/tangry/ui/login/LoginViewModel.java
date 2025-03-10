/**
 * LoginViewModel.java
 *
 * This ViewModel is responsible for managing the login state and credentials for the LoginFragment.
 * It holds the user's email (as username) and password, as well as a flag for the "Remember Me" option.
 * The ViewModel interacts with FirebaseAuth to attempt login and exposes a LiveData<Boolean> to
 * indicate whether the user is logged in.
 *
 * Outstanding Issues:
 * - Additional input validation (e.g., proper email format) can be implemented.
 * - Error messages could be exposed via LiveData for more detailed user feedback.
 */

package com.example.tangry.ui.login;

import android.text.TextUtils;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginViewModel extends ViewModel {

    private static final String TAG = "LoginViewModel";

    private final MutableLiveData<String> username = new MutableLiveData<>();
    private final MutableLiveData<String> password = new MutableLiveData<>();
    private final MutableLiveData<Boolean> rememberMe = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> isLoggedIn = new MutableLiveData<>(false);

    /**
     * Sets the email (username) for the login process.
     *
     * @param user the email address of the user.
     */
    public void setEmail(String user) {
        username.setValue(user);
    }

    /**
     * Sets the password for the login process.
     *
     * @param pass the password of the user.
     */
    public void setPassword(String pass) {
        password.setValue(pass);
    }

    /**
     * Sets the "Remember Me" flag.
     *
     * @param remember true if the user wants to be remembered; false otherwise.
     */
    public void setRememberMe(boolean remember) {
        rememberMe.setValue(remember);
    }

    /**
     * Returns a LiveData instance indicating the login status.
     *
     * @return LiveData<Boolean> that is true when login is successful.
     */
    public LiveData<Boolean> getIsLoggedIn() {
        return isLoggedIn;
    }

    /**
     * Attempts to sign in the user with the provided email and password using FirebaseAuth.
     * If the email or password is empty, the login fails immediately.
     */
    public void login() {
        String email = username.getValue();
        String pass = password.getValue();
        Log.d(TAG, "Attempting login with email: " + email);
        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(pass)) {
            isLoggedIn.setValue(false);
            return;
        }
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        mAuth.signInWithEmailAndPassword(email, pass)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        isLoggedIn.setValue(user != null);
                        Log.d(TAG, "Login successful");
                    } else {
                        isLoggedIn.setValue(false);
                        Log.e(TAG, "Login failed: ", task.getException());
                    }
                });
    }
}
