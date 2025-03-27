package com.example.tangry.models;

import com.google.firebase.Timestamp;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Represents a mood event with attributes like emotion, explanation,
 * image URI, location, social situation, username, timestamp, and postId.
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

    /**
     * Default constructor.
     */
    public EmotionPost() {
    }

    private static final List<String> VALID_SOCIAL_SITUATIONS = Arrays.asList("Select social situation", "Alone",
            "With one other person", "With two to several people", "With a crowd");

    private static final List<String> VALID_EMOTIONS = Arrays.asList("Angry", "Confused", "Disgust",
            "Fear", "Happiness", "Sadness", "Shame", "Surprise");

    /**
     * Private constructor to create an EmotionPost instance with specified
     * attributes.
     *
     * @param emotion         the emotion type.
     * @param explanation     the explanation text.
     * @param imageUri        the URI of the image.
     * @param location        the location information.
     * @param socialSituation the social situation description.
     * @param username        the username who created the post.
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
     * @param emotion         the emotion type; must not be null or empty and must
     *                        be valid.
     * @param explanation     the explanation text; if provided, must be at most 20
     *                        characters or 3 words.
     * @param imageUri        the URI of the image.
     * @param location        the location information.
     * @param socialSituation the social situation; if provided, must be one of the
     *                        valid options.
     * @param username        the username; if null, defaults are applied in
     *                        getters.
     * @return a new EmotionPost object.
     * @throws IllegalArgumentException if any validation fails.
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
        if (socialSituation != null && (!VALID_SOCIAL_SITUATIONS.contains(socialSituation) || socialSituation.isEmpty())) {
            throw new IllegalArgumentException("Invalid social situation.");
        }
        if (!VALID_EMOTIONS.contains(emotion)) {
            throw new IllegalArgumentException("Invalid emotion.");
        }
        return new EmotionPost(emotion, explanation, imageUri, location, socialSituation, username);
    }

    public boolean isOfflineImagePending() {
        return offlineImagePending;
    }

    public void setOfflineImagePending(boolean offlineImagePending) {
        this.offlineImagePending = offlineImagePending;
    }

    /**
     * Gets the emotion value.
     *
     * @return the emotion.
     */
    public String getEmotion() {
        return emotion;
    }

    /**
     * Sets the emotion value.
     *
     * @param emotion the emotion to set.
     */
    public void setEmotion(String emotion) {
        this.emotion = emotion;
    }

    /**
     * Gets the explanation text.
     *
     * @return the explanation, or an empty string if null.
     */
    public String getExplanation() {
        return explanation != null ? explanation : "";
    }

    /**
     * Sets the explanation text.
     *
     * @param explanation the explanation to set.
     */
    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    /**
     * Gets the image URI.
     *
     * @return the image URI, or an empty string if null.
     */
    public String getImageUri() {
        return imageUri != null ? imageUri : "";
    }

    /**
     * Sets the image URI.
     *
     * @param imageUri the image URI to set.
     */
    public void setImageUri(String imageUri) {
        this.imageUri = imageUri;
    }

    /**
     * Gets the location information.
     *
     * @return the location, or an empty string if null.
     */
    public String getLocation() {
        return location != null ? location : "";
    }

    /**
     * Sets the location information.
     *
     * @param location the location to set.
     */
    public void setLocation(String location) {
        this.location = location;
    }

    /**
     * Gets the social situation description.
     *
     * @return the social situation, or an empty string if null.
     */
    public String getSocialSituation() {
        return socialSituation != null ? socialSituation : "";
    }

    /**
     * Sets the social situation description.
     *
     * @param socialSituation the social situation to set.
     */
    public void setSocialSituation(String socialSituation) {
        this.socialSituation = socialSituation;
    }

    /**
     * Gets the username of the post's creator.
     *
     * @return the username, or "Anonymous" if null.
     */
    public String getUsername() {
        return username != null ? username : "Anonymous";
    }

    /**
     * Sets the username of the post's creator.
     *
     * @param username the username to set.
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Gets the timestamp when the post was created.
     *
     * @return the timestamp.
     */
    public Timestamp getTimestamp() {
        return timestamp;
    }

    /**
     * Sets the timestamp for the post.
     *
     * @param timestamp the timestamp to set.
     */
    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * Retrieves all comments associated with this emotion post.
     *
     * @return A list of Comment objects.
     */
    public List<Comment> getComments() {
        return comments;
    }

    /**
     * Adds a comment to this emotion post.
     *
     * @param comment The Comment object to add.
     */
    public void addComment(Comment comment) {
        if (comment != null) {
            comments.add(comment);
        }
    }

    public String getPostId() {
        return postId;
    }

    /**
     * Sets the Firestore document ID of the post.
     *
     * @param postId the postId to set.
     */
    public void setPostId(String postId) {
        this.postId = postId;
    }

    /**
     * Generates a string representation of the EmotionPost object.
     *
     * @return a string containing the details of the EmotionPost.
     */
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