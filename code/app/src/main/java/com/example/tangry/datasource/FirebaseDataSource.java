/**
 * FirebaseDataSource.java
 * 
 * This file contains a data access layer implementation for Firebase Firestore operations in the Tangry
 * application. It provides a simplified interface for CRUD operations on Firestore collections.
 * 
 * Key features:
 * - Abstracts Firebase Firestore implementation details from the rest of the application
 * - Provides methods for creating, reading, updating, and deleting documents
 * - Supports custom collection specification for different data types
 * - Handles query construction and execution with appropriate ordering
 * - Implements callback patterns for asynchronous operations
 * - Serves as the foundation for all repository classes in the application
 * - Enables testability through dependency injection of Firestore instances
 */
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

    public FirebaseDataSource(FirebaseFirestore db, String collectionName) {
        this.db = db;
        this.collectionName = collectionName;
    }

    public FirebaseDataSource() {
        this.db = FirebaseFirestore.getInstance();
        this.collectionName = "emotions";
    }

    public FirebaseDataSource(String collectionName) {
        this.collectionName = collectionName;
        this.db = FirebaseFirestore.getInstance();
    }

    public FirebaseFirestore getDBDataSource() {
        return db;
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
