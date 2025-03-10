package com.example.tangry.controllers;

import android.util.Log;

import com.example.tangry.models.EmotionPost;
import com.example.tangry.repositories.EmotionPostRepository;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.Query;
import com.google.firebase.Timestamp;

import java.util.List;

public class EmotionPostController {
    private final EmotionPostRepository repository;

    // Use Singleton Repository
    public EmotionPostController() {
        this.repository = EmotionPostRepository.getInstance();
    }

    /**
     * Creates a new EmotionPost and saves it to Firestore
     *
     * @param post            The EmotionPost object
     * @param onSuccess       Callback for successful Firestore save
     * @param onFailure       Callback for failure scenario
     */
    public void createPost(EmotionPost post,
                           OnSuccessListener<DocumentReference> onSuccess,
                           OnFailureListener onFailure) {
        repository.saveEmotionPostToFirestore(post, onSuccess, onFailure);
    }

    /**
     * Retrieves a Query for all EmotionPosts.
     *
     * This can be used by the view (e.g., in a RecyclerView adapter) to display posts.
     *
     * @return A Firestore Query for retrieving EmotionPosts.
     */
    public Query getPostsQuery() {
        return repository.getPostsQuery();
    }

    public Query getFilteredPostsQuery(List<String> emotions, boolean filterRecent) {
        if (emotions.isEmpty() && !filterRecent) {
            return repository.getPostsQuery(); // No filters, return all posts
        }

        Query query = repository.getFilteredPostsQuery(emotions);

        if (filterRecent) {
            Timestamp oneWeekAgo = new Timestamp(Timestamp.now().getSeconds() - (7 * 24 * 60 * 60), 0);
            query = query.whereGreaterThanOrEqualTo("timestamp", oneWeekAgo);
        }

        return query;
    }

    /**
     * Deletes an EmotionPost by ID.
     *
     * @param postId     The Firestore document ID of the post to delete.
     * @param onSuccess  Callback for successful deletion.
     * @param onFailure  Callback for failure scenario.
     */
    public void deleteEmotionPost(String postId,
                                  Runnable onSuccess,
                                  OnFailureListener onFailure) {
        if (postId == null || postId.isEmpty()) {
            Log.e("EmotionPostController", "Invalid post ID");
            return;
        }
        repository.deleteEmotionPost(postId, onSuccess, onFailure);
    }

    /**
     * Updates an existing EmotionPost in Firestore.
     *
     * @param postId    The Firestore document ID of the post to update.
     * @param updatedPost The updated EmotionPost object.
     * @param onSuccess  Callback for successful update.
     * @param onFailure  Callback for failure scenario.
     */
    public void updateEmotionPost(String postId,
                                  EmotionPost updatedPost,
                                  Runnable onSuccess,
                                  OnFailureListener onFailure) {
        if (postId == null || postId.isEmpty()) {
            Log.e("EmotionPostController", "Invalid post ID");
            return;
        }

        repository.updateEmotionPost(postId, updatedPost, onSuccess, onFailure);
    }
}


