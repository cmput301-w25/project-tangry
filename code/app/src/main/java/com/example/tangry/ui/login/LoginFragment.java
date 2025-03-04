package com.example.tangry.ui.login;

import android.os.Bundle;
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

import com.example.tangry.MainActivity;
import com.example.tangry.R;

public class LoginFragment extends Fragment {

    private LoginViewModel loginViewModel;
    private EditText usernameEditText, passwordEditText;
    private Button loginButton;
    private Button createButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        loginViewModel = new ViewModelProvider(this).get(LoginViewModel.class);

        usernameEditText = view.findViewById(R.id.editTextUsername);
        passwordEditText = view.findViewById(R.id.editTextPassword);
        loginButton = view.findViewById(R.id.buttonLogin);
        createButton = view.findViewById(R.id.buttonCreate);


        loginButton.setOnClickListener(v -> {
            String username = usernameEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();

            loginViewModel.setUsername(username);
            loginViewModel.setPassword(password);
            loginViewModel.login();
        });

        createButton.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_activity_main);
            navController.navigate(R.id.action_navigation_login_to_navigation_add_user);
        });

        loginViewModel.getIsLoggedIn().observe(getViewLifecycleOwner(), isLoggedIn -> {
            if (isLoggedIn) {
                ((MainActivity) requireActivity()).onLoginSuccess();
            } else {
                Toast.makeText(getContext(), "Invalid login", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }
    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() instanceof AppCompatActivity) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().hide();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (getActivity() instanceof AppCompatActivity) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().show();
        }
    }
}
