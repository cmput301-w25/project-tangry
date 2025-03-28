package com.example.tangry.ui.profile.personal;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import com.example.tangry.R;
import com.example.tangry.repositories.UserRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.auth.User;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

/**
 * Fragment that displays the personal profile of the current user.
 */
public class PersonalProfileFragment extends Fragment {

    private PersonalProfileViewModel viewModel;

    /**
     * Default public constructor required for fragment instantiation.
     */
    public PersonalProfileFragment() {
        // Required empty public constructor
    }

    /**
     * Called to have the fragment instantiate its user interface view.
     *
     * @param inflater           The LayoutInflater object that can be used to inflate any views in the fragment.
     * @param container          If non-null, this is the parent view that the fragment's UI should be attached to.
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state as given here.
     * @return The View for the fragment's UI or null.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_personal_profile, container, false);
    }

    /**
     * Called immediately after onCreateView has returned.
     * This method initializes the view model and populates the UI with the current user's information.
     *
     * @param view               The View returned by onCreateView.
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state as given here.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(PersonalProfileViewModel.class);

        // Populate personal info with current user data
        TextView personalInfoTextView = view.findViewById(R.id.text_personal_info);
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String email = currentUser.getEmail();
            String username = currentUser.getDisplayName();
            personalInfoTextView.setText("Email: " + email + "\nUsername: " + username);
        } else {
            personalInfoTextView.setText("User info not available");
        }

        Button followRequestsButton = view.findViewById(R.id.button_follow_requests);
        followRequestsButton.setOnClickListener(v ->
                Navigation.findNavController(v)
                          .navigate(R.id.action_personalProfileFragment_to_followRequestsFragment)
        );
    }
}