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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.tangry.R;
import com.example.tangry.adapters.EmotionPostAdapter;
import com.example.tangry.controllers.EmotionPostController;
import com.example.tangry.models.EmotionPost;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class YourMoodFragment extends Fragment {

    private RecyclerView recyclerView;
    private EmotionPostAdapter adapter;
    private final List<EmotionPost> posts = new ArrayList<>();
    private EmotionPostController controller;
    private List<String> selectedFilters = new ArrayList<>();

    public YourMoodFragment() {
        // Required empty constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_your_mood, container, false);
        recyclerView = root.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new EmotionPostAdapter(posts);
        recyclerView.setAdapter(adapter);
        controller = new EmotionPostController();

        Toolbar toolbar = requireActivity().findViewById(R.id.toolbar_primary);

        // Ensure menu is not duplicated
        toolbar.getMenu().clear();
        toolbar.inflateMenu(R.menu.menu_profile);

        // Set click listener if not already set
        toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.ic_filter) {
                showFilterBottomSheet();
                return true;
            }
            return false;
        });

        setupFirestoreListener(null, false); // Load all posts initially
        return root;
    }

    private void setupFirestoreListener(@Nullable List<String> filters, boolean filterRecent) {
        if (filters == null) {
            filters = new ArrayList<>();
        }
        Query query = controller.getFilteredPostsQuery(filters, filterRecent); // 🔥 Use updated method

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

            setupFirestoreListener(selectedFilters, filterRecent);  // Fetch filtered posts from Firestore
            dialog.dismiss();
        });

        dialog.show();
    }
}
