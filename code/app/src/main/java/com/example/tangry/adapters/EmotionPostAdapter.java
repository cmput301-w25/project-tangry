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
import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class EmotionPostAdapter extends RecyclerView.Adapter<EmotionPostAdapter.PostViewHolder> {
    private final List<EmotionPost> posts;

    public EmotionPostAdapter(List<EmotionPost> posts) {
        this.posts = posts;
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
            Bundle bundle = new Bundle();

            // ✅ Convert EmotionPost to JSON String
            Gson gson = new Gson();
            String postJson = gson.toJson(post);

            bundle.putString("post", postJson);  // 🔥 Pass JSON instead of Object
            bundle.putString("postId", post.getPostId());

            // ✅ Navigate using Safe Args
            Navigation.findNavController(v).navigate(R.id.action_homeFragment_to_postDetailsFragment, bundle);
        });
    }


    @Override
    public int getItemCount() {
        return posts.size();
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
            userName.setText(post.getUsername() + " feels ");
            moodText.setText(post.getEmotion());
            userHandle.setText("@" + post.getUsername());
            locationText.setText(post.getLocation());
            withText.setText(post.getSocialSituation());
            reasonText.setText(post.getExplanation());

            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault());
            timeText.setText(sdf.format(post.getTimestamp().toDate()));

            // Load mood image
            if (post.getImageUri() != null) {
                Glide.with(itemView.getContext())
                        .load(Uri.parse(post.getImageUri()))
                        .into(moodImage);
            } else {
                moodImage.setImageResource(R.drawable.ic_placeholder);
            }

            // Load emoji based on mood
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
