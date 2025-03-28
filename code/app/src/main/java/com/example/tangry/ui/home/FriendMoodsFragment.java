/**
 * FriendMoodsFragment.java
 *
 * This fragment displays a UI layout for viewing friend moods. It inflates the corresponding layout
 * resource (fragment_friend_moods.xml) and serves as a placeholder for further functionality related
 * to friend mood interactions.
 *
 * Outstanding Issues:
 * - Additional UI components and interaction logic may be added in future iterations.
 */

package com.example.tangry.ui.home;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tangry.R;
import com.example.tangry.adapters.EmotionPostAdapter;
import com.example.tangry.controllers.EmotionPostController;
import com.example.tangry.models.EmotionPost;
import com.example.tangry.repositories.UserRepository;
import com.example.tangry.utils.FilterBottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment displaying the user's friends' emotion posts with filtering capabilities.
 */
public class FriendMoodsFragment extends Fragment {

    private static final String TAG = "FriendMoodsFragment";
    private RecyclerView recyclerView;
    private EmotionPostAdapter adapter;
    private EmotionPostController emotionPostController;
    private List<String> friendUsernames = new ArrayList<>();
    private List<String> selectedEmotions = new ArrayList<>();
    private boolean filterRecent = false;
    private TextView emptyStateText;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_friend_moods, container, false);

        emotionPostController = new EmotionPostController();
        recyclerView = root.findViewById(R.id.friend_recycler_view);
        emptyStateText = root.findViewById(R.id.empty_state_text);

        // Set up recycler view
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new EmotionPostAdapter(null, (post, itemView) -> {
            // Handle post item click - navigate to details
            Bundle bundle = new Bundle();
            Gson gson = new Gson();
            String postJson = gson.toJson(post);
            bundle.putString("post", postJson);
            bundle.putString("postId", post.getPostId());

            // Use the action from HomeFragment context
            Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_activity_main)
                    .navigate(R.id.action_homeFragment_to_postDetailsFragment, bundle);
        });
        recyclerView.setAdapter(adapter);

        // Set up filter button
        ImageButton filterBtn = root.findViewById(R.id.btn_filter);
        filterBtn.setOnClickListener(v -> showFilterDialog());

        // Load friends list first, then posts
        loadFriendsList();

        return root;
    }

    private void loadFriendsList() {
        String currentUserEmail = getCurrentUserEmail();

        if (currentUserEmail != null) {
            UserRepository.getInstance().getFriendsList(
                    currentUserEmail,
                    friendsList -> {
                        friendUsernames = friendsList;
                        loadPosts();
                    },
                    e -> {
                        Log.e(TAG, "Error loading friends list", e);
                        showEmptyState();
                    }
            );
        } else {
            showEmptyState();
        }
    }

    // In YourMoodFragment.java
    private void showFilterDialog() {
        new FilterBottomSheetDialog(
                selectedEmotions,
                filterRecent,
                (emotions, recent) -> {
                    selectedEmotions = emotions;
                    filterRecent = recent;
                    loadPosts();
                }
        ).show(getChildFragmentManager(), "filter_dialog");
    }

    private void loadPosts() {
        if (friendUsernames.isEmpty()) {
            showEmptyState();
            return;
        }

        Query query = emotionPostController.getFriendsPostsWithFilter(
                friendUsernames,
                selectedEmotions,
                filterRecent
        );

        query.addSnapshotListener((value, error) -> {
            if (error != null) {
                Log.e(TAG, "Error loading friend posts", error);
                return;
            }

            List<EmotionPost> posts = new ArrayList<>();
            if (value != null) {
                for (DocumentSnapshot doc : value.getDocuments()) {
                    EmotionPost post = doc.toObject(EmotionPost.class);
                    if (post != null) {
                        post.setPostId(doc.getId());
                        posts.add(post);
                    }
                }
            }

            if (posts.isEmpty()) {
                showEmptyState();
            } else {
                emptyStateText.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
            }

            adapter.setPosts(posts);
        });
    }

    private void showEmptyState() {
        emptyStateText.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
    }

    private String getCurrentUserEmail() {
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            return FirebaseAuth.getInstance().getCurrentUser().getEmail();
        }
        return null;
    }
}