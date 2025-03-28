package com.example.tangry;

import static org.junit.Assert.*;

import com.example.tangry.models.EmotionPost;

import org.junit.Test;

public class EmotionPostTest {

    @Test
    public void testCreateValidEmotionPost() {
        // Valid inputs
        EmotionPost post = EmotionPost.create(
                "Happiness", // emotion
                "I'm happy", // explanation
                "image_uri", // imageUri
                "Home", // location
                "Alone", // socialSituation
                "testUser" // username
        );

        assertEquals("Happiness", post.getEmotion());
        assertEquals("I'm happy", post.getExplanation());
        assertEquals("image_uri", post.getImageUri());
        assertEquals("Home", post.getLocation());
        assertEquals("Alone", post.getSocialSituation());
        assertEquals("testUser", post.getUsername());
    }

    @Test
    public void testCreateWithImageNoText() {
        // Valid case: no explanation but has image
        EmotionPost post = EmotionPost.create(
                "Happiness", // emotion
                "", // explanation - empty but valid because we have image
                "image_uri", // imageUri
                "Home", // location
                "Alone", // socialSituation
                "testUser" // username
        );

        assertEquals("Happiness", post.getEmotion());
        assertEquals("", post.getExplanation());
        assertEquals("image_uri", post.getImageUri());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateWithEmptyEmotion() {
        // Should throw exception for empty emotion
        EmotionPost.create(
                "", // emotion - empty (invalid)
                "Test", // explanation
                "image_uri", // imageUri
                "Location", // location
                "Alone", // socialSituation
                "testUser" // username
        );
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateWithNullEmotion() {
        // Should throw exception for null emotion
        EmotionPost.create(
                null, // emotion - null (invalid)
                "Test", // explanation
                "image_uri", // imageUri
                "Location", // location
                "Alone", // socialSituation
                "testUser" // username
        );
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateWithTooLongExplanation() {
        // Should throw exception for explanation that exceeds 20 chars
        EmotionPost.create(
                "Happiness", // emotion
                "This explanation is way too long", // explanation - too many words/chars
                "image_uri", // imageUri
                "Location", // location
                "Alone", // socialSituation
                "testUser" // username
        );
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateWithNoExplanationOrImage() {
        // Should throw exception when both explanation is empty and image is null
        EmotionPost.create(
                "Happiness", // emotion
                "", // explanation - empty
                null, // imageUri - null
                "Location", // location
                "Alone", // socialSituation
                "testUser" // username
        );
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateWithInvalidSocialSituation() {
        // Should throw exception for invalid social situation
        EmotionPost.create(
                "Happiness", // emotion
                "Test", // explanation
                "image_uri", // imageUri
                "Location", // location
                "Invalid Situation", // socialSituation - invalid
                "testUser" // username
        );
    }

    @Test
    public void testCreateWithNullSocialSituation() {
        // Valid case: social situation can be null
        EmotionPost post = EmotionPost.create(
                "Happiness", // emotion
                "Test", // explanation
                "image_uri", // imageUri
                "Location", // location
                null, // socialSituation - null but valid
                "testUser" // username
        );

        assertEquals("Happiness", post.getEmotion());
        assertEquals("", post.getSocialSituation());
    }

    @Test
    public void testCreateWithNullLocation() {
        // Valid case: location can be null
        EmotionPost post = EmotionPost.create(
                "Happiness", // emotion
                "Test", // explanation
                "image_uri", // imageUri
                null, // location - null
                "Alone", // socialSituation
                "testUser" // username
        );

        assertEquals("Happiness", post.getEmotion());
        assertEquals("", post.getLocation());
    }
}