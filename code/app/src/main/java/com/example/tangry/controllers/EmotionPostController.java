/**
 * EmotionPostController.java
 *
 * This controller mediates between the view layer and the EmotionPostRepository for operations on EmotionPost objects.
 * It provides methods to create, retrieve, update, and delete EmotionPosts in Firestore, and to query posts with optional
 * filters. The controller uses a singleton instance of the repository for data persistence.
 *
 * Outstanding Issues:
 * - Error handling could be further improved for asynchronous operations.
 * - Additional logging and user feedback mechanisms may be required.
 */

package com.example.tangry.controllers;

import android.util.Log;

import com.example.tangry.models.Comment;
import com.example.tangry.models.EmotionPost;
import com.example.tangry.repositories.EmotionPostRepository;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.Query;
import com.google.firebase.Timestamp;

import android.content.Context;
import com.example.tangry.utils.NetworkMonitor;
import com.example.tangry.utils.OfflineSyncManager;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.OnFailureListener;

import java.util.List;
import java.util.Objects;

public class EmotionPostController {
    private final EmotionPostRepository repository;

    /**
     * Constructs a new EmotionPostController using the singleton instance of
     * EmotionPostRepository.
     */
    public EmotionPostController() {
        this.repository = EmotionPostRepository.getInstance();
    }

    /**
     * Creates a new EmotionPost and saves it to Firestore.
     *
     * @param post      the EmotionPost object to be saved
     * @param onSuccess callback for successful Firestore save, receiving the
     *                  DocumentReference of the saved post
     * @param onFailure callback for failure scenario
     */
    public void createPost(EmotionPost post,
            OnSuccessListener<DocumentReference> onSuccess,
            OnFailureListener onFailure) {
        repository.saveEmotionPostToFirestore(post, onSuccess, onFailure);
    }

    /**
     * Retrieves a Firestore Query for all EmotionPosts.
     *
     * This query can be used by views (e.g., in a RecyclerView adapter) to display
     * posts.
     *
     * @return a Query object for retrieving all EmotionPosts
     */
    public Query getPostsQuery() {
        return repository.getPostsQuery();
    }

    /**
     * Retrieves a Firestore Query for EmotionPosts filtered by specified emotions
     * and an optional recent time filter.
     *
     * @param emotions     a list of emotion strings to filter posts; if empty, no
     *                     emotion filter is applied
     * @param filterRecent if true, the query will be filtered to only include posts
     *                     from the past week
     * @return a Query object for retrieving filtered EmotionPosts
     */
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
     * Deletes an EmotionPost by its document ID.
     *
     * @param postId    the Firestore document ID of the post to delete
     * @param onSuccess callback invoked upon successful deletion
     * @param onFailure callback for failure scenario
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
     * @param postId      the Firestore document ID of the post to update
     * @param updatedPost the updated EmotionPost object
     * @param onSuccess   callback invoked upon successful update
     * @param onFailure   callback for failure scenario
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

    /**
     * Adds a comment to an EmotionPost.
     *
     * @param postId    the ID of the post to comment on
     * @param comment   the Comment object to add
     * @param onSuccess callback on success
     * @param onFailure callback on failure
     */
    public void addCommentToPost(String postId, Comment comment, Runnable onSuccess, OnFailureListener onFailure) {
        repository.addCommentToPost(postId, comment, onSuccess, onFailure);
    }

    public void createPostWithOfflineSupport(Context context, EmotionPost post,
            OnSuccessListener<DocumentReference> onSuccess, OnFailureListener onFailure) {
        OfflineSyncManager syncManager = OfflineSyncManager.getInstance(context);
        NetworkMonitor networkMonitor = new NetworkMonitor(context);

        if (networkMonitor.isConnected()) {
            createPost(post, onSuccess, onFailure);
        } else {
            syncManager.addPendingCreate(post);
            onSuccess.onSuccess(null); // Return null document reference since we're offline
        }
    }

