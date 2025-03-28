/**
 * CreateUserFragment.java
 *
 * This fragment provides the UI for creating a new user account. It binds input fields for email,
 * username, password, and confirm password, and interacts with CreateUserViewModel to handle account
 * creation. On successful account creation, it navigates back by popping the fragment back stack.
 *
 * Outstanding Issues:
 * - We considering adding more robust input validation and error handling.
 * - UI improvements could include progress indicators while account creation is in progress.
 */

package com.example.tangry.ui.create_user;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.tangry.R;
import com.example.tangry.databinding.FragmentAddUserBinding;
import com.example.tangry.databinding.FragmentCreateUserBinding;

public class CreateUserFragment extends Fragment {

    private @NonNull FragmentCreateUserBinding binding;
    private CreateUserViewModel viewModel;

    /**
     * Inflates the fragment layout, initializes the ViewModel, and sets up observers for UI events.
     *
     * @param inflater           the LayoutInflater object that can be used to inflate any views in the fragment
     * @param container          the parent ViewGroup (if non-null) that the fragment's UI should be attached to
     * @param savedInstanceState a Bundle containing previous saved state, if any
     * @return the root View of the inflated layout
     */
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentCreateUserBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(CreateUserViewModel.class);

        // Observe message updates from ViewModel to display user notifications
        viewModel.getMessage().observe(getViewLifecycleOwner(), message -> {
            if (message != null) {
                Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
            }
        });

        // Observe account creation status to navigate back when successful
        viewModel.getAccountCreated().observe(getViewLifecycleOwner(), accountCreated -> {
            if (accountCreated != null && accountCreated) {
                getParentFragmentManager().popBackStack();
            }
        });

        // Set click listener for the "Create Account" button to initiate user creation
        binding.createAccountButton.setOnClickListener(v -> {
            String email = binding.emailInput.getText().toString().trim();
            String username = binding.usernameInput.getText().toString().trim();
            String password = binding.passwordInput.getText().toString();
            String confirmPassword = binding.passwordInput2.getText().toString();

            viewModel.createUser(email, password, confirmPassword, username);
        });

        return root;
    }

    /**
     * Hides the support action bar and toolbars when the fragment resumes.
     */
    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() instanceof AppCompatActivity) {
            ((AppCompatActivity)getActivity()).getSupportActionBar().hide();
        }
        // Hide primary and secondary toolbars
        View toolbarPrimary = getActivity().findViewById(R.id.toolbar_primary);
        if (toolbarPrimary != null) {
            toolbarPrimary.setVisibility(View.GONE);
        }
        View toolbarSecondary = getActivity().findViewById(R.id.toolbar_secondary);
        if (toolbarSecondary != null) {
            toolbarSecondary.setVisibility(View.GONE);
        }
    }

    /**
     * Shows the support action bar and toolbars when the fragment pauses.
     */
    @Override
    public void onPause() {
        super.onPause();
        if (getActivity() instanceof AppCompatActivity) {
            ((AppCompatActivity)getActivity()).getSupportActionBar().show();
        }
        // Show primary and secondary toolbars when the fragment pauses
        View toolbarPrimary = getActivity().findViewById(R.id.toolbar_primary);
        if (toolbarPrimary != null) {
            toolbarPrimary.setVisibility(View.VISIBLE);
        }
        View toolbarSecondary = getActivity().findViewById(R.id.toolbar_secondary);
        if (toolbarSecondary != null) {
            toolbarSecondary.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Cleans up the binding when the view is destroyed to avoid memory leaks.
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
