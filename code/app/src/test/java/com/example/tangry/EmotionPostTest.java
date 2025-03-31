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
                "testUser", // username
                true
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
                "testUser", // username
                true
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
                "testUser", // username
                true
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
                "testUser", // username
                true
        );
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateWithTooLongExplanation() {
        // Should throw exception for explanation that exceeds 200 chars
        EmotionPost.create(
                "Happiness", // emotion
                "This explanation is way too long and needs to be expanded to exceed the 200 character limit in order to properly test the validation. I am adding more and more text to ensure that we definitely go beyond the limit. We need to make sure this string contains well over two hundred characters to trigger the validation check and throw the expected exception.", // explanation - too many chars
                "image_uri", // imageUri
                "Location", // location
                "Alone", // socialSituation
                "testUser", // username
                true
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
                "testUser", // username
                true
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
                "testUser", // username
                true
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
                "testUser", // username
                true
        );

        assertEquals("Happiness", post.getEmotion());
        assertEquals("null", post.getSocialSituation());
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
                "testUser", // username
                true
        );

        assertEquals("Happiness", post.getEmotion());
        assertEquals("null", post.getLocation());
    }
}