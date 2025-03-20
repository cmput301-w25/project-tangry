package com.example.tangry;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.Observer;

import com.example.tangry.test.EmulatorTestHelper;
import com.example.tangry.ui.create_user.CreateUserViewModel;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Tests for user creation using CreateUserViewModel.
 * Ensures that:
 * - Emails and usernames are not empty.
 * - Passwords are at least 8 characters.
 * - Passwords must match.
 * - Duplicate usernames and emails are rejected.
 * - A valid set of parameters creates an account successfully.
 */
public class UserCreateTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    private CreateUserViewModel viewModel;

    @Before
    public void setUp() {
        EmulatorTestHelper.useFirebaseEmulators();
        viewModel = new CreateUserViewModel();
    }

    /**
     * Helper method to wait for asynchronous LiveData updates.
     *
     * @param latch CountDownLatch used to wait for the update.
     * @throws InterruptedException if waiting is interrupted.
     */
    private void awaitLatch(CountDownLatch latch) throws InterruptedException {
        latch.await(3, TimeUnit.SECONDS);
    }

    @Test
    public void testEmptyEmail() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        viewModel.createUser("", "password123", "password123", "testuser");
        viewModel.getMessage().observeForever(new Observer<String>() {
            @Override
            public void onChanged(String s) {
                latch.countDown();
            }
        });
        awaitLatch(latch);
        assertEquals("Email is required", viewModel.getMessage().getValue());
    }

    @Test
    public void testEmptyUsername() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        viewModel.createUser("test@example.com", "password123", "password123", "");
        viewModel.getMessage().observeForever(s -> latch.countDown());
        awaitLatch(latch);
        assertEquals("Username is required", viewModel.getMessage().getValue());
    }

    @Test
    public void testPasswordLessThan8Chars() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        viewModel.createUser("test@example.com", "short", "short", "testuser");
        viewModel.getMessage().observeForever(s -> latch.countDown());
        awaitLatch(latch);
        assertEquals("Password must be at least 8 characters", viewModel.getMessage().getValue());
    }

    @Test
    public void testPasswordMismatch() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        viewModel.createUser("test@example.com", "password123", "different", "testuser");
        viewModel.getMessage().observeForever(s -> latch.countDown());
        awaitLatch(latch);
        assertEquals("Passwords do not match", viewModel.getMessage().getValue());
    }

    @Test
    public void testDuplicateUsername() throws InterruptedException, ExecutionException, TimeoutException {
        // Seed Firestore with a record that uses the username.
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> duplicateRecord = new HashMap<>();
        duplicateRecord.put("username", "exists");
        duplicateRecord.put("email", "duplicate@example.com");
        // Use Tasks.await to synchronously insert the duplicate document.
        DocumentReference ref = Tasks.await(
                db.collection("users").add(duplicateRecord),
                5,
                TimeUnit.SECONDS
        );
        assertNotNull(ref);
        CountDownLatch latch = new CountDownLatch(1);
        // Assuming that using "exists" as username triggers duplicate detection.
        viewModel.createUser("test@example.com", "password123", "password123", "exists");
        viewModel.getMessage().observeForever(s -> latch.countDown());
        awaitLatch(latch);
        assertEquals("Username already exists", viewModel.getMessage().getValue());
    }

    @Test
    public void testDuplicateEmail() throws Exception {
        // Seed Firestore with a record that uses the duplicate email.
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> duplicateRecord = new HashMap<>();
        duplicateRecord.put("username", "existingUser");
        duplicateRecord.put("email", "duplicate@example.com");
        // Use Tasks.await to synchronously insert the duplicate document.
        DocumentReference ref = Tasks.await(
                db.collection("users").add(duplicateRecord),
                5,
                TimeUnit.SECONDS
        );
        assertNotNull(ref);

        CountDownLatch latch = new CountDownLatch(1);
        // Now attempt to create a user with the duplicate email.
        viewModel.createUser("duplicate@example.com", "password123", "password123", "newuser");
        viewModel.getMessage().observeForever(s -> latch.countDown());
        awaitLatch(latch);
        assertEquals("Email already in use", viewModel.getMessage().getValue());
    }

    @Test
    public void testSuccessfulUserCreation() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        // Using unique values should lead to a successful account creation.
        viewModel.createUser("unique@example.com", "password123", "password123", "uniqueuser");
        viewModel.getAccountCreated().observeForever(created -> latch.countDown());
        awaitLatch(latch);
        Boolean created = viewModel.getAccountCreated().getValue();
        String message = viewModel.getMessage().getValue();
        assertNotNull(created);
        assertTrue(created);
        assertEquals("Account created successfully. Please login", message);
    }
}