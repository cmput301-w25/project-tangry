/**
 * MapViewModel.java
 *
 * This ViewModel holds the data used by the MapFragment. It exposes a LiveData object containing a text message
 * that can be observed by the UI. The default message indicates that this is the map fragment.
 *
 * Outstanding Issues:
 * - Additional data related to map functionality (e.g., markers, location data) can be added in future iterations.
 */

package com.example.tangry.ui.map;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class MapViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    /**
     * Constructs a new MapViewModel and initializes the text LiveData with a default value.
     */
    public MapViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is map fragment");
    }

    /**
     * Returns the LiveData containing the text message for the MapFragment.
     *
     * @return a LiveData instance holding the map fragment's text message.
     */
    public LiveData<String> getText() {
        return mText;
    }
}
