/**
 * ImageCaptureUtil.java
 *
 * Utility class that provides shared methods for camera and gallery image selection
 * across multiple fragments. This reduces code duplication and ensures consistent
 * behavior for image handling throughout the app.
 */

package com.example.tangry.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ImageCaptureUtil {
    private static final String TAG = "ImageCaptureUtil";
    public static final int PICK_IMAGE_REQUEST = 1;
    public static final int CAMERA_REQUEST = 2;
    public static final int CAMERA_PERMISSION_REQUEST = 100;

    /**
     * Shows a dialog allowing the user to choose between camera or gallery for
     * image selection
     *
     * @param fragment                 The fragment where the selection occurs
     * @param cameraPermissionCallback Callback when camera permission is needed
     */
    public static void showImageSourceDialog(Fragment fragment, Runnable cameraPermissionCallback) {
        AlertDialog.Builder builder = new AlertDialog.Builder(fragment.requireContext());
        builder.setTitle("Choose Image Source");

        String[] options = { "Take Photo", "Choose from Gallery", "Cancel" };

        builder.setItems(options, (dialog, which) -> {
            if (which == 0) {
                // Camera option selected
                cameraPermissionCallback.run();
            } else if (which == 1) {
                // Gallery option selected
                openGallery(fragment);
            } else {
                dialog.dismiss();
            }
        });

        builder.show();
    }

    /**
     * Opens the gallery for image selection
     */
    public static void openGallery(Fragment fragment) {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        fragment.startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    /**
     * Opens the camera to take a photo
     *
     * @param fragment The fragment where the camera is being accessed
     * @return The URI of the image file that will be created, or null if there was
     *         an error
     */
    public static Uri openCamera(Fragment fragment) {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        Uri cameraImageUri = null;

        // Create file to store the image
        File photoFile = null;
        try {
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String imageFileName = "JPEG_" + timeStamp + "_";
            File storageDir = fragment.requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            photoFile = File.createTempFile(imageFileName, ".jpg", storageDir);
        } catch (IOException ex) {
            Log.e(TAG, "Error creating image file", ex);
            Toast.makeText(fragment.getContext(), "Error creating image file", Toast.LENGTH_SHORT).show();
            return null;
        }

        // Continue only if the file was successfully created
        if (photoFile != null) {
            // Use FileProvider to get content URI that's accessible to the camera app
            cameraImageUri = FileProvider.getUriForFile(fragment.requireContext(),
                    "com.example.tangry.fileprovider",
                    photoFile);

            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, cameraImageUri);
            fragment.startActivityForResult(cameraIntent, CAMERA_REQUEST);
        }

        return cameraImageUri;
    }

    /**
     * Checks if the camera permission is granted, and requests it if not
     *
     * @param fragment The fragment requesting the permission
     * @return true if permission is already granted, false otherwise
     */
    public static boolean checkCameraPermission(Fragment fragment) {
        return ContextCompat.checkSelfPermission(fragment.requireContext(),
                android.Manifest.permission.CAMERA) == android.content.pm.PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Shows a rationale dialog when the user has previously denied camera
     * permission
     */
    public static void showCameraPermissionRationale(Fragment fragment) {
        new AlertDialog.Builder(fragment.requireContext())
                .setTitle("Camera Permission Required")
                .setMessage("This app needs camera access to take photos for your emotion posts")
                .setPositiveButton("OK", (dialog, which) -> {
                    fragment.requestPermissions(new String[] { android.Manifest.permission.CAMERA },
                            CAMERA_PERMISSION_REQUEST);
                })
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    /**
     * Shows a dialog directing the user to the app settings when they've
     * permanently denied permission
     */
    public static void showPermissionPermanentlyDeniedDialog(Fragment fragment) {
        new AlertDialog.Builder(fragment.requireContext())
                .setTitle("Permission Denied")
                .setMessage("Camera permission has been permanently denied. Please go to Settings to enable it.")
                .setPositiveButton("Settings", (dialog, which) -> {
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", fragment.requireActivity().getPackageName(), null);
                    intent.setData(uri);
                    fragment.startActivity(intent);
                })
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    /**
     * Handle the camera result properly by ensuring the image file exists and
     * refreshing media scanner
     * 
     * @param context        The context
     * @param cameraImageUri The URI of the camera image
     * @return true if image exists and was processed successfully
     */
    public static boolean processCameraResult(Context context, Uri cameraImageUri) {
        if (cameraImageUri == null) {
            Log.e(TAG, "Camera image URI is null");
            return false;
        }

        try {
            // Check if the file exists
            File imageFile = new File(cameraImageUri.getPath());
            if (!imageFile.exists()) {
                Log.e(TAG, "Camera image file doesn't exist at: " + cameraImageUri.getPath());
                return false;
            }

            // Notify the system that a new image is available
            Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            mediaScanIntent.setData(cameraImageUri);
            context.sendBroadcast(mediaScanIntent);

            Log.d(TAG, "Camera image successfully processed: " + cameraImageUri);
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error processing camera image result", e);
            return false;
        }
    }
}