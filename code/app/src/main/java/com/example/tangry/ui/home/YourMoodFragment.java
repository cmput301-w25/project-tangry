package com.example.tangry.ui.home;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.tangry.R;
import com.example.tangry.adapters.EmotionPostAdapter;
import com.example.tangry.models.EmotionPost;
import com.example.tangry.repositories.EmotionPostRepository;
import com.google.firebase.firestore.DocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class YourMoodFragment extends Fragment {

    private RecyclerView recyclerView;
    private EmotionPostAdapter adapter;
    private final List<EmotionPost> posts = new ArrayList<>();
    private EmotionPostRepository repository;

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
        repository = EmotionPostRepository.getInstance();
        setupFirestoreListener();
        return root;
    }

    private void setupFirestoreListener() {
        repository.getPostsQuery().addSnapshotListener((value, error) -> {
            if (error != null) {
                Log.e("HomeFragment", "Listen failed", error);
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
}
