package com.example.tangry.repositories;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class UsernameRepository {
    private static UsernameRepository instance;
    private final FirebaseFirestore db;
    private static final String COLLECTION_NAME = "usernames";

    private UsernameRepository() {
        db = FirebaseFirestore.getInstance();
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

        db.collection(COLLECTION_NAME)
                .add(data)
                .addOnSuccessListener(successListener)
                .addOnFailureListener(failureListener);
    }

    public Query getPostsQuery() {
        return db.collection(COLLECTION_NAME)
                .orderBy("username", Query.Direction.DESCENDING);
    }

    public void getUsernameFromEmail(String email, OnSuccessListener<String> successListener, OnFailureListener failureListener) {
        FirebaseFirestore.getInstance()
                .collection(COLLECTION_NAME)
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
