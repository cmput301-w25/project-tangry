package com.example.tangry.repositories;

import android.util.Log;

import com.example.tangry.models.EmotionPost;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

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

    public void getEmotionPostFromFirestore(String postId, Consumer<EmotionPost> onSuccess, Consumer<Exception> onFailure) {
        db.collection("emotions").document(postId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        EmotionPost post = documentSnapshot.toObject(EmotionPost.class);
                        onSuccess.accept(post);
                    } else {
                        onSuccess.accept(null);
                    }
                })
                .addOnFailureListener(e -> onFailure.accept(e));
    }

    public void updateEmotionPostInFirestore(String postId, EmotionPost post, Runnable onSuccess, OnFailureListener onFailure) {
        db.collection("emotions").document(postId)
                .set(post)
                .addOnSuccessListener(aVoid -> {
                    Log.d("Firestore", "Post updated successfully");
                    onSuccess.run();
                })
                .addOnFailureListener(onFailure);
    }

}