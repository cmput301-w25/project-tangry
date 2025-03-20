package com.example.tangry.models;

import com.google.firebase.Timestamp;
import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

/**
 * Represents a mood event with various attributes such as emotion, explanation,
 * image, location, social situation, and username.
 */
public class EmotionPost implements Serializable {
    private static final String TAG = "EmotionPost";

    private String emotion;
    private String explanation;
    private String imageUri;
    private String location;
    private String socialSituation;
    private String username;
    private Timestamp timestamp;
    private String postId; // Firestore Document ID

    public EmotionPost() {
    }

    private static final List<String> VALID_SOCIAL_SITUATIONS = Arrays.asList("Select social situation", "Alone",
            "With one other person", "With two to several people", "With a crowd");

    private static final List<String> VALID_EMOTIONS = Arrays.asList("Angry", "Confused", "Disgust",
            "Fear", "Happiness", "Sadness", "Shame", "Surprise");

    /**
     * Private constructor to create an EmotionPost object.
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
    }

    /**
     * Factory method to create an EmotionPost object with validation.
     *
     * @throws IllegalArgumentException If any validation fails.
     */
    public static EmotionPost create(String emotion, String explanation, String imageUri, String location,
                                     String socialSituation, String username) throws IllegalArgumentException {
        if (emotion == null || emotion.trim().isEmpty()) {
            throw new IllegalArgumentException("Emotion is required.");
        }
        if (explanation != null && (explanation.length() > 20 || explanation.split("\\s+").length > 3)) {
            throw new IllegalArgumentException("Explanation must be max 20 characters or 3 words.");
        }
        if ((explanation == null || explanation.isEmpty()) && (imageUri == null || imageUri.isEmpty())) {
            throw new IllegalArgumentException("Emotion post requires text or image.");
        }
        if (socialSituation != null && !VALID_SOCIAL_SITUATIONS.contains(socialSituation)) {
            throw new IllegalArgumentException("Invalid social situation.");
        }
        if (!VALID_EMOTIONS.contains(emotion)) {
            throw new IllegalArgumentException("Invalid emotion.");
        }
        return new EmotionPost(emotion, explanation, imageUri, location, socialSituation, username);
    }

    // Getters and setters

    public String getEmotion() {
        return emotion;
    }
    public void setEmotion(String emotion) {
        this.emotion = emotion;
    }

    public String getExplanation() {
        return explanation != null ? explanation : "";
    }
    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    public String getImageUri() {
        return imageUri != null ? imageUri : "";
    }
    public void setImageUri(String imageUri) {
        this.imageUri = imageUri;
    }

    public String getLocation() {
        return location != null ? location : "";
    }
    public void setLocation(String location) {
        this.location = location;
    }

    public String getSocialSituation() {
        return socialSituation != null ? socialSituation : "";
    }
    public void setSocialSituation(String socialSituation) {
        this.socialSituation = socialSituation;
    }

    public String getUsername() {
        return username != null ? username : "Anonymous";
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
                '}';
    }
}