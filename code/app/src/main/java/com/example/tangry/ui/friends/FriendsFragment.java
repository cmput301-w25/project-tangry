/**
 * FriendsFragment.java
 *
 * This fragment displays the friends-related UI within the application. It uses a ViewModel
 * (FriendsViewModel) to observe and update UI components, such as a TextView displaying friend-related data.
 * The fragment uses View Binding to reference its views.
 *
 * Outstanding Issues:
 * - Currently, the fragment only displays a simple text view. Additional functionality and UI elements
 *   for managing or displaying a list of friends may be implemented in future iterations.
 */

package com.example.tangry.ui.friends;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tangry.R;
import com.example.tangry.databinding.FragmentFriendsBinding;

import java.util.ArrayList;
import java.util.List;

public class FriendsFragment extends Fragment {
    /**
     * Default public constructor.
     */
    public FriendsFragment() {
        // Required empty public constructor
    }

    private FragmentFriendsBinding binding;
    private FriendsViewModel viewModel;
    private FriendsAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentFriendsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(this).get(FriendsViewModel.class);

        // Instantiate adapter with an empty list and a click listener.
        adapter = new FriendsAdapter(new ArrayList<>(), friendUsername -> {
            Bundle bundle = new Bundle();
            bundle.putString("username", friendUsername);
            // Ensure that your navigation graph includes an action for this ID.
            Navigation.findNavController(view).navigate(R.id.alobal_userProfileFragment, bundle);
        });

        binding.friendsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.friendsRecyclerView.setAdapter(adapter);

        viewModel.getFriendsList().observe(getViewLifecycleOwner(), friends -> {
            if (friends == null || friends.isEmpty()) {
                binding.emptyTextView.setVisibility(View.VISIBLE);
                binding.friendsRecyclerView.setVisibility(View.GONE);
            } else {
                binding.emptyTextView.setVisibility(View.GONE);
                binding.friendsRecyclerView.setVisibility(View.VISIBLE);
                adapter.setFriends(friends);
            }
        });

        viewModel.getMessage().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null && !msg.isEmpty()) {
                Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Updated adapter with a "View Profile" button
    public static class FriendsAdapter extends RecyclerView.Adapter<FriendsAdapter.FriendViewHolder> {
        private List<String> friends;
        private final OnViewProfileClickListener viewProfileClickListener;

        public interface OnViewProfileClickListener {
            void onViewProfileClick(String friendUsername);
        }

        public FriendsAdapter(List<String> friends, OnViewProfileClickListener listener) {
            this.friends = friends;
            this.viewProfileClickListener = listener;
        }

        public void setFriends(List<String> friends) {
            this.friends = friends;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public FriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_friend, parent, false);
            return new FriendViewHolder(view, viewProfileClickListener);
        }

        @Override
        public void onBindViewHolder(@NonNull FriendViewHolder holder, int position) {
            String friend = friends.get(position);
            holder.bind(friend);
        }

        @Override
        public int getItemCount() {
            return friends == null ? 0 : friends.size();
        }

        static class FriendViewHolder extends RecyclerView.ViewHolder {
            private final android.widget.TextView usernameText;
            private final android.widget.Button viewProfileButton;

            FriendViewHolder(@NonNull View itemView, OnViewProfileClickListener listener) {
                super(itemView);
                usernameText = itemView.findViewById(R.id.text_username);
                viewProfileButton = itemView.findViewById(R.id.button_view_profile);
                viewProfileButton.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onViewProfileClick(usernameText.getText().toString());
                    }
                });
            }

            void bind(String username) {
                usernameText.setText(username);
            }
        }
    }
}