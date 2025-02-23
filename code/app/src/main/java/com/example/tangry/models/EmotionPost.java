package com.example.tangry.models;

import android.net.Uri;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

public class EmotionPost {
    private String emotion;
    private String explanation;
    private Uri imageUri;
    private String location;
    private String socialSituation;

    private static final List<String> VALID_SOCIAL_SITUATIONS = Arrays.asList("alone", "with one other person",
            "with two to several people", "with a crowd");

    private EmotionPost(String emotion, String explanation, Uri imageUri, String location, String socialSituation) {
        this.emotion = emotion;
        this.explanation = explanation;
        this.imageUri = imageUri;
        this.location = location;
        this.socialSituation = socialSituation;
    }

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

    public String getEmotion() {
        return emotion;
    }

    public String getExplanation() {
        return explanation;
    }

    public Uri getImageUri() {
        return imageUri;
    }

    public String getLocation() {
        return location;
    }

    public String getSocialSituation() {
        return socialSituation;
    }

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