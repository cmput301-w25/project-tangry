/**
 * EmotionPostAdapter.java
 *
 * This adapter binds a list of EmotionPost model objects to a RecyclerView, representing each post as an item.
 * It handles inflating the item layout, binding post data to UI components, and managing navigation to a detailed
 * view when a post is clicked. The adapter leverages the ViewHolder pattern for efficient view recycling and
 * uses Safe Args with the Android Navigation component to pass post details to the PostDetailsFragment.
 *
 * Outstanding Issues:
 * - Additional error handling may be required if post data is missing or malformed.
 * - We might consider refactoring some of the binding logic in the PostViewHolder for improved readability and testability.
 */

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
import java.util.List;

public class EmotionPostAdapter extends RecyclerView.Adapter<EmotionPostAdapter.PostViewHolder> {

    private final List<EmotionPost> posts;

    /**
     * Constructs a new EmotionPostAdapter with the specified list of posts.
     *
     * @param posts the list of EmotionPost objects to be displayed in the RecyclerView
     */
    public EmotionPostAdapter(List<EmotionPost> posts) {
        this.posts = posts;
    }

    /**
     * Inflates the item layout and returns a new PostViewHolder.
     *
     * @param parent   the parent ViewGroup that the new view will be attached to
     * @param viewType the view type of the new view (not used here)
     * @return a new instance of PostViewHolder containing the inflated view
     */
    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_mood, parent, false);
        return new PostViewHolder(view);
    }

    /**
     * Binds the data from an EmotionPost to the corresponding PostViewHolder.
     * Also sets up click navigation to the post details view using Safe Args.
     *
     * @param holder   the PostViewHolder to bind data to
     * @param position the position of the EmotionPost within the adapter's data set
     */
    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        EmotionPost post = posts.get(position);
        holder.bind(post);

        holder.itemView.setOnClickListener(v -> {
            Bundle bundle = new Bundle();

            // Convert EmotionPost to JSON String for safe passage between fragments.
            Gson gson = new Gson();
            String postJson = gson.toJson(post);

            bundle.putString("post", postJson);
            bundle.putString("postId", post.getPostId());

            // Navigate to the PostDetailsFragment using Safe Args
            Navigation.findNavController(v)
                    .navigate(R.id.action_homeFragment_to_postDetailsFragment, bundle);
        });
    }

    /**
     * Returns the total number of posts in the adapter.
     *
     * @return the number of EmotionPost items
     */
    @Override
    public int getItemCount() {
        return posts.size();
    }

    /**
     * ViewHolder class that holds the views for an individual EmotionPost item.
     */
    static class PostViewHolder extends RecyclerView.ViewHolder {
        private final TextView userName, moodText, userHandle, locationText, withText, reasonText, timeText;
        private final ImageView moodImage, emojiImage;

        /**
         * Constructs a new PostViewHolder by initializing the view components.
         *
         * @param itemView the root view of the item layout
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
         * Binds the provided EmotionPost data to the corresponding UI components.
         *
         * @param post the EmotionPost object containing data to be displayed
         */
        public void bind(EmotionPost post) {
            userName.setText(post.getUsername() + " feels ");
            moodText.setText(post.getEmotion());
            userHandle.setText("@" + post.getUsername());

            if (!post.getLocation().isEmpty()) {
                locationText.setText(post.getLocation());
                locationText.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.black));
            } else {
                locationText.setText("Not Provided");
                locationText.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.gray));
            }

            if (post.getSocialSituation() != null) {
                withText.setText(post.getSocialSituation());
                withText.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.black));
            } else {
                withText.setText("Not Provided");
                withText.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.gray));
            }

            if (!post.getExplanation().isEmpty()) {
                reasonText.setText(post.getExplanation());
                reasonText.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.black));
            } else {
                reasonText.setText("Not Provided");
                reasonText.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.gray));
            }

            if (post.getTimestamp() != null) {
                timeText.setText(TimeUtils.getTimeAgo(post.getTimestamp().toDate()));
            } else {
                timeText.setText("Unknown Time");
            }

            // Load mood image using Glide if an image URI is provided
            if (post.getImageUri() != null) {
                moodImage.setVisibility(View.VISIBLE);
                Glide.with(itemView.getContext())
                        .load(Uri.parse(post.getImageUri()))
                        .into(moodImage);
            } else {
                moodImage.setVisibility(View.GONE);
            }

            // Set emoji and text color based on the post's emotion
            switch (post.getEmotion().toLowerCase()) {
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
