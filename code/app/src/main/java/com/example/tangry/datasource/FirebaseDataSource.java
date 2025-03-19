/**
 * FirebaseDataSource.java
 *
 * This class serves as a data access layer for Firestore, providing methods for common
 * CRUD operations on a specified collection. It encapsulates FirebaseFirestore operations,
 * allowing the application to easily save, retrieve, update, and delete data.
 *
 * Outstanding Issues:
 * - We considering adding more robust error handling and logging.
 * - Support for more complex queries and transaction operations could be added in future iterations.
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

    /**
     * Constructs a FirebaseDataSource with the specified FirebaseFirestore instance and collection name.
     *
     * @param db             the FirebaseFirestore instance to use
     * @param collectionName the name of the Firestore collection
     */
    public FirebaseDataSource(FirebaseFirestore db, String collectionName) {
        this.db = db;
        this.collectionName = collectionName;
    }

    /**
     * Constructs a FirebaseDataSource using the default FirebaseFirestore instance and the default collection "emotions".
     */
    public FirebaseDataSource() {
        this.db = FirebaseFirestore.getInstance();
        this.collectionName = "emotions";
    }

    /**
     * Constructs a FirebaseDataSource with the specified collection name using the default FirebaseFirestore instance.
     *
     * @param collectionName the name of the Firestore collection
     */
    public FirebaseDataSource(String collectionName) {
        this.collectionName = collectionName;
        this.db = FirebaseFirestore.getInstance();
    }

    /**
     * Returns the FirebaseFirestore instance used by this data source.
     *
     * @return the FirebaseFirestore instance
     */
    public FirebaseFirestore getDBDataSource() {
        return db;
    }

    /**
     * Saves a data object to the Firestore collection.
     *
     * @param data            a map representing the data to be saved
     * @param successListener callback for a successful save operation, receiving the DocumentReference of the new document
     * @param failureListener callback for handling save failures
     */
    public void saveData(Map<String, Object> data,
                         OnSuccessListener<DocumentReference> successListener,
                         OnFailureListener failureListener) {
        db.collection(collectionName)
                .add(data)
                .addOnSuccessListener(successListener)
                .addOnFailureListener(failureListener);
    }

    /**
     * Returns a Firestore Query for the collection, ordered by "timestamp" in descending order.
     *
     * @return a Query object for retrieving documents ordered by timestamp
     */
    public Query getQuery() {
        return db.collection(collectionName)
                .orderBy("timestamp", Query.Direction.DESCENDING);
    }

    /**
     * Retrieves a single document from the collection.
     *
     * @param documentId      the ID of the document to retrieve
     * @param successListener callback for successful retrieval, receiving a DocumentSnapshot
     * @param failureListener callback for handling retrieval failures
     */
    public void getData(String documentId,
                        OnSuccessListener<DocumentSnapshot> successListener,
                        OnFailureListener failureListener) {
        db.collection(collectionName).document(documentId)
                .get()
                .addOnSuccessListener(successListener)
                .addOnFailureListener(failureListener);
    }

    /**
     * Updates an existing document in the collection with new data.
     *
     * @param documentId      the ID of the document to update
     * @param data            the new data to set (this will overwrite the existing document)
     * @param successListener callback for successful update
     * @param failureListener callback for handling update failures
     */
    public void updateData(String documentId, Object data,
                           OnSuccessListener<Void> successListener,
                           OnFailureListener failureListener) {
        db.collection(collectionName).document(documentId)
                .set(data)
                .addOnSuccessListener(successListener)
                .addOnFailureListener(failureListener);
    }

    /**
     * Deletes a document from the collection.
     *
     * @param documentId      the ID of the document to delete
     * @param successListener callback for successful deletion
     * @param failureListener callback for handling deletion failures
     */
    public void deleteData(String documentId,
                           OnSuccessListener<Void> successListener,
                           OnFailureListener failureListener) {
        db.collection(collectionName).document(documentId)
                .delete()
                .addOnSuccessListener(successListener)
                .addOnFailureListener(failureListener);
    }

    /**
     * Returns a CollectionReference for the Firestore collection.
     *
     * @return the CollectionReference for the specified collection
     */
    public CollectionReference getCollectionReference() {
        return db.collection(collectionName);
    }
}
