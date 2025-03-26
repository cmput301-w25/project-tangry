/**
 * CreateEmotionPostFragment.java
 *
 * This fragment allows the user to create a new emotion post by entering details such as an explanation,
 * location, and social situation, as well as attaching an optional image. It retrieves the current user's
 * email via FirebaseAuth and uses a UserController and EmotionPostController to handle post creation.
 * The fragment also provides image selection and compression functionality via the ImageHelper utility.
 *
 * Outstanding Issues:
 * - Additional input validation and error handling can be implemented.
 * - The user experience could be enhanced with progress indicators during image upload.
 */

package com.example.tangry.ui.home;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
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
import com.example.tangry.R;
import com.example.tangry.controllers.EmotionPostController;
import com.example.tangry.controllers.UserController;
import com.example.tangry.models.EmotionPost;
import com.example.tangry.utils.ImageHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class CreateEmotionPostFragment extends Fragment {
    private static final String TAG = "CreateEmotionPostFragment";
    private static final int PICK_IMAGE_REQUEST = 1;

    private TextView emotionTextView;
    private EditText explanationInput, locationInput;
    private Spinner socialSituationSpinner;
    private ImageView imageAttachment;
    private Button saveButton;
    private String imageUri;
    private NavController navController;

    private EmotionPostController emotionPostController;
    private UserController usernameController;

    private static final List<String> VALID_SOCIAL_SITUATIONS = Arrays.asList(
            "Select social situation", "Alone", "With one other person", "With two to several people", "With a crowd");

    /**
     * Inflates the fragment layout.
     *
     * @param inflater           the LayoutInflater to use
     * @param container          the parent container
     * @param savedInstanceState the previously saved state, if any
     * @return the root view of the inflated layout
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_detail_emotion, container, false);
    }

    /**
     * Initializes UI components, controllers, and sets up event listeners.
     *
     * @param view               the root view returned by onCreateView
     * @param savedInstanceState the previously saved state, if any
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);

        emotionTextView = view.findViewById(R.id.emotion_text);
        explanationInput = view.findViewById(R.id.explanation_input);
        locationInput = view.findViewById(R.id.location_input);
        socialSituationSpinner = view.findViewById(R.id.social_situation_spinner);
        imageAttachment = view.findViewById(R.id.image_attachment);
        saveButton = view.findViewById(R.id.save_button);

        // Initialize controllers
        emotionPostController = new EmotionPostController();
        usernameController = new UserController();

        // Retrieve passed arguments (using Safe Args if applicable)
        if (getArguments() != null) {
            String emotionText = CreateEmotionPostFragmentArgs.fromBundle(getArguments()).getItemText();
            emotionTextView.setText(emotionText);
        }

        // Populate the social situation spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_item, VALID_SOCIAL_SITUATIONS);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        socialSituationSpinner.setAdapter(adapter);
        socialSituationSpinner.setSelection(0);

        imageAttachment.setOnClickListener(v -> selectImage());
        saveButton.setOnClickListener(v -> saveMoodEvent());
    }

    /**
     * Launches an intent to select an image from the device's gallery.
     */
    private void selectImage() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    /**
     * Handles the result from the image selection intent.
     *
     * @param requestCode the request code identifying the request
     * @param resultCode  the result code returned by the activity
     * @param data        the Intent data containing the selected image URI
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            Uri uri = data.getData();
            if (uri != null) {
                imageUri = uri.toString();
                imageAttachment.setImageURI(uri);
                Log.d(TAG, "Image selected: " + uri.toString());
            } else {
                Log.e(TAG, "Image URI is null");
            }
        }
    }

    /**
     * Validates input fields and initiates the process of saving a new emotion
     * post.
     * It retrieves the username associated with the current user's email and
     * handles image upload if present.
     */
    private void saveMoodEvent() {
        String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        usernameController.getUsername(email,
                username -> {
                    final String emotion = emotionTextView.getText().toString();
                    final String explanation = explanationInput.getText().toString().trim();
                    final String location = locationInput.getText().toString().trim();
                    String socialSituation = socialSituationSpinner.getSelectedItem().toString();
                    if ("Select social situation".equals(socialSituation)) {
                        socialSituation = null;
                    }
                    final String finalSocialSituation = socialSituation;

                    if (imageUri != null) {
                        try {
                            Uri processedUri = ImageHelper.compressImage(Uri.parse(imageUri), getContext());
                            uploadImage(processedUri, emotion, explanation, location, finalSocialSituation, username);
                        } catch (IOException e) {
                            Log.e(TAG, "Error processing image", e);
                            Toast.makeText(getContext(), "Error processing image.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        createEmotionPost(emotion, explanation, null, location, finalSocialSituation, username);
                    }
                },
                e -> Toast.makeText(getContext(), "Failed to get username.", Toast.LENGTH_SHORT).show());
    }

    /**
     * Uploads an image using ImageHelper and creates an emotion post upon
     * successful upload.
     *
     * @param imageUri        the URI of the processed image
     * @param emotion         the emotion text
     * @param explanation     the explanation text
     * @param location        the location string
     * @param socialSituation the social situation string (nullable)
     * @param username        the username associated with the current user
     */
    private void uploadImage(Uri imageUri, String emotion, String explanation,
            String location, String socialSituation, String username) {
        ImageHelper.uploadImage(imageUri,
                downloadUri -> createEmotionPost(emotion, explanation, downloadUri.toString(), location,
                        socialSituation, username),
                e -> Toast.makeText(getContext(), "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    /**
     * Creates an EmotionPost object and saves it to Firestore using the
     * EmotionPostController.
     *
     * @param emotion         the emotion text
     * @param explanation     the explanation text
     * @param imageUrl        the download URL of the uploaded image (nullable)
     * @param location        the location string
     * @param socialSituation the social situation string (nullable)
     * @param username        the username associated with the current user
     */
    private void createEmotionPost(String emotion, String explanation, String imageUrl, String location,
            String socialSituation, String username) {
        try {
            EmotionPost post = EmotionPost.create(emotion, explanation, imageUrl, location, socialSituation, username);
            Log.d(TAG, "Saving mood event: " + post.toString());

            emotionPostController.createPostWithOfflineSupport(
                    getContext(),
                    post,
                    (DocumentReference docRef) -> {
                        // Post saved successfully or queued for offline
                        String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();

                        // Only increment karma if we're online (docRef will be null if offline)
                        if (docRef != null) {
                            int incrementAmount = calculateKarmaIncrement(post);
                            usernameController.incrementKarma(email,
                                    aVoid -> Log.d(TAG, "User karma incremented by " + incrementAmount),
                                    e -> Log.e(TAG, "Failed to increment karma", e),
                                    incrementAmount);
                        }

                        Toast.makeText(getContext(), "Mood event saved!", Toast.LENGTH_SHORT).show();
                        navController.popBackStack();
                        navController.popBackStack();
                    },
                    e -> {
                        Toast.makeText(getContext(), "Failed to save. " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Failed to save mood event", e);
                    });
        } catch (Exception e) {
            Log.e(TAG, "Error creating emotion post", e);
            Toast.makeText(getContext(), "Error creating post: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private int calculateKarmaIncrement(EmotionPost post) {
        int karma = 0; // Base karma

        if (post.getImageUri() == null) {
            karma += 5; // Bonus for no image
        } else {
            karma += 10; // Bonus for posting an image
            if (post.getLocation() != null || !post.getLocation().isEmpty()) {
                karma += 5;
            }
            if (post.getExplanation() != null || !post.getExplanation().isEmpty()) {
                karma += 5;
            }
            if (post.getSocialSituation() != null || !post.getSocialSituation().isEmpty()) {
                karma += 5;
            }
        }

        return karma;
    }

}
