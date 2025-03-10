/**
 * UsernameRepository.java
 *
 * This repository class manages the persistence of username information in Firestore.
 * It provides methods to save a username with its associated email and to retrieve a username
 * based on the email address. The class uses a FirebaseDataSource instance initialized with the
 * "usernames" collection and follows the singleton design pattern.
 *
 * Outstanding Issues:
 * - Additional error handling or logging may be required in a production environment.
 * - We are considering expanding the query capabilities as the application scales.
 */

package com.example.tangry.repositories;

import com.example.tangry.datasource.FirebaseDataSource;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.Query.Direction;
import java.util.HashMap;
import java.util.Map;

public class UsernameRepository {
    private static UsernameRepository instance;
    private FirebaseDataSource firebaseDataSource;
    private static final String COLLECTION_NAME = "usernames";

    /**
     * Private constructor initializes the FirebaseDataSource with the "usernames" collection.
     */
    private UsernameRepository() {
        // Initialize the data source with the "usernames" collection.
        firebaseDataSource = new FirebaseDataSource(COLLECTION_NAME);
    }

    /**
     * Returns the singleton instance of UsernameRepository.
     *
     * @return the UsernameRepository instance
     */
    public static synchronized UsernameRepository getInstance() {
        if (instance == null) {
            instance = new UsernameRepository();
        }
        return instance;
    }

    /**
     * Saves a username and email pair to Firestore.
     *
     * @param username        the username to save
     * @param email           the email address associated with the username
     * @param successListener callback invoked on successful save with a DocumentReference of the saved document
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
        // Return a query that orders usernames in descending order.
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
}
