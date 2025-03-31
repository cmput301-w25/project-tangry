package com.example.tangry;

import static org.junit.Assert.*;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.tangry.models.EmotionPost;
import com.example.tangry.repositories.EmotionPostRepository;
import com.example.tangry.repositories.UserRepository;
import com.example.tangry.test.EmulatorTestHelper;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.*;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

@RunWith(AndroidJUnit4.class)
public class LeaderboardBadgeEmulatorTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    private FirebaseFirestore db;
    private UserRepository userRepository;
    private EmotionPostRepository repository;
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
        userRepository = new UserRepository(db, "users");
        repository = new EmotionPostRepository(db, "emotions");
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
     * Simple helper inner class to hold a test user's DocumentReference and unique username.
     */
    private static class TestUser {
        DocumentReference docRef;
        String uniqueUsername;
    }

    /**
     * Helper method to create a test user using the UserRepository.
     * It generates a random email suffix for uniqueness and appends it to the base username.
     *
     * @param baseUsername the base username desired
     * @return a TestUser object containing the DocumentReference and the unique username.
     */
    private TestUser createTestUser(String baseUsername) throws InterruptedException, ExecutionException, TimeoutException {
        String randomSuffix = UUID.randomUUID().toString().substring(0, 8);
        String email = baseUsername + randomSuffix + "@example.com";
        String uniqueUsername = baseUsername.concat(randomSuffix);

        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<DocumentReference> docRef = new AtomicReference<>();
        userRepository.saveUsernameToFirestore(uniqueUsername, email,
                documentReference -> {
                    docRef.set(documentReference);
                    latch.countDown();
                },
                error -> {
                    fail("Failed to create test user: " + error);
                    latch.countDown();
                });
        latch.await();
        assertNotNull("Test user should be created", docRef.get());
        testUserDocIds.add(docRef.get().getId());
        TestUser testUser = new TestUser();
        testUser.docRef = docRef.get();
        testUser.uniqueUsername = uniqueUsername;
        return testUser;
    }

    @Test
    public void testGoldBadgeAwardViaPostSubmissions() throws InterruptedException, ExecutionException, TimeoutException {
        TestUser testUser = createTestUser("badgeTestUser");
        DocumentReference userDoc = testUser.docRef;
        String uniqueUsername = testUser.uniqueUsername;

        // Create 3 posts using the unique username.
        for (int i = 1; i <= 3; i++) {
            EmotionPost post = EmotionPost.create(
                    "Happiness",
                    "Test post " + i,
                    null,
                    "Location",
                    "Alone",
                    uniqueUsername);

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

    @Test
    public void testGoldBadgeAwardAfterThreePosts() throws InterruptedException, ExecutionException, TimeoutException {
        TestUser testUser = createTestUser("testUser1");
        DocumentReference userDoc = testUser.docRef;

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

    @Test
    public void testGoldBadgeAwardAfterSixPosts() throws InterruptedException, ExecutionException, TimeoutException {
        TestUser testUser = createTestUser("testUser2");
        DocumentReference userDoc = testUser.docRef;

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

    @Test
    public void testDailyBadgeCount() throws InterruptedException, ExecutionException, TimeoutException {
        TestUser testUser = createTestUser("testUser3");
        DocumentReference userDoc = testUser.docRef;

        // Use a single Date instance so that arrayUnion adds only one unique value.
        Date now = new Date();
        for (int i = 0; i < 3; i++) {
            CountDownLatch updateLatch = new CountDownLatch(1);
            userDoc.update("badges.dailyBadgeDates", FieldValue.arrayUnion(now))
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
        assertEquals("Daily badge count should equal the number of unique dates", 1, dailyBadgeDates.size());
    }

    @Test
    public void testLeaderboardOrdering() throws InterruptedException, ExecutionException, TimeoutException {
        // Create three test users with fixed usernames for predictable ordering.
        DocumentReference userDoc1 = createTestUserForOrdering("user1");
        DocumentReference userDoc2 = createTestUserForOrdering("user2");
        DocumentReference userDoc3 = createTestUserForOrdering("user3");

        // Update each user with desired karma and badge values.
        CountDownLatch updateLatch = new CountDownLatch(3);
        userDoc1.update("karma", 50, "badges.goldBadges", 2, "badges.silverBadges", 1, "badges.dailyBadgeDates", new ArrayList<Date>())
                .addOnCompleteListener(task -> updateLatch.countDown());
        userDoc2.update("karma", 60, "badges.goldBadges", 1, "badges.silverBadges", 2, "badges.dailyBadgeDates", new ArrayList<Date>())
                .addOnCompleteListener(task -> updateLatch.countDown());
        userDoc3.update("karma", 55, "badges.goldBadges", 1, "badges.silverBadges", 1, "badges.dailyBadgeDates", new ArrayList<Date>())
                .addOnCompleteListener(task -> updateLatch.countDown());
        updateLatch.await();

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

    /**
     * Helper method to create a test user with fixed username (for leaderboard ordering).
     *
     * @param username the username to set exactly
     * @return the DocumentReference for the created user.
     */
    private DocumentReference createTestUserForOrdering(String username) throws InterruptedException, ExecutionException, TimeoutException {
        Map<String, Object> userData = new HashMap<>();
        userData.put("username", username);
        userData.put("email", username + "@example.com");
        userData.put("karma", 0);
        userData.put("badges.goldBadges", 0);
        userData.put("badges.silverBadges", 0);
        userData.put("badges.dailyBadgeDates", new ArrayList<Date>());
        userData.put("postCount", 0);
        userData.put("commentCount", 0);
        DocumentReference ref = Tasks.await(db.collection("users").add(userData), 5, TimeUnit.SECONDS);
        assertNotNull(ref);
        testUserDocIds.add(ref.getId());
        return ref;
    }
}
