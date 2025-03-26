/**
 * PostDetailsFragment.java
 *
 * This fragment is responsible for displaying the details of an emotion post. Users can view the post's
 * content, including text, images, and associated metadata (such as time, location, and social situation).
 * The fragment also allows users to navigate to an editing screen or delete the post. Firestore operations
 * are managed through the EmotionPostController, and image operations (if any) are handled via Firebase Storage.
 *
 * Outstanding Issues:
 * - Further error handling and UI feedback (e.g., progress indicators) could enhance the user experience.
 * - We considering refactoring repetitive UI updates and Firestore operations for improved maintainability.
 */

package com.example.tangry.ui.home;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
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
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.navigation.NavController;

import com.bumptech.glide.Glide;
import com.example.tangry.R;
import com.example.tangry.adapters.CommentAdapter;
import com.example.tangry.controllers.EmotionPostController;
import com.example.tangry.controllers.UserController;
import com.example.tangry.models.Comment;
import com.example.tangry.models.EmotionPost;
import com.example.tangry.utils.NetworkMonitor;
import com.example.tangry.utils.TimeUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

public class PostDetailsFragment extends Fragment {
    private TextView userName, moodText, userHandle, locationText, withText, reasonText, timeText;
    private ImageView moodImage, emojiImage;
    private Button editButton, deleteButton;
    private EmotionPost post;
    private String postId;
    private EmotionPostController emotionPostController;
    private UserController userController;
    private RecyclerView commentsRecyclerView;
    private EditText commentInput;
    private Button commentSubmitButton;
    private CommentAdapter commentAdapter;
    private List<Comment> commentList = new ArrayList<>();

    /**
     * Default empty constructor required for Fragments.
     */
    public PostDetailsFragment() {
        // Required empty constructor
    }

    /**
     * Inflates the fragment layout.
     *
     * @param inflater           The LayoutInflater object used to inflate the view.
     * @param container          The parent view container.
     * @param savedInstanceState Previously saved state, if any.
     * @return The root View for this fragment's UI.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_mood_details, container, false);
    }

    /**
     * Initializes UI components, retrieves post data from arguments, and sets up
     * event listeners.
     *
     * @param view               The root view returned by onCreateView.
     * @param savedInstanceState Previously saved state, if any.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        emotionPostController = new EmotionPostController();
        userController = new UserController();

        // Initialize Views
        userName = view.findViewById(R.id.user_name);
        moodText = view.findViewById(R.id.mood_text);
        userHandle = view.findViewById(R.id.user_handle);
        locationText = view.findViewById(R.id.location_text);
        withText = view.findViewById(R.id.with_text);
        reasonText = view.findViewById(R.id.reason_text);
        timeText = view.findViewById(R.id.time_text);
        moodImage = view.findViewById(R.id.mood_image);
        emojiImage = view.findViewById(R.id.emoji_image);
        editButton = view.findViewById(R.id.edit_button);
        deleteButton = view.findViewById(R.id.delete_button);

        // Retrieve Arguments (Post Data)
        if (getArguments() != null) {
            String postJson = getArguments().getString("post");
            postId = getArguments().getString("postId");
            if (postJson != null) {
                post = new Gson().fromJson(postJson, EmotionPost.class);
                bindPostDetails(post);
            }
        }

        editButton.setOnClickListener(v -> editPost());
        deleteButton.setOnClickListener(v -> confirmDeletePost());

        commentsRecyclerView = view.findViewById(R.id.comments_recycler_view);
        commentInput = view.findViewById(R.id.comment_input);
        commentSubmitButton = view.findViewById(R.id.comment_submit_button);

        commentAdapter = new CommentAdapter(commentList);
        commentsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        commentsRecyclerView.setAdapter(commentAdapter);

        commentsRecyclerView.setNestedScrollingEnabled(false);

        if (post != null && post.getComments() != null) {
            commentList.addAll(post.getComments());
            commentAdapter.notifyDataSetChanged();
        }

        commentSubmitButton.setOnClickListener(v -> {
            String content = commentInput.getText().toString().trim();
            if (!content.isEmpty() && postId != null) {
                String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();

                userController.getUsername(email,
                        username -> {
                            Comment comment = new Comment(username, content);
                            emotionPostController.addCommentToPost(postId, comment,
                                    () -> {
                                        commentList.add(comment);
                                        commentAdapter.notifyItemInserted(commentList.size() - 1);
                                        commentInput.setText("");
                                        int commentKarma = 5;
                                        userController.incrementKarma(email,
                                                aVoid -> Log.d("PostDetails",
                                                        "Karma incremented by " + commentKarma + " for commenting."),
                                                e -> Log.e("PostDetails", "Failed to increment karma after comment", e),
                                                commentKarma);
                                    },
                                    e -> Log.e("PostDetails", "Failed to add comment", e));
                        },
                        e -> {
                            Log.e("PostDetails", "Failed to fetch username", e);
                            // Optional fallback
                            Comment fallbackComment = new Comment("Unknown", content);
                            emotionPostController.addCommentToPost(postId, fallbackComment,
                                    () -> {
                                        commentList.add(fallbackComment);
                                        commentAdapter.notifyItemInserted(commentList.size() - 1);
                                        commentInput.setText("");
                                    },
                                    error -> Log.e("PostDetails", "Failed to add comment (fallback)", error));
                        });
            }
        });

    }

    /**
     * Binds the details of the given emotion post to the UI components.
     *
     * @param post The emotion post object containing details to be displayed.
     */
    private void bindPostDetails(EmotionPost post) {
        userName.setText(post.getUsername() + " feels ");
        moodText.setText(post.getEmotion());
        userHandle.setText("@" + post.getUsername());
        locationText.setText(post.getLocation().isEmpty() ? "No Location" : post.getLocation());
        withText.setText(post.getSocialSituation() == null ? "Not provided" : post.getSocialSituation());
        reasonText.setText(post.getExplanation().isEmpty() ? "No Explanation" : post.getExplanation());
        timeText.setText(TimeUtils.getTimeAgo(post.getTimestamp().toDate()));

        // Load mood image if available
        if (post.getImageUri() != null) {
            Glide.with(requireContext())
                    .load(Uri.parse(post.getImageUri()))
                    .into(moodImage);
        } else {
            moodImage.setVisibility(View.GONE);
        }

        // Set emoji and mood color based on emotion
        setEmojiAndColor(post.getEmotion());
    }

