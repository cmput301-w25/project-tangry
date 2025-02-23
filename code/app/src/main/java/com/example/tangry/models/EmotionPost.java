package com.example.tangry.models;

import android.net.Uri;
import com.google.firebase.Timestamp;
import java.io.Serializable;

public class EmotionPost implements Serializable {
    private String emotion;
    private String explanation;
    private String imageUri;
    private String location;
    private String socialSituation;
    private Timestamp timestamp;

    public EmotionPost() {}

    private EmotionPost(String emotion, String explanation, String imageUri,
                        String location, String socialSituation) {
        this.emotion = emotion;
        this.explanation = explanation;
        this.imageUri = imageUri;
        this.location = location;
        this.socialSituation = socialSituation;
    }

    public static EmotionPost create(String emotion, String explanation, Uri imageUri,
                                     String location, String socialSituation) {
        if (explanation.length() > 20 || explanation.split("\\s+").length > 3) {
            throw new IllegalArgumentException("Explanation must be max 20 characters or 3 words.");
        }
        return new EmotionPost(
                emotion,
                explanation,
                imageUri != null ? imageUri.toString() : null,
                location,
                socialSituation
        );
    }

    // Getters
    public Uri getImageUri() {
        return imageUri != null ? Uri.parse(imageUri) : null;
    }

    public String getEmotion() { return emotion; }
    public String getExplanation() { return explanation; }
    public String getLocation() { return location; }
    public String getSocialSituation() { return socialSituation; }
    public Timestamp getTimestamp() { return timestamp; }

    public void setEmotion(String emotion) { this.emotion = emotion; }
    public void setExplanation(String explanation) { this.explanation = explanation; }
    public void setImageUri(String imageUri) { this.imageUri = imageUri; }
    public void setLocation(String location) { this.location = location; }
    public void setSocialSituation(String socialSituation) { this.socialSituation = socialSituation; }
    public void setTimestamp(Timestamp timestamp) { this.timestamp = timestamp; }
}