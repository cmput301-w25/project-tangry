package com.example.tangry.ui.add_user;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tangry.R;

import java.util.ArrayList;
import java.util.List;

public class AddUserFragment extends Fragment {

    private AddUserViewModel viewModel;
    private EditText usernameInput;
    private Button searchButton;
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
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(AddUserViewModel.class);
        usernameInput = view.findViewById(R.id.username_input);
        searchButton = view.findViewById(R.id.search_button);
        resultsRecyclerView = view.findViewById(R.id.results_recycler_view);

        adapter = new SearchResultsAdapter(new ArrayList<>(), new SearchResultsAdapter.ProfileClickListener() {
            @Override
            public void onViewProfileClicked(String username) {
                // Navigate to the UserProfileFragment, passing the username as an argument.
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
            if(query.isEmpty()){
                Toast.makeText(getContext(), "Please enter a username", Toast.LENGTH_SHORT).show();
            } else {
                viewModel.searchUser(query);
            }
        });

        viewModel.getSearchResults().observe(getViewLifecycleOwner(), results -> adapter.setResults(results));
        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
            }
        });
        viewModel.getFollowSuccessMessage().observe(getViewLifecycleOwner(), message -> {
            if(message != null){
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public static class SearchResultsAdapter extends RecyclerView.Adapter<SearchResultsAdapter.ResultViewHolder> {

        private List<String> results;
        private final ProfileClickListener clickListener;

        public interface ProfileClickListener {
            void onViewProfileClicked(String username);
            void onFollowClicked(String username);
        }

        public SearchResultsAdapter(List<String> results, ProfileClickListener listener) {
            this.results = results;
            this.clickListener = listener;
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
            return new ResultViewHolder(view, clickListener);
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
            private final TextView usernameText;
            private final Button viewProfileButton;
            private final Button followButton;

            public ResultViewHolder(@NonNull View itemView, ProfileClickListener listener) {
                super(itemView);
                usernameText = itemView.findViewById(R.id.username_text);
                viewProfileButton = itemView.findViewById(R.id.view_profile_button);
                // If you have a follow button in your item layout; otherwise remove.
                followButton = itemView.findViewById(R.id.follow_button);

                viewProfileButton.setOnClickListener(v -> {
                    if(getAdapterPosition() != RecyclerView.NO_POSITION){
                        listener.onViewProfileClicked(usernameText.getText().toString());
                    }
                });
                if(followButton != null){
                    followButton.setOnClickListener(v -> {
                        if(getAdapterPosition() != RecyclerView.NO_POSITION){
                            listener.onFollowClicked(usernameText.getText().toString());
                        }
                    });
                }
            }

            void bind(String username) {
                usernameText.setText(username);
            }
        }
    }
}