/**
 * FriendMoodsFragment.java
 *
 * This fragment displays a UI layout for viewing friend moods. It inflates the corresponding layout
 * resource (fragment_friend_moods.xml) and serves as a placeholder for further functionality related
 * to friend mood interactions.
 */

package com.example.tangry.ui.home;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
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
import com.example.tangry.repositories.EmotionPostRepository;
import com.example.tangry.repositories.UserRepository;
import com.example.tangry.utils.FilterBottomSheetDialog;
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
    private EditText searchInput;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_friend_moods, container, false);

        emotionPostController = new EmotionPostController();
        recyclerView = root.findViewById(R.id.friend_recycler_view);
        emptyStateText = root.findViewById(R.id.empty_state_text);
        searchInput = root.findViewById(R.id.search_input);

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

        // Set up search functionality
        setupSearchFunctionality();

        // Load friends list first, then posts
        loadFriendsList();

        return root;
    }

    private void setupSearchFunctionality() {
        // Handle search action when user presses search on keyboard
        searchInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch(searchInput.getText().toString());
                hideKeyboard();
                return true;
            }
            return false;
        });

        // Real-time filtering as user types
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                performSearch(s.toString());
            }
        });
    }

    private void performSearch(String query) {
        if (adapter != null) {
            adapter.getFilter().filter(query);

            // Show empty state if filtered list is empty
            if (adapter.getItemCount() == 0) {
                emptyStateText.setText("No posts match your search");
                showEmptyState();
            } else {
                emptyStateText.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
            }
        }
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) requireActivity()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(searchInput.getWindowToken(), 0);
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

    private void showFilterDialog() {
        new FilterBottomSheetDialog(
                selectedEmotions,
                filterRecent,
                (emotions, recent) -> {
                    selectedEmotions = emotions;
                    filterRecent = recent;
                    loadPosts();

                    // Reset search when applying filters
                    searchInput.setText("");
                }
        ).show(getChildFragmentManager(), "filter_dialog");
    }

    private void loadPosts() {
        if (friendUsernames == null || friendUsernames.isEmpty()) {
            showEmptyState();
            return;
        }

        // Show loading indicator (optional)
        // loadingIndicator.setVisibility(View.VISIBLE);

        // Use the new method to get 3 most recent posts per friend
        EmotionPostRepository.getInstance().getThreeMostRecentPostsPerFriend(
                friendUsernames,
                selectedEmotions,
                posts -> {
                    // Hide loading indicator if you added one
                    // loadingIndicator.setVisibility(View.GONE);

                    if (posts.isEmpty()) {
                        emptyStateText.setText("No posts to display");
                        showEmptyState();
                    } else {
                        emptyStateText.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                    }

                    adapter.setPosts(posts);

                    // Re-apply search if there's text in the search field
                    if (searchInput.getText().length() > 0) {
                        performSearch(searchInput.getText().toString());
                    }
                }
        );
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