package com.example.tangry.UITests;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
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
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutionException;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class LoginUITest {

    private static final String TEST_EMAIL = "logintest@example.com";
    private static final String TEST_PASSWORD = "password123";
    private static final String TEST_USERNAME = "testloginuser";
    private static final String WRONG_PASSWORD = "wrongpassword";
    private static final String projectId = "tangry-7f852";

    @Rule
    public ActivityScenarioRule<MainActivity> scenario = new ActivityScenarioRule<>(MainActivity.class);

    @BeforeClass
    public static void setup() {
        // Configure Firebase to use emulators
        FirebaseAuth auth = FirebaseAuth.getInstance();
        auth.useEmulator("10.0.2.2", 9099);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.useEmulator("10.0.2.2", 8080);

        try {
            // Create a test user for login tests
            Tasks.await(auth.createUserWithEmailAndPassword(TEST_EMAIL, TEST_PASSWORD));
            FirebaseUser user = auth.getCurrentUser();
            if (user != null) {
                UserProfileChangeRequest profileUpdate = new UserProfileChangeRequest.Builder()
                        .setDisplayName(TEST_USERNAME)
                        .build();
                Tasks.await(user.updateProfile(profileUpdate));
                Log.d("LOGIN_TEST_SETUP", "Test user created with display name: " + user.getDisplayName());
            }

            // Sign out after creating the test user
            auth.signOut();
        } catch (Exception e) {
            Log.e("LOGIN_TEST_SETUP", "Error creating test user", e);
        }
    }

    @After
    public void cleanup() {
        try {
            FirebaseAuth.getInstance().signOut();
        } catch (Exception e) {
            // Ignore cleanup errors
        }
    }

    @Test
    public void testSuccessfulLogin() {
        // Wait for UI to load
        SystemClock.sleep(1000);

        // Enter valid credentials
        onView(withId(R.id.editTextUsername)).perform(typeText(TEST_EMAIL), closeSoftKeyboard());
        onView(withId(R.id.editTextPassword)).perform(typeText(TEST_PASSWORD), closeSoftKeyboard());

        // Click login button
        onView(withId(R.id.buttonLogin)).perform(click());

        // Wait for login to complete and main screen to load
        SystemClock.sleep(3000);

        // Verify we're logged in by checking for the floating action button in the main
        // activity
        onView(withId(R.id.fab)).check(matches(isDisplayed()));
    }

    @Test
    public void testLoginWithIncorrectPassword() {
        // Wait for UI to load
        SystemClock.sleep(1000);

        // Enter email with wrong password
        onView(withId(R.id.editTextUsername)).perform(typeText(TEST_EMAIL), closeSoftKeyboard());
        onView(withId(R.id.editTextPassword)).perform(typeText(WRONG_PASSWORD), closeSoftKeyboard());

        // Click login button
        onView(withId(R.id.buttonLogin)).perform(click());

        // Wait for response
        SystemClock.sleep(2000);

        // Verify we're still on login screen by checking for the login button
        onView(withId(R.id.buttonLogin)).check(matches(isDisplayed()));
    }

    @Test
    public void testLoginWithNonExistentUser() {
        // Wait for UI to load
        SystemClock.sleep(1000);

        // Enter non-existent email
        String nonExistentEmail = "nonexistent@example.com";
        onView(withId(R.id.editTextUsername)).perform(typeText(nonExistentEmail), closeSoftKeyboard());
        onView(withId(R.id.editTextPassword)).perform(typeText(TEST_PASSWORD), closeSoftKeyboard());

        // Click login button
        onView(withId(R.id.buttonLogin)).perform(click());

        // Wait for response
        SystemClock.sleep(2000);

        // Verify we're still on login screen
        onView(withId(R.id.buttonLogin)).check(matches(isDisplayed()));
    }

    @Test
    public void testLoginWithEmptyFields() {
        // Wait for UI to load
        SystemClock.sleep(1000);

        // Click login button without entering any credentials
        onView(withId(R.id.buttonLogin)).perform(click());

        // Wait for response
        SystemClock.sleep(1000);

        // Verify we're still on login screen
        onView(withId(R.id.buttonLogin)).check(matches(isDisplayed()));
    }

    @Test
    public void testNavigationToCreateUser() {
        // Wait for UI to load
        SystemClock.sleep(1000);

        // Click on Create Account button
        onView(withText("CREATE ACCOUNT")).perform(click());

        // Wait for navigation
        SystemClock.sleep(1000);

        // Verify we're on the create user screen by checking for the create account
        // button
        onView(withId(R.id.create_account_button)).check(matches(isDisplayed()));
    }
}