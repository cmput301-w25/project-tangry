package com.example.tangry.ui.login;

import android.text.TextUtils;
import android.widget.Toast;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.tangry.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginViewModel extends ViewModel {

    private final MutableLiveData<String> username = new MutableLiveData<>();
    private final MutableLiveData<String> password = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoggedIn = new MutableLiveData<>(false);

    public void setEmail(String user) {
        username.setValue(user);
    }

    public void setPassword(String pass) {
        password.setValue(pass);
    }

    public LiveData<Boolean> getIsLoggedIn() {
        return isLoggedIn;
    }

    public void login() {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        if (TextUtils.isEmpty(username.getValue()) || TextUtils.isEmpty(password.getValue())) {
            isLoggedIn.setValue(false);
            return;
        }

        mAuth.signInWithEmailAndPassword(username.getValue(), password.getValue())
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    FirebaseUser user = mAuth.getCurrentUser();
                    isLoggedIn.setValue(user != null);
                } else {
                    isLoggedIn.setValue(false);
                }
            });
    }
}
