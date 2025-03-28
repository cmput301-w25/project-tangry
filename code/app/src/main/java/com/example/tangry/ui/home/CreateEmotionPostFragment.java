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
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

import com.example.tangry.R;
import com.example.tangry.controllers.EmotionPostController;
import com.example.tangry.controllers.UserController;
import com.example.tangry.models.EmotionPost;
import com.example.tangry.utils.ImageCaptureUtil;
import com.example.tangry.utils.ImageHelper;
import com.example.tangry.utils.NetworkMonitor;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CreateEmotionPostFragment extends Fragment {
    private static final String TAG = "CreateEmotionPostFragment";
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int CAMERA_REQUEST = 2;
    private static final int CAMERA_PERMISSION_REQUEST = 100;

    private TextView emotionTextView;
    private EditText explanationInput;
    // Changed locationInput type to AutoCompleteTextView so we can show suggestions
    private AutoCompleteTextView locationInput;
    private Spinner socialSituationSpinner;
    private ImageView imageAttachment;
    private Button saveButton;
    private String imageUri;
    private NavController navController;

    private EmotionPostController emotionPostController;
    private UserController usernameController;

    private Uri cameraImageUri; // To store the camera image URI

    private static final List<String> VALID_SOCIAL_SITUATIONS = Arrays.asList(
            "Select social situation", "Alone", "With one other person", "With two to several people", "With a crowd");

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_detail_emotion, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);

        emotionTextView = view.findViewById(R.id.emotion_text);
        explanationInput = view.findViewById(R.id.explanation_input);
        // Ensure your layout uses an AutoCompleteTextView with id "location_input"
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

        // Set up image click listener for selecting or capturing an image
        imageAttachment.setOnClickListener(v -> selectImage());

        // Set up the save button click listener
        saveButton.setOnClickListener(v -> saveMoodEvent());

        // Setup location auto-complete suggestions
        // When the user types three or more characters, fetch address suggestions.
        locationInput.setThreshold(1);
        locationInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // no-op
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() >= 3) {
                    fetchAddressSuggestions(s.toString());
                }
            }
            @Override
            public void afterTextChanged(Editable s) {
                // no-op
            }
        });
    }

    /**
     * Fetches address suggestions (up to 5) in a background thread and updates
     * the AutoCompleteTextView adapter on the UI thread.
     */
    private void fetchAddressSuggestions(final String query) {
        new Thread(() -> {
            try {
                // Use Android's built-in Geocoder
                android.location.Geocoder geoCoder = new android.location.Geocoder(getContext(), Locale.getDefault());
                List<android.location.Address> addresses = geoCoder.getFromLocationName(query, 5);
                final List<String> suggestions = new ArrayList<>();
                if (addresses != null && !addresses.isEmpty()) {
                    for (android.location.Address addr : addresses) {
                        String addressLine = addr.getAddressLine(0);
                        if (addressLine != null && !addressLine.isEmpty()) {
                            suggestions.add(addressLine);
                        }
                    }
                }
                // Update the adapter on the UI thread
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(),
                                android.R.layout.simple_dropdown_item_1line, suggestions);
                        locationInput.setAdapter(adapter);
                        adapter.notifyDataSetChanged();
                    });
                }
            } catch (IOException e) {
                Log.e(TAG, "Error fetching address suggestions", e);
            }
        }).start();
    }

    /**
     * Launches an intent to select an image from the device's gallery or open the camera.
     */
    private void selectImage() {
        ImageCaptureUtil.showImageSourceDialog(this, this::checkCameraPermissionAndOpenCamera);
    }

    /**
     * Checks for camera permission and opens the camera if granted.
     */
    private void checkCameraPermissionAndOpenCamera() {
        if (!ImageCaptureUtil.checkCameraPermission(this)) {
            if (shouldShowRequestPermissionRationale(android.Manifest.permission.CAMERA)) {
                ImageCaptureUtil.showCameraPermissionRationale(this);
            } else {
                requestPermissions(new String[]{android.Manifest.permission.CAMERA},
                        ImageCaptureUtil.CAMERA_PERMISSION_REQUEST);
            }
        } else {
            openCamera();
        }
    }

    /**
     * Opens the device camera to take a photo.
     */
    private void openCamera() {
        cameraImageUri = ImageCaptureUtil.openCamera(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == ImageCaptureUtil.CAMERA_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Toast.makeText(getContext(),
                        "Camera permission is required to take photos",
                        Toast.LENGTH_LONG).show();
                if (!shouldShowRequestPermissionRationale(android.Manifest.permission.CAMERA)) {
                    ImageCaptureUtil.showPermissionPermanentlyDeniedDialog(this);
                }
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            Uri uri = null;
            if (requestCode == ImageCaptureUtil.PICK_IMAGE_REQUEST && data != null) {
                uri = data.getData();
            } else if (requestCode == ImageCaptureUtil.CAMERA_REQUEST) {
                uri = cameraImageUri;
            }
            if (uri != null) {
                this.imageUri = uri.toString();
                imageAttachment.setImageURI(uri);
                Log.d(TAG, "Image selected/captured: " + uri.toString());
            } else {
                Log.e(TAG, "Image URI is null");
            }
        }
    }

    /**
     * Validates input fields and initiates the process of saving a new emotion post.
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
                            NetworkMonitor networkMonitor = new NetworkMonitor(getContext());
                            if (!networkMonitor.isConnected()) {
                                handleOfflineImageUpload(processedUri, emotion, explanation,
                                        location, finalSocialSituation, username);
                            } else {
                                uploadImage(processedUri, emotion, explanation, location,
                                        finalSocialSituation, username);
                            }
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
     * Handles offline image upload by saving the image locally and creating an emotion post with a pending flag.
     */
    private void handleOfflineImageUpload(Uri imageUri, String emotion, String explanation,
                                          String location, String socialSituation, String username) {
        try {
            File cacheDir = getContext().getCacheDir();
            File localImageFile = new File(cacheDir,
                    "offline_image_" + System.currentTimeMillis() + ".jpg");
            InputStream inputStream = getContext().getContentResolver().openInputStream(imageUri);
            FileOutputStream outputStream = new FileOutputStream(localImageFile);
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            inputStream.close();
            outputStream.close();
            Uri localUri = Uri.fromFile(localImageFile);
            EmotionPost post = EmotionPost.create(emotion, explanation, localUri.toString(),
                    location, socialSituation, username);
            post.setOfflineImagePending(true);
            emotionPostController.createPostWithOfflineSupport(
                    getContext(),
                    post,
                    (DocumentReference docRef) -> {
                        Toast.makeText(getContext(),
                                "Post saved locally and will upload when online",
                                Toast.LENGTH_SHORT).show();
                        NavController navController = NavHostFragment.findNavController(this);
                        navController.popBackStack(R.id.navigation_home, false);
                    },
                    e -> {
                        Toast.makeText(getContext(), "Error saving post: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Error saving post", e);
                    });
        } catch (IOException e) {
            Log.e(TAG, "Error saving image locally", e);
            Toast.makeText(getContext(), "Failed to save image locally: " + e.getMessage(),
                    Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Uploads an image using ImageHelper and creates an emotion post upon successful upload.
     */
    private void uploadImage(Uri imageUri, String emotion, String explanation,
                             String location, String socialSituation, String username) {
        ImageHelper.uploadImage(imageUri,
                downloadUri -> createEmotionPost(emotion, explanation, downloadUri.toString(),
                        location, socialSituation, username),
                e -> {
                    Toast.makeText(getContext(), "Upload failed: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Image upload failed", e);
                });
    }

    /**
     * Creates an EmotionPost object and saves it to Firestore using the EmotionPostController.
     */
    private void createEmotionPost(String emotion, String explanation, String imageUrl,
                                   String location, String socialSituation, String username) {
        try {
            EmotionPost post = EmotionPost.create(emotion, explanation, imageUrl,
                    location, socialSituation, username);
            Log.d(TAG, "Saving mood event: " + post.toString());
            emotionPostController.createPostWithOfflineSupport(
                    getContext(),
                    post,
                    (DocumentReference docRef) -> {
                        String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
                        if (docRef != null) {
                            int incrementAmount = calculateKarmaIncrement(post);
                            usernameController.incrementKarma(email,
                                    aVoid -> Log.d(TAG, "User karma incremented by " + incrementAmount),
                                    e -> Log.e(TAG, "Error incrementing karma", e),
                                    incrementAmount);
                            usernameController.incrementPostCount(email,
                                    aVoid -> {
                                        usernameController.updateDailyBadge(email,
                                                aVoid2 -> Log.d(TAG, "Daily badge updated for " + email),
                                                e -> Log.e(TAG, "Failed to update daily badge", e));
                                    },
                                    e -> Log.e(TAG, "Failed to update post count", e));
                        }
                        Toast.makeText(getContext(),
                                docRef != null ? "Post created successfully!" : "Post will be uploaded when online",
                                Toast.LENGTH_SHORT).show();
                        NavController navController = NavHostFragment.findNavController(this);
                        navController.popBackStack(R.id.navigation_home, false);
                    },
                    e -> {
                        Toast.makeText(getContext(), "Error creating post: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Error creating post", e);
                    });
        } catch (IllegalArgumentException e) {
            Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private int calculateKarmaIncrement(EmotionPost post) {
        int karma = 0;
        if (post.getImageUri() == null) {
            karma += 5;
        } else {
            karma += 10;
            if (post.getLocation() != null && !post.getLocation().isEmpty()) {
                karma += 5;
            }
            if (post.getExplanation() != null && !post.getExplanation().isEmpty()) {
                karma += 5;
            }
            if (post.getSocialSituation() != null && !post.getSocialSituation().isEmpty()) {
                karma += 5;
            }
        }
        return karma;
    }
}