    public void updateEmotionPostWithOfflineSupport(Context context, String postId, EmotionPost post,
            Runnable onSuccess, OnFailureListener onFailure) {
        try {
            OfflineSyncManager syncManager = OfflineSyncManager.getInstance(context);
            NetworkMonitor networkMonitor = new NetworkMonitor(context);

            if (networkMonitor.isConnected()) {
                updateEmotionPost(postId, post, onSuccess, onFailure);
            } else {
                // Create defensive copy with null checks
                String emotion = post.getEmotion() != null ? post.getEmotion() : "";
                String explanation = post.getExplanation() != null ? post.getExplanation() : "";
                String imageUri = post.getImageUri() != null ? post.getImageUri() : null;
                String location = post.getLocation() != null ? post.getLocation() : "";
                String socialSituation = post.getSocialSituation();
                if (socialSituation == null || socialSituation.isEmpty() ||
                        "Select social situation".equals(socialSituation)) {
                    socialSituation = null;
                }
                String username = post.getUsername() != null ? post.getUsername() : "";

                // Make a defensive copy of the post
                EmotionPost postCopy = EmotionPost.create(
                        emotion,
                        explanation,
                        imageUri,
                        location,
                        socialSituation,
                        username);

                // Add timestamp and other metadata
                if (post.getTimestamp() != null) {
                    postCopy.setTimestamp(post.getTimestamp());
                }

                syncManager.addPendingUpdate(postId, postCopy);
                if (onSuccess != null) {
                    onSuccess.run();
                }
            }
        } catch (Exception e) {
            Log.e("EmotionPostController", "Error in updateEmotionPostWithOfflineSupport: " + e.getMessage(), e);
            if (onFailure != null) {
                onFailure.onFailure(e);
            }
        }
    }

    public void deleteEmotionPostWithOfflineSupport(Context context, String postId, EmotionPost post,
            Runnable onSuccess, OnFailureListener onFailure) {
        final String TAG = "EmotionPostController";
        OfflineSyncManager syncManager = OfflineSyncManager.getInstance(context);
        NetworkMonitor networkMonitor = new NetworkMonitor(context);

        if (networkMonitor.isConnected()) {
            // We're online - proceed with normal delete
            deleteEmotionPost(postId, onSuccess, onFailure);
        } else {
            // We're offline - queue for later and provide immediate feedback
            try {
                // Just store the post ID for deletion without any image handling
                syncManager.addPendingDelete(postId);

                // Provide immediate success feedback to user
                if (onSuccess != null) {
                    onSuccess.run();
                }

                Log.d(TAG, "Added delete operation to offline queue for postId: " + postId);
            } catch (Exception e) {
                Log.e(TAG, "Error adding delete operation to offline queue", e);
                if (onFailure != null) {
                    onFailure.onFailure(e);
                }
            }
        }
    }
    /**
     * Retrieves a Firestore Query for the current user's posts with optional emotion filters
     * and recent time filter.
     *
     * @param username     the username of the current user
     * @param emotions     a list of emotion strings to filter posts; if empty, no emotion filter is applied
     * @param filterRecent if true, the query will be filtered to only include posts from the past week
     * @return a Query object for retrieving the user's filtered EmotionPosts
     */
    public Query getUserPostsWithFilter(String username, List<String> emotions, boolean filterRecent) {
        Query query = repository.getFilteredUserPosts(username, emotions);

        if (filterRecent) {
            Timestamp oneWeekAgo = new Timestamp(Timestamp.now().getSeconds() - (7 * 24 * 60 * 60), 0);
            query = query.whereGreaterThanOrEqualTo("timestamp", oneWeekAgo);
        }

        return query;
    }

    /**
     * Retrieves a Firestore Query for posts from the user's friends with optional emotion filters
     * and recent time filter.
     *
     * @param friendUsernames list of usernames of the user's friends
     * @param emotions        a list of emotion strings to filter posts; if empty, no emotion filter is applied
     * @param filterRecent    if true, the query will be filtered to only include posts from the past week
     * @return a Query object for retrieving the friends' filtered EmotionPosts
     */
    public Query getFriendsPostsWithFilter(List<String> friendUsernames, List<String> emotions, boolean filterRecent) {
        Query query = repository.getFilteredFriendsPosts(friendUsernames, emotions);

        if (filterRecent) {
            Timestamp oneWeekAgo = new Timestamp(Timestamp.now().getSeconds() - (7 * 24 * 60 * 60), 0);
            query = query.whereGreaterThanOrEqualTo("timestamp", oneWeekAgo);
        }

        return query;
    }
}
