package com.example.tangry.ui.login;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.tangry.R;

public class LoginFragment extends Fragment {

    private EditText emailEditText, passwordEditText;
    private Button loginButton;
    private Button createButton;
    private LoginViewModel viewModel;
    // Flag to prevent showing toast on startup
    private boolean loginAttempted = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        emailEditText = view.findViewById(R.id.editTextUsername);
        passwordEditText = view.findViewById(R.id.editTextPassword);
        loginButton = view.findViewById(R.id.buttonLogin);
        createButton = view.findViewById(R.id.buttonCreate);

        viewModel = new ViewModelProvider(this).get(LoginViewModel.class);

        // Observe login status
        viewModel.getIsLoggedIn().observe(getViewLifecycleOwner(), isLoggedIn -> {
            if (loginAttempted) {
                if (isLoggedIn) {
                    NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_activity_main);
                    navController.navigate(R.id.navigation_home);
                } else {
                    Toast.makeText(requireContext(), "Login failed", Toast.LENGTH_SHORT).show();
                }
                loginAttempted = false; // reset flag after handling result
            }
        });

        loginButton.setOnClickListener(v -> {
            viewModel.setEmail(emailEditText.getText().toString().trim());
            viewModel.setPassword(passwordEditText.getText().toString());
            loginAttempted = true;
            viewModel.login();
        });

        createButton.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_activity_main);
            navController.navigate(R.id.action_navigation_login_to_navigation_add_user);
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() instanceof AppCompatActivity) {
            ((AppCompatActivity)getActivity()).getSupportActionBar().hide();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (getActivity() instanceof AppCompatActivity) {
            ((AppCompatActivity)getActivity()).getSupportActionBar().show();
        }
    }
}
