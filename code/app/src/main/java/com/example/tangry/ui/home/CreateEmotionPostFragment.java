package com.example.tangry.ui.home;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
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
import androidx.appcompat.app.AlertDialog;
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
import com.google.firebase.firestore.DocumentSnapshot;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class CreateEmotionPostFragment extends Fragment {
    private static final String TAG = "CreateEmotionPostFragment";
    private static final int PICK_IMAGE_REQUEST = 1;

    private TextView emotionTextView;
    private EditText explanationInput;
    // Use AutoCompleteTextView for location suggestions.
    private AutoCompleteTextView locationInput;
    private Spinner socialSituationSpinner;
    private ImageView imageAttachment;
    private Button saveButton;
    private String imageUri;
    private NavController navController;

    private EmotionPostController emotionPostController;
    private UsernameController usernameController;

    private static final List<String> VALID_SOCIAL_SITUATIONS = Arrays.asList(
            "Select social situation", "Alone", "With one other person", "With two to several people", "With a crowd");

    // Adapter for address suggestions.
    private ArrayAdapter<String> addressAdapter;
    // OkHttp client for network requests.
    private OkHttpClient httpClient = new OkHttpClient();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout. Make sure your layout (e.g., fragment_detail_emotion.xml)
        // contains an AutoCompleteTextView with id "location_input".
        return inflater.inflate(R.layout.fragment_detail_emotion, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);

        emotionTextView = view.findViewById(R.id.emotion_text);
        explanationInput = view.findViewById(R.id.explanation_input);
        // Use AutoCompleteTextView instead of plain EditText for location.
        locationInput = view.findViewById(R.id.location_input);
        socialSituationSpinner = view.findViewById(R.id.social_situation_spinner);
        imageAttachment = view.findViewById(R.id.image_attachment);
        saveButton = view.findViewById(R.id.save_button);

        // Initialize controllers.
        emotionPostController = new EmotionPostController();
        usernameController = new UsernameController();

        // Retrieve passed arguments (if using Safe Args).
        if (getArguments() != null) {
            String emotionText = CreateEmotionPostFragmentArgs.fromBundle(getArguments()).getItemText();
            emotionTextView.setText(emotionText);
        }

        // Populate the social situation spinner.
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_item, VALID_SOCIAL_SITUATIONS);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        socialSituationSpinner.setAdapter(spinnerAdapter);
        socialSituationSpinner.setSelection(0);

        // Set up the adapter for address suggestions.
        addressAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, new ArrayList<>());
        locationInput.setAdapter(addressAdapter);
        locationInput.setThreshold(2); // Start suggesting after 2 characters.

        // Listen to text changes and fetch suggestions.
        locationInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Not needed.
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Not needed.
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() < 2) return;
                String query = s.toString();
                fetchAddressSuggestions(query, suggestions -> {
                    addressAdapter.clear();
                    addressAdapter.addAll(suggestions);
                    addressAdapter.notifyDataSetChanged();
                });
            }
        });

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
                    // Use the text from the AutoCompleteTextView as location.
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
                        Toast.makeText(getContext(), "Mood event saved!", Toast.LENGTH_SHORT).show();
                        navController.popBackStack();
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

    /**
     * Uses the free Nominatim API to fetch address suggestions.
     *
     * @param query    The text to search for.
     * @param callback Callback to deliver the list of suggestion strings.
     */
    private void fetchAddressSuggestions(String query, SuggestionCallback callback) {
        try {
            String encodedQuery = URLEncoder.encode(query, "UTF-8");
            String url = "https://nominatim.openstreetmap.org/search?format=json&addressdetails=1&q=" + encodedQuery;
            Request request = new Request.Builder()
                    .url(url)
                    // Include a custom User-Agent per Nominatim's usage policy.
                    .header("User-Agent", "TangryApp/1.0 (your_email@example.com)")
                    .build();

            httpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    Log.e(TAG, "Nominatim request failed: " + e.getMessage());
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    if (response.isSuccessful()) {
                        String json = response.body().string();
                        try {
                            JSONArray array = new JSONArray(json);
                            List<String> suggestions = new ArrayList<>();
                            for (int i = 0; i < array.length(); i++) {
                                JSONObject obj = array.getJSONObject(i);
                                String displayName = obj.getString("display_name");
                                suggestions.add(displayName);
                            }
                            // Return the suggestions on the main thread.
                            if (getActivity() != null) {
                                getActivity().runOnUiThread(() -> callback.onSuggestionsFetched(suggestions));
                            }
                        } catch (JSONException e) {
                            Log.e(TAG, "JSON parsing error: " + e.getMessage());
                        }
                    }
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error encoding query: " + e.getMessage());
        }
    }

    // Callback interface for address suggestions.
    interface SuggestionCallback {
        void onSuggestionsFetched(List<String> suggestions);
    }
}
