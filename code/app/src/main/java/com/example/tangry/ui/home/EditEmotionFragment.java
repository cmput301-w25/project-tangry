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
import com.example.tangry.models.EmotionPost;
import com.example.tangry.repositories.EmotionPostRepository;
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

/**
 * Fragment responsible for editing an existing emotion post.
 * <p>
 * This fragment allows users to modify an existing mood post, update details like
 * explanation, location, social situation, and optionally attach an image.
 * It communicates with Firestore using {@link EmotionPostRepository}.
 * <p>
 */
public class EditEmotionFragment extends Fragment {
    private static final String TAG = "EditEmotionFragment";
    private static final int PICK_IMAGE_REQUEST = 1;
    private String postId, imageUri;
    private EmotionPost updatedPost;
    private EmotionPostRepository repository;
    private NavController navController;

    private TextView emotionTextView;
    private EditText explanationInput, locationInput;
    private Spinner socialSituationSpinner;
    private ImageView imageAttachment;
    private Button actionButton;

    private static final List<String> VALID_SOCIAL_SITUATIONS = Arrays.asList(
            "Select social situation", "Alone", "With one other person",
            "With two to several people", "With a crowd"
    );

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
        return inflater.inflate(R.layout.fragment_detail_emotion, container, false); // 🔥 Reusing the same XML
    }

    /**
     * Called when the view has been created.
     * Initializes UI components, retrieves arguments, and sets up event listeners.
     *
     * @param view               The root view of the fragment.
     * @param savedInstanceState Previously saved state.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);
        repository = EmotionPostRepository.getInstance();

        // Initialize UI components
        emotionTextView = view.findViewById(R.id.emotion_text);
        explanationInput = view.findViewById(R.id.explanation_input);
        locationInput = view.findViewById(R.id.location_input);
        socialSituationSpinner = view.findViewById(R.id.social_situation_spinner);
        imageAttachment = view.findViewById(R.id.image_attachment);
        actionButton = view.findViewById(R.id.save_button);

        // Populate Spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, VALID_SOCIAL_SITUATIONS);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        socialSituationSpinner.setAdapter(adapter);

        // Retrieve Data
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
     * Prefills UI fields with the existing emotion post data.
     */
    private void prefillFields() {
        emotionTextView.setText(updatedPost.getEmotion());
        explanationInput.setText(updatedPost.getExplanation());
        locationInput.setText(updatedPost.getLocation());
        socialSituationSpinner.setSelection(getSpinnerIndex(socialSituationSpinner, updatedPost.getSocialSituation()));

        if (updatedPost.getImageUri() != null) {
            imageUri = updatedPost.getImageUri();
            Glide.with(requireContext()).load(Uri.parse(imageUri)).into(imageAttachment);
        }
    }

    /**
     * Retrieves the index of a value in the Spinner.
     *
     * @param spinner The Spinner component.
     * @param value   The value to find in the Spinner.
     * @return The index of the value or 0 if not found.
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
     * Updates the emotion post in Firestore.
     * If an image is selected, it will be compressed and uploaded before updating the post.
     */
    private void updatePost() {
        String explanation = explanationInput.getText().toString().trim();
        String location = locationInput.getText().toString().trim();
        String socialSituation = socialSituationSpinner.getSelectedItem().toString();
        String emotion = emotionTextView.getText().toString();

        if (socialSituation.equals("Select social situation")) {
            Toast.makeText(getContext(), "Please select a valid social situation.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (explanation.isEmpty() && (imageUri == null || imageUri.isEmpty())) {
            Toast.makeText(getContext(), "Please provide either an explanation or an image.", Toast.LENGTH_SHORT).show();
            return;
        }

        updatedPost.setExplanation(explanation);
        updatedPost.setLocation(location);
        updatedPost.setSocialSituation(socialSituation);
        updatedPost.setEmotion(emotion);

        if (imageUri != null) {
            try {
                Uri processedUri = checkAndCompressImage(Uri.parse(imageUri));
                uploadImage(processedUri);
            } catch (IOException e) {
                Log.e(TAG, "Error processing image", e);
                Toast.makeText(getContext(), "Error processing image.", Toast.LENGTH_SHORT).show();
            }
        } else {
            // No image to upload, proceed with updating the post
            updatePostInFirestore(null);
        }
    }

    /**
     * Updates the post in Firestore with the given image URL.
     *
     * @param imageUrl The new image URL (or null if unchanged).
     */
    private void updatePostInFirestore(String imageUrl) {
        if (imageUrl != null) {
            updatedPost.setImageUri(imageUrl);
        }

        repository.updateEmotionPost(postId, updatedPost, () -> {
            Toast.makeText(getContext(), "Post updated!", Toast.LENGTH_SHORT).show();
            navController.popBackStack(R.id.navigation_home, false);
        }, e -> {
            Toast.makeText(getContext(), "Failed to update. " + e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Failed to update post", e);
        });
    }

    /**
     * Compresses an image if it's larger than 64KB.
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
     * Uploads an image to Firebase Storage and updates the post with the download URL.
     *
     * @param imageUri The URI of the image to upload.
     */
    private void uploadImage(Uri imageUri) {
        Log.d(TAG, "Uploading image: " + imageUri);
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        StorageReference imageRef = storageRef.child("images/" + UUID.randomUUID().toString());

        imageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        String downloadUrl = uri.toString();
                        updatePostInFirestore(downloadUrl);
                    });
                })
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
     * Handles the result of the image picker.
     *
     * @param requestCode The request code.
     * @param resultCode  The result code.
     * @param data        The returned data.
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            Uri uri = data.getData();
            assert uri != null;
            imageUri = uri.toString();
            imageAttachment.setImageURI(uri);
            Log.d(TAG, "Image selected: " + uri.toString());
        }
    }
}
