package com.example.tangry;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.tangry.models.EmotionPost;
import com.example.tangry.repositories.EmotionPostRepository;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

@RunWith(MockitoJUnitRunner.class)
public class EmotionPostRepositoryTest {

    @Mock
    private FirebaseFirestore mockFirestore;

    @Mock
    private CollectionReference mockCollection;

    @Mock
    private DocumentReference mockDocument;

    @Mock
    private Task<DocumentReference> mockDocRefTask;

    @Mock
    private Task<Void> mockVoidTask;

    @Mock
    private OnSuccessListener<DocumentReference> mockSuccessListener;

    @Mock
    private OnFailureListener mockFailureListener;

    // Use reflection to set the mockFirestore in our singleton repository
    private EmotionPostRepository repository;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        // Get the singleton instance
        repository = EmotionPostRepository.getInstance();

        // Use reflection to replace the private db field with our mock
        java.lang.reflect.Field dbField = EmotionPostRepository.class.getDeclaredField("db");
        dbField.setAccessible(true);
        dbField.set(repository, mockFirestore);

        // Set up common mock behaviors
        when(mockFirestore.collection(any())).thenReturn(mockCollection);
        when(mockCollection.document(any())).thenReturn(mockDocument);
        when(mockCollection.add(anyMap())).thenReturn(mockDocRefTask);
        when(mockDocument.set(any())).thenReturn(mockVoidTask);
        when(mockDocument.delete()).thenReturn(mockVoidTask);
    }

    @Test
    public void testSaveEmotionPostToFirestore() {
        // Create a valid emotion post
        EmotionPost post = EmotionPost.create(
                "happiness",
                "I am happy",
                null,
                "Home",
                "Alone",
                "testUser");

        // Set up Mockito to capture the data passed to Firestore
        ArgumentCaptor<Map<String, Object>> dataCaptor = ArgumentCaptor.forClass(Map.class);

        // Call the method under test
        repository.saveEmotionPostToFirestore(post, mockSuccessListener, mockFailureListener);

        // Verify interactions
        verify(mockFirestore).collection(eq("emotions"));
        verify(mockCollection).add(dataCaptor.capture());
        verify(mockDocRefTask).addOnSuccessListener(mockSuccessListener);
        verify(mockDocRefTask).addOnFailureListener(mockFailureListener);

        // Verify the data is correct
        Map<String, Object> capturedData = dataCaptor.getValue();
        assertEquals("happiness", capturedData.get("emotion"));
        assertEquals("I am happy", capturedData.get("explanation"));
        assertNull(capturedData.get("imageUri"));
        assertEquals("Home", capturedData.get("location"));
        assertEquals("Alone", capturedData.get("socialSituation"));
        assertEquals("testUser", capturedData.get("username"));
        assertNotNull(capturedData.get("timestamp")); // Should be FieldValue.serverTimestamp()
    }

    @Test
    public void testUpdateEmotionPost() {
        // Create a valid emotion post
        EmotionPost post = EmotionPost.create(
                "sadness",
                "I am sad",
                null,
                "Office",
                "With one other person",
                "testUser");

        String postId = "post123";
        Runnable mockSuccessRunnable = mock(Runnable.class);

        // Call the method under test
        repository.updateEmotionPost(postId, post, mockSuccessRunnable, mockFailureListener);

        // Verify interactions
        verify(mockFirestore).collection(eq("emotions"));
        verify(mockCollection).document(eq(postId));
        verify(mockDocument).set(eq(post));
        verify(mockVoidTask).addOnSuccessListener(any());
        verify(mockVoidTask).addOnFailureListener(eq(mockFailureListener));

        // Simulate success callback
        ArgumentCaptor<OnSuccessListener<Void>> successCaptor = ArgumentCaptor.forClass(OnSuccessListener.class);
        verify(mockVoidTask).addOnSuccessListener(successCaptor.capture());
        successCaptor.getValue().onSuccess(null);

        // Verify success callback was triggered
        verify(mockSuccessRunnable).run();
    }

    @Test
    public void testDeleteEmotionPost() {
        String postId = "post456";
        Runnable mockSuccessRunnable = mock(Runnable.class);
        Consumer<Exception> mockFailureConsumer = mock(Consumer.class);

        // Call the method under test
        repository.deleteEmotionPost(postId, mockSuccessRunnable, mockFailureConsumer);

        // Verify interactions
        verify(mockFirestore).collection(eq("emotions"));
        verify(mockCollection).document(eq(postId));
        verify(mockDocument).delete();

        // Simulate success callback
        ArgumentCaptor<OnSuccessListener<Void>> successCaptor = ArgumentCaptor.forClass(OnSuccessListener.class);
        verify(mockVoidTask).addOnSuccessListener(successCaptor.capture());
        successCaptor.getValue().onSuccess(null);

        // Verify success callback was triggered
        verify(mockSuccessRunnable).run();
    }

    @Test
    public void testGetEmotionPost() {
        String postId = "post789";
        Consumer<EmotionPost> mockSuccessConsumer = mock(Consumer.class);
        Consumer<Exception> mockFailureConsumer = mock(Consumer.class);

        // Mock the get() operation and its task
        Task<com.google.firebase.firestore.DocumentSnapshot> mockDocSnapshotTask = mock(Task.class);
        when(mockDocument.get()).thenReturn(mockDocSnapshotTask);

        // Call the method under test
        repository.getEmotionPost(postId, mockSuccessConsumer, mockFailureConsumer);

        // Verify basic interactions
        verify(mockFirestore).collection(eq("emotions"));
        verify(mockCollection).document(eq(postId));
        verify(mockDocument).get();
        verify(mockDocSnapshotTask).addOnSuccessListener(any());
        verify(mockDocSnapshotTask).addOnFailureListener(any());

        // For full testing, we would need to simulate the document snapshot callback
        // but that's more complex and requires additional mocking
    }

    @Test
    public void testCreateEmotionPostWithInvalidInputs() {
        // Test exception is thrown for empty emotion
        try {
            EmotionPost.create(
                    "", // Empty emotion - should fail
                    "Test",
                    null,
                    "Location",
                    "Alone",
                    "testUser");
            fail("Should have thrown IllegalArgumentException for empty emotion");
        } catch (IllegalArgumentException e) {
            // Expected
        }

        // Test exception for too long explanation
        try {
            EmotionPost.create(
                    "happiness",
                    "This explanation is way too long and should fail the validation",
                    null,
                    "Location",
                    "Alone",
                    "testUser");
            fail("Should have thrown IllegalArgumentException for too long explanation");
        } catch (IllegalArgumentException e) {
            // Expected
        }

        // Test exception for no explanation and no image
        try {
            EmotionPost.create(
                    "happiness",
                    "", // Empty explanation
                    null, // No image
                    "Location",
                    "Alone",
                    "testUser");
            fail("Should have thrown IllegalArgumentException for no explanation and no image");
        } catch (IllegalArgumentException e) {
            // Expected
        }

        // Test exception for invalid social situation
        try {
            EmotionPost.create(
                    "happiness",
                    "Test",
                    null,
                    "Location",
                    "Invalid Situation", // Invalid social situation
                    "testUser");
            fail("Should have thrown IllegalArgumentException for invalid social situation");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }
}