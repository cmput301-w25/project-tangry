/**
 * FriendsFragment.java
 *
 * This fragment displays the friends-related UI within the application. It uses a ViewModel
 * (FriendsViewModel) to observe and update UI components, such as a TextView displaying friend-related data.
 * The fragment uses View Binding to reference its views.
 *
 * Outstanding Issues:
 * - Currently, the fragment only displays a simple text view. Additional functionality and UI elements
 *   for managing or displaying a list of friends may be implemented in future iterations.
 */

package com.example.tangry.ui.friends;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.example.tangry.databinding.FragmentFriendsBinding;

public class FriendsFragment extends Fragment {
    /**
     * Default public constructor.
     */
    public FriendsFragment() {
        // Required empty public constructor
    }

    private FragmentFriendsBinding binding;

    /**
     * Called to have the fragment instantiate its user interface view.
     *
     * @param inflater           The LayoutInflater object that can be used to inflate any views in the fragment,
     *
     * @param container          The parent view that the fragment's UI should be attached to.
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state.
     * @return The root View for the fragment's UI.
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        FriendsViewModel friendsViewModel =
                new ViewModelProvider(this).get(FriendsViewModel.class);

        binding = FragmentFriendsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textFriends;
        friendsViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;
    }

    /**
     * Called when the view previously created by onCreateView() is being destroyed.
     * This method cleans up the binding to avoid memory leaks.
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
