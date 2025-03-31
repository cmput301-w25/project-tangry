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

/**
 * Fragment that allows users to search for and add new users.
 */
public class AddUserFragment extends Fragment {

    private AddUserViewModel viewModel;
    private TextInputEditText usernameInput;
    private MaterialButton searchButton;
    private RecyclerView resultsRecyclerView;
    private SearchResultsAdapter adapter;

    /**
     * Inflates the layout for this fragment.
     *
     * @param inflater LayoutInflater used to inflate views.
     * @param container The parent view that the fragment's UI should be attached to.
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state.
     * @return the View for the fragment's UI, or null.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_user, container, false);
    }

    /**
     * Called immediately after onCreateView. Sets up UI components and observers.
     *
     * @param view The view returned by onCreateView.
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state.
     */
    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(AddUserViewModel.class);
        usernameInput = view.findViewById(R.id.username_input);
        searchButton = view.findViewById(R.id.search_button);
        resultsRecyclerView = view.findViewById(R.id.results_recycler_view);

        adapter = new SearchResultsAdapter(new ArrayList<>(), viewModel, new SearchResultsAdapter.ProfileClickListener() {
            /**
             * Callback invoked when the view profile button is clicked.
             *
             * @param username the username whose profile should be viewed.
             */
            @Override
            public void onViewProfileClicked(String username) {
                Bundle bundle = new Bundle();
                bundle.putString("username", username);
                Navigation.findNavController(view)
                        .navigate(R.id.action_navigation_add_user_to_userProfileFragment, bundle);
            }

            /**
             * Callback invoked when the follow button is clicked.
             *
             * @param username the username to follow.
             */
            @Override
            public void onFollowClicked(String username) {
                viewModel.followUser(username);
            }
        });
        resultsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        resultsRecyclerView.setAdapter(adapter);

        // Set click listener for the search button.
        searchButton.setOnClickListener(v -> {
            String query = usernameInput.getText().toString().trim();
            if (query.isEmpty()) {
                Toast.makeText(getContext(), "Please enter a username", Toast.LENGTH_SHORT).show();
            } else {
                viewModel.searchUser(query);
            }
        });

        // Observe search results and update adapter.
        viewModel.getSearchResults().observe(getViewLifecycleOwner(), results -> adapter.setResults(results));
        // Observe messages and display a toast.
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

    /**
     * Adapter class for displaying search results in a RecyclerView.
     */
    public static class SearchResultsAdapter extends RecyclerView.Adapter<SearchResultsAdapter.ResultViewHolder> {

        private List<String> results;
        private final ProfileClickListener listener;
        private final AddUserViewModel viewModel;

        /**
         * Interface for handling profile-related click events.
         */
        public interface ProfileClickListener {
            /**
             * Called when a view profile button is clicked.
             *
             * @param username the username whose profile should be viewed.
             */
            void onViewProfileClicked(String username);

            /**
             * Called when a follow button is clicked.
             *
             * @param username the username to follow.
             */
            void onFollowClicked(String username);
        }

        /**
         * Constructs the SearchResultsAdapter.
         *
         * @param results List of usernames representing search results.
         * @param viewModel The ViewModel used to determine follow status.
         * @param listener Listener for profile-related actions.
         */
        public SearchResultsAdapter(List<String> results, AddUserViewModel viewModel, ProfileClickListener listener) {
            this.results = results;
            this.viewModel = viewModel;
            this.listener = listener;
        }

        /**
         * Updates the adapter with new search results.
         *
         * @param newResults List of usernames representing updated search results.
         */
        public void setResults(List<String> newResults) {
            this.results = newResults;
            notifyDataSetChanged();
        }

        /**
         * Called when RecyclerView needs a new ViewHolder of the given type to represent an item.
         *
         * @param parent The ViewGroup into which the new View will be added.
         * @param viewType The view type of the new View.
         * @return A new ResultViewHolder that holds a View of the given view type.
         */
        @NonNull
        @Override
        public ResultViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_user_result, parent, false);
            return new ResultViewHolder(view, viewModel, listener);
        }

        /**
         * Binds the data to the ViewHolder.
         *
         * @param holder The ViewHolder to update.
         * @param position The position of the item within the data set.
         */
        @Override
        public void onBindViewHolder(@NonNull ResultViewHolder holder, int position) {
            String username = results.get(position);
            holder.bind(username);
        }

        /**
         * Returns the total number of items in the data set held by the adapter.
         *
         * @return The total number of items.
         */
        @Override
        public int getItemCount() {
            return results.size();
        }

        /**
         * ViewHolder class for search result items.
         */
        static class ResultViewHolder extends RecyclerView.ViewHolder {
            private final android.widget.TextView usernameText;
            private final android.widget.Button viewProfileButton;
            private final android.widget.Button followButton;
            private final AddUserViewModel viewModel;
            private final ProfileClickListener listener;

            /**
             * Constructs a ResultViewHolder.
             *
             * @param itemView The item view.
             * @param viewModel The ViewModel to check follow status.
             * @param listener Listener for click actions.
             */
            public ResultViewHolder(@NonNull View itemView, AddUserViewModel viewModel, ProfileClickListener listener) {
                super(itemView);
                usernameText = itemView.findViewById(R.id.username_text);
                viewProfileButton = itemView.findViewById(R.id.view_profile_button);
                followButton = itemView.findViewById(R.id.follow_button);
                this.viewModel = viewModel;
                this.listener = listener;

                // Set listener for view profile button.
                viewProfileButton.setOnClickListener(v -> {
                    if (getAdapterPosition() != RecyclerView.NO_POSITION) {
                        listener.onViewProfileClicked(usernameText.getText().toString());
                    }
                });
                // Set listener for follow button.
                followButton.setOnClickListener(v -> {
                    if (getAdapterPosition() != RecyclerView.NO_POSITION) {
                        listener.onFollowClicked(usernameText.getText().toString());
                    }
                });
            }

            /**
             * Binds a username to the item view and updates button state.
             *
             * @param username The username to bind.
             */
            void bind(String username) {
                usernameText.setText(username);
                // Disable the follow button if already following or if a follow request was sent.
                boolean disabled = viewModel.isAlreadyFollowingOrRequested(username);
                followButton.setEnabled(!disabled);
            }
        }
    }
}