package com.example.tangry.repositories;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import java.util.HashMap;
import java.util.Map;

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

    public Task<String> getUsernameFromEmail(String email) {
        return db.collection(COLLECTION_NAME)
                .whereEqualTo("email", email)
                .limit(1)
                .get()
                .continueWith(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        return task.getResult().getDocuments().get(0).getString("username");
                    } else {
                        return null;
                    }
                });
    }
}
