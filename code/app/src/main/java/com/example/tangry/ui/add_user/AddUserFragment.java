package com.example.tangry.ui.add_user;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.tangry.databinding.FragmentAddUserBinding;
import com.example.tangry.repositories.UsernameRepository;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class AddUserFragment extends Fragment {

    private FragmentAddUserBinding binding;
    private FirebaseAuth mAuth;
    private final String TAG = "AddUserFragment";

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout using view binding
        binding = FragmentAddUserBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Initialize FirebaseAuth
        mAuth = FirebaseAuth.getInstance();

        // Set click listener on the "Create Account" button
        binding.createAccountButton.setOnClickListener(v -> {
            String email = binding.emailInput.getText().toString().trim();
            String username = binding.usernameInput.getText().toString().trim();
            String password = binding.passwordInput.getText().toString();
            String confirmPassword = binding.passwordInput2.getText().toString();

            // Basic validation
            if (TextUtils.isEmpty(email)) {
                Toast.makeText(getContext(), "Email is required", Toast.LENGTH_SHORT).show();
            } else if (password.length() < 8) {
                Toast.makeText(getContext(), "Password must be at least 8 characters", Toast.LENGTH_SHORT).show();
            } else if (!password.equals(confirmPassword)) {
                Toast.makeText(getContext(), "Passwords do not match", Toast.LENGTH_SHORT).show();
            } else {
                createUser(email, password, username);
            }
        });


        return root;
    }

    private void createUser(String email, String password, String username) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                    } else {
                        Toast.makeText(getContext(), "Registration failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }
                });
        UsernameRepository repository = UsernameRepository.getInstance();
        repository.saveUsernameToFirestore(username, email,
                docRef -> {
                    Toast.makeText(getContext(), "Account created successfully", Toast.LENGTH_SHORT).show();
                    // navigate back
                    getParentFragmentManager().popBackStack();
                },
                e -> {
                    Toast.makeText(getContext(), "Registration failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Failed to save username BUT ACCOUNT CREATED!", e);
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
