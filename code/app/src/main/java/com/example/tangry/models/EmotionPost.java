package com.example.tangry.models;

import android.net.Uri;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

/**
 * Represents a mood event with various attributes such as emotion, explanation,
 * image, location, and social situation.
 */
public class EmotionPost {
    private String emotion;
    private String explanation;
    private Uri imageUri;
    private String location;
    private String socialSituation;

    private static final List<String> VALID_SOCIAL_SITUATIONS = Arrays.asList("alone", "with one other person",
            "with two to several people", "with a crowd");

    /**
     * Private constructor to create an EmotionPost object.
     *
     * @param emotion         The emotion associated with the mood event.
     * @param explanation     A brief textual explanation for the mood event.
     * @param imageUri        The URI of the image associated with the mood event.
     * @param location        The location where the mood event occurred.
     * @param socialSituation The social situation during the mood event.
     */
    private EmotionPost(String emotion, String explanation, Uri imageUri, String location, String socialSituation) {
        this.emotion = emotion;
        this.explanation = explanation;
        this.imageUri = imageUri;
        this.location = location;
        this.socialSituation = socialSituation;
    }

    /**
     * Factory method to create an EmotionPost object with validation.
     *
     * @param emotion         The emotion associated with the mood event.
     * @param explanation     A brief textual explanation for the mood event.
     * @param imageUri        The URI of the image associated with the mood event.
     * @param location        The location where the mood event occurred.
     * @param socialSituation The social situation during the mood event.
     * @param imageStream     The InputStream of the image to validate its size.
     * @return A new EmotionPost object.
     * @throws IllegalArgumentException If any validation fails.
     * @throws IOException              If an I/O error occurs.
     */
    public static EmotionPost create(String emotion, String explanation, Uri imageUri, String location,
            String socialSituation, InputStream imageStream) throws IllegalArgumentException, IOException {
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

        return new EmotionPost(emotion, explanation, imageUri, location, socialSituation);
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
     * Gets the explanation for the mood event.
     *
     * @return The explanation.
     */
    public String getExplanation() {
        return explanation;
    }

    /**
     * Gets the URI of the image associated with the mood event.
     *
     * @return The image URI.
     */
    public Uri getImageUri() {
        return imageUri;
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
     * Gets the social situation during the mood event.
     *
     * @return The social situation.
     */
    public String getSocialSituation() {
        return socialSituation;
    }

    /**
     * Returns a string representation of the EmotionPost object.
     *
     * @return A string representation of the EmotionPost object.
     */
    @Override
    public String toString() {
        return "EmotionPost{" +
                "emotion='" + emotion + '\'' +
                ", explanation='" + explanation + '\'' +
                ", imageUri=" + imageUri +
                ", location='" + location + '\'' +
                ", socialSituation='" + socialSituation + '\'' +
                '}';
    }
}