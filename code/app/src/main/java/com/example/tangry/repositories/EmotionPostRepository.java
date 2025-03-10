
package com.example.tangry.repositories;

import android.util.Log;
import com.example.tangry.datasource.FirebaseDataSource;
import com.example.tangry.models.EmotionPost;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class EmotionPostRepository {
    private static EmotionPostRepository instance;
    private FirebaseDataSource firebaseDataSource;

    private static final String TAG = "EmotionPostRepository";

    public EmotionPostRepository() {
        this.firebaseDataSource = new FirebaseDataSource("emotions");
    }

    //Testing environment
    public EmotionPostRepository(FirebaseFirestore db, String collection1) {
        firebaseDataSource = new FirebaseDataSource(db, collection1);
    }

    public static synchronized EmotionPostRepository getInstance() {
        if (instance == null) {
            instance = new EmotionPostRepository();
        }
        return instance;
    }

    /**
     * Saves an EmotionPost to Firestore
     *
     * @param post           EmotionPost object to save
     * @param successListener Callback for success
     * @param failureListener Callback for failure
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

        firebaseDataSource.saveData(data, successListener, failureListener);
    }

    /**
     * Retrieves all EmotionPosts in descending timestamp order.
     *
     * @return Firestore Query object for retrieving posts
     */
    public Query getPostsQuery() {
        return firebaseDataSource.getQuery();
    }

    public Query getFilteredPostsQuery(List<String> emotions) {
        Query query = firebaseDataSource.getCollectionReference();

        if (!emotions.isEmpty()) {
            query = query.whereIn("emotion", emotions);
        }

        query = query.orderBy("timestamp", Query.Direction.DESCENDING); // 🔥 Sort by latest posts

        return query;
    }

    /**
     * Retrieves an EmotionPost by ID
     *
     * @param postId    The document ID in Firestore
     * @param onSuccess Callback function on successful retrieval
     * @param onFailure Callback function for failure
     */
    public void getEmotionPost(String postId, Consumer<EmotionPost> onSuccess, OnFailureListener onFailure) {
        firebaseDataSource.getData(postId,
                documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        EmotionPost post = documentSnapshot.toObject(EmotionPost.class);
                        onSuccess.accept(post);
                    } else {
                        onSuccess.accept(null);
                    }
                },
                onFailure
        );
    }

    /**
     * Updates an existing EmotionPost in Firestore
     *
     * @param postId    The Firestore document ID
     * @param post      Updated EmotionPost object
     * @param onSuccess Callback for success
     * @param onFailure Callback for failure
     */
    public void updateEmotionPost(String postId, EmotionPost post, Runnable onSuccess, OnFailureListener onFailure) {
        firebaseDataSource.updateData(postId, post,
                aVoid -> {
                    Log.d(TAG, "Post updated successfully");
                    onSuccess.run();
                },
                onFailure
        );
    }

    /**
     * Deletes an EmotionPost from Firestore
     *
     * @param postId    The Firestore document ID
     * @param onSuccess Callback for success
     * @param onFailure Callback for failure
     */
    public void deleteEmotionPost(String postId, Runnable onSuccess, OnFailureListener onFailure) {
        firebaseDataSource.deleteData(postId,
                aVoid -> {
                    Log.d(TAG, "Post deleted successfully");
                    onSuccess.run();
                },
                onFailure
        );
    }
}
