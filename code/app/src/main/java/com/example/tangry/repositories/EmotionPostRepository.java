package com.example.tangry.repositories;

import com.example.tangry.models.EmotionPost;

import java.util.ArrayList;
import java.util.List;

public class EmotionPostRepository {
    private static EmotionPostRepository instance;
    private final List<EmotionPost> emotionPosts;

    private EmotionPostRepository() {
        emotionPosts = new ArrayList<>();
    }

    public static EmotionPostRepository getInstance() {
        if (instance == null) {
            instance = new EmotionPostRepository();
        }
        return instance;
    }

    public void saveEmotionPost(EmotionPost post) {
        emotionPosts.add(post);
        System.out.println("Saved: " + post); // Log for debugging
    }

    public List<EmotionPost> getAllPosts() {
        return new ArrayList<>(emotionPosts); // Return a copy to prevent external modification
    }
}
