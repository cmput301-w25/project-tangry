package com.example.tangry.ui.login;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class LoginViewModel extends ViewModel {

    private final MutableLiveData<String> username = new MutableLiveData<>();
    private final MutableLiveData<String> password = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoggedIn = new MutableLiveData<>(false);

    public void setUsername(String user) {
        username.setValue(user);
    }

    public void setPassword(String pass) {
        password.setValue(pass);
    }

    public LiveData<Boolean> getIsLoggedIn() {
        return isLoggedIn;
    }

    public void login() {
        // Placeholder logic: Accept any username/password that's non-empty
        if (username.getValue() != null && !username.getValue().isEmpty()
                && password.getValue() != null && !password.getValue().isEmpty()) {
            isLoggedIn.setValue(true);
        } else {
            isLoggedIn.setValue(false);
        }
    }
}
