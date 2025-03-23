package com.example.tangry.ui.leaderboard;

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
import com.example.tangry.controllers.UsernameController;
import com.example.tangry.models.User;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class LeaderboardFragment extends Fragment {
    private static final String TAG = "LeaderboardFragment";
    private RecyclerView recyclerView;
    private LeaderboardAdapter adapter;
    private UsernameController usernameController;

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
        usernameController = new UsernameController();

        // Live listener for real-time updates
        Query query = usernameController.getTopUsersQuery();
        query.addSnapshotListener((querySnapshot, e) -> {
            if (e != null || querySnapshot == null) {
                Log.e(TAG, "Error fetching leaderboard", e);
                return;
            }

            List<User> users = new ArrayList<>();
            for (QueryDocumentSnapshot doc : querySnapshot) {
                String username = doc.getString("username");
                Long karma = doc.getLong("karma");
                if (username != null) {
                    users.add(new User(username, karma != null ? karma.intValue() : 0));
                }
            }

            adapter.updateUsers(users);
        });
    }
}
