package com.example.tangry.ui.leaderboard;
/**
 * LeaderboardFragment.java
 * 
 * Fragment that displays a leaderboard ranking users by karma points and badges.
 * Shows real-time user rankings with their username, karma points, and badge counts.
 * Uses a live Firestore listener to update the leaderboard automatically when data changes.
 */
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.tangry.R;
import com.example.tangry.adapters.LeaderboardAdapter;
import com.example.tangry.controllers.UserController;
import com.example.tangry.models.User;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class LeaderboardFragment extends Fragment {
    private static final String TAG = "LeaderboardFragment";
    private RecyclerView recyclerView;
    private LeaderboardAdapter adapter;
    private UserController userController;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_leaderboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize RecyclerView
        recyclerView = view.findViewById(R.id.leaderboard_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new LeaderboardAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);

        // Initialize controller
        userController = new UserController();

        // Live listener for real-time updates
        Query query = userController.getTopUsersQuery();
        query.addSnapshotListener((querySnapshot, e) -> {
            if (e != null || querySnapshot == null) {
                Log.e(TAG, "Error fetching leaderboard", e);
                return;
            }

            List<User> users = new ArrayList<>();
            for (QueryDocumentSnapshot doc : querySnapshot) {
                String username = doc.getString("username");
                Long karma = doc.getLong("karma");

                // Retrieve badge fields from nested 'badges'
                Map<String, Object> badges = (Map<String, Object>) doc.get("badges");
                int goldBadgeCount = 0;
                int silverBadgeCount = 0;
                int dailyBadgeCount = 0;  // daily = size of dailyBadgeDates array

                if (badges != null) {
                    // goldBadges
                    if (badges.get("goldBadges") != null) {
                        goldBadgeCount = ((Long) badges.get("goldBadges")).intValue();
                    }
                    // silverBadges
                    if (badges.get("silverBadges") != null) {
                        silverBadgeCount = ((Long) badges.get("silverBadges")).intValue();
                    }
                    // dailyBadgeDates
                    List<String> dailyBadgeDates = (List<String>) badges.get("dailyBadgeDates");
                    if (dailyBadgeDates != null) {
                        dailyBadgeCount = dailyBadgeDates.size();
                    }
                }

                if (username != null) {
                    // Construct User object with the badge counts
                    User user = new User(
                            username,
                            karma != null ? karma.intValue() : 0,
                            goldBadgeCount,
                            silverBadgeCount,
                            dailyBadgeCount
                    );
                    users.add(user);
                }
            }
            adapter.updateUsers(users);
        });
    }
}
