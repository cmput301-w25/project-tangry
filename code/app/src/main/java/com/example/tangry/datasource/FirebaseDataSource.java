package com.example.tangry.datasource;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.Map;

public class FirebaseDataSource {
    private FirebaseFirestore db;
    private String collectionName;

    public FirebaseDataSource(FirebaseFirestore db1, String collectionName) {
        db = db1;
        this.collectionName = collectionName;
    }

    public FirebaseDataSource() {
        db = FirebaseFirestore.getInstance();
        collectionName = "emotions";
    }

    public FirebaseFirestore getDBDataSource() {
        return db;
    }


    public FirebaseDataSource(String collectionName) {
        this.collectionName = collectionName;
        this.db = FirebaseFirestore.getInstance();
    }

    public void saveData(Map<String, Object> data,
                         OnSuccessListener<DocumentReference> successListener,
                         OnFailureListener failureListener) {
        db.collection(collectionName)
                .add(data)
                .addOnSuccessListener(successListener)
                .addOnFailureListener(failureListener);
    }

    public Query getQuery() {
        return db.collection(collectionName)
                .orderBy("timestamp", Query.Direction.DESCENDING);
    }

    public void getData(String documentId,
                        OnSuccessListener<DocumentSnapshot> successListener,
                        OnFailureListener failureListener) {
        db.collection(collectionName).document(documentId)
                .get()
                .addOnSuccessListener(successListener)
                .addOnFailureListener(failureListener);
    }

    public void updateData(String documentId, Object data,
                           OnSuccessListener<Void> successListener,
                           OnFailureListener failureListener) {
        db.collection(collectionName).document(documentId)
                .set(data)
                .addOnSuccessListener(successListener)
                .addOnFailureListener(failureListener);
    }

    public void deleteData(String documentId,
                           OnSuccessListener<Void> successListener,
                           OnFailureListener failureListener) {
        db.collection(collectionName).document(documentId)
                .delete()
                .addOnSuccessListener(successListener)
                .addOnFailureListener(failureListener);
    }

    public CollectionReference getCollectionReference() {
        return db.collection(collectionName);
    }
}
