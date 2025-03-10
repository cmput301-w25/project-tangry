package com.example.tangry;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.tangry.models.EmotionPost;
import com.example.tangry.repositories.EmotionPostRepository;
import com.example.tangry.test.EmulatorTestHelper;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class EmotionPostRepositoryEmulatorTest {

    private FirebaseFirestore db;
    private EmotionPostRepository repository;
    private List<String> testDocumentIds = new ArrayList<>();

    @BeforeClass
    public static void setupClass() {
        // Configure Firebase to use emulators
        EmulatorTestHelper.useFirebaseEmulators();
    }

    @Before
    public void setup() {
        db = FirebaseFirestore.getInstance();
        repository = new EmotionPostRepository(db, "emotions");
    }

    @After
    public void cleanup() {
        // Delete all test documents
        for (String docId : testDocumentIds) {
            try {
                Tasks.await(
                        db.collection("emotions").document(docId).delete(),
                        5, TimeUnit.SECONDS
                );
            } catch (Exception e) {
                System.err.println("Error cleaning up test data: " + e);
            }
        }
        testDocumentIds.clear();
    }

    @Test
    public void testSaveEmotionPost() throws Exception {
        // Create test data
        EmotionPost testPost = EmotionPost.create(
                "happiness",
                "Test emulator",
                null,
                "Test location",
                "Alone",
                "testUser"
        );

        // Create latch for async operations
        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicReference<DocumentReference> resultRef = new AtomicReference<>();

        // Call repository method
        repository.saveEmotionPostToFirestore(
                testPost,
                documentRef -> {
                    resultRef.set(documentRef);
                    testDocumentIds.add(documentRef.getId());
                    latch.countDown();
                },
                error -> {
                    System.err.println("Failed: " + error);
                    latch.countDown();
                }
        );

        // Wait for operation to complete
        assertTrue("Save operation timed out", latch.await(5, TimeUnit.SECONDS));

        // Verify document was created
        DocumentReference savedRef = resultRef.get();
        assertNotNull("Document reference should not be null", savedRef);

        // Fetch the document to verify contents
        Task<DocumentSnapshot> getTask = savedRef.get();
        DocumentSnapshot document = Tasks.await(getTask, 5, TimeUnit.SECONDS);

        assertTrue("Document should exist", document.exists());
        assertEquals("happiness", document.getString("emotion"));
        assertEquals("Test emulator", document.getString("explanation"));
        assertEquals("Test location", document.getString("location"));
        assertEquals("Alone", document.getString("socialSituation"));
        assertEquals("testUser", document.getString("username"));
        assertNotNull("Timestamp should be set", document.getTimestamp("timestamp"));
    }

    @Test
    public void testSaveEmotionPostWithImage() throws Exception {
        // Create test data with image
        EmotionPost testPost = EmotionPost.create(
                "sadness",
                "",  // Empty explanation but has image
                "test_image_uri",
                "Home",
                "Alone",
                "testUser"
        );

        // Create latch for async operations
        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicReference<DocumentReference> resultRef = new AtomicReference<>();

        // Call repository method
        repository.saveEmotionPostToFirestore(
                testPost,
                documentRef -> {
                    resultRef.set(documentRef);
                    testDocumentIds.add(documentRef.getId());
                    latch.countDown();
                },
                error -> {
                    System.err.println("Failed: " + error);
                    latch.countDown();
                }
        );

        // Wait for operation to complete
        assertTrue("Save operation timed out", latch.await(5, TimeUnit.SECONDS));

        // Fetch the document to verify contents
        DocumentSnapshot document = Tasks.await(resultRef.get().get(), 5, TimeUnit.SECONDS);

        assertTrue("Document should exist", document.exists());
        assertEquals("sadness", document.getString("emotion"));
        assertEquals("", document.getString("explanation"));
        assertEquals("test_image_uri", document.getString("imageUri"));
    }

    @Test
    public void testGetEmotionPost() throws Exception {
        // First create a post to retrieve
        EmotionPost originalPost = EmotionPost.create(
                "anger",
                "Test get",
                null,
                "Office",
                "With a crowd",
                "testUser"
        );

        // Save directly to Firestore
        DocumentReference docRef = Tasks.await(
                db.collection("emotions").add(originalPost),
                5, TimeUnit.SECONDS
        );
        String docId = docRef.getId();
        testDocumentIds.add(docId);

        // Create latch for async operations
        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicReference<EmotionPost> resultPost = new AtomicReference<>();

        // Call repository method to retrieve the post
        repository.getEmotionPost(
                docId,
                post -> {
                    resultPost.set(post);
                    latch.countDown();
                },
                error -> {
                    System.err.println("Failed to get post: " + error);
                    latch.countDown();
                }
        );

        // Wait for operation to complete
        assertTrue("Get operation timed out", latch.await(5, TimeUnit.SECONDS));

        // Verify the retrieved post
        EmotionPost retrievedPost = resultPost.get();
        assertNotNull("Retrieved post should not be null", retrievedPost);
        assertEquals("anger", retrievedPost.getEmotion());
        assertEquals("Test get", retrievedPost.getExplanation());
        assertEquals("Office", retrievedPost.getLocation());
        assertEquals("With a crowd", retrievedPost.getSocialSituation());
        assertEquals("testUser", retrievedPost.getUsername());
    }

    @Test
    public void testGetNonExistentPost() throws Exception {
        // Create latch for async operations
        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicReference<EmotionPost> resultPost = new AtomicReference<>();

        // Call repository method with non-existent ID
        repository.getEmotionPost(
                "non_existent_doc_id",
                post -> {
                    resultPost.set(post);
                    latch.countDown();
                },
                error -> {
                    System.err.println("Failed to get post: " + error);
                    latch.countDown();
                }
        );

        // Wait for operation to complete
        assertTrue("Get operation timed out", latch.await(5, TimeUnit.SECONDS));

        // Should return null for non-existent post
        assertNull("Result should be null for non-existent post", resultPost.get());
    }

    @Test
    public void testUpdateEmotionPost() throws Exception {
        // First create a post to update
        EmotionPost originalPost = EmotionPost.create(
                "fear",
                "Original",
                null,
                "Home",
                "Alone",
                "testUser"
        );

        // Save directly to Firestore
        DocumentReference docRef = Tasks.await(
                db.collection("emotions").add(originalPost),
                5, TimeUnit.SECONDS
        );
        String docId = docRef.getId();
        testDocumentIds.add(docId);

        // Create updated post
        EmotionPost updatedPost = EmotionPost.create(
                "joy",
                "Updated",
                "new_image",
                "Park",
                "With one other person",
                "testUser"
        );
        updatedPost.setPostId(docId); // Set the document ID

        // Create latch for async operations
        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicReference<Boolean> updateSuccess = new AtomicReference<>(false);

        // Call repository method to update
        repository.updateEmotionPost(
                docId,
                updatedPost,
                () -> {
                    updateSuccess.set(true);
                    latch.countDown();
                },
                error -> {
                    System.err.println("Update failed: " + error);
                    latch.countDown();
                }
        );

        // Wait for operation to complete
        assertTrue("Update operation timed out", latch.await(5, TimeUnit.SECONDS));
        assertTrue("Update should succeed", updateSuccess.get());

        // Verify the post was updated
        DocumentSnapshot updatedDoc = Tasks.await(docRef.get(), 5, TimeUnit.SECONDS);
        assertEquals("joy", updatedDoc.getString("emotion"));
        assertEquals("Updated", updatedDoc.getString("explanation"));
        assertEquals("new_image", updatedDoc.getString("imageUri"));
        assertEquals("Park", updatedDoc.getString("location"));
        assertEquals("With one other person", updatedDoc.getString("socialSituation"));
    }

    @Test
    public void testDeleteEmotionPost() throws Exception {
        // First create a post to delete
        EmotionPost post = EmotionPost.create(
                "disgust",
                "Delete me",
                null,
                "Nowhere",
                "Alone",
                "testUser"
        );

        // Save directly to Firestore
        DocumentReference docRef = Tasks.await(
                db.collection("emotions").add(post),
                5, TimeUnit.SECONDS
        );
        String docId = docRef.getId();
        // Don't add to testDocumentIds since we're deleting it

        // Verify document exists before deletion
        DocumentSnapshot beforeDelete = Tasks.await(docRef.get(), 5, TimeUnit.SECONDS);
        assertTrue("Document should exist before deletion", beforeDelete.exists());

        // Create latch for async operations
        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicReference<Boolean> deleteSuccess = new AtomicReference<>(false);

        // Call repository method to delete
        repository.deleteEmotionPost(
                docId,
                () -> {
                    deleteSuccess.set(true);
                    latch.countDown();
                },
                error -> {
                    System.err.println("Delete failed: " + error);
                    latch.countDown();
                }
        );

        // Wait for operation to complete
        assertTrue("Delete operation timed out", latch.await(5, TimeUnit.SECONDS));
        assertTrue("Delete should succeed", deleteSuccess.get());

        // Verify the post was deleted
        DocumentSnapshot afterDelete = Tasks.await(docRef.get(), 5, TimeUnit.SECONDS);
        assertFalse("Document should no longer exist", afterDelete.exists());
    }

    @Test
    public void testGetPostsQuery() throws Exception {
        // Create multiple posts with different timestamps
        createTestPostWithTimestamp("Post 1", Timestamp.now());

        // Create a post with an older timestamp
        Timestamp olderTimestamp = new Timestamp(
                Timestamp.now().getSeconds() - 3600, // 1 hour ago
                Timestamp.now().getNanoseconds()
        );
        createTestPostWithTimestamp("Post 2", olderTimestamp);

        // Create post with newest timestamp
        Timestamp newerTimestamp = new Timestamp(
                Timestamp.now().getSeconds() + 3600, // 1 hour in future
                Timestamp.now().getNanoseconds()
        );
        createTestPostWithTimestamp("Post 3", newerTimestamp);

        // Get query from repository
        Task<QuerySnapshot> queryTask = repository.getPostsQuery().get();
        QuerySnapshot querySnapshot = Tasks.await(queryTask, 5, TimeUnit.SECONDS);

        // Verify the ordering (should be descending by timestamp)
        List<DocumentSnapshot> docs = querySnapshot.getDocuments();
        assertTrue("Should have at least 3 posts", docs.size() >= 3);

        // First 3 posts should be in timestamp descending order
        Timestamp ts1 = docs.get(0).getTimestamp("timestamp");
        Timestamp ts2 = docs.get(1).getTimestamp("timestamp");
        Timestamp ts3 = docs.get(2).getTimestamp("timestamp");

        assertTrue("Posts should be ordered by timestamp desc",
                ts1.compareTo(ts2) >= 0 && ts2.compareTo(ts3) >= 0);
    }

    // Helper method to create a post with a specific timestamp
    private void createTestPostWithTimestamp(String explanation, Timestamp timestamp) throws Exception {
        EmotionPost post = EmotionPost.create(
                "neutral",
                explanation,
                null,
                "Test",
                "Alone",
                "testUser"
        );

        // Create a map with the post data and explicit timestamp
        java.util.Map<String, Object> data = new java.util.HashMap<>();
        data.put("emotion", post.getEmotion());
        data.put("explanation", post.getExplanation());
        data.put("imageUri", post.getImageUri());
        data.put("location", post.getLocation());
        data.put("socialSituation", post.getSocialSituation());
        data.put("username", post.getUsername());
        data.put("timestamp", timestamp);

        // Save to Firestore with the specified timestamp
        DocumentReference docRef = Tasks.await(
                db.collection("emotions").add(data),
                5, TimeUnit.SECONDS
        );

        testDocumentIds.add(docRef.getId());
    }

    @Test
    public void testEdgeCaseEmptyFields() throws Exception {
        // Test with empty but valid fields
        EmotionPost post = EmotionPost.create(
                "happiness",  // Required field
                "",           // Empty explanation, but valid since we have an image
                "image_uri",  // Has image
                "",           // Empty location (optional)
                "Select social situation", // Default social situation
                "testUser"    // Required field
        );

        // Create latch for async operations
        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicReference<DocumentReference> resultRef = new AtomicReference<>();

        // Call repository method
        repository.saveEmotionPostToFirestore(
                post,
                documentRef -> {
                    resultRef.set(documentRef);
                    testDocumentIds.add(documentRef.getId());
                    latch.countDown();
                },
                error -> {
                    System.err.println("Failed: " + error);
                    latch.countDown();
                }
        );

        // Wait for operation to complete
        assertTrue("Save operation timed out", latch.await(5, TimeUnit.SECONDS));

        // Verify document was saved correctly
        DocumentSnapshot document = Tasks.await(resultRef.get().get(), 5, TimeUnit.SECONDS);

        assertTrue("Document should exist", document.exists());
        assertEquals("happiness", document.getString("emotion"));
        assertEquals("", document.getString("explanation"));
        assertEquals("image_uri", document.getString("imageUri"));
        assertEquals("", document.getString("location"));
        assertEquals("Select social situation", document.getString("socialSituation"));
    }

    @Test
    public void testErrorHandlingForInvalidPost() throws Exception {
        // Try to save an invalid post that should fail client-side validation
        // This requires modifying your repository to expose a method that skips validation
        // or handling the exception in this test

        try {
            // This should throw an exception before reaching Firestore
            EmotionPost invalidPost = EmotionPost.create(
                    "",            // Empty emotion - invalid
                    "",            // Empty explanation
                    null,          // No image
                    "Location",
                    "Alone",
                    "testUser"
            );

            fail("Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Expected exception
            assertTrue(e.getMessage().contains("Emotion is required"));
        }
    }
}