package com.example.tangry;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.tangry.models.Emotion;
import com.example.tangry.EmotionAdapter;
import com.example.tangry.EmotionsFragmentDirections;
import java.util.Arrays;
import java.util.List;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class EmotionsFragment extends Fragment implements EmotionAdapter.ItemClickListener {

    private NavController navController;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_emotions, container, false);

        navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_activity_main);

        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);

        // Create the adapter using the new EmotionAdapter
        EmotionAdapter adapter = new EmotionAdapter(getSampleEmotions(), this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        return view;
    }

    private List<Emotion> getSampleEmotions() {
        // Replace with your actual selector drawables & color resources
        return Arrays.asList(
                new Emotion("Angry",     R.drawable.ic_angry_selector,     R.color.colorAngry),
                new Emotion("Confused",  R.drawable.ic_confused_selector,  R.color.colorConfused),
                new Emotion("Disgust",   R.drawable.ic_disgust_selector,   R.color.colorDisgust),
                new Emotion("Fear",      R.drawable.ic_fear_selector,      R.color.colorFear),
                new Emotion("Happiness", R.drawable.ic_happiness_selector, R.color.colorHappiness),
                new Emotion("Sadness",   R.drawable.ic_sadness_selector,   R.color.colorSadness),
                new Emotion("Shame",     R.drawable.ic_shame_selector,     R.color.colorShame),
                new Emotion("Surprise",  R.drawable.ic_surprise_selector,  R.color.colorSurprise)
        );
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
            // Add Mode → Navigate to Add Post Screen
            EmotionsFragmentDirections.ActionEmotionsFragmentToDetailFragment action =
                    EmotionsFragmentDirections.actionEmotionsFragmentToDetailFragment(emotion.getName());
            navController.navigate(action);
        }
    }
}
