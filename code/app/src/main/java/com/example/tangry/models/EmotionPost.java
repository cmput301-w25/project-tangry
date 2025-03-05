package com.example.tangry.models;

import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.Timestamp;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

/**
 * Represents a mood event with various attributes such as emotion, explanation,
 * image, location, social situation, and username.
 */
public class EmotionPost {
    private static final String TAG = "EmotionPost";

    private String emotion;
    private String explanation;
    private String imageUri;
    private String location;
    private String socialSituation;
    private String username;
    private Timestamp timestamp;

    public EmotionPost() {
    };

    private static final List<String> VALID_SOCIAL_SITUATIONS = Arrays.asList("Select social situation", "Alone",
            "With one other person", "With two to several people", "With a crowd");

    /**
     * Private constructor to create an EmotionPost object.
     *
     * @param emotion         The emotion associated with the mood event.
     * @param explanation     A brief textual explanation for the mood event.
     * @param imageUri        The URI of the image associated with the mood event.
     * @param location        The location where the mood event occurred.
     * @param socialSituation The social situation during the mood event.
     * @param username        The username of the person posting the emotion.
     */
    private EmotionPost(String emotion, String explanation, String imageUri, String location, String socialSituation, String username) {
        this.emotion = emotion;
        this.explanation = explanation;
        this.imageUri = imageUri;
        this.location = location;
        this.socialSituation = socialSituation;
        this.username = username;
        this.timestamp = Timestamp.now(); // Set the current timestamp
    }

    /**
     * Factory method to create an EmotionPost object with validation.
     *
     * @param emotion         The emotion associated with the mood event.
     * @param explanation     A brief textual explanation for the mood event.
     * @param imageUri        The URI of the image associated with the mood event.
     * @param location        The location where the mood event occurred.
     * @param socialSituation The social situation during the mood event.
     * @param username        The username of the person posting the emotion.
     * @param imageStream     The InputStream of the image to validate its size.
     * @return A new EmotionPost object.
     * @throws IllegalArgumentException If any validation fails.
     * @throws IOException              If an I/O error occurs.
     */
    public static EmotionPost create(String emotion, String explanation, String imageUri, String location,
                                     String socialSituation, String username, InputStream imageStream) throws IllegalArgumentException, IOException {
        if (emotion == null || emotion.trim().isEmpty()) {
            throw new IllegalArgumentException("Emotion is required.");
        }

        if (explanation.length() > 20 || explanation.split("\\s+").length > 3) {
            throw new IllegalArgumentException("Explanation must be max 20 characters or 3 words.");
        }

        if (!VALID_SOCIAL_SITUATIONS.contains(socialSituation)) {
            throw new IllegalArgumentException("Invalid social situation.");
        }

        if (imageStream != null) {
            int imageSizeInBytes = imageStream.available();
            if (imageSizeInBytes > 65536) {
                throw new IllegalArgumentException("Image size must be under 65536 bytes.");
            }
        }

        return new EmotionPost(emotion, explanation, imageUri, location, socialSituation, username);
    }

    /**
     * Gets the emotion associated with the mood event.
     *
     * @return The emotion.
     */
    public String getEmotion() {
        return emotion;
    }

    /**
     * Sets the emotion associated with the mood event.
     *
     * @param emotion The emotion to set.
     */
    public void setEmotion(String emotion) {
        this.emotion = emotion;
    }

    /**
     * Gets the explanation for the mood event.
     *
     * @return The explanation.
     */
    public String getExplanation() {
        return explanation;
    }

    /**
     * Sets the explanation for the mood event.
     *
     * @param explanation The explanation to set.
     */
    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    /**
     * Gets the URI of the image associated with the mood event.
     *
     * @return The image URI.
     */
    public String getImageUri() {
        return imageUri;
    }

    /**
     * Sets the URI of the image associated with the mood event.
     *
     * @param imageUri The image URI to set.
     */
    public void setImageUri(String imageUri) {
        this.imageUri = imageUri;
    }

    /**
     * Gets the location where the mood event occurred.
     *
     * @return The location.
     */
    public String getLocation() {
        return location;
    }

    /**
     * Sets the location where the mood event occurred.
     *
     * @param location The location to set.
     */
    public void setLocation(String location) {
        this.location = location;
    }

    /**
     * Gets the social situation during the mood event.
     *
     * @return The social situation.
     */
    public String getSocialSituation() {
        return socialSituation;
    }

    /**
     * Sets the social situation during the mood event.
     *
     * @param socialSituation The social situation to set.
     */
    public void setSocialSituation(String socialSituation) {
        this.socialSituation = socialSituation;
    }

    /**
     * Gets the username of the person posting the emotion.
     *
     * @return The username.
     */
    public String getUsername() {
        return username;
    }

    /**
     * Sets the username of the person posting the emotion.
     *
     * @param username The username to set.
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Gets the timestamp of the mood event.
     *
     * @return The timestamp.
     */
    public Timestamp getTimestamp() {
        return timestamp;
    }

    /**
     * Sets the timestamp of the mood event.
     *
     * @param timestamp The timestamp to set.
     */
    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * Returns a string representation of the EmotionPost object.
     *
     * @return A string representation of the EmotionPost object.
     */
    @NonNull
    @Override
    public String toString() {
        return "EmotionPost{" +
                "emotion='" + emotion + '\'' +
                ", explanation='" + explanation + '\'' +
                ", imageUri=" + imageUri +
                ", location='" + location + '\'' +
                ", socialSituation='" + socialSituation + '\'' +
                ", username='" + username + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
