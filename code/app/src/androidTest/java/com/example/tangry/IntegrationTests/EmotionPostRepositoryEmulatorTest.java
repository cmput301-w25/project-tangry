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
                        5, TimeUnit.SECONDS);
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
                "Happiness",
                "Test emulator",
                null,
                "Test location",
                "Alone",
                "testUser");

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
                });

        // Wait for operation to complete
        assertTrue("Save operation timed out", latch.await(5, TimeUnit.SECONDS));

        // Verify document was created
        DocumentReference savedRef = resultRef.get();
        assertNotNull("Document reference should not be null", savedRef);

        // Fetch the document to verify contents
        Task<DocumentSnapshot> getTask = savedRef.get();
        DocumentSnapshot document = Tasks.await(getTask, 5, TimeUnit.SECONDS);

        assertTrue("Document should exist", document.exists());
        assertEquals("Happiness", document.getString("emotion"));
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
                "Sadness",
                "", // Empty explanation but has image
                "test_image_uri",
                "Home",
                "Alone",
                "testUser");

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
                });

        // Wait for operation to complete
        assertTrue("Save operation timed out", latch.await(5, TimeUnit.SECONDS));

        // Fetch the document to verify contents
        DocumentSnapshot document = Tasks.await(resultRef.get().get(), 5, TimeUnit.SECONDS);

        assertTrue("Document should exist", document.exists());
        assertEquals("Sadness", document.getString("emotion"));
        assertEquals("", document.getString("explanation"));
        assertEquals("test_image_uri", document.getString("imageUri"));
    }

    @Test
    public void testGetEmotionPost() throws Exception {
        // First create a post to retrieve
        EmotionPost originalPost = EmotionPost.create(
                "Angry",
                "Test get",
                null,
                "Office",
                "With a crowd",
                "testUser");

        // Save directly to Firestore
        DocumentReference docRef = Tasks.await(
                db.collection("emotions").add(originalPost),
                5, TimeUnit.SECONDS);
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
                });

        // Wait for operation to complete
        assertTrue("Get operation timed out", latch.await(5, TimeUnit.SECONDS));

        // Verify the retrieved post
        EmotionPost retrievedPost = resultPost.get();
        assertNotNull("Retrieved post should not be null", retrievedPost);
        assertEquals("Angry", retrievedPost.getEmotion());
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
                });

        // Wait for operation to complete
        assertTrue("Get operation timed out", latch.await(5, TimeUnit.SECONDS));

        // Should return null for non-existent post
        assertNull("Result should be null for non-existent post", resultPost.get());
    }

    @Test
    public void testUpdateEmotionPost() throws Exception {
        // First create a post to update
        EmotionPost originalPost = EmotionPost.create(
                "Fear",
                "Original",
                null,
                "Home",
                "Alone",
                "testUser");

        // Save directly to Firestore
        DocumentReference docRef = Tasks.await(
                db.collection("emotions").add(originalPost),
                5, TimeUnit.SECONDS);
        String docId = docRef.getId();
        testDocumentIds.add(docId);

        // Create updated post
        EmotionPost updatedPost = EmotionPost.create(
                "Happiness",
                "Updated",
                "new_image",
                "Park",
                "With one other person",
                "testUser");
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
                });

        // Wait for operation to complete
        assertTrue("Update operation timed out", latch.await(5, TimeUnit.SECONDS));
        assertTrue("Update should succeed", updateSuccess.get());

        // Verify the post was updated
        DocumentSnapshot updatedDoc = Tasks.await(docRef.get(), 5, TimeUnit.SECONDS);
        assertEquals("Happiness", updatedDoc.getString("emotion"));
        assertEquals("Updated", updatedDoc.getString("explanation"));
        assertEquals("new_image", updatedDoc.getString("imageUri"));
        assertEquals("Park", updatedDoc.getString("location"));
        assertEquals("With one other person", updatedDoc.getString("socialSituation"));
    }

    @Test
    public void testDeleteEmotionPost() throws Exception {
        // First create a post to delete
        EmotionPost post = EmotionPost.create(
                "Disgust",
                "Delete me",
                null,
                "Nowhere",
                "Alone",
                "testUser");

        // Save directly to Firestore
        DocumentReference docRef = Tasks.await(
                db.collection("emotions").add(post),
                5, TimeUnit.SECONDS);
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
                });

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
                Timestamp.now().getNanoseconds());
        createTestPostWithTimestamp("Post 2", olderTimestamp);

        // Create post with newest timestamp
        Timestamp newerTimestamp = new Timestamp(
                Timestamp.now().getSeconds() + 3600, // 1 hour in future
                Timestamp.now().getNanoseconds());
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
                "Happiness",
                explanation,
                null,
                "Test",
                "Alone",
                "testUser");

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
                5, TimeUnit.SECONDS);

        testDocumentIds.add(docRef.getId());
    }

    @Test
    public void testEdgeCaseEmptyFields() throws Exception {
        // Test with empty but valid fields
        EmotionPost post = EmotionPost.create(
                "Happiness", // Required field
                "", // Empty explanation, but valid since we have an image
                "image_uri", // Has image
                "", // Empty location (optional)
                "Select social situation", // Default social situation
                "testUser" // Required field
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
                });

        // Wait for operation to complete
        assertTrue("Save operation timed out", latch.await(5, TimeUnit.SECONDS));

        // Verify document was saved correctly
        DocumentSnapshot document = Tasks.await(resultRef.get().get(), 5, TimeUnit.SECONDS);

        assertTrue("Document should exist", document.exists());
        assertEquals("Happiness", document.getString("emotion"));
        assertEquals("", document.getString("explanation"));
        assertEquals("image_uri", document.getString("imageUri"));
        assertEquals("", document.getString("location"));
        assertEquals("Select social situation", document.getString("socialSituation"));
    }

    @Test
    public void testErrorHandlingForInvalidPost() throws Exception {
        // Try to save an invalid post that should fail client-side validation
        // This requires modifying your repository to expose a method that skips
        // validation
        // or handling the exception in this test

        try {
            // This should throw an exception before reaching Firestore
            EmotionPost invalidPost = EmotionPost.create(
                    "", // Empty emotion - invalid
                    "", // Empty explanation
                    null, // No image
                    "Location",
                    "Alone",
                    "testUser");

            fail("Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Expected exception
            assertTrue(e.getMessage().contains("Emotion is required"));
        }
    }

    @Test
    public void testSavePostWithMaximumLengthExplanation() throws Exception {
        // Test with explanation at the character limit
        String maxLengthExplanation = "Twenty char explain"; // Exactly 20 characters

        EmotionPost testPost = EmotionPost.create(
                "Fear",
                maxLengthExplanation,
                null,
                "Home",
                "Alone",
                "testUser");

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
                });

        // Wait for operation to complete
        assertTrue("Save operation timed out", latch.await(5, TimeUnit.SECONDS));

        // Verify document contents
        DocumentSnapshot document = Tasks.await(resultRef.get().get(), 5, TimeUnit.SECONDS);
        assertEquals(maxLengthExplanation, document.getString("explanation"));
    }

    @Test
    public void testSavePostWithAllSocialSituationValues() throws Exception {
        // Test all valid social situation values
        String[] socialSituations = {
                "Select social situation", "Alone",
                "With one other person", "With two to several people", "With a crowd"
        };

        for (String situation : socialSituations) {
            EmotionPost testPost = EmotionPost.create(
                    "Happiness",
                    "Test",
                    null,
                    "Location",
                    situation,
                    "testUser");

            final CountDownLatch latch = new CountDownLatch(1);
            final AtomicReference<DocumentReference> resultRef = new AtomicReference<>();

            repository.saveEmotionPostToFirestore(
                    testPost,
                    documentRef -> {
                        resultRef.set(documentRef);
                        testDocumentIds.add(documentRef.getId());
                        latch.countDown();
                    },
                    error -> {
                        System.err.println("Failed for situation " + situation + ": " + error);
                        latch.countDown();
                    });

            assertTrue("Save operation timed out for " + situation, latch.await(5, TimeUnit.SECONDS));

            DocumentSnapshot document = Tasks.await(resultRef.get().get(), 5, TimeUnit.SECONDS);
            assertEquals(situation, document.getString("socialSituation"));
        }
    }

    @Test
    public void testEmotionPostTimestampIsSet() throws Exception {
        // Test that timestamp is automatically set
        EmotionPost testPost = EmotionPost.create(
                "Surprise",
                "Test",
                null,
                "Location",
                "Alone",
                "testUser");

        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicReference<DocumentReference> resultRef = new AtomicReference<>();

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
                });

        assertTrue("Save operation timed out", latch.await(5, TimeUnit.SECONDS));

        DocumentSnapshot document = Tasks.await(resultRef.get().get(), 5, TimeUnit.SECONDS);
        Timestamp timestamp = document.getTimestamp("timestamp");

        assertNotNull("Timestamp should be set", timestamp);

        // Timestamp should be recent (within last minute)
        long currentTimeSeconds = System.currentTimeMillis() / 1000;
        long postTimeSeconds = timestamp.getSeconds();
        long differenceSeconds = Math.abs(currentTimeSeconds - postTimeSeconds);

        assertTrue("Timestamp should be recent", differenceSeconds < 60);
    }

    @Test
    public void testUpdateNonexistentPost() throws Exception {
        // Test updating a post that doesn't exist
        String nonExistentId = "nonexistent_doc_id_" + System.currentTimeMillis();
        EmotionPost post = EmotionPost.create(
                "Disgust",
                "Test",
                null,
                "Location",
                "Alone",
                "testUser");

        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicReference<Exception> resultError = new AtomicReference<>();

        repository.updateEmotionPost(
                nonExistentId,
                post,
                () -> {
                    latch.countDown();
                },
                error -> {
                    resultError.set(error);
                    latch.countDown();
                });

        assertTrue("Update operation timed out", latch.await(5, TimeUnit.SECONDS));

        // Should succeed but not throw an error even if document doesn't exist
        // This is Firebase behavior - updates to non-existent docs create them
        assertNull("No error expected for updating non-existent document", resultError.get());

        // Optional verification that document was created
        DocumentSnapshot document = Tasks.await(
                db.collection("emotions").document(nonExistentId).get(),
                5, TimeUnit.SECONDS);

        if (document.exists()) {
            testDocumentIds.add(nonExistentId); // Add to cleanup list
        }
    }

    @Test
    public void testBatchOperations() throws Exception {
        // Test creating multiple posts and retrieving them in order
        List<String> docIds = new ArrayList<>();

        // Create 5 posts
        for (int i = 0; i < 5; i++) {
            EmotionPost post = EmotionPost.create(
                    "Happiness",
                    "Post " + i,
                    null,
                    "Location " + i,
                    "Alone",
                    "testUser");

            DocumentReference docRef = Tasks.await(
                    db.collection("emotions").add(post),
                    5, TimeUnit.SECONDS);

            docIds.add(docRef.getId());
            testDocumentIds.add(docRef.getId());

            // Small delay to ensure timestamps differ
            Thread.sleep(100);
        }

        // Get all posts using query
        QuerySnapshot querySnapshot = Tasks.await(
                repository.getPostsQuery().get(),
                5, TimeUnit.SECONDS);

        // Should have at least the 5 posts we created
        assertTrue("Query should return at least 5 documents", querySnapshot.size() >= 5);

        // Verify all created posts can be retrieved by ID
        for (String docId : docIds) {
            final CountDownLatch latch = new CountDownLatch(1);
            final AtomicReference<EmotionPost> resultPost = new AtomicReference<>();

            repository.getEmotionPost(
                    docId,
                    post -> {
                        resultPost.set(post);
                        latch.countDown();
                    },
                    error -> {
                        System.err.println("Failed to get post: " + error);
                        latch.countDown();
                    });

            assertTrue("Get operation timed out", latch.await(5, TimeUnit.SECONDS));
            assertNotNull("Post should be retrieved by ID", resultPost.get());
            assertEquals("Post should have the expected emotion", "Happiness", resultPost.get().getEmotion());
        }
    }

    @Test
    public void testConcurrentOperations() throws Exception {
        // Test multiple concurrent operations
        final int numOperations = 5;
        final CountDownLatch allOperationsLatch = new CountDownLatch(numOperations);
        final List<Exception> errors = new ArrayList<>();

        for (int i = 0; i < numOperations; i++) {
            final int index = i;

            // Create posts concurrently
            new Thread(() -> {
                try {
                    EmotionPost post = EmotionPost.create(
                            "Fear",
                            "Concurrent " + index,
                            null,
                            "Location",
                            "Alone",
                            "testUser" + index);

                    final CountDownLatch opLatch = new CountDownLatch(1);

                    repository.saveEmotionPostToFirestore(
                            post,
                            documentRef -> {
                                synchronized (testDocumentIds) {
                                    testDocumentIds.add(documentRef.getId());
                                }
                                opLatch.countDown();
                            },
                            error -> {
                                synchronized (errors) {
                                    errors.add(error);
                                }
                                opLatch.countDown();
                            });

                    opLatch.await(5, TimeUnit.SECONDS);
                } catch (Exception e) {
                    synchronized (errors) {
                        errors.add(e);
                    }
                } finally {
                    allOperationsLatch.countDown();
                }
            }).start();
        }

        // Wait for all operations to complete
        assertTrue("Concurrent operations timed out",
                allOperationsLatch.await(15, TimeUnit.SECONDS));

        // No errors should have occurred
        assertEquals("No errors should occur during concurrent operations", 0, errors.size());
    }

    @Test
    public void testDeleteOperationIdempotence() throws Exception {
        // Create a post
        EmotionPost post = EmotionPost.create(
                "Shame",
                "Delete test",
                null,
                "Home",
                "Alone",
                "testUser");

        DocumentReference docRef = Tasks.await(
                db.collection("emotions").add(post),
                5, TimeUnit.SECONDS);
        String docId = docRef.getId();

        // First delete - should succeed
        final CountDownLatch firstLatch = new CountDownLatch(1);
        repository.deleteEmotionPost(
                docId,
                () -> firstLatch.countDown(),
                error -> {
                    System.err.println("First delete failed: " + error);
                    firstLatch.countDown();
                });
        assertTrue("First delete timed out", firstLatch.await(5, TimeUnit.SECONDS));

        // Verify document no longer exists
        DocumentSnapshot checkAfterFirst = Tasks.await(docRef.get(), 5, TimeUnit.SECONDS);
        assertFalse("Document should not exist after first delete", checkAfterFirst.exists());

        // Second delete of same ID - should not cause errors
        final CountDownLatch secondLatch = new CountDownLatch(1);
        final AtomicReference<Boolean> secondDeleteSucceeded = new AtomicReference<>(false);

        repository.deleteEmotionPost(
                docId,
                () -> {
                    secondDeleteSucceeded.set(true);
                    secondLatch.countDown();
                },
                error -> {
                    System.err.println("Second delete failed: " + error);
                    secondLatch.countDown();
                });
        assertTrue("Second delete timed out", secondLatch.await(5, TimeUnit.SECONDS));

        // Second delete should have "succeeded" (Firebase treats delete as idempotent)
        assertTrue("Second delete should succeed", secondDeleteSucceeded.get());
    }

    @Test
    public void testInvalidEmotionValues() throws Exception {
        // Test various invalid input values
        String[] invalidEmotions = { "", "  ", null, "invalid_emotion" };

        for (String emotion : invalidEmotions) {
            try {
                EmotionPost.create(
                        emotion,
                        "Test",
                        "image_uri",
                        "Location",
                        "Alone",
                        "testUser");
                fail("Should throw exception for invalid emotion: " + emotion);
            } catch (IllegalArgumentException e) {
                // Expected
            }
        }
    }
}