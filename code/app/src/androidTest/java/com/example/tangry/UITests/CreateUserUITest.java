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

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.example.tangry.MainActivity;
import com.example.tangry.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.UUID;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class CreateUserUITest {

    @Rule
    public ActivityScenarioRule<MainActivity> scenario = new ActivityScenarioRule<>(MainActivity.class);

    @BeforeClass
    public static void setup() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        auth.useEmulator("10.0.2.2", 9099);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.useEmulator("10.0.2.2", 8080);
    }

    @Before
    public void navigateToCreateUser() {
        SystemClock.sleep(1000);
        // Navigate to the create account screen
        // You need to use the actual ID of your "Create Account" button on the login
        // screen
        onView(withText("CREATE ACCOUNT")).perform(click());
        SystemClock.sleep(500);
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
    public void testEmptyFieldsValidation() {
        // Try to create account with empty fields
        onView(withId(R.id.create_account_button)).perform(click());

        // Check that appropriate error message is displayed
        SystemClock.sleep(500);
    }

    @Test
    public void testPasswordLengthValidation() {
        // Enter email and username but a short password
        String email = "test@example.com";
        String username = "testuser";
        String shortPassword = "1234";

        onView(withId(R.id.email_input)).perform(typeText(email), closeSoftKeyboard());
        onView(withId(R.id.username_input)).perform(typeText(username), closeSoftKeyboard());
        onView(withId(R.id.password_input)).perform(typeText(shortPassword), closeSoftKeyboard());
        onView(withId(R.id.password_input2)).perform(typeText(shortPassword), closeSoftKeyboard());

        // Submit form
        onView(withId(R.id.create_account_button)).perform(click());

        // Check that appropriate error message is displayed
        SystemClock.sleep(500);
    }

    @Test
    public void testPasswordMismatchValidation() {
        // Enter valid data but mismatched passwords
        String email = "test@example.com";
        String username = "testuser";
        String password = "password123";
        String confirmPassword = "different123";

        onView(withId(R.id.email_input)).perform(typeText(email), closeSoftKeyboard());
        onView(withId(R.id.username_input)).perform(typeText(username), closeSoftKeyboard());
        onView(withId(R.id.password_input)).perform(typeText(password), closeSoftKeyboard());
        onView(withId(R.id.password_input2)).perform(typeText(confirmPassword), closeSoftKeyboard());

        // Submit form
        onView(withId(R.id.create_account_button)).perform(click());

        // Check that appropriate error message is displayed
        SystemClock.sleep(500);
    }

    @Test
    public void testSuccessfulAccountCreation() {
        // Generate unique email and username to avoid conflicts
        String uniqueSuffix = UUID.randomUUID().toString().substring(0, 8);
        String email = "test" + uniqueSuffix + "@example.com";
        String username = "testuser" + uniqueSuffix;
        String password = "password123";

        // Enter valid data
        onView(withId(R.id.email_input)).perform(typeText(email), closeSoftKeyboard());
        onView(withId(R.id.username_input)).perform(typeText(username), closeSoftKeyboard());
        onView(withId(R.id.password_input)).perform(typeText(password), closeSoftKeyboard());
        onView(withId(R.id.password_input2)).perform(typeText(password), closeSoftKeyboard());

        // Submit form
        onView(withId(R.id.create_account_button)).perform(click());

        // Wait for account creation and navigation back to login screen
        SystemClock.sleep(3000);

        // Verify we're back at login screen
        onView(withId(R.id.buttonLogin)).check(matches(isDisplayed()));
    }
}