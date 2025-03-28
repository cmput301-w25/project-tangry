/**
 * LoginFragment.java
 *
 * This fragment provides the user interface for logging into the application. It handles user input for email
 * and password, supports "Remember Me" functionality via SharedPreferences, and performs both manual and auto-login
 * using the LoginViewModel. Successful login navigates the user to the home screen.
 *
 * Outstanding Issues:
 * - Further input validation (e.g., email format) and error handling can be implemented.
 * - UI improvements such as loading indicators during authentication may enhance the user experience.
 */

package com.example.tangry.ui.login;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
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

    private EditText emailEditText, passwordEditText;
    private Button loginButton, createButton;
    private CheckBox rememberMeCheckBox;
    private LoginViewModel viewModel;
    // Flag to mark manual login attempts
    private boolean manualLoginAttempted = false;
    private static final String TAG = "LoginFragment";

    /**
     * Inflates the layout for the login fragment and initializes UI components.
     *
     * @param inflater           The LayoutInflater used to inflate the view.
     * @param container          The parent container.
     * @param savedInstanceState Previously saved state, if any.
     * @return The root view of the inflated layout.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        emailEditText = view.findViewById(R.id.editTextUsername);
        passwordEditText = view.findViewById(R.id.editTextPassword);
        loginButton = view.findViewById(R.id.buttonLogin);
        createButton = view.findViewById(R.id.buttonCreate);
        rememberMeCheckBox = view.findViewById(R.id.rememberMeCheckBox);

        // Retrieve saved credentials from SharedPreferences
        SharedPreferences prefs = requireContext().getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE);
        String savedEmail = prefs.getString("username", "");
        String savedPassword = prefs.getString("password", "");
        boolean savedRemember = prefs.getBoolean("remember", false);
        emailEditText.setText(savedEmail);
        passwordEditText.setText(savedPassword);
        rememberMeCheckBox.setChecked(savedRemember);

        viewModel = new ViewModelProvider(this).get(LoginViewModel.class);

        // Observe login status changes
        viewModel.getIsLoggedIn().observe(getViewLifecycleOwner(), isLoggedIn -> {
            Log.d(TAG, "Login observer: isLoggedIn = " + isLoggedIn);
            if (isLoggedIn != null && isLoggedIn) {
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).onLoginSuccess();
                }
                NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_activity_main);
                navController.navigate(R.id.navigation_home);
            } else {
                // Show toast only when a manual login was attempted.
                if (manualLoginAttempted) {
                    Toast.makeText(requireContext(), "Login failed", Toast.LENGTH_SHORT).show();
                }
            }
            // Reset the manual login flag after handling result.
            manualLoginAttempted = false;
        });

        // Set IME option for username to advance to next field.
        emailEditText.setImeOptions(EditorInfo.IME_ACTION_NEXT);

        // Set listener on the password field to hide the keyboard on "Done".
        passwordEditText.setOnEditorActionListener((TextView textView, int actionId, KeyEvent event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
                return true;
            }
            return false;
        });

        // Manual login click listener.
        loginButton.setOnClickListener(v -> {
            String email = emailEditText.getText().toString().trim();
            String pass = passwordEditText.getText().toString();
            viewModel.setEmail(email);
            viewModel.setPassword(pass);
            viewModel.setRememberMe(rememberMeCheckBox.isChecked());
            manualLoginAttempted = true;
            if (rememberMeCheckBox.isChecked()) {
                prefs.edit()
                        .putString("username", email)
                        .putString("password", pass)
                        .putBoolean("remember", true)
                        .apply();
            } else {
                prefs.edit().clear().apply();
            }
            viewModel.login();
        });

        // "Create account" click listener.
        createButton.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_activity_main);
            navController.navigate(R.id.action_navigation_login_to_navigation_create_user);
        });

        // Auto–login: if "Remember Me" was checked and saved credentials are available.
        if (savedRemember && !savedEmail.isEmpty() && !savedPassword.isEmpty()) {
            Log.d(TAG, "Attempting auto-login with saved credentials");
            viewModel.setEmail(savedEmail.trim());
            viewModel.setPassword(savedPassword);
            // Do not mark as manual so that toast won't be shown if auto–login fails.
            viewModel.login();
        }

        return view;
    }

    /**
     * Hides the action bar and toolbars when the fragment resumes.
     */
    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() instanceof AppCompatActivity) {
            ((AppCompatActivity)getActivity()).getSupportActionBar().hide();
        }
        // Hide primary and secondary toolbars.
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
     * Shows the action bar and toolbars when the fragment is paused.
     */
    @Override
    public void onPause() {
        super.onPause();
        if (getActivity() instanceof AppCompatActivity) {
            ((AppCompatActivity)getActivity()).getSupportActionBar().show();
        }
        // Show primary and secondary toolbars.
        View toolbarPrimary = getActivity().findViewById(R.id.toolbar_primary);
        if (toolbarPrimary != null) {
            toolbarPrimary.setVisibility(View.VISIBLE);
        }
        View toolbarSecondary = getActivity().findViewById(R.id.toolbar_secondary);
        if (toolbarSecondary != null) {
            toolbarSecondary.setVisibility(View.VISIBLE);
        }
    }
}
