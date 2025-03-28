package com.example.tangry.ui.home;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tangry.R;
import com.example.tangry.adapters.EmotionPostAdapter;
import com.example.tangry.controllers.EmotionPostController;
import com.example.tangry.models.EmotionPost;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

public class YourMoodFragment extends Fragment {

    private RecyclerView recyclerView;
    private EmotionPostAdapter adapter;
    private final List<EmotionPost> posts = new ArrayList<>();
    private EmotionPostController controller;
    private List<String> selectedFilters = new ArrayList<>();

    /**
     * Default empty constructor required for fragment instantiation.
     */
    public YourMoodFragment() {
        // Required empty constructor
    }

    /**
     * Inflates the fragment layout and initializes the RecyclerView and Firestore listener.
     *
     * @param inflater           The LayoutInflater object used to inflate the layout.
     * @param container          The parent view container.
     * @param savedInstanceState Previously saved state, if any.
     * @return The root View of the inflated layout.
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_your_mood, container, false);
        recyclerView = root.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Updated adapter instantiation with two arguments.
        // Instead of calling action_yourMoodFragment_to_postDetailsFragment, we use the HomeFragment action.
        adapter = new EmotionPostAdapter(posts, (post, itemView) -> {
            Bundle bundle = new Bundle();
            Gson gson = new Gson();
            String postJson = gson.toJson(post);
            bundle.putString("post", postJson);
            bundle.putString("postId", post.getPostId());
            Navigation.findNavController(itemView)
                    .navigate(R.id.action_homeFragment_to_postDetailsFragment, bundle);
        });
        recyclerView.setAdapter(adapter);
        controller = new EmotionPostController();

        // Setup toolbar filter menu
        Toolbar toolbar = requireActivity().findViewById(R.id.toolbar_primary);
        // Ensure menu is not duplicated.
        toolbar.getMenu().clear();
        toolbar.inflateMenu(R.menu.menu_profile);
        toolbar.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.ic_filter) {
                showFilterBottomSheet();
                return true;
            } else if (id == R.id.action_profile) {
                Navigation.findNavController(getView())
                        .navigate(R.id.action_global_personal_profile);
                return true;
            }
            return false;
        });

        // Initialize Firestore listener to load all posts initially.
        setupFirestoreListener(null, false);
        return root;
    }

    /**
     * Sets up a Firestore listener to retrieve emotion posts, optionally filtering by
     * specified emotions and a recent-week flag.
     *
     * @param filters      A list of emotion filters to apply; if null, an empty list is used.
     * @param filterRecent If true, only posts from the recent week are retrieved.
     */
    private void setupFirestoreListener(@Nullable List<String> filters, boolean filterRecent) {
        if (filters == null) {
            filters = new ArrayList<>();
        }
        Query query = controller.getFilteredPostsQuery(filters, filterRecent);

        query.addSnapshotListener((value, error) -> {
            if (error != null) {
                Log.e("YourMoodFragment", "Listen failed", error);
                return;
            }

            if (value != null) {
                posts.clear();
                for (DocumentSnapshot doc : value.getDocuments()) {
                    EmotionPost post = doc.toObject(EmotionPost.class);
                    if (post != null) {
                        post.setPostId(doc.getId());
                        posts.add(post);
                    }
                }
                adapter.notifyDataSetChanged();
            }
        });
    }

    /**
     * Displays a BottomSheetDialog with mood filter options. Selected filters are applied
     * when the "Apply" button is pressed.
     */
    private void showFilterBottomSheet() {
        View view = getLayoutInflater().inflate(R.layout.modal_bottom_sheet, null);
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        dialog.setContentView(view);

        CheckBox filterHappy = view.findViewById(R.id.Happy_filter);
        CheckBox filterSad = view.findViewById(R.id.Sad_filter);
        CheckBox filterAngry = view.findViewById(R.id.Angry_filter);
        CheckBox filterSurprised = view.findViewById(R.id.Surprised_filter);
        CheckBox filterDisgusted = view.findViewById(R.id.Disgusted_filter);
        CheckBox filterFearful = view.findViewById(R.id.Fearful_filter);
        CheckBox filterShame = view.findViewById(R.id.Shame_filter);
        CheckBox filterConfused = view.findViewById(R.id.Confused_filter);
        CheckBox filterRecentWeek = view.findViewById(R.id.recent_week_filter);

        Button applyFilterButton = view.findViewById(R.id.apply_filter_button);
        applyFilterButton.setOnClickListener(v -> {
            selectedFilters.clear();
            if (filterHappy.isChecked()) selectedFilters.add("Happiness");
            if (filterSad.isChecked()) selectedFilters.add("Sadness");
            if (filterAngry.isChecked()) selectedFilters.add("Angry");
            if (filterSurprised.isChecked()) selectedFilters.add("Surprise");
            if (filterDisgusted.isChecked()) selectedFilters.add("Disgust");
            if (filterFearful.isChecked()) selectedFilters.add("Fear");
            if (filterShame.isChecked()) selectedFilters.add("Shame");
            if (filterConfused.isChecked()) selectedFilters.add("Confused");

            boolean filterRecent = filterRecentWeek.isChecked();
            setupFirestoreListener(selectedFilters, filterRecent);  // Fetch filtered posts from Firestore.
            dialog.dismiss();
        });

        dialog.show();
    }
}