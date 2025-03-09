package com.example.tangry.controllers;

import com.example.tangry.models.EmotionPost;
import com.example.tangry.repositories.EmotionPostRepository;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.Query;
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
}
