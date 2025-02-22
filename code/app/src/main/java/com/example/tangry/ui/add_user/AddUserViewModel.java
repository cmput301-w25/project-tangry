package com.example.tangry.ui.add_user;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class AddUserViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public AddUserViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is add user fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}