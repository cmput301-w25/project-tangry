package com.example.tangry.models;

import android.net.Uri;

public class EmotionPost {
    private String emotion;
    private String explanation;
    private Uri imageUri;
    private String location;
    private String socialSituation;

    private EmotionPost(String emotion, String explanation, Uri imageUri, String location, String socialSituation) {
        this.emotion = emotion;
        this.explanation = explanation;
        this.imageUri = imageUri;
        this.location = location;
        this.socialSituation = socialSituation;
    }

    public static EmotionPost create(String emotion, String explanation, Uri imageUri, String location, String socialSituation) {
        if (explanation.length() > 20 || explanation.split("\\s+").length > 3) {
            throw new IllegalArgumentException("Explanation must be max 20 characters or 3 words.");
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

    // toString() for debugging/logging
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
