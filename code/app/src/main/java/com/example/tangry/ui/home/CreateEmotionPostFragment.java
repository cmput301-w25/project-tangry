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
import com.example.tangry.controllers.UsernameController;
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
    private UsernameController usernameController;

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
        locationInput = view.findViewById(R.id.location_input);
        socialSituationSpinner = view.findViewById(R.id.social_situation_spinner);
        imageAttachment = view.findViewById(R.id.image_attachment);
        saveButton = view.findViewById(R.id.save_button);

        // Initialize controllers
        emotionPostController = new EmotionPostController();
        usernameController = new UsernameController();

        // Retrieve passed arguments (using Safe Args if applicable)
        if (getArguments() != null) {
            String emotionText = CreateEmotionPostFragmentArgs.fromBundle(getArguments()).getItemText();
            emotionTextView.setText(emotionText);
        }

        // Populate the spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, VALID_SOCIAL_SITUATIONS);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        socialSituationSpinner.setAdapter(adapter);
        socialSituationSpinner.setSelection(0);

        imageAttachment.setOnClickListener(v -> selectImage());
        saveButton.setOnClickListener(v -> saveMoodEvent());
    }

    private void selectImage() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

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

    private void uploadImage(Uri imageUri, String emotion, String explanation,
                             String location, String socialSituation, String username) {
        ImageHelper.uploadImage(imageUri,
                downloadUri -> createEmotionPost(emotion, explanation, downloadUri.toString(), location, socialSituation, username),
                e -> Toast.makeText(getContext(), "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
    private void createEmotionPost(String emotion, String explanation, String imageUrl, String location,
                                   String socialSituation, String username) {
        try {
            EmotionPost post = EmotionPost.create(emotion, explanation, imageUrl, location, socialSituation, username);
            Log.d(TAG, "Saving mood event: " + post.toString());
            emotionPostController.createPost(post,
                    (DocumentReference docRef) -> {
                        Toast.makeText(getContext(), "Mood event saved to Firestore!", Toast.LENGTH_SHORT).show();
                        navController.navigateUp();
                    },
                    e -> {
                        Toast.makeText(getContext(), "Failed to save. " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Failed to save mood event", e);
                    });
        } catch (IllegalArgumentException e) {
            Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Error saving mood event", e);
        }
    }
}
