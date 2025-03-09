package com.example.tangry.ui.add_user;

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

public class AddUserFragment extends Fragment {

    private FragmentAddUserBinding binding;
    private AddUserViewModel viewModel;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentAddUserBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(AddUserViewModel.class);

        // Observe message updates from ViewModel
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

        // Set click listener for the "Create Account" button
        binding.createAccountButton.setOnClickListener(v -> {
            String email = binding.emailInput.getText().toString().trim();
            String username = binding.usernameInput.getText().toString().trim();
            String password = binding.passwordInput.getText().toString();
            String confirmPassword = binding.passwordInput2.getText().toString();

            viewModel.createUser(email, password, confirmPassword, username);
        });

        return root;
    }

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

    @Override
    public void onPause() {
        super.onPause();
        if (getActivity() instanceof AppCompatActivity) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().show();
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
