package com.example.tangry.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tangry.R;
import com.example.tangry.models.Emotion;
import java.util.List;

public class EmotionEmojisAdapter extends RecyclerView.Adapter<EmotionEmojisAdapter.ViewHolder> {

    public interface ItemClickListener {
        void onItemClick(Emotion emotion);
    }

    private final List<Emotion> emotionList;
    private final ItemClickListener listener;

    public EmotionEmojisAdapter(List<Emotion> emotionList, ItemClickListener listener) {
        this.emotionList = emotionList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate our custom item layout
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_emotion, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Emotion emotion = emotionList.get(position);

        // Set the emotion name
        holder.emotionText.setText(emotion.getName());
        // Set the text color from resource
        holder.emotionText.setTextColor(
                ContextCompat.getColor(holder.itemView.getContext(), emotion.getTextColorResId())
        );
        // Set the icon using the selector drawable
        holder.emotionIcon.setImageResource(emotion.getIconResId());

        // Handle clicks
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(emotion);
            }
        });
    }

    @Override
    public int getItemCount() {
        return emotionList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView emotionIcon;
        TextView emotionText;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            emotionIcon = itemView.findViewById(R.id.emotionIcon);
            emotionText = itemView.findViewById(R.id.emotionText);
        }
    }
}
