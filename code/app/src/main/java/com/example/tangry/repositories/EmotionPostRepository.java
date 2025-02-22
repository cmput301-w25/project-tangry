package com.example.tangry.repositories;

import com.example.tangry.database.FirebaseConfig;
import com.example.tangry.models.EmotionPost;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.Timestamp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public void saveEmotionPostToFirestore(EmotionPost post,
            OnSuccessListener<DocumentReference> successListener,
            OnFailureListener failureListener) {
        FirebaseFirestore db = FirebaseConfig.getFirestoreInstance();

        Map<String, Object> data = new HashMap<>();
        data.put("emotion", post.getEmotion());
        data.put("explanation", post.getExplanation());
        data.put("location", post.getLocation());
        data.put("socialSituation", post.getSocialSituation());
        data.put("imageUri", post.getImageUri() != null ? post.getImageUri().toString() : null);
        data.put("timestamp", Timestamp.now()); // Set the current timestamp

        db.collection("emotions").add(data)
                .addOnSuccessListener(successListener)
                .addOnFailureListener(failureListener);
    }
}