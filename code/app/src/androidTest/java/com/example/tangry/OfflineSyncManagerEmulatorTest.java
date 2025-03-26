package com.example.tangry;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.tangry.controllers.EmotionPostController;
import com.example.tangry.models.EmotionPost;
import com.example.tangry.models.SyncStatus;
import com.example.tangry.interfaces.SyncStatusListener;
import com.example.tangry.utils.NetworkMonitor;
import com.example.tangry.utils.OfflineSyncManager;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.QuerySnapshot;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class OfflineSyncManagerEmulatorTest {

    private FirebaseFirestore db;
    private OfflineSyncManager syncManager;
    private SharedPreferences sharedPreferences;
    private FakeNetworkMonitor fakeNetworkMonitor;
    private RealEmotionPostController realEmotionPostController;
    private TestSyncStatusListener testSyncStatusListener;

    @Before
    public void setup() {
        // Obtain a valid Context.
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();

        // Initialize SharedPreferences for testing.
        sharedPreferences = context.getSharedPreferences("offline_sync_test_prefs", Context.MODE_PRIVATE);
        sharedPreferences.edit().clear().apply();

        // Initialize Firestore and configure it to use your emulator.
        db = FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setHost("10.0.2.2:8080") // Adjust host/port as needed for your emulator.
                .setSslEnabled(false)
                .build();
        db.setFirestoreSettings(settings);

        // Create a "real" controller that writes to Firestore.
        realEmotionPostController = new RealEmotionPostController(db, "emotions");

        // Create a fake network monitor. Pass the context to the base class
        // constructor.
        fakeNetworkMonitor = new FakeNetworkMonitor(context);
        fakeNetworkMonitor.setConnected(false); // Start offline.

        // Instantiate OfflineSyncManager with our fake network monitor and real
        // controller.
        syncManager = new OfflineSyncManager(context, fakeNetworkMonitor, realEmotionPostController);

        // Register a sync status listener.
        testSyncStatusListener = new TestSyncStatusListener();
        syncManager.registerSyncStatusListener(testSyncStatusListener);

        // Clear any pending operations from previous tests.
        syncManager.clearPendingOperations();
    }

    @After
    public void cleanup() {
        // Clear pending operations after each test, but check for null first
        if (syncManager != null) {
            syncManager.clearPendingOperations();
        }
    }

    @Test
    public void testOfflineCreateThenSync() throws Exception {
        // 1. While offline, add a pending CREATE operation.
        EmotionPost post = EmotionPost.create("Happiness", "Created offline", null, "Home", "Alone", "testUser");
        fakeNetworkMonitor.setConnected(false);
        syncManager.addPendingCreate(post);
        assertTrue("Should have pending operations", syncManager.hasPendingOperations());

        // 2. Now simulate network availability.
        fakeNetworkMonitor.setConnected(true);
        CountDownLatch latch = new CountDownLatch(1);
        testSyncStatusListener.setOnSyncedListener(() -> latch.countDown());
        syncManager.syncPendingOperations();
        assertTrue("Sync operation timed out", latch.await(10, TimeUnit.SECONDS));
        assertFalse("Pending operations should be cleared", syncManager.hasPendingOperations());

        // 3. Verify that the document was created in Firestore.
        // FIXED: Query string matches the actual explanation text in the created post
        QuerySnapshot qs = Tasks.await(
                db.collection("emotions")
                        .whereEqualTo("explanation", "Created offline")
                        .get(),
                5, TimeUnit.SECONDS);
        assertFalse("Document should exist in Firestore", qs.isEmpty());
    }

    @Test
    public void testOfflineUpdateThenSync() throws Exception {
        // 1. Create a document online.
        EmotionPost original = EmotionPost.create("Sadness", "Original content", null, "Office", "Alone", "testUser");
        DocumentReference docRef = Tasks.await(db.collection("emotions").add(original), 5, TimeUnit.SECONDS);
        String docId = docRef.getId();

        // 2. Go offline and queue an UPDATE operation.
        fakeNetworkMonitor.setConnected(false);
        EmotionPost updated = EmotionPost.create("Angry", "Updated offline", null, "Home", "Alone", "testUser");
        syncManager.addPendingUpdate(docId, updated);
        assertTrue("Should have pending operations", syncManager.hasPendingOperations());

        // 3. Verify Firestore still has the original content.
        DocumentSnapshot preUpdate = Tasks.await(db.collection("emotions").document(docId).get(), 5, TimeUnit.SECONDS);
        assertEquals("Sadness", preUpdate.getString("emotion"));
        assertEquals("Original content", preUpdate.getString("explanation"));

        // 4. Go online and sync.
        fakeNetworkMonitor.setConnected(true);
        CountDownLatch latch = new CountDownLatch(1);
        testSyncStatusListener.setOnSyncedListener(() -> latch.countDown());
        syncManager.syncPendingOperations();
        assertTrue("Sync operation timed out", latch.await(10, TimeUnit.SECONDS));
        assertFalse("Pending operations should be cleared", syncManager.hasPendingOperations());

        // 5. Verify that Firestore now reflects the update.
        DocumentSnapshot postUpdate = Tasks.await(db.collection("emotions").document(docId).get(), 5, TimeUnit.SECONDS);
        assertEquals("Angry", postUpdate.getString("emotion"));
        assertEquals("Updated offline", postUpdate.getString("explanation"));
    }

    @Test
    public void testOfflineDeleteThenSync() throws Exception {
        // 1. Create a document online.
        EmotionPost post = EmotionPost.create("Fear", "To be deleted", null, "School", "Alone", "testUser");
        DocumentReference docRef = Tasks.await(db.collection("emotions").add(post), 5, TimeUnit.SECONDS);
        String docId = docRef.getId();

        // 2. Verify that the document exists.
        DocumentSnapshot preDelete = Tasks.await(db.collection("emotions").document(docId).get(), 5, TimeUnit.SECONDS);
        assertTrue("Document should exist before deletion", preDelete.exists());

        // 3. Go offline and queue a DELETE operation.
        fakeNetworkMonitor.setConnected(false);
        syncManager.addPendingDelete(docId);
        assertTrue("Should have pending operations", syncManager.hasPendingOperations());

        // 4. Go online and sync.
        fakeNetworkMonitor.setConnected(true);
        CountDownLatch latch = new CountDownLatch(1);
        testSyncStatusListener.setOnSyncedListener(() -> latch.countDown());
        syncManager.syncPendingOperations();
        assertTrue("Sync operation timed out", latch.await(10, TimeUnit.SECONDS));
        assertFalse("Pending operations should be cleared", syncManager.hasPendingOperations());

        // 5. Verify that the document was deleted from Firestore.
        DocumentSnapshot postDelete = Tasks.await(db.collection("emotions").document(docId).get(), 5, TimeUnit.SECONDS);
        assertFalse("Document should no longer exist", postDelete.exists());
    }

    @Test
    public void testMultipleOfflineOperationsThenSync() throws Exception {
        // 1. Go offline and queue multiple operations
        fakeNetworkMonitor.setConnected(false);

        // Add multiple create operations
        EmotionPost post1 = EmotionPost.create("Happiness", "First offline post", null, "Home", "Alone", "testUser");
        syncManager.addPendingCreate(post1);

        EmotionPost post2 = EmotionPost.create("Sadness", "Second offline post", null, "Work", "Alone", "testUser");
        syncManager.addPendingCreate(post2);

        assertTrue("Should have pending operations", syncManager.hasPendingOperations());

        // 2. Go online and sync
        fakeNetworkMonitor.setConnected(true);
        CountDownLatch latch = new CountDownLatch(1);

        // Since we're syncing multiple operations, wait longer
        final AtomicInteger syncCounter = new AtomicInteger(0);
        testSyncStatusListener.setOnSyncedListener(() -> {
            // Count the number of sync operations completed
            if (syncCounter.incrementAndGet() >= 2 || !syncManager.hasPendingOperations()) {
                latch.countDown();
            }
        });

        syncManager.syncPendingOperations();

        // Wait longer for both operations to complete
        assertTrue("Sync operation timed out", latch.await(20, TimeUnit.SECONDS));

        // After waiting longer, check again if pending operations are cleared
        // If not, force a second sync attempt
        if (syncManager.hasPendingOperations()) {
            CountDownLatch secondLatch = new CountDownLatch(1);
            testSyncStatusListener.setOnSyncedListener(() -> secondLatch.countDown());
            syncManager.syncPendingOperations();
            assertTrue("Second sync operation timed out", secondLatch.await(10, TimeUnit.SECONDS));
        }

        // Now check if operations are cleared
        assertFalse("Pending operations should be cleared", syncManager.hasPendingOperations());

        // 3. Verify both documents were created in Firestore
        QuerySnapshot qs1 = Tasks.await(
                db.collection("emotions")
                        .whereEqualTo("explanation", "First offline post")
                        .get(),
                5, TimeUnit.SECONDS);
        assertFalse("First document should exist in Firestore", qs1.isEmpty());

        QuerySnapshot qs2 = Tasks.await(
                db.collection("emotions")
                        .whereEqualTo("explanation", "Second offline post")
                        .get(),
                5, TimeUnit.SECONDS);
        assertFalse("Second document should exist in Firestore", qs2.isEmpty());
    }
    // --- Helper Classes ---

    // A FakeNetworkMonitor that extends your project's NetworkMonitor.
    private static class FakeNetworkMonitor extends NetworkMonitor {
        private boolean connected;

        public FakeNetworkMonitor(Context context) {
            super(context);
        }

        public void setConnected(boolean connected) {
            this.connected = connected;
        }

        @Override
        public boolean isConnected() {
            return connected;
        }
    }

    // A simple "real" controller that writes directly to Firestore.
    private static class RealEmotionPostController extends EmotionPostController {
        private final FirebaseFirestore db;
        private final String collectionName;

        public RealEmotionPostController(FirebaseFirestore db, String collectionName) {
            this.db = db;
            this.collectionName = collectionName;
        }

        public void createPost(EmotionPost post,
                com.google.android.gms.tasks.OnSuccessListener<DocumentReference> onSuccess, Runnable onFailure) {
            db.collection(collectionName)
                    .add(post)
                    .addOnSuccessListener(onSuccess)
                    .addOnFailureListener(e -> onFailure.run());
        }

        public void updateEmotionPost(String postId, EmotionPost post, Runnable onSuccess, Runnable onFailure) {
            db.collection(collectionName)
                    .document(postId)
                    .set(post)
                    .addOnSuccessListener(aVoid -> onSuccess.run())
                    .addOnFailureListener(e -> onFailure.run());
        }

        public void deleteEmotionPost(String postId, Runnable onSuccess, Runnable onFailure) {
            db.collection(collectionName)
                    .document(postId)
                    .delete()
                    .addOnSuccessListener(aVoid -> onSuccess.run())
                    .addOnFailureListener(e -> onFailure.run());
        }
    }

    // A test listener that calls a provided Runnable when a SYNCED status is
    // reported.
    private static class TestSyncStatusListener implements SyncStatusListener {
        private Runnable onSyncedListener;
        private Runnable onSyncStartedListener;

        public void setOnSyncedListener(Runnable listener) {
            this.onSyncedListener = listener;
        }

        public void setOnSyncStartedListener(Runnable listener) {
            this.onSyncStartedListener = listener;
        }

        @Override
        public void onSyncStatusChanged(SyncStatus status, String message) {
            if (status == SyncStatus.SYNCED && onSyncedListener != null) {
                onSyncedListener.run();
            } else if (status == SyncStatus.SYNCING && onSyncStartedListener != null) {
                onSyncStartedListener.run();
            }
        }
    }
}
