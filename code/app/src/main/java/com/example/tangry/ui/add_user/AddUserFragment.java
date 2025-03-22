package com.example.tangry.ui.add_user;

import android.os.Bundle;
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
import androidx.recyclerview.widget.RecyclerView;

import com.example.tangry.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class AddUserFragment extends Fragment {

    private AddUserViewModel viewModel;
    private TextInputEditText usernameInput;
    private MaterialButton searchButton;
    private RecyclerView resultsRecyclerView;
    private SearchResultsAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_user, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(AddUserViewModel.class);
        usernameInput = view.findViewById(R.id.username_input);
        searchButton = view.findViewById(R.id.search_button);
        resultsRecyclerView = view.findViewById(R.id.results_recycler_view);

        adapter = new SearchResultsAdapter(new ArrayList<>(), viewModel, new SearchResultsAdapter.ProfileClickListener() {
            @Override
            public void onViewProfileClicked(String username) {
                Bundle bundle = new Bundle();
                bundle.putString("username", username);
                Navigation.findNavController(view)
                        .navigate(R.id.action_navigation_add_user_to_userProfileFragment, bundle);
            }

            @Override
            public void onFollowClicked(String username) {
                viewModel.followUser(username);
            }
        });
        resultsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        resultsRecyclerView.setAdapter(adapter);

        searchButton.setOnClickListener(v -> {
            String query = usernameInput.getText().toString().trim();
            if (query.isEmpty()) {
                Toast.makeText(getContext(), "Please enter a username", Toast.LENGTH_SHORT).show();
            } else {
                viewModel.searchUser(query);
            }
        });

        viewModel.getSearchResults().observe(getViewLifecycleOwner(), results -> adapter.setResults(results));
        viewModel.getMessage().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null) {
                Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
            }
        });

        // Load follow status for the current user.
        viewModel.loadFollowStatus();
        viewModel.getFollowings().observe(getViewLifecycleOwner(), list -> adapter.notifyDataSetChanged());
        viewModel.getSentFollowRequests().observe(getViewLifecycleOwner(), list -> adapter.notifyDataSetChanged());
    }

    public static class SearchResultsAdapter extends RecyclerView.Adapter<SearchResultsAdapter.ResultViewHolder> {

        private List<String> results;
        private final ProfileClickListener listener;
        private final AddUserViewModel viewModel;

        public interface ProfileClickListener {
            void onViewProfileClicked(String username);
            void onFollowClicked(String username);
        }

        public SearchResultsAdapter(List<String> results, AddUserViewModel viewModel, ProfileClickListener listener) {
            this.results = results;
            this.viewModel = viewModel;
            this.listener = listener;
        }

        public void setResults(List<String> newResults) {
            this.results = newResults;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ResultViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_user_result, parent, false);
            return new ResultViewHolder(view, viewModel, listener);
        }

        @Override
        public void onBindViewHolder(@NonNull ResultViewHolder holder, int position) {
            String username = results.get(position);
            holder.bind(username);
        }

        @Override
        public int getItemCount() {
            return results.size();
        }

        static class ResultViewHolder extends RecyclerView.ViewHolder {
            private final android.widget.TextView usernameText;
            private final android.widget.Button viewProfileButton;
            private final android.widget.Button followButton;
            private final AddUserViewModel viewModel;
            private final ProfileClickListener listener;

            public ResultViewHolder(@NonNull View itemView, AddUserViewModel viewModel, ProfileClickListener listener) {
                super(itemView);
                usernameText = itemView.findViewById(R.id.username_text);
                viewProfileButton = itemView.findViewById(R.id.view_profile_button);
                followButton = itemView.findViewById(R.id.follow_button);
                this.viewModel = viewModel;
                this.listener = listener;

                viewProfileButton.setOnClickListener(v -> {
                    if (getAdapterPosition() != RecyclerView.NO_POSITION) {
                        listener.onViewProfileClicked(usernameText.getText().toString());
                    }
                });
                followButton.setOnClickListener(v -> {
                    if (getAdapterPosition() != RecyclerView.NO_POSITION) {
                        listener.onFollowClicked(usernameText.getText().toString());
                    }
                });
            }

            void bind(String username) {
                usernameText.setText(username);
                // Disable the follow button if already following or if a follow request was sent.
                boolean disabled = viewModel.isAlreadyFollowingOrRequested(username);
                followButton.setEnabled(!disabled);
            }
        }
    }
}