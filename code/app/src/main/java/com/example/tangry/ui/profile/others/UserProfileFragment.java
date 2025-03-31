package com.example.tangry.ui.profile.others;
/**
 * UserProfileFragment.java
 * 
 * Fragment for displaying another user's profile information.
 * Shows the selected user's stats, allows sending follow requests,
 * and displays their public posts in a scrollable list.
 */
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.tangry.R;
import com.example.tangry.adapters.EmotionPostAdapter;
import com.example.tangry.controllers.UserController;
import com.example.tangry.models.UserStats;
import com.example.tangry.databinding.FragmentUserProfileBinding;
import com.example.tangry.models.EmotionPost;
import com.example.tangry.repositories.EmotionPostRepository;
import com.google.gson.Gson;
import java.util.List;

public class UserProfileFragment extends Fragment {

    private FragmentUserProfileBinding binding;
    private UserProfileViewModel viewModel;
    private EmotionPostAdapter adapter;
    private String profileUsername; // The username of the profile being viewed

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentUserProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(UserProfileViewModel.class);

        // Retrieve the profile username from arguments.
        if (getArguments() != null && getArguments().getString("username") != null) {
            profileUsername = getArguments().getString("username");
        } else {
            profileUsername = "defaultUser";
        }
        Log.d("UserProfileFragment", "Profile username: " + profileUsername);
        binding.usernameText.setText(profileUsername);

        // Fetch and update user stats based on the profile's username.
        UserController userController = new UserController();
        userController.getUserStatsByUsername(profileUsername,
                stats -> {
                    binding.karmaText.setText("🔥 " + stats.getKarma());
                    binding.goldBadgeCount.setText("x" + stats.getGoldBadges());
                    binding.silverBadgeCount.setText("x" + stats.getSilverBadges());
                    binding.dailyBadgeCount.setText("x" + stats.getDailyBadgeCount());
                },
                e -> {
                    Log.e("UserProfileFragment", "Error fetching user stats: " + e.getMessage());
                }
        );

        // Set up follower button.
        binding.followButton.setOnClickListener(v ->
                viewModel.sendFollowRequest(profileUsername)
        );

        // Observe button enabled state.
        viewModel.getFollowButtonEnabled().observe(getViewLifecycleOwner(), enabled ->
                binding.followButton.setEnabled(enabled)
        );

        // Observe follow request messages.
        viewModel.getFollowMessage().observe(getViewLifecycleOwner(), message -> {
            if (message != null) {
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
            }
        });

        // Load the follow status for the current user relative to this profile.
        viewModel.loadFollowStatus(profileUsername);

        // Setup RecyclerView for posts.
        binding.postsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new EmotionPostAdapter(null, (post, itemView) -> {
            Bundle bundle = new Bundle();
            Gson gson = new Gson();
            String postJson = gson.toJson(post);
            bundle.putString("post", postJson);
            bundle.putString("postId", String.valueOf(post.getPostId()));
            Navigation.findNavController(itemView)
                    .navigate(R.id.action_userProfileFragment_to_postDetailsFragment, bundle);
        });
        binding.postsRecyclerView.setAdapter(adapter);

        // Load the user's posts.
        EmotionPostRepository.getInstance().getPostsByUser(profileUsername)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<EmotionPost> posts = querySnapshot.toObjects(EmotionPost.class);
                    Log.d("UserProfileFragment", "Found " + posts.size() + " posts for " + profileUsername);
                    adapter.setPosts(posts);

                    // Toggle visibility based on whether there are posts
                    if (posts.isEmpty()) {
                        binding.postsRecyclerView.setVisibility(View.GONE);
                        binding.noPostsTextView.setVisibility(View.VISIBLE);
                    } else {
                        binding.postsRecyclerView.setVisibility(View.VISIBLE);
                        binding.noPostsTextView.setVisibility(View.GONE);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("UserProfileFragment", "Error retrieving posts", e);
                    // Show "no posts" message on error
                    binding.postsRecyclerView.setVisibility(View.GONE);
                    binding.noPostsTextView.setVisibility(View.VISIBLE);
                });
    }
}