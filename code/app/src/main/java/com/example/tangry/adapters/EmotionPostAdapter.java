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

/**
 * Adapter class for displaying EmotionPost objects in a RecyclerView.
 */
public class EmotionPostAdapter extends RecyclerView.Adapter<EmotionPostAdapter.PostViewHolder> {

    /**
     * Callback interface for handling click events on EmotionPost items.
     */
    public interface OnItemClickListener {
        /**
         * Called when an EmotionPost item is clicked.
         *
         * @param post the EmotionPost that was clicked
         * @param view the View associated with the clicked item
         */
        void onItemClick(EmotionPost post, View view);
    }

    private List<EmotionPost> posts;
    private OnItemClickListener listener;

    /**
     * Constructs a new EmotionPostAdapter.
     *
     * @param posts    the list of EmotionPost objects to be displayed; if null, an empty list is used
     * @param listener a callback to handle item click events
     */
    public EmotionPostAdapter(List<EmotionPost> posts, OnItemClickListener listener) {
        this.posts = (posts != null) ? posts : new ArrayList<>();
        this.listener = listener;
    }

    /**
     * Updates the list of EmotionPost objects and notifies the adapter of data changes.
     *
     * @param posts the new list of EmotionPost objects; if null, an empty list is used
     */
    public void setPosts(List<EmotionPost> posts) {
        this.posts = (posts != null) ? posts : new ArrayList<>();
        notifyDataSetChanged();
    }

    /**
     * Called when RecyclerView needs a new PostViewHolder.
     *
     * @param parent   the parent ViewGroup into which the new view will be added
     * @param viewType the view type of the new view
     * @return a new PostViewHolder for an EmotionPost item
     */
    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_mood, parent, false);
        return new PostViewHolder(view);
    }

    /**
     * Called by RecyclerView to display data at the specified position.
     *
     * @param holder   the PostViewHolder which should be updated to represent the contents of the item
     * @param position the position of the item within the adapter's data set
     */
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

    /**
     * Returns the total number of items in the data set held by the adapter.
     *
     * @return the number of EmotionPost items
     */
    @Override
    public int getItemCount() {
        return (posts != null) ? posts.size() : 0;
    }

    /**
     * ViewHolder class for holding and binding the UI elements of an EmotionPost item.
     */
    static class PostViewHolder extends RecyclerView.ViewHolder {
        private final TextView userName, moodText, userHandle, locationText, withText, reasonText, timeText;
        private final ImageView moodImage, emojiImage;

        /**
         * Constructs a new PostViewHolder.
         *
         * @param itemView the View representing an item in the RecyclerView
         */
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

        /**
         * Binds the EmotionPost data to the UI elements of the ViewHolder.
         *
         * @param post the EmotionPost object containing the data to bind
         */
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