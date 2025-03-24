package com.example.tangry;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;

import com.example.tangry.controllers.FollowController;
import com.example.tangry.controllers.FollowController.FollowRequest;
import com.example.tangry.controllers.FollowController.FollowStatus;
import com.example.tangry.test.EmulatorTestHelper;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class FollowRequestTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    private FirebaseFirestore db;
    private FollowController followController;

    // We'll use these test users.
    private final String currentUsername = "testUser";
    private final String currentEmail = "testuser@example.com";
    private final String senderUsername = "senderUser";
    private final String senderEmail = "sender@example.com";

    @Before
    public void setUp() throws Exception {
        EmulatorTestHelper.useFirebaseEmulators();
        db = FirebaseFirestore.getInstance();
        followController = new FollowController();

        // Ensure that test users exist in the 'users' collection.
        Map<String, Object> testUserData = new HashMap<>();
        testUserData.put("username", currentUsername);
        testUserData.put("email", currentEmail);
        testUserData.put("followers", new ArrayList<String>());
        testUserData.put("followings", new ArrayList<String>());
        Tasks.await(db.collection("users").document(currentUsername).set(testUserData), 5, TimeUnit.SECONDS);

        Map<String, Object> senderUserData = new HashMap<>();
        senderUserData.put("username", senderUsername);
        senderUserData.put("email", senderEmail);
        senderUserData.put("followers", new ArrayList<String>());
        senderUserData.put("followings", new ArrayList<String>());
        Tasks.await(db.collection("users").document(senderUsername).set(senderUserData), 5, TimeUnit.SECONDS);
    }

    @Test
    public void testSendFollowRequest() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        followController.sendFollowRequest("send", "recv",
                documentReference -> latch.countDown(),
                e -> latch.countDown());
        assertTrue(latch.await(5, TimeUnit.SECONDS));

        // Verify that a follow request document is created.
        QuerySnapshot qs = Tasks.await(
                db.collection("followrequests")
                        .whereEqualTo("from", "send")
                        .whereEqualTo("to", "recv")
                        .get(), 5, TimeUnit.SECONDS);
        assertFalse(qs.isEmpty());
        DocumentSnapshot doc = qs.getDocuments().get(0);
        assertEquals("send", doc.getString("from"));
        assertEquals("recv", doc.getString("to"));
        assertFalse(doc.getBoolean("accepted"));
    }

    @Test
    public void testDenyFollowRequest() throws Exception {
        // Create a follow request document.
        Map<String, Object> requestData = new HashMap<>();
        requestData.put("from", senderUsername);
        requestData.put("to", currentUsername);
        requestData.put("accepted", false);
        DocumentReference docRef = Tasks.await(
                db.collection("followrequests").add(requestData), 5, TimeUnit.SECONDS);
        String requestId = docRef.getId();

        CountDownLatch latch = new CountDownLatch(1);
        followController.denyFollowRequest(requestId,
                aVoid -> latch.countDown(),
                e -> latch.countDown());
        assertTrue(latch.await(5, TimeUnit.SECONDS));

        // Verify that the follow request document is deleted.
        DocumentSnapshot snapshot = Tasks.await(
                db.collection("followrequests").document(requestId).get(), 5, TimeUnit.SECONDS);
        assertFalse(snapshot.exists());
    }

    @Test
    public void testAcceptFollowRequest() throws Exception {
        // Sign in as currentUser so that FirebaseAuth returns a non-null user with displayName.
        Tasks.await(FirebaseAuth.getInstance().signInAnonymously(), 5, TimeUnit.SECONDS);
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(currentUsername)
                .build();
        Tasks.await(FirebaseAuth.getInstance().getCurrentUser().updateProfile(profileUpdates), 5, TimeUnit.SECONDS);

        // Create a follow request document from senderUser to testUser.
        Map<String, Object> requestData = new HashMap<>();
        requestData.put("from", senderUsername);
        requestData.put("to", currentUsername);
        requestData.put("accepted", false);
        DocumentReference docRef = Tasks.await(
                db.collection("followrequests").add(requestData), 5, TimeUnit.SECONDS);
        String requestId = docRef.getId();

        // Build a FollowRequest model.
        FollowRequest request = new FollowRequest(requestId, senderUsername, currentUsername, false);

        CountDownLatch latch = new CountDownLatch(1);
        // Accept the follow request.
        followController.acceptFollowRequest(request,
                aVoid -> latch.countDown(),
                e -> latch.countDown());
        assertTrue("Follow request acceptance did not complete in time", latch.await(5, TimeUnit.SECONDS));

        // Verify the follow request is now marked as accepted.
        DocumentSnapshot requestSnapshot = Tasks.await(
                db.collection("followrequests").document(requestId).get(), 5, TimeUnit.SECONDS);
        assertTrue(requestSnapshot.getBoolean("accepted"));

        // Verify that testUser's document contains senderUser in "followers".
        DocumentSnapshot testUserDoc = Tasks.await(
                db.collection("users").document(currentUsername).get(), 5, TimeUnit.SECONDS);
        List<String> followers = (List<String>) testUserDoc.get("followers");
        assertNotNull(followers);
        assertTrue(followers.contains(senderUsername));

        // Verify that senderUser's document contains testUser in "followings".
        DocumentSnapshot senderDoc = Tasks.await(
                db.collection("users").document(senderUsername).get(), 5, TimeUnit.SECONDS);
        List<String> followingList = (List<String>) senderDoc.get("followings");
        assertNotNull(followingList);
        assertTrue(followingList.contains(currentUsername));
    }

    @Test
    public void testLoadFollowStatus() throws Exception {
        // For loadFollowStatus, update testUser document's followings and
        // add a follow request where testUser is the sender.
        // Update testUser's followings.
        List<String> initialFollowings = new ArrayList<>();
        initialFollowings.add("someone");
        Tasks.await(db.collection("users").document(currentUsername)
                .update("followings", initialFollowings), 5, TimeUnit.SECONDS);

        // Create a follow request with testUser as the sender.
        Map<String, Object> reqData = new HashMap<>();
        reqData.put("from", currentUsername);
        reqData.put("to", "otherUser");
        reqData.put("accepted", false);
        Tasks.await(db.collection("followrequests").add(reqData), 5, TimeUnit.SECONDS);

        CountDownLatch latch = new CountDownLatch(1);
        followController.loadFollowStatus(currentEmail, currentUsername,
                status -> {
                    List<String> foll = status.getFollowings();
                    List<String> sent = status.getSentFollowRequests();
                    assertNotNull(foll);
                    assertNotNull(sent);
                    assertTrue(foll.contains("someone"));
                    assertTrue(sent.contains("otherUser"));
                    latch.countDown();
                },
                e -> latch.countDown());
        assertTrue(latch.await(5, TimeUnit.SECONDS));
    }
}