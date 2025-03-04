package com.example.tangry.ui.add_user;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.tangry.databinding.FragmentAddUserBinding;

public class AddUserFragment extends Fragment {

    private FragmentAddUserBinding binding;

    public AddUserFragment() {
        // Required empty public constructor
    }

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout using view binding
        binding = FragmentAddUserBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Set click listener on the "Create Account" button to create a dummy user
        binding.createAccountButton.setOnClickListener(v -> {
            String username = binding.usernameInput.getText().toString().trim();
            String password = binding.passwordInput.getText().toString();
            String confirmPassword = binding.passwordInput2.getText().toString();

            // Basic validation
            if (TextUtils.isEmpty(username)) {
                Toast.makeText(getContext(), "Username is required", Toast.LENGTH_SHORT).show();
            } else if (password.length() < 8) {
                Toast.makeText(getContext(), "Password must be at least 8 characters", Toast.LENGTH_SHORT).show();
            } else if (!password.equals(confirmPassword)) {
                Toast.makeText(getContext(), "Passwords do not match", Toast.LENGTH_SHORT).show();
            } else {
                // Here we simply create a dummy user and show a confirmation toast.
                // In real usage this is where you'd add database/user creation logic.
                Toast.makeText(getContext(), "Dummy user created: " + username, Toast.LENGTH_SHORT).show();

                // Optionally clear the inputs
                binding.usernameInput.setText("");
                binding.passwordInput.setText("");
                binding.passwordInput2.setText("");
                // navigate back to the login screen
                getParentFragmentManager().popBackStack();
            }
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}