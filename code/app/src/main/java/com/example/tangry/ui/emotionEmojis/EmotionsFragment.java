package com.example.tangry.ui.emotionEmojis;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tangry.models.EmotionProvider;
import com.example.tangry.ui.emotionEmojis.EmotionsFragmentDirections;
import com.example.tangry.R;
import com.example.tangry.adapters.EmotionEmojisAdapter;
import com.example.tangry.models.Emotion;

import java.util.Arrays;
import java.util.List;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


public class EmotionsFragment extends Fragment implements EmotionEmojisAdapter.ItemClickListener {

    private NavController navController;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_emotions, container, false);
        navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_activity_main);

        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        // Use EmotionProvider to retrieve the sample emotions.
        List<Emotion> emotions = EmotionProvider.getSampleEmotions();
        EmotionEmojisAdapter adapter = new EmotionEmojisAdapter(emotions, this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        return view;
    }

    @Override
    public void onItemClick(Emotion emotion) {
        Bundle args = getArguments();
        if (args != null && args.getBoolean("isEditing", false)) {
            // Editing Mode → Pass Selected Emotion to EditEmotionFragment
            Bundle bundle = new Bundle();
            bundle.putString("postJson", args.getString("postJson")); // Keep post data
            bundle.putString("postId", args.getString("postId")); // Keep post ID
            bundle.putString("selectedEmotion", emotion.getName()); // Pass new emotion

            navController.navigate(R.id.action_emotionsFragment_to_editEmotionFragment, bundle);
        } else {
            // Add Mode → Navigate to Create (Add) Post Screen
            EmotionsFragmentDirections.ActionEmotionsFragmentToDetailFragment action =
                    EmotionsFragmentDirections.actionEmotionsFragmentToDetailFragment(emotion.getName());
            navController.navigate(action);
        }
    }
}

