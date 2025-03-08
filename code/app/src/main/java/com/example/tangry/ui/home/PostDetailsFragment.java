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
import androidx.appcompat.app.AlertDialog;
import com.bumptech.glide.Glide;
import com.example.tangry.R;
import com.example.tangry.models.EmotionPost;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

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
            String postJson = getArguments().getString("post");  // Retrieve JSON
            postId = getArguments().getString("postId");

            if (postJson != null) {
                Gson gson = new Gson();
                post = gson.fromJson(postJson, EmotionPost.class);  // Convert JSON back to Object
                bindPostDetails(post);
            }
        }

        // Edit Button Click Listener
        editButton.setOnClickListener(v -> editPost());

        // Delete Button Click Listener
        deleteButton.setOnClickListener(v -> deletePost());
    }

    private void bindPostDetails(EmotionPost post) {
        userName.setText(post.getUsername() + " feels ");
        moodText.setText(post.getEmotion());
        userHandle.setText("@" + post.getUsername());
        if (!post.getLocation().isEmpty()) {
            locationText.setText(post.getLocation());
            locationText.setTextColor(ContextCompat.getColor(requireContext(), R.color.black));
        } else {
            locationText.setTextColor(ContextCompat.getColor(requireContext(), R.color.gray));
        }
        withText.setText(post.getSocialSituation());
        if (!post.getExplanation().isEmpty()) {
            reasonText.setText(post.getExplanation());
            reasonText.setTextColor(ContextCompat.getColor(requireContext(), R.color.black));
        } else {
            reasonText.setTextColor(ContextCompat.getColor(requireContext(), R.color.gray));
        }

        timeText.setText(getTimeAgo(post.getTimestamp().toDate()));

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

    private String getTimeAgo(Date date) {
        long timeDiff = System.currentTimeMillis() - date.getTime();

        long minutes = TimeUnit.MILLISECONDS.toMinutes(timeDiff);
        long hours = TimeUnit.MILLISECONDS.toHours(timeDiff);
        long days = TimeUnit.MILLISECONDS.toDays(timeDiff);

        if (minutes < 1) {
            return "Just now";
        } else if (minutes < 60) {
            return minutes + " minutes ago";
        } else if (hours < 24) {
            return hours + " hours ago";
        } else if (days < 5) {
            return days + " days ago";
        } else {
            return new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(date);
        }
    }

    private void editPost() {
        if (post != null) {
            // Navigate to EmotionsFragment FIRST
            Bundle bundle = new Bundle();
            bundle.putString("postJson", new Gson().toJson(post)); // Pass the post JSON
            bundle.putString("postId", postId); // Pass Firestore ID
            bundle.putBoolean("isEditing", true); // Indicate that this is editing mode

            Navigation.findNavController(requireView()).navigate(R.id.action_postDetailsFragment_to_emotionsFragment, bundle);
        }
    }

    private void deletePost() {
        if (postId != null) {
            // Show Confirmation Dialog Before Deleting
            new AlertDialog.Builder(requireContext())
                    .setTitle("Delete Post")
                    .setMessage("Are you sure you want to delete this post? This action cannot be undone.")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        // User Confirmed: Proceed with Deletion
                        FirebaseFirestore.getInstance().collection("emotions").document(postId)
                                .delete()
                                .addOnSuccessListener(aVoid -> {
                                    Log.d("PostDetails", "Post deleted successfully");
                                    requireActivity().getSupportFragmentManager().popBackStack();
                                })
                                .addOnFailureListener(e -> Log.e("PostDetails", "Error deleting post", e));
                    })
                    .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss()) // Cancel Deletion
                    .show();
        }
    }
}
