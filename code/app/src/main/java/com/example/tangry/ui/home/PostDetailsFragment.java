package com.example.tangry.ui.home;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import com.bumptech.glide.Glide;
import com.example.tangry.R;
import com.example.tangry.models.EmotionPost;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class PostDetailsFragment extends Fragment {
    private TextView userName, moodText, userHandle, locationText, withText, reasonText, timeText;
    private ImageView moodImage, emojiImage;
    private Button editButton, deleteButton;
    private EmotionPost post;
    private String postId;

    public PostDetailsFragment() { }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_mood_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize views
        userName = view.findViewById(R.id.user_name);
        moodText = view.findViewById(R.id.mood_text);
        userHandle = view.findViewById(R.id.user_handle);
        locationText = view.findViewById(R.id.location_text);
        withText = view.findViewById(R.id.with_text);
        reasonText = view.findViewById(R.id.reason_text);
        timeText = view.findViewById(R.id.time_text);
        moodImage = view.findViewById(R.id.mood_image);
        emojiImage = view.findViewById(R.id.emoji_image);
        editButton = view.findViewById(R.id.edit_button);
        deleteButton = view.findViewById(R.id.delete_button);

        // Retrieve Data Using Arguments
        if (getArguments() != null) {
            String postJson = getArguments().getString("post");  // ✅ Retrieve JSON
            postId = getArguments().getString("postId");

            if (postJson != null) {
                Gson gson = new Gson();
                post = gson.fromJson(postJson, EmotionPost.class);  // ✅ Convert JSON back to Object
                bindPostDetails(post);
            }
        }

        // Edit Button Click Listener
        editButton.setOnClickListener(v -> Log.d("PostDetails", "Edit button clicked"));

        // Delete Button Click Listener
        deleteButton.setOnClickListener(v -> deletePost());
    }

    private void bindPostDetails(EmotionPost post) {
        userName.setText(post.getUsername() + " feels ");
        moodText.setText(post.getEmotion());
        userHandle.setText("@" + post.getUsername());
        locationText.setText(post.getLocation());
        withText.setText(post.getSocialSituation());
        reasonText.setText(post.getExplanation());

        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault());
        timeText.setText(sdf.format(post.getTimestamp().toDate()));

        if (post.getImageUri() != null) {
            Glide.with(requireContext())
                    .load(Uri.parse(post.getImageUri()))
                    .into(moodImage);
        } else {
            moodImage.setImageResource(R.drawable.ic_placeholder);
        }

        // Load emoji based on mood
        setEmojiAndColor(post.getEmotion());
    }

    private void setEmojiAndColor(String emotion) {
        int emojiResId, colorResId;
        switch (emotion.toLowerCase()) {
            case "happiness": emojiResId = R.drawable.ic_happiness; colorResId = R.color.colorHappiness; break;
            case "sadness": emojiResId = R.drawable.ic_sadness; colorResId = R.color.colorSadness; break;
            case "angry": emojiResId = R.drawable.ic_angry; colorResId = R.color.colorAngry; break;
            case "fear": emojiResId = R.drawable.ic_fear; colorResId = R.color.colorFear; break;
            case "disgust": emojiResId = R.drawable.ic_disgust; colorResId = R.color.colorDisgust; break;
            case "shame": emojiResId = R.drawable.ic_shame; colorResId = R.color.colorShame; break;
            case "surprise": emojiResId = R.drawable.ic_surprise; colorResId = R.color.colorSurprise; break;
            case "confused": emojiResId = R.drawable.ic_confused; colorResId = R.color.colorConfused; break;
            default: emojiResId = R.drawable.ic_placeholder; colorResId = R.color.colorPrimaryDark; break;
        }

        emojiImage.setImageResource(emojiResId);
        moodText.setTextColor(ContextCompat.getColor(requireContext(), colorResId));
    }

    private void deletePost() {
        if (postId != null) {
            FirebaseFirestore.getInstance().collection("posts").document(postId)
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        Log.d("PostDetails", "Post deleted successfully");
                        requireActivity().getSupportFragmentManager().popBackStack();
                    })
                    .addOnFailureListener(e -> Log.e("PostDetails", "Error deleting post", e));
        }
    }
}
