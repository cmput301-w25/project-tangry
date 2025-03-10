/**
 * FriendsViewModel.java
 *
 * This ViewModel class holds the UI-related data for the FriendsFragment. It exposes a LiveData object
 * containing a text message that is observed by the fragment to update the UI. The initial text message
 * is set to a default value.
 *
 * Outstanding Issues:
 * - Additional data or complex logic for handling friend-related operations can be added in future iterations.
 */

package com.example.tangry.ui.friends;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class FriendsViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    /**
     * Constructs a new FriendsViewModel and initializes the text LiveData with a default message.
     */
    public FriendsViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is Friends fragment");
    }

    /**
     * Returns the LiveData object containing the text message for the FriendsFragment.
     *
     * @return a LiveData containing the friends fragment text message
     */
    public LiveData<String> getText() {
        return mText;
    }
}
