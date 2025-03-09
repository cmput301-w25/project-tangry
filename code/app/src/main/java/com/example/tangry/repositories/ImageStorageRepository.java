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

    // Private constructor for singleton
    private ImageStorageRepository() {
        storage = FirebaseStorage.getInstance();
    }

    // Get singleton instance
    public static synchronized ImageStorageRepository getInstance() {
        if (instance == null) {
            instance = new ImageStorageRepository();
        }
        return instance;
    }

    // For testing
    ImageStorageRepository(FirebaseStorage storageInstance) {
        storage = storageInstance;
    }

    /**
     * Uploads an image to Firebase Storage
     *
     * @param imageUri The local URI of the image
     * @param storagePath The path in Firebase Storage where the image should be stored
     * @param onSuccess Callback with the download URL on success
     * @param onFailure Callback with the exception on failure
     */
    public void uploadImage(Uri imageUri, String storagePath, Consumer<String> onSuccess, Consumer<Exception> onFailure) {
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
     * Upload an image with progress tracking
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
     * Gets the download URL for an image in Firebase Storage
     *
     * @param storagePath The path in Firebase Storage where the image is stored
     * @param onSuccess Callback with the download URL on success
     * @param onFailure Callback with the exception on failure
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
     * Deletes an image from Firebase Storage
     *
     * @param storagePath The path in Firebase Storage where the image is stored
     * @param onSuccess Callback when the deletion is successful
     * @param onFailure Callback with the exception when deletion fails
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