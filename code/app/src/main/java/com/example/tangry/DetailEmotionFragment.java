package com.example.tangry;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import com.example.tangry.models.EmotionPost;
import com.example.tangry.repositories.EmotionPostRepository;

public class DetailEmotionFragment extends Fragment {

    private static final int PICK_IMAGE_REQUEST = 1;

    private TextView emotionTextView;
    private EditText explanationInput, locationInput, socialSituationInput;
    private ImageView imageAttachment;
    private Button saveButton;
    private Uri imageUri;
    private NavController navController;
    private EmotionPostRepository repository;

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
        socialSituationInput = view.findViewById(R.id.social_situation_input);
        imageAttachment = view.findViewById(R.id.image_attachment);
        saveButton = view.findViewById(R.id.save_button);

        repository = EmotionPostRepository.getInstance(); // Get repository instance

        // Get passed argument using Safe Args
        if (getArguments() != null) {
            String emotionText = DetailEmotionFragmentArgs.fromBundle(getArguments()).getItemText();
            emotionTextView.setText(emotionText);
        }

        // Temporal image placeholder: daniel implem
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
            imageUri = data.getData();
            imageAttachment.setImageURI(imageUri);
        }
    }

    private void saveMoodEvent() {
        String emotion = emotionTextView.getText().toString();
        String explanation = explanationInput.getText().toString().trim();
        String location = locationInput.getText().toString().trim();
        String socialSituation = socialSituationInput.getText().toString().trim();

        try {
            EmotionPost post = EmotionPost.create(emotion, explanation, imageUri, location, socialSituation);
            // Call Firestore here
            repository.saveEmotionPostToFirestore(post,
                    docRef -> {
                        Toast.makeText(getContext(), "Mood event saved to Firestore!", Toast.LENGTH_SHORT).show();
                        navController.navigateUp();
                    },
                    e -> {
                        Toast.makeText(getContext(), "Failed to save. " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } catch (IllegalArgumentException e) {
            Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
