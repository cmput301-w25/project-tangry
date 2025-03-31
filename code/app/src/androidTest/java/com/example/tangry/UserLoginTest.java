package com.example.tangry;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.Observer;

import com.example.tangry.test.EmulatorTestHelper;
import com.example.tangry.ui.login.LoginViewModel;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Tests for user login using LoginViewModel.
 * Ensures that:
 * - An empty email or password results in a failed login.
 * - A successful login updates the LiveData accordingly.
 * - A failed login (e.g., due to a wrong password) does not mark the user as logged in.
 */
public class UserLoginTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    private LoginViewModel viewModel;

    /**
     * Helper method to wait for asynchronous LiveData updates.
     *
     * @param latch CountDownLatch used to wait for the update.
     * @throws InterruptedException if waiting is interrupted.
     */
    private void awaitLatch(CountDownLatch latch) throws InterruptedException {
        latch.await(5, TimeUnit.SECONDS);
    }

    @Before
    public void setUp() throws Exception {
        // Ensure Firebase emulators are used
        EmulatorTestHelper.useFirebaseEmulators();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        // Create the user asynchronously
        String email = "testuser@test.com";
        String password = "password123";
        Task<AuthResult> task = auth.createUserWithEmailAndPassword(email, password);
        Tasks.await(task, 5, TimeUnit.SECONDS);
        if (!task.isSuccessful()) {
            throw task.getException();
        }
        viewModel = new LoginViewModel();
        auth.signOut();
    }

    @After
    public void tearDown() throws Exception {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        // If no user is currently signed in, sign in with the test user's credentials.
        if (auth.getCurrentUser() == null) {
            Tasks.await(auth.signInWithEmailAndPassword("testuser@test.com", "password123"), 5, TimeUnit.SECONDS);
        }
        // Delete the currently signed-in test user.
        if (auth.getCurrentUser() != null) {
            Tasks.await(auth.getCurrentUser().delete(), 5, TimeUnit.SECONDS);
        }
        auth.signOut();
    }

    @Test
    public void testEmptyEmailLogin() throws InterruptedException, FirebaseAuthUserCollisionException {
        CountDownLatch latch = new CountDownLatch(1);
        viewModel.setEmail("");
        viewModel.setPassword("password123");
        viewModel.login();
        viewModel.getIsLoggedIn().observeForever(new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean loggedIn) {
                latch.countDown();
            }
        });
        awaitLatch(latch);
        Boolean loggedIn = viewModel.getIsLoggedIn().getValue();
        assertNotNull(loggedIn);
        // Expect login to fail with empty email.
        assertFalse(loggedIn);
    }

    @Test
    public void testEmptyPasswordLogin() throws InterruptedException, FirebaseAuthUserCollisionException{
        CountDownLatch latch = new CountDownLatch(1);
        viewModel.setEmail("test@example.com");
        viewModel.setPassword("");
        viewModel.login();
        viewModel.getIsLoggedIn().observeForever(new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean loggedIn) {
                latch.countDown();
            }
        });
        awaitLatch(latch);
        Boolean loggedIn = viewModel.getIsLoggedIn().getValue();
        assertNotNull(loggedIn);
        // Expect login to fail with empty password.
        assertFalse(loggedIn);
    }

    @Test
    public void testSuccessfulLogin() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        String email = "testuser@test.com";
        String password = "password123";
        viewModel.setEmail(email);
        viewModel.setPassword(password);

        // Use an observer that captures the first successful login state
        final boolean[] loginResult = {false};
        final Observer<Boolean>[] observerHolder = new Observer[1];
        observerHolder[0] = new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean loggedIn) {
                if (loggedIn != null && loggedIn) {
                    loginResult[0] = true;
                    latch.countDown();
                    viewModel.getIsLoggedIn().removeObserver(this);
                }
            }
        };
        viewModel.getIsLoggedIn().observeForever(observerHolder[0]);

        viewModel.login();
        awaitLatch(latch);
        assertTrue("Expected successful login", loginResult[0]);
    }

    @Test
    public void testFailedLogin() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        // Attempt login with an incorrect password.
        viewModel.setEmail("unique@example.com");
        viewModel.setPassword("wrongpassword");
        viewModel.login();
        viewModel.getIsLoggedIn().observeForever(new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean loggedIn) {
                latch.countDown();
            }
        });
        awaitLatch(latch);
        Boolean loggedIn = viewModel.getIsLoggedIn().getValue();
        assertNotNull(loggedIn);
        // Expect login to fail with wrong credentials.
        assertFalse("Expected login failure due to wrong password", loggedIn);
    }
}
