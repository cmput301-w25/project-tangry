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

/**
 * Repository class for handling CRUD operations on Firestore for Emotion Posts.
 * <p>
 * Implements Singleton pattern to ensure a single instance across the app.
 * <p>
 * Provides methods for:
 * - Saving new posts
 * - Retrieving posts
 * - Updating posts
 * - Deleting posts
 */
public class EmotionPostRepository {
    private static EmotionPostRepository instance;
    private final FirebaseFirestore db;
    private static final String COLLECTION_NAME = "emotions";

    /**
     * Private constructor to prevent direct instantiation.
     * Initializes Firestore instance.
     */
    private EmotionPostRepository() {
        db = FirebaseFirestore.getInstance();
    }

    // Constructor for testing
    public EmotionPostRepository(FirebaseFirestore firestore) {
        db = firestore;
    }

    /**
     * Returns the singleton instance of the repository.
     *
     * @return The singleton instance.
     */
    public static synchronized EmotionPostRepository getInstance() {
        if (instance == null) {
            instance = new EmotionPostRepository();
        }
        return instance;
    }

    /**
     * Saves a new EmotionPost to Firestore.
     *
     * @param post            The EmotionPost object to save.
     * @param successListener Callback for successful save.
     * @param failureListener Callback for failure event.
     */
    public void saveEmotionPostToFirestore(EmotionPost post,
            OnSuccessListener<DocumentReference> successListener,
            OnFailureListener failureListener) {
        Map<String, Object> data = new HashMap<>();
        data.put("emotion", post.getEmotion());
        data.put("explanation", post.getExplanation());
        data.put("imageUri", post.getImageUri());
        data.put("location", post.getLocation());
        data.put("socialSituation", post.getSocialSituation());
        data.put("username", post.getUsername());
        data.put("timestamp", FieldValue.serverTimestamp());

        db.collection(COLLECTION_NAME)
                .add(data)
                .addOnSuccessListener(successListener)
                .addOnFailureListener(failureListener);
    }

    /**
     * Retrieves all posts ordered by timestamp (descending).
     *
     * @return Firestore Query for retrieving posts.
     */
    public Query getPostsQuery() {
        return db.collection(COLLECTION_NAME)
                .orderBy("timestamp", Query.Direction.DESCENDING);
    }

    /**
     * Retrieves a specific EmotionPost from Firestore using its ID.
     *
     * @param postId    The ID of the post.
     * @param onSuccess Callback for successful retrieval (returns EmotionPost).
     * @param onFailure Callback for failure event.
     */
    public void getEmotionPost(String postId, Consumer<EmotionPost> onSuccess, Consumer<Exception> onFailure) {
        db.collection(COLLECTION_NAME).document(postId)
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

    /**
     * Updates an existing EmotionPost in Firestore.
     *
     * @param postId    The ID of the post to update.
     * @param post      The updated EmotionPost object.
     * @param onSuccess Callback for successful update.
     * @param onFailure Callback for failure event.
     */
    public void updateEmotionPost(String postId, EmotionPost post, Runnable onSuccess, OnFailureListener onFailure) {
        db.collection(COLLECTION_NAME).document(postId)
                .set(post)
                .addOnSuccessListener(aVoid -> {
                    Log.d("Firestore", "Post updated successfully");
                    onSuccess.run();
                })
                .addOnFailureListener(onFailure);
    }

    /**
     * Deletes an EmotionPost from Firestore.
     *
     * @param postId    The ID of the post to delete.
     * @param onSuccess Callback for successful deletion.
     * @param onFailure Callback for failure event.
     */
    public void deleteEmotionPost(String postId, Runnable onSuccess, Consumer<Exception> onFailure) {
        db.collection(COLLECTION_NAME).document(postId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d("Firestore", "Post deleted successfully");
                    onSuccess.run();
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error deleting post", e);
                    onFailure.accept(e);
                });
    }
}