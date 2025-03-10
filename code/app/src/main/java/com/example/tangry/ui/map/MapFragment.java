/**
 * MapFragment.java
 *
 * This fragment displays a map-related UI using data provided by the MapViewModel. It uses view binding
 * to reference its views, and observes a LiveData object from the ViewModel to update the UI accordingly.
 *
 * Outstanding Issues:
 * - Additional map functionality (e.g., markers, user location) may be added in future iterations.
 */

package com.example.tangry.ui.map;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.example.tangry.databinding.FragmentMapBinding;

public class MapFragment extends Fragment {

    /**
     * Default empty public constructor.
     */
    public MapFragment() {
        // Required empty public constructor
    }

    private FragmentMapBinding binding;

    /**
     * Inflates the fragment layout, initializes view binding, and sets up observation of the ViewModel data.
     *
     * @param inflater           The LayoutInflater object that can be used to inflate any views in the fragment.
     * @param container          The parent view that the fragment's UI should be attached to.
     * @param savedInstanceState A Bundle containing previously saved state, if any.
     * @return The root View of the inflated layout.
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        MapViewModel mapViewModel = new ViewModelProvider(this).get(MapViewModel.class);
        binding = FragmentMapBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textMap;
        mapViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;
    }

    /**
     * Cleans up the binding when the view is destroyed to prevent memory leaks.
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
