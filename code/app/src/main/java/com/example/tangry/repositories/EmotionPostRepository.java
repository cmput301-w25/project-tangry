package com.example.tangry.repositories;

import com.example.tangry.models.EmotionPost;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import java.util.HashMap;
import java.util.Map;

public class EmotionPostRepository {
    private static EmotionPostRepository instance;
    private final FirebaseFirestore db;
    private static final String COLLECTION_NAME = "emotions";

    private EmotionPostRepository() {
        db = FirebaseFirestore.getInstance();
    }

    public static synchronized EmotionPostRepository getInstance() {
        if (instance == null) {
            instance = new EmotionPostRepository();
        }
        return instance;
    }

    public void saveEmotionPostToFirestore(EmotionPost post,
            OnSuccessListener<DocumentReference> successListener,
            OnFailureListener failureListener) {
        Map<String, Object> data = new HashMap<>();
        data.put("emotion", post.getEmotion());
        data.put("explanation", post.getExplanation());
        data.put("imageUri", post.getImageUri());
        data.put("location", post.getLocation());
        data.put("socialSituation",post.getSocialSituation());
        data.put("username", post.getUsername());
        data.put("timestamp", FieldValue.serverTimestamp());

        db.collection(COLLECTION_NAME)
                .add(data)
                .addOnSuccessListener(successListener)
                .addOnFailureListener(failureListener);
    }

    public Query getPostsQuery() {
        return db.collection(COLLECTION_NAME)
                .orderBy("timestamp", Query.Direction.DESCENDING);
    }
}