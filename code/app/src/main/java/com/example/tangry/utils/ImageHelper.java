/**
 * ImageHelper.java
 *
 * This utility class provides methods for compressing and uploading images.
 * The compressImage method ensures that an image from a given Uri is compressed to a size
 * under 64KB, using quality reduction and scaling if necessary. The uploadImage method uploads
 * an image to Firebase Storage and returns the download URL via callbacks.
 *
 * Outstanding Issues:
 * - The compression strategy may need adjustments for different image types or desired quality.
 * - Additional error handling and support for different file formats could be implemented in the future.
 */

package com.example.tangry.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

public class ImageHelper {

    /**
     * Compresses an image from the provided Uri so that its size is under 64KB.
     * The method first attempts to reduce the JPEG quality, and if necessary, scales down the image
     * dimensions until the size requirement is met.
     *
     * @param originalUri The Uri of the original image.
     * @param context     The application context.
     * @return A Uri pointing to the compressed image file.
     * @throws IOException If the image cannot be compressed to under 64KB.
     */
    public static Uri compressImage(Uri originalUri, Context context) throws IOException {
        ParcelFileDescriptor pfd = context.getContentResolver().openFileDescriptor(originalUri, "r");
        long fileSize = pfd.getStatSize();
        pfd.close();

        if (fileSize <= 65536) {
            return originalUri;
        }

        Bitmap originalBitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), originalUri);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        int quality = 80;
        do {
            outputStream.reset();
            originalBitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream);
            quality -= 10;
        } while (outputStream.size() > 65536 && quality > 0);

        if (outputStream.size() > 65536) {
            float scale = 0.8f;
            while (outputStream.size() > 65536 && scale > 0.1f) {
                int newWidth = (int) (originalBitmap.getWidth() * scale);
                int newHeight = (int) (originalBitmap.getHeight() * scale);
                Bitmap scaledBitmap = Bitmap.createScaledBitmap(originalBitmap, newWidth, newHeight, true);
                outputStream.reset();
                scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream);
                scale -= 0.1f;
            }
        }

        if (outputStream.size() > 65536) {
            throw new IOException("Could not compress image to under 64KB.");
        }

        File cacheDir = context.getCacheDir();
        File tempFile = File.createTempFile("compressed_image", ".jpg", cacheDir);
        FileOutputStream fos = new FileOutputStream(tempFile);
        fos.write(outputStream.toByteArray());
        fos.close();

        return Uri.fromFile(tempFile);
    }

    /**
     * Uploads an image file to Firebase Storage and returns its download Uri via the provided callback.
     *
     * @param imageUri  The Uri of the image to upload.
     * @param onSuccess Callback with the download Uri upon successful upload.
     * @param onFailure Callback for handling any errors during upload.
     */
    public static void uploadImage(Uri imageUri,
                                   OnSuccessListener<Uri> onSuccess,
                                   OnFailureListener onFailure) {
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        StorageReference imageRef = storageRef.child("images/" + UUID.randomUUID().toString());
        imageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot ->
                        imageRef.getDownloadUrl()
                                .addOnSuccessListener(onSuccess)
                                .addOnFailureListener(onFailure))
                .addOnFailureListener(onFailure);
    }
}
