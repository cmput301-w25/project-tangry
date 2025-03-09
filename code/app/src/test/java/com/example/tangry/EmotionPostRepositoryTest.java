package com.example.tangry;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.example.tangry.models.EmotionPost;
import com.example.tangry.repositories.EmotionPostRepository;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.*;

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
    // Existing mocks and setup
    @Mock
    private FirebaseFirestore mockFirestore;
    
    @Mock
    private CollectionReference mockCollection;
    
    @Mock
    private DocumentReference mockDocument;
    
    @Mock
    private Task<DocumentReference> mockAddTask;
    
    @Mock
    private Task<Void> mockSetTask;
    
    @Mock
    private Task<Void> mockDeleteTask;
    
    @Mock
    private Task<DocumentSnapshot> mockGetTask;
    
    @Mock
    private DocumentSnapshot mockDocumentSnapshot;
    
    private EmotionPostRepository repository;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Set up the mocking chain for add operation
        when(mockFirestore.collection("emotions")).thenReturn(mockCollection);
        when(mockCollection.add(anyMap())).thenReturn(mockAddTask);
        when(mockAddTask.addOnSuccessListener(any(OnSuccessListener.class))).thenReturn(mockAddTask);
        when(mockAddTask.addOnFailureListener(any(OnFailureListener.class))).thenReturn(mockAddTask);
        
        // Set up mocking chain for update operation
        when(mockCollection.document(anyString())).thenReturn(mockDocument);
        when(mockDocument.set(any(EmotionPost.class))).thenReturn(mockSetTask);
        when(mockSetTask.addOnSuccessListener(any(OnSuccessListener.class))).thenReturn(mockSetTask);
        when(mockSetTask.addOnFailureListener(any(OnFailureListener.class))).thenReturn(mockSetTask);
        
        // Set up mocking chain for delete operation
        when(mockDocument.delete()).thenReturn(mockDeleteTask);
        when(mockDeleteTask.addOnSuccessListener(any(OnSuccessListener.class))).thenReturn(mockDeleteTask);
        when(mockDeleteTask.addOnFailureListener(any(OnFailureListener.class))).thenReturn(mockDeleteTask);
        
        // Set up mocking chain for get operation
        when(mockDocument.get()).thenReturn(mockGetTask);
        when(mockGetTask.addOnSuccessListener(any(OnSuccessListener.class))).thenReturn(mockGetTask);
        when(mockGetTask.addOnFailureListener(any(OnFailureListener.class))).thenReturn(mockGetTask);
        
        // Create repository with mocked Firestore
        repository = new EmotionPostRepository(mockFirestore);
    }

    @Test
    public void testSaveEmotionPostToFirestore() {
        // Create a test post
        EmotionPost post = EmotionPost.create(
            "happiness",
            "Test",
            "image_uri",
            "Location",
            "Alone",
            "testUser"
        );
        
        // Mock callbacks
        OnSuccessListener<DocumentReference> mockSuccessListener = mock(OnSuccessListener.class);
        OnFailureListener mockFailureListener = mock(OnFailureListener.class);
        
        // Call the repository method
        repository.saveEmotionPostToFirestore(post, mockSuccessListener, mockFailureListener);
        
        // Verify that collection.add() was called with the correct data
        ArgumentCaptor<Map<String, Object>> mapCaptor = ArgumentCaptor.forClass(Map.class);
        verify(mockCollection).add(mapCaptor.capture());
        
        Map<String, Object> capturedMap = mapCaptor.getValue();
        assertEquals("happiness", capturedMap.get("emotion"));
        assertEquals("Test", capturedMap.get("explanation"));
        assertEquals("image_uri", capturedMap.get("imageUri"));
        assertEquals("Location", capturedMap.get("location"));
        assertEquals("Alone", capturedMap.get("socialSituation"));
        assertEquals("testUser", capturedMap.get("username"));
        
        // Verify listeners were added
        verify(mockAddTask).addOnSuccessListener(mockSuccessListener);
        verify(mockAddTask).addOnFailureListener(mockFailureListener);
    }
    
    // Test getEmotionPost when post exists
    @Test
    public void testGetEmotionPostWhenExists() {
        // Create test data
        String postId = "test_post_id";
        EmotionPost expectedPost = EmotionPost.create(
            "sadness", 
            "Feeling sad", 
            null, 
            "Home", 
            "Alone", 
            "testUser"
        );
        
        // Set up mock to return a post when document exists
        when(mockDocumentSnapshot.exists()).thenReturn(true);
        when(mockDocumentSnapshot.toObject(EmotionPost.class)).thenReturn(expectedPost);
        
        // Mock the success callback to capture the result
        Consumer<EmotionPost> mockSuccessConsumer = mock(Consumer.class);
        Consumer<Exception> mockFailureConsumer = mock(Consumer.class);
        
        // Call the repository method
        repository.getEmotionPost(postId, mockSuccessConsumer, mockFailureConsumer);
        
        // Capture and execute the success listener
        ArgumentCaptor<OnSuccessListener> successCaptor = ArgumentCaptor.forClass(OnSuccessListener.class);
        verify(mockGetTask).addOnSuccessListener(successCaptor.capture());
        successCaptor.getValue().onSuccess(mockDocumentSnapshot);
        
        // Verify the correct document was requested
        verify(mockCollection).document(postId);
        
        // Verify the result was passed to the success consumer
        verify(mockSuccessConsumer).accept(expectedPost);
        
        // Verify failure consumer was never called
        verify(mockFailureConsumer, never()).accept(any(Exception.class));
    }
    
    // Test getEmotionPost when post does not exist
    @Test
    public void testGetEmotionPostWhenNotExists() {
        // Create test data
        String postId = "nonexistent_post_id";
        
        // Set up mock to return no post (document doesn't exist)
        when(mockDocumentSnapshot.exists()).thenReturn(false);
        
        // Mock the callbacks
        Consumer<EmotionPost> mockSuccessConsumer = mock(Consumer.class);
        Consumer<Exception> mockFailureConsumer = mock(Consumer.class);
        
        // Call the repository method
        repository.getEmotionPost(postId, mockSuccessConsumer, mockFailureConsumer);
        
        // Capture and execute the success listener
        ArgumentCaptor<OnSuccessListener> successCaptor = ArgumentCaptor.forClass(OnSuccessListener.class);
        verify(mockGetTask).addOnSuccessListener(successCaptor.capture());
        successCaptor.getValue().onSuccess(mockDocumentSnapshot);
        
        // Verify the correct document was requested
        verify(mockCollection).document(postId);
        
        // Verify the null result was passed to the success consumer
        verify(mockSuccessConsumer).accept(null);
    }
    
    // Test getEmotionPost with failure
    @Test
    public void testGetEmotionPostFailure() {
        // Create test data
        String postId = "test_post_id";
        Exception expectedException = new RuntimeException("Test exception");
        
        // Mock the callbacks
        Consumer<EmotionPost> mockSuccessConsumer = mock(Consumer.class);
        Consumer<Exception> mockFailureConsumer = mock(Consumer.class);
        
        // Call the repository method
        repository.getEmotionPost(postId, mockSuccessConsumer, mockFailureConsumer);
        
        // Capture and execute the failure listener
        ArgumentCaptor<OnFailureListener> failureCaptor = ArgumentCaptor.forClass(OnFailureListener.class);
        verify(mockGetTask).addOnFailureListener(failureCaptor.capture());
        failureCaptor.getValue().onFailure(expectedException);
        
        // Verify the correct document was requested
        verify(mockCollection).document(postId);
        
        // Verify the exception was passed to the failure consumer
        verify(mockFailureConsumer).accept(expectedException);
        
        // Verify success consumer was never called
        verify(mockSuccessConsumer, never()).accept(any());
    }
    
    // Test updateEmotionPost with success
    @Test
    public void testUpdateEmotionPostSuccess() {
        // Create test data
        String postId = "test_post_id";
        EmotionPost post = EmotionPost.create(
            "fear", 
            "Scary", 
            "image_uri", 
            "Outside", 
            "With one other person", 
            "testUser"
        );
        
        // Mock the callbacks
        Runnable mockSuccessRunnable = mock(Runnable.class);
        OnFailureListener mockFailureListener = mock(OnFailureListener.class);
        
        // Call the repository method
        repository.updateEmotionPost(postId, post, mockSuccessRunnable, mockFailureListener);
        
        // Verify the correct document was updated with the post
        verify(mockCollection).document(postId);
        verify(mockDocument).set(post);
        
        // Capture and execute the success listener
        ArgumentCaptor<OnSuccessListener> successCaptor = ArgumentCaptor.forClass(OnSuccessListener.class);
        verify(mockSetTask).addOnSuccessListener(successCaptor.capture());
        successCaptor.getValue().onSuccess(null);
        
        // Verify the success callback was executed
        verify(mockSuccessRunnable).run();
        
        // Verify failure listener was never called
        verify(mockFailureListener, never()).onFailure(any(Exception.class));
    }
    
    // Test updateEmotionPost with failure
    @Test
    public void testUpdateEmotionPostFailure() {
        // Create test data
        String postId = "test_post_id";
        EmotionPost post = EmotionPost.create(
            "anger", 
            "Angry", 
            null, 
            "Work", 
            "With a crowd", 
            "testUser"
        );
        Exception expectedException = new RuntimeException("Update failed");
        
        // Mock the callbacks
        Runnable mockSuccessRunnable = mock(Runnable.class);
        OnFailureListener mockFailureListener = mock(OnFailureListener.class);
        
        // Call the repository method
        repository.updateEmotionPost(postId, post, mockSuccessRunnable, mockFailureListener);
        
        // Verify the correct document was attempted to be updated
        verify(mockCollection).document(postId);
        verify(mockDocument).set(post);
        
        // Capture and execute the failure listener
        ArgumentCaptor<OnFailureListener> failureCaptor = ArgumentCaptor.forClass(OnFailureListener.class);
        verify(mockSetTask).addOnFailureListener(failureCaptor.capture());
        failureCaptor.getValue().onFailure(expectedException);
        
        // Verify the failure callback was executed with the exception
        verify(mockFailureListener).onFailure(expectedException);
        
        // Verify success runnable was never called
        verify(mockSuccessRunnable, never()).run();
    }
    
    // Test deleteEmotionPost with success
    @Test
    public void testDeleteEmotionPostSuccess() {
        // Create test data
        String postId = "test_post_id";
        
        // Mock the callbacks
        Runnable mockSuccessRunnable = mock(Runnable.class);
        Consumer<Exception> mockFailureConsumer = mock(Consumer.class);
        
        // Call the repository method
        repository.deleteEmotionPost(postId, mockSuccessRunnable, mockFailureConsumer);
        
        // Verify the correct document was deleted
        verify(mockCollection).document(postId);
        verify(mockDocument).delete();
        
        // Capture and execute the success listener
        ArgumentCaptor<OnSuccessListener> successCaptor = ArgumentCaptor.forClass(OnSuccessListener.class);
        verify(mockDeleteTask).addOnSuccessListener(successCaptor.capture());
        successCaptor.getValue().onSuccess(null);
        
        // Verify the success callback was executed
        verify(mockSuccessRunnable).run();
        
        // Verify failure consumer was never called
        verify(mockFailureConsumer, never()).accept(any(Exception.class));
    }
    
    // Test deleteEmotionPost with failure
    @Test
    public void testDeleteEmotionPostFailure() {
        // Create test data
        String postId = "test_post_id";
        Exception expectedException = new RuntimeException("Delete failed");
        
        // Mock the callbacks
        Runnable mockSuccessRunnable = mock(Runnable.class);
        Consumer<Exception> mockFailureConsumer = mock(Consumer.class);
        
        // Call the repository method
        repository.deleteEmotionPost(postId, mockSuccessRunnable, mockFailureConsumer);
        
        // Verify the correct document was attempted to be deleted
        verify(mockCollection).document(postId);
        verify(mockDocument).delete();
        
        // Capture and execute the failure listener
        ArgumentCaptor<OnFailureListener> failureCaptor = ArgumentCaptor.forClass(OnFailureListener.class);
        verify(mockDeleteTask).addOnFailureListener(failureCaptor.capture());
        failureCaptor.getValue().onFailure(expectedException);
        
        // Verify the failure callback was executed with the exception
        verify(mockFailureConsumer).accept(expectedException);
        
        // Verify success runnable was never called
        verify(mockSuccessRunnable, never()).run();
    }
    
    // Test getPostsQuery returns properly configured query
    @Test
    public void testGetPostsQuery() {
        // Mock for query and its orderBy method
        Query mockQuery = mock(Query.class);
        when(mockCollection.orderBy("timestamp", Query.Direction.DESCENDING)).thenReturn(mockQuery);
        
        // Call the repository method
        Query result = repository.getPostsQuery();
        
        // Verify the collection reference was obtained
        verify(mockFirestore).collection("emotions");
        
        // Verify the proper ordering was applied
        verify(mockCollection).orderBy("timestamp", Query.Direction.DESCENDING);
        
        // Verify the result is what we expect
        assertEquals(mockQuery, result);
    }
}