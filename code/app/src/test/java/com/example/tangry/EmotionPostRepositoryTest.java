package com.example.tangry;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import com.example.tangry.models.EmotionPost;
import com.example.tangry.repositories.EmotionPostRepository;
import com.example.tangry.datasource.FirebaseDataSource;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.FieldValue;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

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

        // When repository's FirebaseDataSource calls collection("emotions"), return our mock.
        when(mockFirestore.collection("emotions")).thenReturn(mockCollection);

        // Setup for add operation
        when(mockCollection.add(anyMap())).thenReturn(mockAddTask);
        when(mockAddTask.addOnSuccessListener(any(OnSuccessListener.class))).thenReturn(mockAddTask);
        when(mockAddTask.addOnFailureListener(any(OnFailureListener.class))).thenReturn(mockAddTask);

        // Setup for update operation
        when(mockCollection.document(anyString())).thenReturn(mockDocument);
        when(mockDocument.set(any(EmotionPost.class))).thenReturn(mockSetTask);
        when(mockSetTask.addOnSuccessListener(any(OnSuccessListener.class))).thenReturn(mockSetTask);
        when(mockSetTask.addOnFailureListener(any(OnFailureListener.class))).thenReturn(mockSetTask);

        // Setup for delete operation
        when(mockDocument.delete()).thenReturn(mockDeleteTask);
        when(mockDeleteTask.addOnSuccessListener(any(OnSuccessListener.class))).thenReturn(mockDeleteTask);
        when(mockDeleteTask.addOnFailureListener(any(OnFailureListener.class))).thenReturn(mockDeleteTask);

        // Setup for get operation
        when(mockDocument.get()).thenReturn(mockGetTask);
        when(mockGetTask.addOnSuccessListener(any(OnSuccessListener.class))).thenReturn(mockGetTask);
        when(mockGetTask.addOnFailureListener(any(OnFailureListener.class))).thenReturn(mockGetTask);

        // Inject the mocked Firestore into the repository via FirebaseDataSource.
        repository = new EmotionPostRepository();
    }

    @Test
    public void testSaveEmotionPostToFirestore() {
        EmotionPost post = EmotionPost.create(
                "happiness",
                "Test",
                "image_uri",
                "Location",
                "Alone",
                "testUser"
        );

        OnSuccessListener<DocumentReference> mockSuccessListener = mock(OnSuccessListener.class);
        OnFailureListener mockFailureListener = mock(OnFailureListener.class);

        repository.saveEmotionPostToFirestore(post, mockSuccessListener, mockFailureListener);

        ArgumentCaptor<Map<String, Object>> mapCaptor = ArgumentCaptor.forClass(Map.class);
        verify(mockCollection).add(mapCaptor.capture());
        Map<String, Object> capturedMap = mapCaptor.getValue();
        assertEquals("happiness", capturedMap.get("emotion"));
        assertEquals("Test", capturedMap.get("explanation"));
        assertEquals("image_uri", capturedMap.get("imageUri"));
        assertEquals("Location", capturedMap.get("location"));
        assertEquals("Alone", capturedMap.get("socialSituation"));
        assertEquals("testUser", capturedMap.get("username"));

        verify(mockAddTask).addOnSuccessListener(mockSuccessListener);
        verify(mockAddTask).addOnFailureListener(mockFailureListener);
    }

    @Test
    public void testGetEmotionPostWhenExists() {
        String postId = "test_post_id";
        EmotionPost expectedPost = EmotionPost.create(
                "sadness",
                "Feeling sad",
                null,
                "Home",
                "Alone",
                "testUser"
        );

        when(mockDocumentSnapshot.exists()).thenReturn(true);
        when(mockDocumentSnapshot.toObject(EmotionPost.class)).thenReturn(expectedPost);

        Consumer<EmotionPost> mockSuccessConsumer = mock(Consumer.class);
        OnFailureListener mockFailureListener = mock(OnFailureListener.class);

        repository.getEmotionPost(postId, mockSuccessConsumer, mockFailureListener);

        // Capture and trigger success on the get task.
        ArgumentCaptor<OnSuccessListener> successCaptor = ArgumentCaptor.forClass(OnSuccessListener.class);
        verify(mockGetTask).addOnSuccessListener(successCaptor.capture());
        successCaptor.getValue().onSuccess(mockDocumentSnapshot);

        verify(mockCollection).document(postId);
        verify(mockSuccessConsumer).accept(expectedPost);
        verify(mockFailureListener, never()).onFailure(any(Exception.class));
    }

    @Test
    public void testGetEmotionPostWhenNotExists() {
        String postId = "nonexistent_post_id";

        when(mockDocumentSnapshot.exists()).thenReturn(false);

        Consumer<EmotionPost> mockSuccessConsumer = mock(Consumer.class);
        OnFailureListener mockFailureListener = mock(OnFailureListener.class);

        repository.getEmotionPost(postId, mockSuccessConsumer, mockFailureListener);

        ArgumentCaptor<OnSuccessListener> successCaptor = ArgumentCaptor.forClass(OnSuccessListener.class);
        verify(mockGetTask).addOnSuccessListener(successCaptor.capture());
        successCaptor.getValue().onSuccess(mockDocumentSnapshot);

        verify(mockCollection).document(postId);
        verify(mockSuccessConsumer).accept(null);
    }

    @Test
    public void testGetEmotionPostFailure() {
        String postId = "test_post_id";
        Exception expectedException = new RuntimeException("Test exception");

        Consumer<EmotionPost> mockSuccessConsumer = mock(Consumer.class);
        OnFailureListener mockFailureListener = mock(OnFailureListener.class);

        repository.getEmotionPost(postId, mockSuccessConsumer, mockFailureListener);

        // Capture and simulate failure on the get task.
        ArgumentCaptor<OnFailureListener> failureCaptor = ArgumentCaptor.forClass(OnFailureListener.class);
        verify(mockGetTask).addOnFailureListener(failureCaptor.capture());
        failureCaptor.getValue().onFailure(expectedException);

        verify(mockCollection).document(postId);
        verify(mockFailureListener).onFailure(expectedException);
        verify(mockSuccessConsumer, never()).accept(any());
    }

    @Test
    public void testUpdateEmotionPostSuccess() {
        String postId = "test_post_id";
        EmotionPost post = EmotionPost.create(
                "fear",
                "Scary",
                "image_uri",
                "Outside",
                "With one other person",
                "testUser"
        );

        Runnable mockSuccessRunnable = mock(Runnable.class);
        OnFailureListener mockFailureListener = mock(OnFailureListener.class);

        repository.updateEmotionPost(postId, post, mockSuccessRunnable, mockFailureListener);

        verify(mockCollection).document(postId);
        verify(mockDocument).set(post);

        // Capture and trigger success on the update task.
        ArgumentCaptor<OnSuccessListener> successCaptor = ArgumentCaptor.forClass(OnSuccessListener.class);
        verify(mockSetTask).addOnSuccessListener(successCaptor.capture());
        successCaptor.getValue().onSuccess(null);

        verify(mockSuccessRunnable).run();
        verify(mockFailureListener, never()).onFailure(any(Exception.class));
    }

    @Test
    public void testUpdateEmotionPostFailure() {
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

        Runnable mockSuccessRunnable = mock(Runnable.class);
        OnFailureListener mockFailureListener = mock(OnFailureListener.class);

        repository.updateEmotionPost(postId, post, mockSuccessRunnable, mockFailureListener);

        verify(mockCollection).document(postId);
        verify(mockDocument).set(post);

        // Capture and simulate failure on the update task.
        ArgumentCaptor<OnFailureListener> failureCaptor = ArgumentCaptor.forClass(OnFailureListener.class);
        verify(mockSetTask).addOnFailureListener(failureCaptor.capture());
        failureCaptor.getValue().onFailure(expectedException);

        verify(mockFailureListener).onFailure(expectedException);
        verify(mockSuccessRunnable, never()).run();
    }

    @Test
    public void testDeleteEmotionPostSuccess() {
        String postId = "test_post_id";

        Runnable mockSuccessRunnable = mock(Runnable.class);
        OnFailureListener mockFailureListener = mock(OnFailureListener.class);

        repository.deleteEmotionPost(postId, mockSuccessRunnable, mockFailureListener);

        verify(mockCollection).document(postId);
        verify(mockDocument).delete();

        // Capture and trigger success on the delete task.
        ArgumentCaptor<OnSuccessListener> successCaptor = ArgumentCaptor.forClass(OnSuccessListener.class);
        verify(mockDeleteTask).addOnSuccessListener(successCaptor.capture());
        successCaptor.getValue().onSuccess(null);

        verify(mockSuccessRunnable).run();
        verify(mockFailureListener, never()).onFailure(any(Exception.class));
    }

    @Test
    public void testDeleteEmotionPostFailure() {
        String postId = "test_post_id";
        Exception expectedException = new RuntimeException("Delete failed");

        Runnable mockSuccessRunnable = mock(Runnable.class);
        OnFailureListener mockFailureListener = mock(OnFailureListener.class);

        repository.deleteEmotionPost(postId, mockSuccessRunnable, mockFailureListener);

        verify(mockCollection).document(postId);
        verify(mockDocument).delete();

        // Capture and simulate failure on the delete task.
        ArgumentCaptor<OnFailureListener> failureCaptor = ArgumentCaptor.forClass(OnFailureListener.class);
        verify(mockDeleteTask).addOnFailureListener(failureCaptor.capture());
        failureCaptor.getValue().onFailure(expectedException);

        verify(mockFailureListener).onFailure(expectedException);
        verify(mockSuccessRunnable, never()).run();
    }

    @Test
    public void testGetPostsQuery() {
        Query mockQuery = mock(Query.class);
        when(mockCollection.orderBy("timestamp", Query.Direction.DESCENDING)).thenReturn(mockQuery);

        Query result = repository.getPostsQuery();

        // Verify that the repository calls collection("emotions") on the injected Firestore.
        verify(mockFirestore).collection("emotions");
        verify(mockCollection).orderBy("timestamp", Query.Direction.DESCENDING);
        assertEquals(mockQuery, result);
    }
}
