/**
 * EmotionPostViewHolder.java
 *
 * This ViewHolder is responsible for binding EmotionPost model data to the UI components of
 * a RecyclerView item. It serves as an adapter between the data model and the presentation layer,
 * displaying the user's emotion along with related details such as location, social situation, and
 * an appropriate emoji representation.
 *
 * Outstanding Issues:
 * - The null/empty checks for location and explanation could be improved for more robust error handling.
 * - The time formatting logic in getTimeAgo may be refactored into a utility class for reuse.
 */

package com.example.tangry.adapters.viewholders;

import android.content.Context;
import android.net.Uri;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.tangry.R;
import com.example.tangry.models.EmotionPost;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class EmotionPostViewHolder extends RecyclerView.ViewHolder {

    private final TextView userName;
    private final TextView moodText;
    private final TextView userHandle;
    private final TextView locationText;
    private final TextView withText;
    private final TextView reasonText;
    private final TextView timeText;
    private final ImageView moodImage;
    private final ImageView emojiImage;
    private final Context context;

    /**
     * Constructs a new EmotionPostViewHolder by initializing all view components.
     *
     * @param itemView the root view of the RecyclerView item layout
     */
    public EmotionPostViewHolder(@NonNull View itemView) {
        super(itemView);
        context = itemView.getContext();
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
     * Binds the EmotionPost model data to the corresponding UI components.
     *
     * @param post the EmotionPost object containing the data to be displayed
     */
    public void bind(EmotionPost post) {
        // Bind text views
        userName.setText(post.getUsername() + " feels ");
        moodText.setText(post.getEmotion());
        userHandle.setText("@" + post.getUsername());

        if (post.getLocation() != null && !post.getLocation().isEmpty()) {
            locationText.setText(post.getLocation());
            locationText.setTextColor(ContextCompat.getColor(context, R.color.black));
        } else {
            locationText.setTextColor(ContextCompat.getColor(context, R.color.gray));
        }

        withText.setText(post.getSocialSituation());

        if (post.getExplanation() != null && !post.getExplanation().isEmpty()) {
            reasonText.setText(post.getExplanation());
            reasonText.setTextColor(ContextCompat.getColor(context, R.color.black));
        } else {
            reasonText.setTextColor(ContextCompat.getColor(context, R.color.gray));
        }

        timeText.setText(getTimeAgo(post.getTimestamp().toDate()));

        // Load mood image using Glide (or show a placeholder)
        if (post.getImageUri() != null && !post.getImageUri().isEmpty()) {
            Glide.with(context)
                    .load(Uri.parse(post.getImageUri()))
                    .into(moodImage);
        } else {
            moodImage.setImageResource(R.drawable.ic_placeholder);
        }

        // Map the emotion to the corresponding emoji and set the text color
        setEmojiAndColor(post.getEmotion());
    }

    /**
     * Maps the given emotion string to an emoji image resource and a text color, then applies these
     * settings to the view components.
     *
     * @param emotion the emotion string from the EmotionPost
     */
    private void setEmojiAndColor(String emotion) {
        String e = emotion.toLowerCase();
        int emojiRes;
        int colorRes;
        switch (e) {
            case "happiness":
                emojiRes = R.drawable.ic_happiness;
                colorRes = R.color.colorHappiness;
                break;
            case "sadness":
                emojiRes = R.drawable.ic_sadness;
                colorRes = R.color.colorSadness;
                break;
            case "angry":
                emojiRes = R.drawable.ic_angry;
                colorRes = R.color.colorAngry;
                break;
            case "fear":
                emojiRes = R.drawable.ic_fear;
                colorRes = R.color.colorFear;
                break;
            case "disgust":
                emojiRes = R.drawable.ic_disgust;
                colorRes = R.color.colorDisgust;
                break;
            case "shame":
                emojiRes = R.drawable.ic_shame;
                colorRes = R.color.colorShame;
                break;
            case "surprise":
                emojiRes = R.drawable.ic_surprise;
                colorRes = R.color.colorSurprise;
                break;
            case "confused":
                emojiRes = R.drawable.ic_confused;
                colorRes = R.color.colorConfused;
                break;
            default:
                emojiRes = R.drawable.ic_placeholder;
                colorRes = R.color.gray;
                break;
        }
        emojiImage.setImageResource(emojiRes);
        moodText.setTextColor(ContextCompat.getColor(context, colorRes));
    }

    /**
     * Computes a human-readable relative time string based on the elapsed time since the given date.
     *
     * @param date the date to compare with the current time
     * @return a formatted string representing how long ago the date was (e.g., "Just now", "5 minutes ago")
     */
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
}
