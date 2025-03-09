package com.example.tangry;

import static org.junit.Assert.*;

import com.example.tangry.models.EmotionPost;

import org.junit.Test;

public class EmotionPostTest {

    @Test
    public void testCreateWithValidInputs() {
        EmotionPost post = EmotionPost.create(
                "happiness",     // emotion
                "I'm happy",     // explanation
                null,            // imageUri
                "Home",          // location
                "Alone",         // socialSituation
                "testUser"       // username
        );
        
        assertEquals("happiness", post.getEmotion());
        assertEquals("I'm happy", post.getExplanation());
        assertNull(post.getImageUri());
        assertEquals("Home", post.getLocation());
        assertEquals("Alone", post.getSocialSituation());
        assertEquals("testUser", post.getUsername());
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testCreateWithEmptyEmotion() {
        EmotionPost.create(
                "",          // emotion - should fail
                "Test",      // explanation
                null,        // imageUri
                "Location",  // location
                "Alone",     // socialSituation
                "testUser"   // username
        );
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testCreateWithTooLongExplanation() {
        EmotionPost.create(
                "happiness",                             // emotion
                "This explanation is way too long",      // explanation - too many words
                null,                                    // imageUri
                "Location",                              // location
                "Alone",                                 // socialSituation
                "testUser"                               // username
        );
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testCreateWithNoExplanationOrImage() {
        EmotionPost.create(
                "happiness",     // emotion
                "",              // explanation - empty
                null,            // imageUri - null
                "Location",      // location
                "Alone",         // socialSituation
                "testUser"       // username
        );
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testCreateWithInvalidSocialSituation() {
        EmotionPost.create(
                "happiness",         // emotion
                "Test",              // explanation
                null,                // imageUri
                "Location",          // location
                "Invalid Situation", // socialSituation - invalid
                "testUser"           // username
        );
    }
}