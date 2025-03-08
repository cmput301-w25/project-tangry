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
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import com.bumptech.glide.Glide;
import com.example.tangry.R;
import com.example.tangry.models.EmotionPost;
import com.example.tangry.repositories.EmotionPostRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.gson.Gson;
import java.util.Arrays;
import java.util.List;

public class EditEmotionFragment extends Fragment {
    private static final String TAG = "EditEmotionFragment";
    private static final int PICK_IMAGE_REQUEST = 1;
    private String postId;
    private EmotionPostRepository repository;
    private NavController navController;

    private TextView emotionTextView;
    private EditText explanationInput, locationInput;
    private Spinner socialSituationSpinner;
    private ImageView imageAttachment;
    private Button actionButton;
    private String imageUri;

    private static final List<String> VALID_SOCIAL_SITUATIONS = Arrays.asList("Select social situation", "Alone",
            "With one other person", "With two to several people", "With a crowd");

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_detail_emotion, container, false); // 🔥 Reusing the same XML
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
        actionButton = view.findViewById(R.id.save_button);

        // Populate Spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, VALID_SOCIAL_SITUATIONS);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        socialSituationSpinner.setAdapter(adapter);

        repository = EmotionPostRepository.getInstance();

        // Retrieve Data
        if (getArguments() != null) {
            String postJson = getArguments().getString("postJson");
            postId = getArguments().getString("postId");
            String selectedEmotion = getArguments().getString("selectedEmotion"); // New Emotion

            if (postJson != null) {
                EmotionPost post = new Gson().fromJson(postJson, EmotionPost.class);
                prefillFields(post);
                actionButton.setText("Update");

                // If a new emotion was selected, update the emotion field
                if (selectedEmotion != null) {
                    emotionTextView.setText(selectedEmotion);
                }
            }
        }

        imageAttachment.setOnClickListener(v -> selectImage());
        actionButton.setOnClickListener(v -> updatePost());
    }


    private void prefillFields(EmotionPost post) {
        emotionTextView.setText(post.getEmotion());
        explanationInput.setText(post.getExplanation());
        locationInput.setText(post.getLocation());
        socialSituationSpinner.setSelection(getSpinnerIndex(socialSituationSpinner, post.getSocialSituation()));

        if (post.getImageUri() != null) {
            imageUri = post.getImageUri();
            Glide.with(requireContext()).load(Uri.parse(imageUri)).into(imageAttachment);
        }
    }

    private int getSpinnerIndex(Spinner spinner, String value) {
        for (int i = 0; i < spinner.getCount(); i++) {
            if (spinner.getItemAtPosition(i).toString().equalsIgnoreCase(value)) {
                return i;
            }
        }
        return 0;
    }

    private void updatePost() {
        String explanation = explanationInput.getText().toString().trim();
        String location = locationInput.getText().toString().trim();
        String socialSituation = socialSituationSpinner.getSelectedItem().toString();
        String emotion = emotionTextView.getText().toString();

        if (socialSituation.equals("Select social situation")) {
            Toast.makeText(getContext(), "Please select a valid social situation.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (emotion.isEmpty()) {
            Toast.makeText(getContext(), "Emotion is required.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (explanation.isEmpty() && (imageUri == null || imageUri.isEmpty())) {
            Toast.makeText(getContext(), "Please provide either an explanation or an image.", Toast.LENGTH_SHORT).show();
            return;
        }

        repository.getEmotionPostFromFirestore(postId, existingPost -> {
            if (existingPost == null) {
                Toast.makeText(getContext(), "Failed to retrieve post data.", Toast.LENGTH_SHORT).show();
                return;
            }

            EmotionPost updatedPost = new EmotionPost();
            updatedPost.setEmotion(emotion);
            updatedPost.setExplanation(explanation);
            updatedPost.setLocation(location);
            updatedPost.setSocialSituation(socialSituation);
            updatedPost.setImageUri(imageUri);
            updatedPost.setUsername(existingPost.getUsername());
            updatedPost.setTimestamp(existingPost.getTimestamp());
            updatedPost.setPostId(postId);

            repository.updateEmotionPostInFirestore(postId, updatedPost, () -> {
                Toast.makeText(getContext(), "Post updated!", Toast.LENGTH_SHORT).show();
                navController.popBackStack(R.id.navigation_home, false);
            }, e -> {
                Toast.makeText(getContext(), "Failed to update. " + e.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Failed to update post", e);
            });
        }, e -> {
            Toast.makeText(getContext(), "Failed to fetch post data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Firestore error fetching post", e);
        });
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
            assert uri != null;
            imageUri = uri.toString();
            imageAttachment.setImageURI(uri);
            Log.d(TAG, "Image selected: " + uri.toString());
        }
    }
}
