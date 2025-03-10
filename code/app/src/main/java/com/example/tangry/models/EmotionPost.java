/**
 * EmotionPost.java
 *
 * This model class represents a mood event (or emotion post) with attributes such as emotion,
 * explanation, image URI, location, social situation, username, and timestamp. It includes
 * validation via a factory method to ensure that only valid EmotionPost instances are created.
 * The class is Serializable to facilitate passing instances between Android components.
 *
 * Outstanding Issues:
 * - The validation rules in the create() factory method are currently strict (e.g., explanation length).
 *   Adjust the validations if more flexibility is needed.
 * - Image size validation is mentioned in comments but not implemented.
 */

package com.example.tangry.models;

import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.Timestamp;

import java.io.IOException;
import java.io.InputStream;
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

    /**
     * Default constructor required for deserialization.
     */
    public EmotionPost() {
    }

    private static final List<String> VALID_SOCIAL_SITUATIONS = Arrays.asList(
            "Select social situation", "Alone", "With one other person", "With two to several people", "With a crowd");

    /**
     * Private constructor to create an EmotionPost object.
     *
     * @param emotion         the emotion associated with the mood event
     * @param explanation     a brief textual explanation for the mood event
     * @param imageUri        the URI of the image associated with the mood event
     * @param location        the location where the mood event occurred
     * @param socialSituation the social situation during the mood event
     * @param username        the username of the person posting the emotion
     */
    private EmotionPost(String emotion, String explanation, String imageUri, String location, String socialSituation,
                        String username) {
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
     * @param emotion         the emotion associated with the mood event
     * @param explanation     a brief textual explanation for the mood event; must be max 20 characters or 3 words
     * @param imageUri        the URI of the image associated with the mood event
     * @param location        the location where the mood event occurred
     * @param socialSituation the social situation during the mood event; must be one of the valid options
     * @param username        the username of the person posting the emotion
     * @return a new EmotionPost object
     * @throws IllegalArgumentException if any validation fails
     */
    public static EmotionPost create(String emotion, String explanation, String imageUri, String location,
                                     String socialSituation, String username) throws IllegalArgumentException {
        // Validate emotion
        if (emotion == null || emotion.trim().isEmpty()) {
            throw new IllegalArgumentException("Emotion is required.");
        }
        // Validate explanation length and word count
        if (explanation.length() > 20 || explanation.split("\\s+").length > 3) {
            throw new IllegalArgumentException("Explanation must be max 20 characters or 3 words.");
        }
        // Validate that either explanation or image is provided
        if (explanation.isEmpty() && (imageUri == null || imageUri.isEmpty())) {
            throw new IllegalArgumentException("Emotion post requires text or image.");
        }
        // Validate social situation if provided
        if (socialSituation != null && !VALID_SOCIAL_SITUATIONS.contains(socialSituation)) {
            throw new IllegalArgumentException("Invalid social situation.");
        }
        // Note: Image size validation (if imageStream is provided) is not implemented.
        return new EmotionPost(emotion, explanation, imageUri, location, socialSituation, username);
    }

    /**
     * Gets the emotion associated with the mood event.
     *
     * @return the emotion
     */
    public String getEmotion() {
        return emotion;
    }

    /**
     * Sets the emotion associated with the mood event.
     *
     * @param emotion the emotion to set
     */
    public void setEmotion(String emotion) {
        this.emotion = emotion;
    }

    /**
     * Gets the explanation for the mood event.
     *
     * @return the explanation
     */
    public String getExplanation() {
        return explanation;
    }

    /**
     * Sets the explanation for the mood event.
     *
     * @param explanation the explanation to set
     */
    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    /**
     * Gets the URI of the image associated with the mood event.
     *
     * @return the image URI
     */
    public String getImageUri() {
        return imageUri;
    }

    /**
     * Sets the URI of the image associated with the mood event.
     *
     * @param imageUri the image URI to set
     */
    public void setImageUri(String imageUri) {
        this.imageUri = imageUri;
    }

    /**
     * Gets the location where the mood event occurred.
     *
     * @return the location
     */
    public String getLocation() {
        return location;
    }

    /**
     * Sets the location where the mood event occurred.
     *
     * @param location the location to set
     */
    public void setLocation(String location) {
        this.location = location;
    }

    /**
     * Gets the social situation during the mood event.
     *
     * @return the social situation
     */
    public String getSocialSituation() {
        return socialSituation;
    }

    /**
     * Sets the social situation during the mood event.
     *
     * @param socialSituation the social situation to set
     */
    public void setSocialSituation(String socialSituation) {
        this.socialSituation = socialSituation;
    }

    /**
     * Gets the username of the person posting the emotion.
     *
     * @return the username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Sets the username of the person posting the emotion.
     *
     * @param username the username to set
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Gets the timestamp of the mood event.
     *
     * @return the timestamp
     */
    public Timestamp getTimestamp() {
        return timestamp;
    }

    /**
     * Sets the timestamp of the mood event.
     *
     * @param timestamp the timestamp to set
     */
    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * Gets the Firestore document ID (postId) of the mood event.
     *
     * @return the postId
     */
    public String getPostId() {
        return postId;
    }

    /**
     * Sets the Firestore document ID (postId) of the mood event.
     *
     * @param postId the postId to set
     */
    public void setPostId(String postId) {
        this.postId = postId;
    }

    /**
     * Returns a string representation of the EmotionPost object.
     *
     * @return a string representation of the EmotionPost
     */
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
