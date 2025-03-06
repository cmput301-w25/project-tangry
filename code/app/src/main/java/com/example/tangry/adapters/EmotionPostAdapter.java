// EmotionPostAdapter.java
package com.example.tangry.adapters;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.tangry.models.EmotionPost;
import com.example.tangry.R;
import java.util.List;

public class EmotionPostAdapter extends RecyclerView.Adapter<EmotionPostAdapter.PostViewHolder> {

    private final List<EmotionPost> posts;

    public EmotionPostAdapter(List<EmotionPost> posts) {
        this.posts = posts;
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_emotion_post, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        EmotionPost post = posts.get(position);
        holder.bind(post);
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    static class PostViewHolder extends RecyclerView.ViewHolder {
        private final TextView emotionText;
        private final TextView explanationText;
        private final ImageView postImage;
        private final TextView locationText;
        private final TextView socialSituationText;
        private final TextView usernameText;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            emotionText = itemView.findViewById(R.id.emotion_text);
            explanationText = itemView.findViewById(R.id.explanation_text);
            postImage = itemView.findViewById(R.id.post_image);
            locationText = itemView.findViewById(R.id.location_text);
            socialSituationText = itemView.findViewById(R.id.social_situation_text);
            usernameText = itemView.findViewById(R.id.username_text);
        }

        public void bind(EmotionPost post) {
            emotionText.setText(post.getEmotion());
            explanationText.setText(post.getExplanation());
            locationText.setText(post.getLocation());
            socialSituationText.setText(post.getSocialSituation());
            usernameText.setText(post.getUsername());

            String imageUri = post.getImageUri();
            if (imageUri != null) {
                Glide.with(itemView.getContext())
                        .load(Uri.parse(imageUri))
                        .into(postImage);
            } else {
                postImage.setImageResource(R.drawable.ic_placeholder);
            }
        }
    }
}