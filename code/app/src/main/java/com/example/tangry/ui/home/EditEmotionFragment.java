/**
 * EditEmotionFragment.java
 *
 * This fragment is responsible for editing an existing emotion post. It allows users to modify details such as
 * explanation, location, social situation, and to optionally attach a new image. The fragment pre-fills existing
 * data, supports image selection with compression (if the image exceeds 64KB), and updates the post in Firestore.
 *
 * Outstanding Issues:
 * - Further input validation and error handling could be implemented.
 * - UI enhancements such as progress indicators during image upload may improve the user experience.
 */

package com.example.tangry.ui.home;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import com.bumptech.glide.Glide;
import com.example.tangry.R;
import com.example.tangry.controllers.EmotionPostController;
import com.example.tangry.models.EmotionPost;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class EditEmotionFragment extends Fragment {
    private static final String TAG = "EditEmotionFragment";
    private static final int PICK_IMAGE_REQUEST = 1;

    private String postId, imageUri;
    private boolean isNewImageSelected = false;
    private EmotionPost updatedPost;

    private EmotionPostController emotionPostController;
    private NavController navController;

    private TextView emotionTextView;
    private EditText explanationInput, locationInput;
    private Spinner socialSituationSpinner;
    private ImageView imageAttachment;
    private Button actionButton;

    private static final List<String> VALID_SOCIAL_SITUATIONS = Arrays.asList(
            "Select social situation", "Alone", "With one other person",
            "With two to several people", "With a crowd");

    /**
     * Inflates the layout for this fragment.
     *
     * @param inflater           The LayoutInflater object to inflate the view.
     * @param container          The parent view.
     * @param savedInstanceState Saved state from previous instance.
     * @return The View for the fragment's UI.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_detail_emotion, container, false);
    }

    /**
     * Called when the view has been created. Initializes UI components, retrieves arguments,
     * pre-fills data if available, and sets up event listeners for image selection and post update.
     *
     * @param view               The root view of the fragment.
     * @param savedInstanceState Previously saved state.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);
        emotionPostController = new EmotionPostController();

        // Initialize UI components
        emotionTextView = view.findViewById(R.id.emotion_text);
        explanationInput = view.findViewById(R.id.explanation_input);
        locationInput = view.findViewById(R.id.location_input);
        socialSituationSpinner = view.findViewById(R.id.social_situation_spinner);
        imageAttachment = view.findViewById(R.id.image_attachment);
        actionButton = view.findViewById(R.id.save_button);

        // Populate Spinner with valid social situations
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, VALID_SOCIAL_SITUATIONS);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        socialSituationSpinner.setAdapter(adapter);

        // Retrieve data from arguments if available
        if (getArguments() != null) {
            String postJson = getArguments().getString("postJson");
            postId = getArguments().getString("postId");
            String selectedEmotion = getArguments().getString("selectedEmotion");

            if (postJson != null) {
                updatedPost = new Gson().fromJson(postJson, EmotionPost.class);
                prefillFields();
                actionButton.setText("Update");

                if (selectedEmotion != null) {
                    emotionTextView.setText(selectedEmotion);
                }
            }
        }

        imageAttachment.setOnClickListener(v -> selectImage());
        actionButton.setOnClickListener(v -> updatePost());
    }

    /**
     * Pre-fills the UI fields with data from the existing emotion post.
     */
    private void prefillFields() {
        emotionTextView.setText(updatedPost.getEmotion());
        explanationInput.setText(updatedPost.getExplanation());
        locationInput.setText(updatedPost.getLocation());
        socialSituationSpinner.setSelection(getSpinnerIndex(socialSituationSpinner, updatedPost.getSocialSituation()));

        if (updatedPost.getImageUri() != null) {
            imageUri = updatedPost.getImageUri();
            isNewImageSelected = false;
            Glide.with(requireContext()).load(Uri.parse(imageUri)).into(imageAttachment);
        }
    }

    /**
     * Retrieves the index of a specified value within the Spinner.
     *
     * @param spinner The Spinner component.
     * @param value   The value to locate.
     * @return The index of the value, or 0 if not found.
     */
    private int getSpinnerIndex(Spinner spinner, String value) {
        for (int i = 0; i < spinner.getCount(); i++) {
            if (spinner.getItemAtPosition(i).toString().equalsIgnoreCase(value)) {
                return i;
            }
        }
        return 0;
    }

    /**
     * Updates the emotion post. Validates the input and either uploads a new image if selected or
     * directly updates the post in Firestore.
     */
    private void updatePost() {
        String explanation = explanationInput.getText().toString().trim();
        String location = locationInput.getText().toString().trim();
        String socialSituation = socialSituationSpinner.getSelectedItem().toString();
        String emotion = emotionTextView.getText().toString();

        if ("Select social situation".equals(socialSituation)) {
            socialSituation = null;
        }

        if (explanation.isEmpty() && (imageUri == null || imageUri.isEmpty())) {
            Toast.makeText(getContext(), "Please provide either an explanation or an image.", Toast.LENGTH_SHORT).show();
            return;
        }

        updatedPost.setExplanation(explanation);
        updatedPost.setLocation(location);
        updatedPost.setSocialSituation(socialSituation);
        updatedPost.setEmotion(emotion);

        // If a new image is selected (and not already a Firebase Storage URL), process and upload it.
        if (imageUri != null && !imageUri.startsWith("https://firebasestorage")) {
            try {
                Uri processedUri = checkAndCompressImage(Uri.parse(imageUri));
                uploadImage(processedUri);
            } catch (IOException e) {
                Log.e(TAG, "Error processing image", e);
                Toast.makeText(getContext(), "Error processing image.", Toast.LENGTH_SHORT).show();
            }
        } else {
            // No new image, proceed to update the post.
            updatePostInFirestore(null);
        }
    }

    /**
     * Updates the emotion post in Firestore. If a new image URL is provided, it replaces the old one.
     * Also attempts to delete the previous image from Firebase Storage.
     *
     * @param imageUrl The new image URL, or null if unchanged.
     */
    private void updatePostInFirestore(String imageUrl) {
        // If a new image is provided and an old image exists in Firebase Storage, attempt deletion.
        if (imageUrl != null && updatedPost.getImageUri() != null &&
                !updatedPost.getImageUri().isEmpty() &&
                updatedPost.getImageUri().startsWith("https://firebasestorage")) {
            try {
                StorageReference oldImageRef = FirebaseStorage.getInstance().getReferenceFromUrl(updatedPost.getImageUri());
                oldImageRef.delete().addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Old image deleted successfully");
                }).addOnFailureListener(e -> {
                    Log.e(TAG, "Error deleting old image", e);
                    // Continue update even if deletion fails.
                });
            } catch (IllegalArgumentException e) {
                Log.e(TAG, "Invalid old image storage URL", e);
            }
        }

        // Update the post with the new image URL if available.
        if (imageUrl != null) {
            updatedPost.setImageUri(imageUrl);
        }

        // Save the updated post to Firestore.
        emotionPostController.updateEmotionPost(postId, updatedPost, () -> {
            Toast.makeText(getContext(), "Post updated!", Toast.LENGTH_SHORT).show();
            navController.popBackStack(R.id.navigation_home, true);
        }, e -> {
            Toast.makeText(getContext(), "Failed to update. " + e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Failed to update post", e);
        });
    }

    /**
     * Compresses an image if its size exceeds 64KB by reducing quality and scaling down dimensions.
     *
     * @param originalUri The original URI of the selected image.
     * @return The URI of the compressed image.
     * @throws IOException If the compression fails.
     */
    private Uri checkAndCompressImage(Uri originalUri) throws IOException {
        ParcelFileDescriptor pfd = getActivity().getContentResolver().openFileDescriptor(originalUri, "r");
        long fileSize = pfd.getStatSize();
        pfd.close();

        if (fileSize <= 65536) { // 64KB
            return originalUri;
        }

        Bitmap originalBitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), originalUri);
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

        File cacheDir = getContext().getCacheDir();
        File tempFile = File.createTempFile("compressed_image", ".jpg", cacheDir);
        FileOutputStream fos = new FileOutputStream(tempFile);
        fos.write(outputStream.toByteArray());
        fos.close();

        return Uri.fromFile(tempFile);
    }

    /**
     * Uploads an image to Firebase Storage and updates the post with the download URL upon success.
     *
     * @param imageUri The URI of the image to upload.
     */
    private void uploadImage(Uri imageUri) {
        Log.d(TAG, "Uploading image: " + imageUri);
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        StorageReference imageRef = storageRef.child("images/" + UUID.randomUUID().toString());

        imageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot ->
                        imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            String downloadUrl = uri.toString();
                            updatePostInFirestore(downloadUrl);
                        })
                )
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Image upload failed", e);
                });
    }

    /**
     * Opens an image picker to allow the user to select an image.
     */
    private void selectImage() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    /**
     * Handles the result of the image picker intent.
     *
     * @param requestCode The request code.
     * @param resultCode  The result code.
     * @param data        The returned data containing the selected image URI.
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            Uri uri = data.getData();
            if (uri != null) {
                imageUri = uri.toString();
                isNewImageSelected = true; // Mark as newly selected
                imageAttachment.setImageURI(uri);
            }
        }
    }
}
