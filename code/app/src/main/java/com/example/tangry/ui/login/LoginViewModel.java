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

    public void setEmail(String user) {
        username.setValue(user);
    }

    public void setPassword(String pass) {
        password.setValue(pass);
    }

    public void setRememberMe(boolean remember) {
        rememberMe.setValue(remember);
    }

    public LiveData<Boolean> getIsLoggedIn() {
        return isLoggedIn;
    }

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