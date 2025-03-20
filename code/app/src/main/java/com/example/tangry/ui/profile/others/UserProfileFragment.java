package com.example.tangry.ui.profile.others;

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
import com.example.tangry.databinding.FragmentUserProfileBinding;
import com.example.tangry.models.EmotionPost;
import com.example.tangry.repositories.EmotionPostRepository;
import com.google.gson.Gson;

import java.util.List;

public class UserProfileFragment extends Fragment {

    private FragmentUserProfileBinding binding;
    private UserProfileViewModel viewModel;
    private EmotionPostAdapter adapter;
    private String profileUsername; // The username of the user whose profile is being viewed

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

        // Retrieve the profile username from arguments. In production, pass this in.
        if (getArguments() != null && getArguments().getString("username") != null) {
            profileUsername = getArguments().getString("username");
        } else {
            profileUsername = "defaultUser";
        }
        Log.d("UserProfileFragment", "Profile username: " + profileUsername);
        binding.usernameText.setText(profileUsername);

        // Setup RecyclerView for a scrollable list of posts.
        binding.postsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new EmotionPostAdapter(null, (post, itemView) -> {
            Bundle bundle = new Bundle();
            Gson gson = new Gson();
            String postJson = gson.toJson(post);
            bundle.putString("post", postJson);
            bundle.putString("postId", post.getPostId());
            Navigation.findNavController(itemView)
                    .navigate(R.id.action_userProfileFragment_to_postDetailsFragment, bundle);
        });
        binding.postsRecyclerView.setAdapter(adapter);

        // Follow button sends a follow request.
        binding.followButton.setOnClickListener(v ->
                viewModel.sendFollowRequest(profileUsername)
        );

        viewModel.getFollowMessage().observe(getViewLifecycleOwner(), message -> {
            if (message != null) {
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
            }
        });

        // Load the user's posts (using EmotionPostRepository).
        EmotionPostRepository.getInstance().getPostsByUser(profileUsername)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<EmotionPost> posts = querySnapshot.toObjects(EmotionPost.class);
                    Log.d("UserProfileFragment", "Found " + posts.size() + " posts for " + profileUsername);
                    adapter.setPosts(posts);
                })
                .addOnFailureListener(e -> {
                    Log.e("UserProfileFragment", "Error retrieving posts", e);
                });
    }
}