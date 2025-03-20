package com.example.tangry.adapters;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.tangry.models.EmotionPost;
import com.example.tangry.R;
import com.example.tangry.utils.TimeUtils;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

public class EmotionPostAdapter extends RecyclerView.Adapter<EmotionPostAdapter.PostViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(EmotionPost post, View view);
    }

    private List<EmotionPost> posts;
    private OnItemClickListener listener;

    /**
     * Constructs a new EmotionPostAdapter with the specified list of posts and click listener.
     *
     * @param posts    the list of EmotionPost objects to be displayed
     * @param listener a callback for item clicks
     */
    public EmotionPostAdapter(List<EmotionPost> posts, OnItemClickListener listener) {
        this.posts = (posts != null) ? posts : new ArrayList<>();
        this.listener = listener;
    }

    /**
     * Updates the adapter's list of posts.
     *
     * @param posts the new list of posts
     */
    public void setPosts(List<EmotionPost> posts) {
        this.posts = (posts != null) ? posts : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_mood, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        EmotionPost post = posts.get(position);
        holder.bind(post);
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(post, v);
            }
        });
    }

    @Override
    public int getItemCount() {
        return (posts != null) ? posts.size() : 0;
    }

    static class PostViewHolder extends RecyclerView.ViewHolder {
        private final TextView userName, moodText, userHandle, locationText, withText, reasonText, timeText;
        private final ImageView moodImage, emojiImage;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.user_name);
            moodText = itemView.findViewById(R.id.mood_text);
            userHandle = itemView.findViewById(R.id.user_handle);
            locationText = itemView.findViewById(R.id.location_text);
            withText = itemView.findViewById(R.id.with_text);
            reasonText = itemView.findViewById(R.id.reason_text);
            timeText = itemView.findViewById(R.id.time_text);
            moodImage = itemView.findViewById(R.id.mood_image);
            emojiImage = itemView.findViewById(R.id.emoji_image);
        }

        public void bind(EmotionPost post) {
            // Use default text if any field is missing.
            String username = (post.getUsername() != null) ? post.getUsername() : "Anonymous";
            userName.setText(username + " feels ");
            userHandle.setText("@" + username);

            moodText.setText((post.getEmotion() != null) ? post.getEmotion() : "Unknown");

            String location = (post.getLocation() != null && !post.getLocation().isEmpty())
                    ? post.getLocation() : "Not Provided";
            locationText.setText(location);
            locationText.setTextColor(ContextCompat.getColor(itemView.getContext(),
                    (post.getLocation() != null && !post.getLocation().isEmpty()) ? R.color.black : R.color.gray));

            String social = (post.getSocialSituation() != null && !post.getSocialSituation().isEmpty())
                    ? post.getSocialSituation() : "Not Provided";
            withText.setText(social);
            withText.setTextColor(ContextCompat.getColor(itemView.getContext(),
                    (post.getSocialSituation() != null && !post.getSocialSituation().isEmpty()) ? R.color.black : R.color.gray));

            String explanation = (post.getExplanation() != null && !post.getExplanation().isEmpty())
                    ? post.getExplanation() : "Not Provided";
            reasonText.setText(explanation);
            reasonText.setTextColor(ContextCompat.getColor(itemView.getContext(),
                    (post.getExplanation() != null && !post.getExplanation().isEmpty()) ? R.color.black : R.color.gray));

            if (post.getTimestamp() != null) {
                timeText.setText(TimeUtils.getTimeAgo(post.getTimestamp().toDate()));
            } else {
                timeText.setText("Unknown Time");
            }

            if (post.getImageUri() != null && !post.getImageUri().isEmpty()) {
                moodImage.setVisibility(View.VISIBLE);
                Glide.with(itemView.getContext())
                        .load(Uri.parse(post.getImageUri()))
                        .into(moodImage);
            } else {
                moodImage.setVisibility(View.GONE);
            }

            // Set emoji and text color based on the emotion.
            String emotion = (post.getEmotion() != null) ? post.getEmotion().toLowerCase() : "";
            switch (emotion) {
                case "happiness":
                    emojiImage.setImageResource(R.drawable.ic_happiness);
                    moodText.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.colorHappiness));
                    break;
                case "sadness":
                    emojiImage.setImageResource(R.drawable.ic_sadness);
                    moodText.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.colorSadness));
                    break;
                case "angry":
                    emojiImage.setImageResource(R.drawable.ic_angry);
                    moodText.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.colorAngry));
                    break;
                case "fear":
                    emojiImage.setImageResource(R.drawable.ic_fear);
                    moodText.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.colorFear));
                    break;
                case "disgust":
                    emojiImage.setImageResource(R.drawable.ic_disgust);
                    moodText.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.colorDisgust));
                    break;
                case "shame":
                    emojiImage.setImageResource(R.drawable.ic_shame);
                    moodText.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.colorShame));
                    break;
                case "surprise":
                    emojiImage.setImageResource(R.drawable.ic_surprise);
                    moodText.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.colorSurprise));
                    break;
                case "confused":
                    emojiImage.setImageResource(R.drawable.ic_confused);
                    moodText.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.colorConfused));
                    break;
                default:
                    emojiImage.setImageResource(R.drawable.ic_placeholder);
                    break;
            }
        }
    }
}