    /**
     * Sets the appropriate emoji and text color based on the emotion string.
     *
     * @param emotion The emotion string from the post.
     */
    private void setEmojiAndColor(String emotion) {
        int emojiResId, colorResId;
        switch (emotion.toLowerCase()) {
            case "happiness":
                emojiResId = R.drawable.ic_happiness;
                colorResId = R.color.colorHappiness;
                break;
            case "sadness":
                emojiResId = R.drawable.ic_sadness;
                colorResId = R.color.colorSadness;
                break;
            case "angry":
                emojiResId = R.drawable.ic_angry;
                colorResId = R.color.colorAngry;
                break;
            case "fear":
                emojiResId = R.drawable.ic_fear;
                colorResId = R.color.colorFear;
                break;
            case "disgust":
                emojiResId = R.drawable.ic_disgust;
                colorResId = R.color.colorDisgust;
                break;
            case "shame":
                emojiResId = R.drawable.ic_shame;
                colorResId = R.color.colorShame;
                break;
            case "surprise":
                emojiResId = R.drawable.ic_surprise;
                colorResId = R.color.colorSurprise;
                break;
            case "confused":
                emojiResId = R.drawable.ic_confused;
                colorResId = R.color.colorConfused;
                break;
            default:
                emojiResId = R.drawable.ic_placeholder;
                colorResId = R.color.colorPrimaryDark;
                break;
        }

        emojiImage.setImageResource(emojiResId);
        moodText.setTextColor(ContextCompat.getColor(requireContext(), colorResId));
    }

    /**
     * Navigates to the edit screen, passing the current post data via arguments.
     */
    private void editPost() {
        if (post != null) {
            Bundle bundle = new Bundle();
            bundle.putString("postJson", new Gson().toJson(post));
            bundle.putString("postId", postId);
            bundle.putBoolean("isEditing", true);

            Navigation.findNavController(requireView())
                    .navigate(R.id.action_postDetailsFragment_to_emotionsFragment, bundle);
        }
    }

    /**
     * Shows a confirmation dialog before deleting the post.
     */
    private void confirmDeletePost() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Post")
                .setMessage("Are you sure you want to delete this post? This action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> deletePost())
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    /**
     * Deletes the post by first attempting to remove the associated image (if any)
     * from Firebase Storage,
     * then deleting the post document from Firestore.
     */
    private void deletePost() {
        if (postId != null) {
            // Check if the post has an associated image
            if (post.getImageUri() != null && !post.getImageUri().isEmpty()) {
                try {
                    StorageReference imageRef = FirebaseStorage.getInstance().getReferenceFromUrl(post.getImageUri());
                    // Delete the image file
                    imageRef.delete().addOnSuccessListener(aVoid -> {
                        Log.d("PostDetails", "Image deleted successfully");
                        // After image deletion, delete the post
                        deletePostFromFirestore();
                    }).addOnFailureListener(e -> {
                        Log.e("PostDetails", "Error deleting image", e);
                        // Even if image deletion fails, try deleting the post
                        deletePostFromFirestore();
                    });
                } catch (IllegalArgumentException e) {
                    Log.e("PostDetails", "Invalid storage URL", e);
                    deletePostFromFirestore();
                }
            } else {
                // No image to delete; directly delete the post
                deletePostFromFirestore();
            }
        }
    }

    /**
     * Deletes the post document from Firestore using the EmotionPostController.
     */
    private void deletePostFromFirestore() {
        // Get a reference to navigation before async operation
        final androidx.navigation.NavController navController = isAdded() && getView() != null
                ? androidx.navigation.Navigation.findNavController(requireView())
                : null;

        // Check network connectivity
        NetworkMonitor networkMonitor = new NetworkMonitor(requireContext());
        boolean isConnected = networkMonitor.isConnected();

        // Pass all required parameters including the EmotionPost object
        emotionPostController.deleteEmotionPostWithOfflineSupport(
                requireContext(),
                postId,
                post, // Pass the EmotionPost object
                () -> {
                    Log.d("PostDetails", "Post deleted successfully");

                    // Show toast message based on connectivity
                    if (!isConnected) {
                        Toast.makeText(requireContext(),
                                "Post deleted locally and will be removed when online",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(requireContext(), "Post deleted successfully",
                                Toast.LENGTH_SHORT).show();
                    }

                    // Check if fragment is still attached and navController is available
                    if (isAdded() && navController != null && getView() != null) {
                        navController.popBackStack(R.id.navigation_home, false);
                    } else {
                        // Handle the case when fragment is already detached
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                getActivity().onBackPressed();
                            });
                        }
                    }
                },
                e -> {
                    Log.e("PostDetails", "Error deleting post", e);
                    Toast.makeText(requireContext(),
                            "Failed to delete post: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }
}
