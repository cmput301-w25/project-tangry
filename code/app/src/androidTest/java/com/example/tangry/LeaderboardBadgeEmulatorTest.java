package com.example.tangry;
import static org.junit.Assert.*;

import com.example.tangry.test.EmulatorTestHelper;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class LeaderboardBadgeEmulatorTest {

    private FirebaseFirestore db;
    private List<String> testUserDocIds = new ArrayList<>();

    @BeforeClass
    public static void setupClass() {
        // Configure Firebase to use emulators (assumes EmulatorTestHelper sets up host/port etc.)
        EmulatorTestHelper.useFirebaseEmulators();
    }

    @Before
    public void setup() {
        db = FirebaseFirestore.getInstance();
    }

    @After
    public void cleanup() {
        // Delete all test documents from "users" collection
        for (String docId : testUserDocIds) {
            try {
                Tasks.await(db.collection("users").document(docId).delete(), 5, TimeUnit.SECONDS);
            } catch (Exception e) {
                System.err.println("Error cleaning up test data: " + e);
            }
        }
        testUserDocIds.clear();
    }

    /**
     * Simulates three posting operations by a user.
     * For each post, we increment a "postCount" field.
     * If postCount is a multiple of 3, we simulate awarding a gold badge by incrementing the "goldBadges" field.
     */
    @Test
    public void testGoldBadgeAwardAfterThreePosts() throws Exception {
        // Create a test user document with initial badge values
        Map<String, Object> userData = new HashMap<>();
        userData.put("username", "testUser");
        userData.put("karma", 0);
        userData.put("goldBadges", 0);
        userData.put("silverBadges", 0);
        userData.put("dailyBadgeDates", new ArrayList<Date>());
        userData.put("postCount", 0);

        DocumentReference userDoc = Tasks.await(db.collection("users").add(userData), 5, TimeUnit.SECONDS);
        testUserDocIds.add(userDoc.getId());

        // Simulate 3 posting operations
        for (int i = 1; i <= 3; i++) {
            // Increment postCount by 1
            Tasks.await(userDoc.update("postCount", FieldValue.increment(1)), 5, TimeUnit.SECONDS);
            // If postCount is divisible by 3, increment goldBadges by 1
            if (i % 3 == 0) {
                Tasks.await(userDoc.update("goldBadges", FieldValue.increment(1)), 5, TimeUnit.SECONDS);
            }
        }

        // Fetch the updated user document
        DocumentSnapshot snapshot = Tasks.await(userDoc.get(), 5, TimeUnit.SECONDS);
        Long goldBadges = snapshot.getLong("goldBadges");
        assertNotNull("goldBadges field should not be null", goldBadges);
        assertEquals("Gold badge count should be 1 after 3 posts", 1L, goldBadges.longValue());
    }

    /**
     * Simulates six posting operations and verifies that gold badges increment twice.
     */
    @Test
    public void testGoldBadgeAwardAfterSixPosts() throws Exception {
        // Create a test user document
        Map<String, Object> userData = new HashMap<>();
        userData.put("username", "testUser");
        userData.put("karma", 0);
        userData.put("goldBadges", 0);
        userData.put("silverBadges", 0);
        userData.put("dailyBadgeDates", new ArrayList<Date>());
        userData.put("postCount", 0);

        DocumentReference userDoc = Tasks.await(db.collection("users").add(userData), 5, TimeUnit.SECONDS);
        testUserDocIds.add(userDoc.getId());

        // Simulate 6 posting operations
        for (int i = 1; i <= 6; i++) {
            Tasks.await(userDoc.update("postCount", FieldValue.increment(1)), 5, TimeUnit.SECONDS);
            if (i % 3 == 0) {
                Tasks.await(userDoc.update("goldBadges", FieldValue.increment(1)), 5, TimeUnit.SECONDS);
            }
        }

        DocumentSnapshot snapshot = Tasks.await(userDoc.get(), 5, TimeUnit.SECONDS);
        Long goldBadges = snapshot.getLong("goldBadges");
        assertNotNull("goldBadges field should not be null", goldBadges);
        assertEquals("Gold badge count should be 2 after 6 posts", 2L, goldBadges.longValue());
    }

    /**
     * Simulates adding daily badge dates.
     * In the leaderboard, daily badge count is the size of the dailyBadgeDates array.
     */
    @Test
    public void testDailyBadgeCount() throws Exception {
        Map<String, Object> userData = new HashMap<>();
        userData.put("username", "testUser");
        userData.put("karma", 0);
        userData.put("goldBadges", 0);
        userData.put("silverBadges", 0);
        userData.put("dailyBadgeDates", new ArrayList<Date>());

        DocumentReference userDoc = Tasks.await(db.collection("users").add(userData), 5, TimeUnit.SECONDS);
        testUserDocIds.add(userDoc.getId());

        // Simulate adding 3 daily badge dates
        for (int i = 0; i < 3; i++) {
            Tasks.await(userDoc.update("dailyBadgeDates", FieldValue.arrayUnion(new Date())), 5, TimeUnit.SECONDS);
        }

        DocumentSnapshot snapshot = Tasks.await(userDoc.get(), 5, TimeUnit.SECONDS);
        List<?> dailyBadgeDates = (List<?>) snapshot.get("dailyBadgeDates");
        assertNotNull("dailyBadgeDates field should not be null", dailyBadgeDates);
        assertEquals("Daily badge count should equal the number of dates", 3, dailyBadgeDates.size());
    }

    /**
     * Tests that the leaderboard query orders users correctly based on a chosen field (e.g., karma).
     * (Adjust ordering logic if your leaderboard combines karma and badges.)
     */
    @Test
    public void testLeaderboardOrdering() throws Exception {
        // Create three test users with varying karma values
        Map<String, Object> user1 = new HashMap<>();
        user1.put("username", "user1");
        user1.put("karma", 50);
        user1.put("goldBadges", 2);
        user1.put("silverBadges", 1);
        user1.put("dailyBadgeDates", new ArrayList<Date>());

        Map<String, Object> user2 = new HashMap<>();
        user2.put("username", "user2");
        user2.put("karma", 60);
        user2.put("goldBadges", 1);
        user2.put("silverBadges", 2);
        user2.put("dailyBadgeDates", new ArrayList<Date>());

        Map<String, Object> user3 = new HashMap<>();
        user3.put("username", "user3");
        user3.put("karma", 55);
        user3.put("goldBadges", 1);
        user3.put("silverBadges", 1);
        user3.put("dailyBadgeDates", new ArrayList<Date>());

        DocumentReference doc1 = Tasks.await(db.collection("users").add(user1), 5, TimeUnit.SECONDS);
        DocumentReference doc2 = Tasks.await(db.collection("users").add(user2), 5, TimeUnit.SECONDS);
        DocumentReference doc3 = Tasks.await(db.collection("users").add(user3), 5, TimeUnit.SECONDS);
        testUserDocIds.add(doc1.getId());
        testUserDocIds.add(doc2.getId());
        testUserDocIds.add(doc3.getId());

        // Query users ordered by karma descending
        Query query = db.collection("users").orderBy("karma", Query.Direction.DESCENDING).limit(10);
        QuerySnapshot snapshot = Tasks.await(query.get(), 5, TimeUnit.SECONDS);
        List<DocumentSnapshot> docs = snapshot.getDocuments();

        assertTrue("Should have at least 3 users", docs.size() >= 3);
        // Expected order: user2 (60), user3 (55), user1 (50)
        assertEquals("user2", docs.get(0).getString("username"));
        assertEquals("user3", docs.get(1).getString("username"));
        assertEquals("user1", docs.get(2).getString("username"));
    }
}
