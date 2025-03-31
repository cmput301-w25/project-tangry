package com.example.tangry.models;

import android.util.Log;

import com.google.firebase.Timestamp;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Represents an emotion post that a user creates.
 */
public class EmotionPost implements Serializable {
    private static final String TAG = "EmotionPost";

    private boolean offlineImagePending = false;
    private String emotion;
    private String explanation;
    private String imageUri;
    private String location;
    private String socialSituation;
    private String username;
    private Timestamp timestamp;
    private String postId; // Firestore Document ID
    private List<Comment> comments = new ArrayList<>();
    private boolean isPublic = false; // Default to private

    // List of valid emotions
    public static final List<String> VALID_EMOTIONS = Arrays.asList(
            "Happiness", "Sadness", "Angry", "Disgust", "Fear", "Surprise", "Confused", "Shame");

    // List of valid social situations
    public static final List<String> VALID_SOCIAL_SITUATIONS = Arrays.asList(
            "Select social situation", "Alone", "With one other person", "With two to several people", "With a crowd");

    // Default constructor needed for Firestore
    public EmotionPost() {
    }

    /**
     * Private constructor to create an EmotionPost instance with specified
     * attributes.
     */
    private EmotionPost(String emotion, String explanation, String imageUri, String location, String socialSituation,
                        String username) {
        this.emotion = emotion;
        this.explanation = explanation;
        this.imageUri = imageUri;
        this.location = location;
        this.socialSituation = socialSituation;
        this.username = username;
        this.timestamp = Timestamp.now();
        this.isPublic = false; // Default to private
    }

    /**
     * Factory method to create an EmotionPost object with validation.
     */
    public static EmotionPost create(String emotion, String explanation, String imageUri, String location,
                                     String socialSituation, String username, boolean isPublic) throws IllegalArgumentException {
        if (emotion == null || emotion.trim().isEmpty()) {
            throw new IllegalArgumentException("Emotion is required.");
        }
        if (explanation != null && (explanation.length() > 200)) {
            throw new IllegalArgumentException("Explanation must be max 200 characters.");
        }
        if ((explanation == null || explanation.isEmpty()) && (imageUri == null || imageUri.isEmpty())) {
            throw new IllegalArgumentException("Emotion post requires text or image.");
        }
        if (socialSituation != null && (!VALID_SOCIAL_SITUATIONS.contains(socialSituation) || socialSituation.isEmpty())) {
            throw new IllegalArgumentException("Invalid social situation.");
        }
        if (!VALID_EMOTIONS.contains(emotion)) {
            throw new IllegalArgumentException("Invalid emotion.");
        }

        EmotionPost post = new EmotionPost(emotion, explanation, imageUri, location, socialSituation, username);
        post.setPublic(isPublic);
        return post;
    }

    // For backward compatibility
    public static EmotionPost create(String emotion, String explanation, String imageUri, String location,
                                     String socialSituation, String username) throws IllegalArgumentException {
        return create(emotion, explanation, imageUri, location, socialSituation, username, false);
    }

    public String getEmotion() {
        return emotion;
    }

    public void setEmotion(String emotion) {
        this.emotion = emotion;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    public String getImageUri() {
        return imageUri;
    }

    public void setImageUri(String imageUri) {
        this.imageUri = imageUri;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getSocialSituation() {
        return socialSituation;
    }

    public void setSocialSituation(String socialSituation) {
        this.socialSituation = socialSituation;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public List<Comment> getComments() {
        return comments;
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }

    public boolean isOfflineImagePending() {
        return offlineImagePending;
    }

    public void setOfflineImagePending(boolean offlineImagePending) {
        this.offlineImagePending = offlineImagePending;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public void setPublic(boolean isPublic) {
        this.isPublic = isPublic;
    }

    @Override
    public String toString() {
        return "EmotionPost{" +
                "emotion='" + emotion + '\'' +
                ", explanation='" + explanation + '\'' +
                ", imageUri='" + imageUri + '\'' +
                ", location='" + location + '\'' +
                ", socialSituation='" + socialSituation + '\'' +
                ", username='" + username + '\'' +
                ", timestamp=" + timestamp +
                ", postId='" + postId + '\'' +
                ", offlineImagePending=" + offlineImagePending +
                ", isPublic=" + isPublic +
                '}';
    }
}