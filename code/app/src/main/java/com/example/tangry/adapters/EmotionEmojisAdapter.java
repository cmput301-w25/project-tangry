/**
 * EmotionEmojisAdapter.java
 *
 * This adapter binds a list of Emotion model objects to a RecyclerView, displaying each emotion's icon
 * and name. It utilizes the ViewHolder pattern for efficient recycling of views and provides a callback
 * interface for handling item click events. This class is a key part of the emotion selection feature.
 *
 * Outstanding Issues:
 * - We considering implementing DiffUtil for improved performance when updating the list.
 * - Additional error handling may be needed if the emotion list is modified concurrently.
 */

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

    /**
     * Callback interface to be implemented by classes that handle item click events.
     */
    public interface ItemClickListener {
        /**
         * Called when an emotion item is clicked.
         *
         * @param emotion the Emotion object corresponding to the clicked item
         */
        void onItemClick(Emotion emotion);
    }

    private final List<Emotion> emotionList;
    private final ItemClickListener listener;

    /**
     * Constructs a new EmotionEmojisAdapter.
     *
     * @param emotionList the list of Emotion objects to display
     * @param listener    the listener to handle click events on the emotion items
     */
    public EmotionEmojisAdapter(List<Emotion> emotionList, ItemClickListener listener) {
        this.emotionList = emotionList;
        this.listener = listener;
    }

    /**
     * Called when RecyclerView needs a new {@link ViewHolder} of the given type to represent an item.
     *
     * @param parent   the parent ViewGroup into which the new view will be added
     * @param viewType the view type of the new view (not used in this adapter)
     * @return a new ViewHolder that holds the view for an emotion item
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the custom layout for an emotion item
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_emotion, parent, false);
        return new ViewHolder(view);
    }

    /**
     * Called by RecyclerView to display the data at the specified position.
     *
     * @param holder   the ViewHolder which should be updated to represent the contents of the item at the given position
     * @param position the position of the item within the adapter's data set
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Emotion emotion = emotionList.get(position);

        // Set the emotion name
        holder.emotionText.setText(emotion.getName());
        // Set the text color using a resource color identifier
        holder.emotionText.setTextColor(
                ContextCompat.getColor(holder.itemView.getContext(), emotion.getTextColorResId())
        );
        // Set the emotion icon using the selector drawable
        holder.emotionIcon.setImageResource(emotion.getIconResId());

        // Handle item clicks by invoking the callback
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(emotion);
            }
        });
    }

    /**
     * Returns the total number of items in the adapter's data set.
     *
     * @return the number of emotion items
     */
    @Override
    public int getItemCount() {
        return emotionList.size();
    }

    /**
     * ViewHolder class that holds the views for an emotion item.
     */
    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView emotionIcon;
        TextView emotionText;

        /**
         * Constructs a new ViewHolder by initializing the item view's components.
         *
         * @param itemView the root view of the emotion item layout
         */
        ViewHolder(@NonNull View itemView) {
            super(itemView);
            emotionIcon = itemView.findViewById(R.id.emotionIcon);
            emotionText = itemView.findViewById(R.id.emotionText);
        }
    }
}
