package com.example.tangry.ui.profile.personal;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.tangry.R;
import com.example.tangry.repositories.UserRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.auth.User;

public class PersonalProfileFragment extends Fragment {

    private PersonalProfileViewModel viewModel;

    public PersonalProfileFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_personal_profile, container, false);
    }

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
                Navigation.findNavController(v).navigate(R.id.action_personalProfileFragment_to_followRequestsFragment)
        );
    }
}