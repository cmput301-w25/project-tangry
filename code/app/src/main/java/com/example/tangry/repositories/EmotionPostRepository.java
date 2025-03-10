/**
 * EmotionPostRepository.java
 *
 * This repository class serves as the data access layer for EmotionPost objects in Firestore.
 * It encapsulates CRUD operations using a FirebaseDataSource instance and provides methods to
 * save, retrieve, update, and delete EmotionPosts. The repository also supports filtering posts
 * by emotion.
 *
 * Outstanding Issues:
 * - We considering enhancing error handling and logging for production usage.
 * - Further optimizations may be needed for handling large data sets or more complex queries.
 */

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

    /**
     * Constructs a new EmotionPostRepository using the default "emotions" collection.
     */
    public EmotionPostRepository() {
        this.firebaseDataSource = new FirebaseDataSource("emotions");
    }

    /**
     * Constructs a new EmotionPostRepository for testing with the specified FirebaseFirestore instance
     * and collection name.
     *
     * @param db          the FirebaseFirestore instance to use
     * @param collection1 the name of the collection
     */
    public EmotionPostRepository(FirebaseFirestore db, String collection1) {
        firebaseDataSource = new FirebaseDataSource(db, collection1);
    }

    /**
     * Returns the singleton instance of EmotionPostRepository.
     *
     * @return the EmotionPostRepository instance
     */
    public static synchronized EmotionPostRepository getInstance() {
        if (instance == null) {
            instance = new EmotionPostRepository();
        }
        return instance;
    }

    /**
     * Saves an EmotionPost to Firestore.
     *
     * @param post            the EmotionPost object to save
     * @param successListener callback for success, receiving the DocumentReference of the new document
     * @param failureListener callback for failure
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
     * Retrieves a Firestore Query for all EmotionPosts ordered by timestamp in descending order.
     *
     * @return a Query object for retrieving posts
     */
    public Query getPostsQuery() {
        return firebaseDataSource.getQuery();
    }

    /**
     * Retrieves a Firestore Query for EmotionPosts filtered by a list of emotions.
     * The resulting query orders posts by timestamp in descending order.
     *
     * @param emotions a list of emotion strings to filter posts; if empty, no filter is applied
     * @return a Query object for retrieving filtered posts
     */
    public Query getFilteredPostsQuery(List<String> emotions) {
        Query query = firebaseDataSource.getCollectionReference();

        if (!emotions.isEmpty()) {
            query = query.whereIn("emotion", emotions);
        }

        query = query.orderBy("timestamp", Query.Direction.DESCENDING); // Sort by latest posts

        return query;
    }

    /**
     * Retrieves an EmotionPost by its Firestore document ID.
     *
     * @param postId    the document ID of the EmotionPost
     * @param onSuccess callback function invoked on successful retrieval with the EmotionPost object;
     *                  if the document does not exist, null is passed
     * @param onFailure callback function invoked if retrieval fails
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
     * Updates an existing EmotionPost in Firestore.
     *
     * @param postId    the Firestore document ID of the post to update
     * @param post      the updated EmotionPost object
     * @param onSuccess callback invoked upon successful update
     * @param onFailure callback invoked if the update fails
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
     * Deletes an EmotionPost from Firestore.
     *
     * @param postId    the Firestore document ID of the post to delete
     * @param onSuccess callback invoked upon successful deletion
     * @param onFailure callback invoked if deletion fails
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
