package com.example.tangry;

import static org.junit.Assert.*;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.Observer;

import com.example.tangry.models.EmotionPost;
import com.example.tangry.repositories.EmotionPostRepository;
import com.example.tangry.test.EmulatorTestHelper;
import com.example.tangry.ui.create_user.CreateUserViewModel;
import com.google.firebase.firestore.*;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

public class LeaderboardBadgeEmulatorTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    private FirebaseFirestore db;
    private EmotionPostRepository repository;
    private CreateUserViewModel createUserViewModel;
    private List<String> testUserDocIds = new ArrayList<>();
    private List<String> testEmotionPostIds = new ArrayList<>();

    @BeforeClass
    public static void setupClass() {
        // Configure Firebase to use the emulator
        EmulatorTestHelper.useFirebaseEmulators();
    }

    @Before
    public void setup() {
        db = FirebaseFirestore.getInstance();
        repository = new EmotionPostRepository(db, "emotions");
        createUserViewModel = new CreateUserViewModel();
    }

    @After
    public void cleanup() throws InterruptedException {
        // Clean up test users
        for (String docId : testUserDocIds) {
            CountDownLatch latch = new CountDownLatch(1);
            db.collection("users").document(docId).delete()
                    .addOnCompleteListener(task -> latch.countDown());
            latch.await();
        }
        // Clean up test posts
        for (String postId : testEmotionPostIds) {
            CountDownLatch latch = new CountDownLatch(1);
            db.collection("emotions").document(postId).delete()
                    .addOnCompleteListener(task -> latch.countDown());
            latch.await();
        }
        testUserDocIds.clear();
        testEmotionPostIds.clear();
    }

    /**
     * Helper method to create a user via CreateUserViewModel.
     * Uses the pattern:
     *   createUserViewModel.createUser(email, "password123", "password123", username);
     *   createUserViewModel.getMessage().observeForever(s -> latch.countDown());
     *
     * Then queries Firestore for the created user (by username).
     *
     * @return the DocumentReference for the created user.
     */
    private DocumentReference createUserViaViewModel(String email, String username) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        createUserViewModel.createUser(email, "password123", "password123", username);
        createUserViewModel.getMessage().observeForever(new Observer<String>() {
            @Override
            public void onChanged(String s) {
                latch.countDown();
            }
        });
        latch.await();

        // Query Firestore for the user document by username.
        CountDownLatch queryLatch = new CountDownLatch(1);
        AtomicReference<DocumentReference> userDocRef = new AtomicReference<>();
        db.collection("users").whereEqualTo("username", username).get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.getDocuments().isEmpty()) {
                        userDocRef.set(querySnapshot.getDocuments().get(0).getReference());
                    }
                    queryLatch.countDown();
                })
                .addOnFailureListener(e -> queryLatch.countDown());
        queryLatch.await();
        assertNotNull("User document should be created via ViewModel", userDocRef.get());
        testUserDocIds.add(userDocRef.get().getId());
        return userDocRef.get();
    }

    /**
     * Test: Create a user via the view model, then simulate 3 emotion post submissions
     * to update postCount and award a gold badge.
     */
    @Test
    public void testGoldBadgeAwardViaPostSubmissions() throws InterruptedException {
        DocumentReference userDoc = createUserViaViewModel("badgeTestUser@example.com", "badgeTestUser");

        for (int i = 1; i <= 3; i++) {
            EmotionPost post = EmotionPost.create(
                    "Happiness",
                    "Test post " + i,
                    null,
                    "Location",
                    "Alone",
                    "badgeTestUser");

            CountDownLatch postLatch = new CountDownLatch(1);
            AtomicReference<DocumentReference> postRef = new AtomicReference<>();
            repository.saveEmotionPostToFirestore(post, docRef -> {
                postRef.set(docRef);
                testEmotionPostIds.add(docRef.getId());
                postLatch.countDown();
            }, error -> {
                fail("Post save error: " + error);
                postLatch.countDown();
            });
            postLatch.await();
            assertNotNull("Post document should exist", postRef.get());

            CountDownLatch updateLatch = new CountDownLatch(1);
            userDoc.update("postCount", FieldValue.increment(1))
                    .addOnCompleteListener(task -> updateLatch.countDown());
            updateLatch.await();
            if (i % 3 == 0) {
                CountDownLatch badgeLatch = new CountDownLatch(1);
                userDoc.update("badges.goldBadges", FieldValue.increment(1))
                        .addOnCompleteListener(task -> badgeLatch.countDown());
                badgeLatch.await();
            }
        }

        CountDownLatch getLatch = new CountDownLatch(1);
        AtomicReference<DocumentSnapshot> userSnapshotRef = new AtomicReference<>();
        userDoc.get().addOnSuccessListener(snapshot -> {
            userSnapshotRef.set(snapshot);
            getLatch.countDown();
        }).addOnFailureListener(e -> {
            fail("Failed to get user document: " + e);
            getLatch.countDown();
        });
        getLatch.await();

        DocumentSnapshot snapshot = userSnapshotRef.get();
        assertNotNull("Snapshot should not be null", snapshot);
        assertEquals("Post count should be 3", 3L, snapshot.getLong("postCount").longValue());
        assertEquals("Gold badge count should be 1", 1L, snapshot.getLong("badges.goldBadges").longValue());
    }

    /**
     * Test: Create a user via the view model and simulate 3 post operations,
     * ensuring the gold badge is awarded once.
     */
    @Test
    public void testGoldBadgeAwardAfterThreePosts() throws InterruptedException {
        DocumentReference userDoc = createUserViaViewModel("testUser1@example.com", "testUser1");

        for (int i = 1; i <= 3; i++) {
            CountDownLatch updateLatch = new CountDownLatch(1);
            userDoc.update("postCount", FieldValue.increment(1))
                    .addOnCompleteListener(task -> updateLatch.countDown());
            updateLatch.await();
            if (i % 3 == 0) {
                CountDownLatch badgeLatch = new CountDownLatch(1);
                userDoc.update("badges.goldBadges", FieldValue.increment(1))
                        .addOnCompleteListener(task -> badgeLatch.countDown());
                badgeLatch.await();
            }
        }

        CountDownLatch getLatch = new CountDownLatch(1);
        AtomicReference<DocumentSnapshot> snapshotRef = new AtomicReference<>();
        userDoc.get().addOnSuccessListener(snapshot -> {
            snapshotRef.set(snapshot);
            getLatch.countDown();
        }).addOnFailureListener(e -> {
            fail("Failed to get user document: " + e);
            getLatch.countDown();
        });
        getLatch.await();

        DocumentSnapshot snapshot = snapshotRef.get();
        assertNotNull("Snapshot should not be null", snapshot);
        assertEquals("Gold badge count should be 1 after 3 posts", 1L, snapshot.getLong("badges.goldBadges").longValue());
    }

    /**
     * Test: Create a user via the view model and simulate 6 post operations,
     * ensuring the gold badge is awarded twice.
     */
    @Test
    public void testGoldBadgeAwardAfterSixPosts() throws InterruptedException {
        DocumentReference userDoc = createUserViaViewModel("testUser2@example.com", "testUser2");

        for (int i = 1; i <= 6; i++) {
            CountDownLatch updateLatch = new CountDownLatch(1);
            userDoc.update("postCount", FieldValue.increment(1))
                    .addOnCompleteListener(task -> updateLatch.countDown());
            updateLatch.await();
            if (i % 3 == 0) {
                CountDownLatch badgeLatch = new CountDownLatch(1);
                userDoc.update("badges.goldBadges", FieldValue.increment(1))
                        .addOnCompleteListener(task -> badgeLatch.countDown());
                badgeLatch.await();
            }
        }

        CountDownLatch getLatch = new CountDownLatch(1);
        AtomicReference<DocumentSnapshot> snapshotRef = new AtomicReference<>();
        userDoc.get().addOnSuccessListener(snapshot -> {
            snapshotRef.set(snapshot);
            getLatch.countDown();
        }).addOnFailureListener(e -> {
            fail("Failed to get user document: " + e);
            getLatch.countDown();
        });
        getLatch.await();

        DocumentSnapshot snapshot = snapshotRef.get();
        assertNotNull("Snapshot should not be null", snapshot);
        assertEquals("Gold badge count should be 2 after 6 posts", 2L, snapshot.getLong("badges.goldBadges").longValue());
    }

    /**
     * Test: Simulate adding daily badge dates.
     * Uses the nested field "badges.dailyBadgeDates" and verifies unique addition.
     */
    @Test
    public void testDailyBadgeCount() throws InterruptedException {
        DocumentReference userDoc = createUserViaViewModel("testUser3@example.com", "testUser3");

        for (int i = 0; i < 3; i++) {
            CountDownLatch updateLatch = new CountDownLatch(1);
            userDoc.update("badges.dailyBadgeDates", FieldValue.arrayUnion(new Date()))
                    .addOnCompleteListener(task -> updateLatch.countDown());
            updateLatch.await();
        }

        CountDownLatch getLatch = new CountDownLatch(1);
        AtomicReference<DocumentSnapshot> snapshotRef = new AtomicReference<>();
        userDoc.get().addOnSuccessListener(snapshot -> {
            snapshotRef.set(snapshot);
            getLatch.countDown();
        }).addOnFailureListener(e -> {
            fail("Failed to get user document: " + e);
            getLatch.countDown();
        });
        getLatch.await();

        DocumentSnapshot snapshot = snapshotRef.get();
        assertNotNull("Snapshot should not be null", snapshot);
        List<?> dailyBadgeDates = (List<?>) snapshot.get("badges.dailyBadgeDates");
        assertNotNull("badges.dailyBadgeDates field should not be null", dailyBadgeDates);
        // With arrayUnion, if Date objects are equal then only one unique entry is stored.
        assertEquals("Daily badge count should equal the number of unique dates", 1, dailyBadgeDates.size());
    }

    /**
     * Test: Verify that the leaderboard query orders users correctly based on karma.
     * Here, we create users via the view model and then update their Firestore documents with the desired values.
     */
    @Test
    public void testLeaderboardOrdering() throws InterruptedException {
        // Create three users via the view model.
        DocumentReference userDoc1 = createUserViaViewModel("user1@example.com", "user1");
        DocumentReference userDoc2 = createUserViaViewModel("user2@example.com", "user2");
        DocumentReference userDoc3 = createUserViaViewModel("user3@example.com", "user3");

        // Now update each user with desired karma and badge values.
        CountDownLatch updateLatch = new CountDownLatch(3);
        userDoc1.update("karma", 50, "badges.goldBadges", 2, "badges.silverBadges", 1, "badges.dailyBadgeDates", new ArrayList<Date>())
                .addOnCompleteListener(task -> updateLatch.countDown());
        userDoc2.update("karma", 60, "badges.goldBadges", 1, "badges.silverBadges", 2, "badges.dailyBadgeDates", new ArrayList<Date>())
                .addOnCompleteListener(task -> updateLatch.countDown());
        userDoc3.update("karma", 55, "badges.goldBadges", 1, "badges.silverBadges", 1, "badges.dailyBadgeDates", new ArrayList<Date>())
                .addOnCompleteListener(task -> updateLatch.countDown());
        updateLatch.await();

        // Query users ordered by karma descending.
        CountDownLatch queryLatch = new CountDownLatch(1);
        AtomicReference<QuerySnapshot> querySnapshotRef = new AtomicReference<>();
        db.collection("users").orderBy("karma", Query.Direction.DESCENDING).limit(10)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    querySnapshotRef.set(querySnapshot);
                    queryLatch.countDown();
                })
                .addOnFailureListener(e -> {
                    fail("Failed to query users: " + e);
                    queryLatch.countDown();
                });
        queryLatch.await();

        QuerySnapshot snapshot = querySnapshotRef.get();
        List<DocumentSnapshot> docs = snapshot.getDocuments();

        assertTrue("Should have at least 3 users", docs.size() >= 3);
        // Expected order: user2 (60), user3 (55), user1 (50)
        assertEquals("user2", docs.get(0).getString("username"));
        assertEquals("user3", docs.get(1).getString("username"));
        assertEquals("user1", docs.get(2).getString("username"));
    }
}
