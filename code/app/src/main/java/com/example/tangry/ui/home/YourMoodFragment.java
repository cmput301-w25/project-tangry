package com.example.tangry.ui.home;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
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
import com.example.tangry.utils.FilterBottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

/**
 * YourMoodFragment.java
 *
 * This fragment displays the current user's mood posts.
 * It now uses a dedicated filter button in the layout instead of using the toolbar for filtering.
 */
public class YourMoodFragment extends Fragment {

    private static final String TAG = "YourMoodFragment";
    private RecyclerView recyclerView;
    private EmotionPostAdapter adapter;
    private EmotionPostController emotionPostController;
    private String currentUsername;
    private List<String> selectedEmotions = new ArrayList<>();
    private boolean filterRecent = false;
    private TextView emptyStateText;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_your_mood, container, false);

        emotionPostController = new EmotionPostController();
        currentUsername = getCurrentUsername();

        recyclerView = root.findViewById(R.id.your_recycler_view);
        emptyStateText = root.findViewById(R.id.empty_state_text);

        // Set up recycler view
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new EmotionPostAdapter(null, (post, itemView) -> {
            // Handle post item click - navigate to details
            // Important: Use parent fragment's navigation context
            Bundle bundle = new Bundle();
            Gson gson = new Gson();
            String postJson = gson.toJson(post);
            bundle.putString("post", postJson);
            bundle.putString("postId", post.getPostId());

            // Use the action from HomeFragment instead
            Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_activity_main)
                    .navigate(R.id.action_homeFragment_to_postDetailsFragment, bundle);
        });
        recyclerView.setAdapter(adapter);

        // Set up filter button
        ImageButton filterBtn = root.findViewById(R.id.btn_filter);
        filterBtn.setOnClickListener(v -> showFilterDialog());

        // Setup search functionality
        EditText searchInput = root.findViewById(R.id.search_input);
        searchInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch(searchInput.getText().toString());
                // Hide keyboard
                InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(searchInput.getWindowToken(), 0);
                return true;
            }
            return false;
        });

        // Initial data load
        loadPosts();

        return root;
    }

    private void performSearch(String query) {
        // Assuming your adapter has a filter method
        if (adapter != null) {
            adapter.getFilter().filter(query);
        }
    }

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
        if (currentUsername == null || currentUsername.isEmpty()) {
            Log.e(TAG, "Username is null or empty");
            return;
        }

        Query query = emotionPostController.getUserPostsWithFilter(
                currentUsername,
                selectedEmotions,
                filterRecent
        );

        query.addSnapshotListener((value, error) -> {
            if (error != null) {
                Log.e(TAG, "Error loading posts", error);
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
                emptyStateText.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            } else {
                emptyStateText.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
            }

            adapter.setPosts(posts);
        });
    }

    private String getCurrentUsername() {
        // Get username from Firebase Auth
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            return FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
        }
        return null;
    }
}