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
    private final FirebaseDataSource firebaseDataSource;
    private static final String COLLECTION_NAME = "usernames";

    private UsernameRepository() {
        // Initialize the data source with the "usernames" collection.
        firebaseDataSource = new FirebaseDataSource(COLLECTION_NAME);
    }

    public static synchronized UsernameRepository getInstance() {
        if (instance == null) {
            instance = new UsernameRepository();
        }
        return instance;
    }

    public void saveUsernameToFirestore(String username, String email,
                                        OnSuccessListener<DocumentReference> successListener,
                                        OnFailureListener failureListener) {
        Map<String, Object> data = new HashMap<>();
        data.put("username", username);
        data.put("email", email);

        firebaseDataSource.saveData(data, successListener, failureListener);
    }

    public Query getPostsQuery() {
        // Return a query that orders usernames in descending order.
        return firebaseDataSource.getCollectionReference().orderBy("username", Direction.DESCENDING);
    }

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
