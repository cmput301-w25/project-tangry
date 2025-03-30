package com.example.tangry.repositories;

import android.util.Log;
import com.example.tangry.datasource.FirebaseDataSource;
import com.example.tangry.models.Comment;
import com.example.tangry.models.EmotionPost;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.FieldValue;
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
     * @param collectionName the name of the collection
     */
    public EmotionPostRepository(FirebaseFirestore db, String collectionName) {
        this.firebaseDataSource = new FirebaseDataSource(db, collectionName);
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
     * @param post the EmotionPost object to save
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
        data.put("isPublic", post.isPublic()); // Add the isPublic field
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
        query = query.orderBy("timestamp", Query.Direction.DESCENDING);
        return query;
    }

    /**
     * Retrieves an EmotionPost by its Firestore document ID.
     *
     * @param postId the document ID of the EmotionPost
     * @param onSuccess callback invoked on successful retrieval with the EmotionPost object;
     *                  if the document does not exist, null is passed
     * @param onFailure callback invoked if retrieval fails
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
                onFailure);
    }

    /**
     * Updates an existing EmotionPost in Firestore.
     *
     * @param postId the Firestore document ID of the post to update
     * @param post the updated EmotionPost object
     * @param onSuccess callback invoked upon successful update
     * @param onFailure callback invoked if the update fails
     */
    public void updateEmotionPost(String postId, EmotionPost post, Runnable onSuccess, OnFailureListener onFailure) {
        firebaseDataSource.updateData(postId, post,
                aVoid -> {
                    Log.d(TAG, "Post updated successfully");
                    onSuccess.run();
                },
                onFailure);
    }

    /**
     * Deletes an EmotionPost from Firestore.
     *
     * @param postId the Firestore document ID of the post to delete
     * @param onSuccess callback invoked upon successful deletion
     * @param onFailure callback invoked if deletion fails
     */
    public void deleteEmotionPost(String postId, Runnable onSuccess, OnFailureListener onFailure) {
        firebaseDataSource.deleteData(postId,
                aVoid -> {
                    Log.d(TAG, "Post deleted successfully");
                    onSuccess.run();
                },
                onFailure);
    }

    /**
     * Retrieves posts by a specific user.
     *
     * @param username the username to filter by
     * @return a Query object for the user's posts ordered by timestamp descending
     */
    public Query getPostsByUser(String username) {
        return firebaseDataSource.getCollectionReference()
                .whereEqualTo("username", username)
                .orderBy("timestamp", Query.Direction.DESCENDING);
    }

    /**
     * Adds a comment to an existing EmotionPost in Firestore.
     *
     * @param postId the document ID of the EmotionPost
     * @param comment the Comment object to add
     * @param onSuccess callback on successful update
     * @param onFailure callback on failure
     */
    public void addCommentToPost(String postId, Comment comment, Runnable onSuccess, OnFailureListener onFailure) {
        Map<String, Object> commentMap = new HashMap<>();
        commentMap.put("username", comment.getUsername());
        commentMap.put("text", comment.getText());
        commentMap.put("timestamp", comment.getTimestamp());
        firebaseDataSource.getCollectionReference()
                .document(postId)
                .update("comments", FieldValue.arrayUnion(commentMap))
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Comment added successfully");
                    onSuccess.run();
                })
                .addOnFailureListener(onFailure);
    }

    /**
     * Gets posts by a specific user with optional emotion filtering.
     *
     * @param username the username to filter by
     * @param emotions list of emotions to filter by (optional)
     * @return a Query object for the filtered posts
     */
    public Query getFilteredUserPosts(String username, List<String> emotions) {
        Query query = firebaseDataSource.getCollectionReference()
                .whereEqualTo("username", username);
        if (emotions != null && !emotions.isEmpty()) {
            query = query.whereIn("emotion", emotions);
        }
        return query.orderBy("timestamp", Query.Direction.DESCENDING);
    }

    /**
     * Gets public posts from a list of friends with optional emotion filtering.
     * Only returns posts that are marked as public.
     *
     * @param friendUsernames list of friend usernames
     * @param emotions list of emotions to filter by (optional)
     * @return a Query object for the filtered public posts
     */
    public Query getFilteredFriendsPosts(List<String> friendUsernames, List<String> emotions) {
        if (friendUsernames == null || friendUsernames.isEmpty()) {
            // Return empty query if no friends exist.
            return firebaseDataSource.getCollectionReference().whereEqualTo("username", "NO_MATCHING_USERNAME");
        }

        // Create base query filtering for friends and public posts
        Query query = firebaseDataSource.getCollectionReference()
                .whereIn("username", friendUsernames)
                .whereEqualTo("public", true);

        // Add emotion filtering if specified
        if (emotions != null && !emotions.isEmpty()) {
            query = query.whereIn("emotion", emotions);
        }

        return query.orderBy("timestamp", Query.Direction.DESCENDING);
    }
}