package com.example.tangry;

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
import com.example.tangry.models.EmotionPost;
import com.example.tangry.repositories.EmotionPostRepository;
import com.example.tangry.repositories.UsernameRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Fragment for adding details to a mood event.
 */
public class DetailEmotionFragment extends Fragment {

    private static final String TAG = "DetailEmotionFragment";
    private static final int PICK_IMAGE_REQUEST = 1;

    private TextView emotionTextView;
    private EditText explanationInput, locationInput;
    private Spinner socialSituationSpinner;
    private ImageView imageAttachment;
    private Button saveButton;
    private String imageUri;
    private NavController navController;
    private EmotionPostRepository repository;

    private static final List<String> VALID_SOCIAL_SITUATIONS = Arrays.asList("Select social situation", "Alone",
            "With one other person", "With two to several people", "With a crowd");

    /**
     * Called to have the fragment instantiate its user interface view.
     *
     * @param inflater           The LayoutInflater object that can be used to
     *                           inflate any views in the fragment.
     * @param container          If non-null, this is the parent view that the
     *                           fragment's UI should be attached to.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     *                           from a previous saved state as given here.
     * @return Return the View for the fragment's UI, or null.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_detail_emotion, container, false);
    }

    /**
     * Called immediately after onCreateView(LayoutInflater, ViewGroup, Bundle) has
     * returned, but before any saved state has been restored in to the view.
     *
     * @param view               The View returned by onCreateView(LayoutInflater,
     *                           ViewGroup, Bundle).
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     *                           from a previous saved state as given here.
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

        repository = EmotionPostRepository.getInstance(); // Get repository instance

        // Get passed argument using Safe Args
        if (getArguments() != null) {
            String emotionText = DetailEmotionFragmentArgs.fromBundle(getArguments()).getItemText();
            emotionTextView.setText(emotionText);
        }

        // Populate the Spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item,
                VALID_SOCIAL_SITUATIONS);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        socialSituationSpinner.setAdapter(adapter);
        socialSituationSpinner.setSelection(0); // Set default selection to the placeholder

        imageAttachment.setOnClickListener(v -> selectImage());
        saveButton.setOnClickListener(v -> saveMoodEvent());
    }

    /**
     * Opens the image picker to select an image.
     */
    private void selectImage() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    /**
     * Called when an activity you launched exits, giving you the requestCode you
     * started it with, the resultCode it returned, and any additional data from it.
     *
     * @param requestCode The integer request code originally supplied to
     *                    startActivityForResult(), allowing you to identify who
     *                    this result came from.
     * @param resultCode  The integer result code returned by the child activity
     *                    through its setResult().
     * @param data        An Intent, which can return result data to the caller
     *                    (various data can be attached to Intent "extras").
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

    private void saveMoodEvent() {
        String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        UsernameRepository.getInstance().getUsernameFromEmail(email, username -> {
            final String emotion = emotionTextView.getText().toString();
            final String explanation = explanationInput.getText().toString().trim();
            final String location = locationInput.getText().toString().trim();
            String socialSituation = socialSituationSpinner.getSelectedItem().toString();
            if (socialSituation.equals("Select social situation")) {
                socialSituation = null;
            }
            final String finalSocialSituation = socialSituation;

            if (imageUri != null) {
                Log.d(TAG, "Image URI: " + imageUri);
                // Upload image to Firebase Storage
                StorageReference storageRef = FirebaseStorage.getInstance().getReference();
                StorageReference imageRef = storageRef.child("images/" + UUID.randomUUID().toString());
                imageRef.putFile(Uri.parse(imageUri))
                        .addOnSuccessListener(taskSnapshot -> {
                            Log.d(TAG, "Image upload successful");
                            imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                                String downloadUrl = uri.toString();
                                Log.d(TAG, "Download URL: " + downloadUrl);
                                createEmotionPost(emotion, explanation, downloadUrl, location, finalSocialSituation,
                                        username);
                            }).addOnFailureListener(e -> {
                                Log.e(TAG, "Failed to get download URL", e);
                                Toast.makeText(getContext(), "Failed to get download URL: " + e.getMessage(),
                                        Toast.LENGTH_SHORT).show();
                            });
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "Failed to upload image", e);
                            Toast.makeText(getContext(), "Failed to upload image: " + e.getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        });
            } else {
                createEmotionPost(emotion, explanation, null, location, finalSocialSituation, username);
            }
        }, e -> {
            Toast.makeText(getContext(), "Failed to get username. " + e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Failed to get username", e);
        });
    }

    private void createEmotionPost(String emotion, String explanation, String imageUrl, String location,
            String socialSituation, String username) {
        try {
            EmotionPost post = EmotionPost.create(emotion, explanation, imageUrl, location, socialSituation, username,
                    null);
            Log.d(TAG, "Saving mood event: " + post.toString());
            repository.saveEmotionPostToFirestore(post,
                    docRef -> {
                        Toast.makeText(getContext(), "Mood event saved to Firestore!", Toast.LENGTH_SHORT).show();
                        navController.navigateUp();
                    },
                    e -> {
                        Toast.makeText(getContext(), "Failed to save. " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Failed to save mood event", e);
                    });
        } catch (IllegalArgumentException | IOException e) {
            Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Error saving mood event", e);
        }
    }
}