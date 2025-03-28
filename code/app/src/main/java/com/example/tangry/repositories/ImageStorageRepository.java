/**
 * ImageStorageRepository.java
 *
 * This repository class provides an abstraction layer for interacting with Firebase Storage.
 * It supports common image operations such as uploading images (with or without progress tracking),
 * retrieving download URLs, and deleting images. The class follows the singleton design pattern
 * to ensure a single instance is used throughout the application.
 *
 * Outstanding Issues:
 * - We considering implementing retry logic for network-related failures.
 * - Further customization for different storage buckets or advanced metadata handling might be added.
 */

package com.example.tangry.repositories;

import android.net.Uri;
import android.util.Log;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import java.util.function.Consumer;

public class ImageStorageRepository {
    private static final String TAG = "ImageStorageRepository";

    private static ImageStorageRepository instance;
    private final FirebaseStorage storage;

    /**
     * Private constructor for singleton instantiation.
     * Initializes the repository with the default FirebaseStorage instance.
     */
    private ImageStorageRepository() {
        storage = FirebaseStorage.getInstance();
    }

    /**
     * Returns the singleton instance of ImageStorageRepository.
     *
     * @return the ImageStorageRepository instance
     */
    public static synchronized ImageStorageRepository getInstance() {
        if (instance == null) {
            instance = new ImageStorageRepository();
        }
        return instance;
    }

    /**
     * Constructor for testing purposes that accepts a custom FirebaseStorage
     * instance.
     *
     * @param storageInstance the FirebaseStorage instance to use
     */
    ImageStorageRepository(FirebaseStorage storageInstance) {
        storage = storageInstance;
    }

    /**
     * Uploads an image to Firebase Storage.
     *
     * @param imageUri    the local URI of the image to upload
     * @param storagePath the path in Firebase Storage where the image should be
     *                    stored
     * @param onSuccess   callback that receives the download URL as a String upon
     *                    success
     * @param onFailure   callback that receives an Exception upon failure
     */
    public void uploadImage(Uri imageUri, String storagePath, Consumer<String> onSuccess,
            Consumer<Exception> onFailure) {
        StorageReference imageRef = storage.getReference().child(storagePath);

        imageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    // Get the download URL
                    imageRef.getDownloadUrl()
                            .addOnSuccessListener(uri -> {
                                Log.d(TAG, "Image uploaded successfully: " + uri.toString());
                                onSuccess.accept(uri.toString());
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Failed to get download URL: " + e.getMessage());
                                onFailure.accept(e);
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Image upload failed: " + e.getMessage());
                    onFailure.accept(e);
                });
    }

    /**
     * Uploads an image to Firebase Storage with progress tracking.
     *
     * @param imageUri         the local URI of the image to upload
     * @param storagePath      the path in Firebase Storage where the image should
     *                         be stored
     * @param progressListener callback that receives the current progress
     *                         percentage as an Integer
     * @param onSuccess        callback that receives the download URL as a String
     *                         upon success
     * @param onFailure        callback that receives an Exception upon failure
     */
    public void uploadImageWithProgress(
            Uri imageUri,
            String storagePath,
            Consumer<Integer> progressListener,
            Consumer<String> onSuccess,
            Consumer<Exception> onFailure) {

        StorageReference imageRef = storage.getReference().child(storagePath);

        UploadTask uploadTask = imageRef.putFile(imageUri);

        // Register progress listener
        uploadTask.addOnProgressListener(taskSnapshot -> {
            double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
            progressListener.accept((int) progress);
        });

        uploadTask.continueWithTask(task -> {
            if (!task.isSuccessful()) {
                throw task.getException();
            }
            // Continue with the task to get the download URL
            return imageRef.getDownloadUrl();
        }).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Uri downloadUri = task.getResult();
                onSuccess.accept(downloadUri.toString());
            } else {
                onFailure.accept(task.getException());
            }
        });
    }

    /**
     * Retrieves the download URL for an image stored in Firebase Storage.
     *
     * @param storagePath the path in Firebase Storage where the image is stored
     * @param onSuccess   callback that receives the download URL as a String upon
     *                    success
     * @param onFailure   callback that receives an Exception upon failure
     */
    public void getImageUrl(String storagePath, Consumer<String> onSuccess, Consumer<Exception> onFailure) {
        StorageReference imageRef = storage.getReference().child(storagePath);

        imageRef.getDownloadUrl()
                .addOnSuccessListener(uri -> {
                    Log.d(TAG, "Got download URL: " + uri.toString());
                    onSuccess.accept(uri.toString());
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to get download URL: " + e.getMessage());
                    onFailure.accept(e);
                });
    }

    /**
     * Deletes an image from Firebase Storage.
     *
     * @param storagePath the path in Firebase Storage where the image is stored
     * @param onSuccess   callback invoked when deletion is successful
     * @param onFailure   callback that receives an Exception upon failure
     */
    public void deleteImage(String storagePath, Runnable onSuccess, Consumer<Exception> onFailure) {
        StorageReference imageRef = storage.getReference().child(storagePath);

        imageRef.delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Image deleted successfully");
                    onSuccess.run();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to delete image: " + e.getMessage());
                    onFailure.accept(e);
                });
    }
}
