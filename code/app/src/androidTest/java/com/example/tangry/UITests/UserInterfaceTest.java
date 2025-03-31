package com.example.tangry.UITests;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.pressBack;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.clearText;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.os.SystemClock;
import android.util.Log;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.example.tangry.MainActivity;
import com.example.tangry.R;
import com.example.tangry.models.EmotionPost;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class UserInterfaceTest {

    private static final String projectId = "tangry-7f852";
    private static final String TEST_EMAIL = "user1@test.com";
    private static final String TEST_PASSWORD = "adminadmin";

    @Rule
    public ActivityScenarioRule<MainActivity> scenario =
            new ActivityScenarioRule<>(MainActivity.class);

    @BeforeClass
    public static void setup() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        auth.useEmulator("10.0.2.2", 9099);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.useEmulator("10.0.2.2", 8080);
        CollectionReference emotionRef = db.collection("emotions");
        CollectionReference usersRef = db.collection("users");

        try {
            // Create test user
            Tasks.await(auth.createUserWithEmailAndPassword(TEST_EMAIL, TEST_PASSWORD));
            FirebaseUser user = auth.getCurrentUser();

            if (user != null) {
                UserProfileChangeRequest profileUpdate = new UserProfileChangeRequest.Builder()
                        .setDisplayName("user1")
                        .build();
                Tasks.await(user.updateProfile(profileUpdate));
                Log.d("SETUP", "User created with display name: " + user.getDisplayName());
            }

            Map<String, Object> data = new HashMap<>();
            data.put("username", "user1");
            data.put("email", TEST_EMAIL);
            usersRef.document().set(data);

            // Seed posts for user1
            emotionRef.document().set(EmotionPost.create("Happiness", "Great day!", null, "Home", "Alone", "user1"));
            emotionRef.document().set(EmotionPost.create("Sadness", "Not feeling good", null, "Office", "With one other person", "user1"));
            emotionRef.document().set(EmotionPost.create("Angry", "Frustrated with work", null, "Café", "With a crowd", "user1"));

        } catch (Exception e) {
            Log.e("SETUP", "Error creating test user or seeding posts", e);
        }
    }

    @Before
    public void Login() {
        SystemClock.sleep(1000);
        onView(withId(R.id.editTextUsername)).perform(typeText(TEST_EMAIL));
        onView(withId(R.id.editTextPassword)).perform(typeText(TEST_PASSWORD));
        onView(withId(R.id.buttonLogin)).perform(click());
        SystemClock.sleep(2000);
    }

    @AfterClass
    public static void teardown() {
        try {
            // DELETE Firestore documents
            URL firestoreUrl = new URL("http://10.0.2.2:8080/emulator/v1/projects/" + projectId + "/databases/(default)/documents");
            HttpURLConnection firestoreConn = (HttpURLConnection) firestoreUrl.openConnection();
            firestoreConn.setRequestMethod("DELETE");
            firestoreConn.getResponseCode();
            firestoreConn.disconnect();

            // DELETE Auth users
            URL authUrl = new URL("http://10.0.2.2:9099/emulator/v1/projects/" + projectId + "/accounts");
            HttpURLConnection authConn = (HttpURLConnection) authUrl.openConnection();
            authConn.setRequestMethod("DELETE");
            authConn.getResponseCode();
            authConn.disconnect();

        } catch (IOException e) {
            Log.e("Teardown", Objects.requireNonNull(e.getMessage()));
        }
    }

    @Test
    public void testAddEditDeletePost() {
        addMoodPost();
        editMoodPost();
        deleteMoodPost();
    }

    private void addMoodPost() {
        SystemClock.sleep(1000);
        onView(withId(R.id.fab)).perform(click());
        onView(withText("Angry")).perform(click());
        onView(withId(R.id.explanation_input)).perform(typeText("Testing adding post"));
        onView(withId(R.id.location_input)).perform(typeText("Work Desk"));
        onView(withId(R.id.social_situation_spinner)).perform(click());
        onView(withText("Alone")).perform(click());
        onView(withId(R.id.save_button)).perform(scrollTo(), click());
        SystemClock.sleep(1500);
        onView(withText("Testing adding post")).check(matches(isDisplayed()));
    }

    private void editMoodPost() {
        SystemClock.sleep(1000);
        onView(withText("Testing adding post")).perform(click());
        SystemClock.sleep(1000);
        onView(withId(R.id.edit_button)).perform(click());
        onView(withText("Fear")).perform(click());
        onView(withId(R.id.explanation_input)).perform(clearText(), typeText("Edited post"));
        onView(withText("Update")).perform(scrollTo(), click());
        onView(withText("Edited post")).check(matches(isDisplayed()));
        onView(withText("Fear")).check(matches(isDisplayed()));
    }

    private void deleteMoodPost() {
        SystemClock.sleep(1000);
        onView(withText("Edited post")).perform(click());
        onView(withId(R.id.delete_button)).perform(click());
        onView(withText("Delete")).perform(click());
        onView(withText("Edited post")).check(doesNotExist());
    }

    @Test
    public void testCommentSubmission() {
        SystemClock.sleep(3000);
        onView(withText("Great day!")).perform(click());
        onView(withId(R.id.comment_input)).perform(typeText("Nice one!"));
        onView(withId(R.id.comment_submit_button)).perform(click());
        SystemClock.sleep(3000);
        onView(withText("Nice one!")).check(matches(isDisplayed()));
        onView(withId(R.id.comment_username)).check(matches(withText("user1")));
    }

    @Test
    public void testViewPostDetails() {
        SystemClock.sleep(3000);
        onView(withText("Not feeling good")).perform(click());
        SystemClock.sleep(3000);
        onView(withId(R.id.reason_text)).check(matches(withText("Not feeling good")));
        onView(withId(R.id.location_text)).check(matches(withText("Office")));
    }

    @Test
    public void testLeaderboardUpdates() {
        SystemClock.sleep(3000);
        onView(withId(R.id.navigation_leaderboard)).perform(click());
        onView(withText("user1")).check(matches(isDisplayed()));
    }
}
