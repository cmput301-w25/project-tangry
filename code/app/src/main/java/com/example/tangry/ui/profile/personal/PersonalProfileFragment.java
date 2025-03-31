package com.example.tangry.ui.profile.personal;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import com.example.tangry.R;
import com.example.tangry.controllers.UserController;
import com.example.tangry.models.UserStats;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

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

        // Populate the badges card views
        // These views are in your XML (fragment_personal_profile) and include:
        // username_text, karma_text, gold_badge_count, silver_badge_count, daily_badge_count.
        TextView usernameText = view.findViewById(R.id.username_text);
        TextView karmaText = view.findViewById(R.id.karma_text);
        TextView goldBadgeCount = view.findViewById(R.id.gold_badge_count);
        TextView silverBadgeCount = view.findViewById(R.id.silver_badge_count);
        TextView dailyBadgeCount = view.findViewById(R.id.daily_badge_count);

        UserController userController = new UserController();
        if (currentUser != null) {
            String email = currentUser.getEmail();
            userController.getUserStats(email,
                    stats -> {
                        // Update UI elements with fetched stats
                        karmaText.setText("🔥 " + stats.getKarma());
                        goldBadgeCount.setText("x" + stats.getGoldBadges());
                        silverBadgeCount.setText("x" + stats.getSilverBadges());
                        dailyBadgeCount.setText("x" + stats.getDailyBadgeCount());
                    },
                    e -> {
                        Log.e("PersonalProfile", "Error fetching user stats: " + e.getMessage());
                    }
            );
        }

        Button followRequestsButton = view.findViewById(R.id.button_follow_requests);
        followRequestsButton.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_personalProfileFragment_to_followRequestsFragment)
        );

        Button signOutButton = view.findViewById(R.id.signOutButton);
        signOutButton.setOnClickListener(v -> {
            v.getContext()
                    .getSharedPreferences("LoginPrefs", v.getContext().MODE_PRIVATE)
                    .edit().clear().apply();

            FirebaseAuth.getInstance().signOut();

            if (getActivity() != null) {
                View toolbar = getActivity().findViewById(R.id.toolbar_primary);
                if (toolbar != null) toolbar.setVisibility(View.GONE);
                View fab = getActivity().findViewById(R.id.fab);
                if (fab != null) fab.setVisibility(View.GONE);
                View navView = getActivity().findViewById(R.id.nav_view);
                if (navView != null) navView.setVisibility(View.GONE);
            }
            Navigation.findNavController(v).navigate(R.id.navigation_login);
        });
    }
}
