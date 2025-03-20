package com.example.tangry.repositories;

import com.example.tangry.datasource.FirebaseDataSource;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.Query.Direction;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

public class UserRepository {
    private static UserRepository instance;
    private FirebaseDataSource firebaseDataSource;
    private FirebaseAuth mAuth;
    private static final String COLLECTION_NAME = "users";

    /**
     * Private constructor initializes the FirebaseDataSource with the "usernames" collection.
     */
    private UserRepository() {
        firebaseDataSource = new FirebaseDataSource(COLLECTION_NAME);
    }

    /**
     * Returns the singleton instance of UserRepository.
     *
     * @return the UserRepository instance
     */
    public static synchronized UserRepository getInstance() {
        if (instance == null) {
            instance = new UserRepository();
        }
        return instance;
    }

    /**
     * Searches for usernames that start with the provided prefix.
     *
     * @param prefix            the username prefix
     * @param onSuccessListener callback invoked on a successful query with a QuerySnapshot
     * @param onFailureListener callback invoked if the query fails
     */
    public void searchUsersByPrefix(String prefix,
                                    OnSuccessListener<QuerySnapshot> onSuccessListener,
                                    OnFailureListener onFailureListener) {
        firebaseDataSource.getCollectionReference()
                .orderBy("username")
                .startAt(prefix)
                .endAt(prefix + "\uf8ff")
                .get()
                .addOnSuccessListener(onSuccessListener)
                .addOnFailureListener(onFailureListener);
    }

    /**
     * Saves a username and email pair to Firestore.
     *
     * @param username        the username to save
     * @param email           the email address associated with the username
     * @param successListener callback invoked on a successful save with a DocumentReference of the saved document
     * @param failureListener callback invoked if the save operation fails
     */
    public void saveUsernameToFirestore(String username, String email,
                                        OnSuccessListener<DocumentReference> successListener,
                                        OnFailureListener failureListener) {
        Map<String, Object> data = new HashMap<>();
        data.put("username", username);
        data.put("email", email);

        firebaseDataSource.saveData(data, successListener, failureListener);
    }

    /**
     * Returns a Firestore Query that orders username documents in descending order.
     *
     * @return a Query object for retrieving username documents ordered by the "username" field in descending order
     */
    public Query getPostsQuery() {
        return firebaseDataSource.getCollectionReference().orderBy("username", Direction.DESCENDING);
    }

    /**
     * Retrieves a username from Firestore based on the provided email.
     *
     * @param email           the email address to query for
     * @param successListener callback invoked with the username String if found, or null if not found
     * @param failureListener callback invoked if the query fails
     */
    public void getUsernameFromEmail(String email,
                                     OnSuccessListener<String> successListener,
                                     OnFailureListener failureListener) {
        firebaseDataSource.getCollectionReference()
                .whereEqualTo("email", email)
                .limit(1)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        successListener.onSuccess(querySnapshot.getDocuments().get(0).getString("username"));
                    } else {
                        successListener.onSuccess(null);
                    }
                })
                .addOnFailureListener(failureListener);
    }

    /**
     * Retrieves the username of the currently authenticated user.
     *
     * @param successListener callback invoked with the username String if found, or null if not found
     * @param failureListener callback invoked if the query fails or the user is not authenticated
     */
    public void getCurrentUsername(OnSuccessListener<String> successListener,
                                   OnFailureListener failureListener) {
        if (FirebaseAuth.getInstance().getCurrentUser() == null ||
                FirebaseAuth.getInstance().getCurrentUser().getEmail() == null) {
            failureListener.onFailure(new Exception("User not authenticated"));
            return;
        }
        String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        getUsernameFromEmail(email, successListener, failureListener);
    }

    /**
     * Sends a follow request by creating a new document in the "followrequests" collection.
     * The document will contain:
     *   from: current user's username
     *   to: target user's username
     *   accepted: false
     *
     * @param fromUser         the username of the current user sending the request
     * @param toUser           the username of the target user to follow
     * @param successListener  callback invoked on a successful document creation
     * @param failureListener  callback invoked if the creation fails
     */
    public void sendFollowRequest(String fromUser, String toUser,
                                  OnSuccessListener<DocumentReference> successListener,
                                  OnFailureListener failureListener) {
        FirebaseDataSource followRequestDataSource = new FirebaseDataSource("followrequests");
        Map<String, Object> data = new HashMap<>();
        data.put("from", fromUser);
        data.put("to", toUser);
        data.put("accepted", false);

        followRequestDataSource.saveData(data, successListener, failureListener);
    }

}