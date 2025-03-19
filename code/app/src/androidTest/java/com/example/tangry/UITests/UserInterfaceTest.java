package com.example.tangry;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.pressBack;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.clearText;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;

import android.os.SystemClock;
import android.util.Log;

import androidx.test.espresso.Espresso;
import androidx.test.espresso.action.ViewActions;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.example.tangry.models.EmotionPost;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class UserInterfaceTest {

    @Rule
    public ActivityScenarioRule<MainActivity> scenario =
            new ActivityScenarioRule<>(MainActivity.class);



    @BeforeClass
    public static void setup() {
        String androidLocalhost = "10.0.2.2";
        FirebaseAuth auth;
        FirebaseFirestore db;
        CollectionReference emotionRef;

        db = FirebaseFirestore.getInstance();
        db.useEmulator(androidLocalhost, 8080);

        emotionRef = db.collection("emotions");


        auth = FirebaseAuth.getInstance();
        auth.useEmulator(androidLocalhost, 9099);

        // Create test user
        auth.createUserWithEmailAndPassword("testing@test.com", "adminadmin")
                .addOnSuccessListener(authResult -> Log.d("Test", "Test user created successfully"))
                .addOnFailureListener(e -> Log.e("Test", "Test user creation failed", e));

        // Seed Database
        emotionRef.document().set(EmotionPost.create("Happiness", "Great day!", null, "Home", "Alone", "User1"))
                .addOnSuccessListener(doc -> Log.d("Test", "Seeded post: Happiness"))
                .addOnFailureListener(e -> Log.e("Test", "Seeding failed", e));

        emotionRef.document().set(EmotionPost.create("Sadness", "Not feeling good", null, "Office", "With one other person", "User2"))
                .addOnSuccessListener(doc -> Log.d("Test", "Seeded post: Sadness"))
                .addOnFailureListener(e -> Log.e("Test", "Seeding failed", e));

        emotionRef.document().set(EmotionPost.create("Angry", "Frustrated with work", null, "Café", "With a crowd", "User3"))
                .addOnSuccessListener(doc -> Log.d("Test", "Seeded post: Angry"))
                .addOnFailureListener(e -> Log.e("Test", "Seeding failed", e));
    }

    @Before
    public void login() {
        SystemClock.sleep(1000);
        onView(withId(R.id.editTextUsername)).perform(typeText("testing@test.com"));
        onView(withId(R.id.editTextPassword)).perform(typeText("adminadmin"));
        onView(withId(R.id.buttonLogin)).perform(click());

        SystemClock.sleep(3000); // Allow UI time to load
    }

    @After
    public void tearDown() {
        String projectId = "tangry-7f852";
        URL url = null;
        try {
            url = new URL("http://10.0.2.2:8080/emulator/v1/projects/" + projectId + "/databases/(default)/documents");
        } catch (MalformedURLException exception) {
            Log.e("URL Error", Objects.requireNonNull(exception.getMessage()));
        }
        HttpURLConnection urlConnection = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("DELETE");
            int response = urlConnection.getResponseCode();
            Log.i("Response Code", "Response Code: " + response);
        } catch (IOException exception) {
            Log.e("IO Error", Objects.requireNonNull(exception.getMessage()));
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
    }

    @Test
    public void testAddEditDeletePost() {
        addMoodPost();
        editMoodPost();
        deleteMoodPost();
    }

    /**
     * Adds a new emotion post and verifies it's added.
     */
    private void addMoodPost() {
        SystemClock.sleep(1000);

        onView(withId(R.id.fab)).perform(click()); // Click "Add Post" button

        onView(withText("Angry")).perform(click()); // Select Emotion
        onView(withId(R.id.explanation_input)).perform(typeText("Testing adding post"));
        onView(withId(R.id.location_input)).perform(typeText("Work Desk"));
        onView(withId(R.id.social_situation_spinner)).perform(click());
        onView(withText("Alone")).perform(click());

        onView(withId(R.id.save_button)).perform(click()); // Save the post

        // Verify that the new post appears
        onView(withText("Testing adding post")).check(matches(isDisplayed()));

        pressBack(); // Go back to Home
    }

    /**
     * Edits an existing emotion post and verifies the update.
     */
    private void editMoodPost() {
        SystemClock.sleep(3000);

        onView(withText("Testing adding post")).perform(click()); // Open the post

        onView(withId(R.id.edit_button)).perform(click()); // Click "Edit"

        onView(withText("Angry")).perform(click()); // Open the post
        onView(withId(R.id.explanation_input)).perform(clearText(), typeText("Edited post"));

        onView(withId(R.id.save_button)).perform(click()); // Save changes

        // Verify the updated post appears
        onView(withText("Edited post")).check(matches(isDisplayed()));
    }

    /**
     * Deletes the post and verifies that it's removed.
     */
    private void deleteMoodPost() {
        SystemClock.sleep(1000);

        onView(withText("Edited post")).perform(click()); // Open the post

        onView(withId(R.id.delete_button)).perform(click()); // Click "Delete"
        onView(withText("Delete")).perform(click()); // Confirm delete

        // Verify the post is no longer displayed
        onView(withText("Edited post")).check(doesNotExist());
    }
}
