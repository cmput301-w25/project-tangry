package com.example.tangry;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.tangry.controllers.EmotionPostController;
import com.example.tangry.interfaces.SyncStatusListener;
import com.example.tangry.models.EmotionPost;
import com.example.tangry.models.SyncStatus;
import com.example.tangry.utils.NetworkMonitor;
import com.example.tangry.utils.OfflineSyncManager;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class OfflineSyncManagerTest {

    @Mock
    private Context mockContext;

    @Mock
    private SharedPreferences mockSharedPreferences;

    @Mock
    private SharedPreferences.Editor mockEditor;

    @Mock
    private NetworkMonitor mockNetworkMonitor;

    @Mock
    private EmotionPostController mockEmotionPostController;

    @Mock
    private SyncStatusListener mockListener;

    @Captor
    private ArgumentCaptor<SyncStatus> syncStatusCaptor;

    @Captor
    private ArgumentCaptor<String> messageCaptor;

    private OfflineSyncManager syncManager;

    @Before
    public void setUp() {
        // Stub getApplicationContext() to return the mock context itself
        when(mockContext.getApplicationContext()).thenReturn(mockContext);

        // Existing stubs
        when(mockContext.getSharedPreferences(anyString(), eq(Context.MODE_PRIVATE)))
                .thenReturn(mockSharedPreferences);
        when(mockSharedPreferences.edit()).thenReturn(mockEditor);
        when(mockEditor.putString(anyString(), anyString())).thenReturn(mockEditor);
        when(mockEditor.remove(anyString())).thenReturn(mockEditor);

        // Default empty preferences initially
        when(mockSharedPreferences.getString(anyString(), eq(null))).thenReturn("[]");

        // Create the OfflineSyncManager instance and register the listener
        syncManager = new OfflineSyncManager(mockContext, mockNetworkMonitor, mockEmotionPostController);
        syncManager.registerSyncStatusListener(mockListener);
    }

    @Test
    public void testAddPendingCreate_NotifiesListener() {
        EmotionPost post = EmotionPost.create("Happiness", "Test content", null, "Location", "Alone", "testUser");
        syncManager.addPendingCreate(post);
        verify(mockListener).onSyncStatusChanged(eq(SyncStatus.PENDING), anyString());
        verify(mockEditor).apply();
    }

    @Test
    public void testAddPendingUpdate_NotifiesListener() {
        EmotionPost post = EmotionPost.create("Happiness", "Test content", null, "Location", "Alone", "testUser");
        syncManager.addPendingUpdate("testId", post);
        verify(mockListener).onSyncStatusChanged(eq(SyncStatus.PENDING), anyString());
        verify(mockEditor).apply();
    }

    @Test
    public void testAddPendingDelete_NotifiesListener() {
        syncManager.addPendingDelete("testId");
        verify(mockListener).onSyncStatusChanged(eq(SyncStatus.PENDING), anyString());
        verify(mockEditor).apply();
    }

    @Test
    public void testHasPendingOperations_ReturnsTrueWhenOperationsExist() {
        when(mockSharedPreferences.getString(anyString(), eq(null)))
                .thenReturn("[{\"id\":\"test-id\",\"type\":\"CREATE\",\"timestamp\":1000,\"postId\":null}]");
        assertTrue(syncManager.hasPendingOperations());
        assertEquals(1, syncManager.getPendingOperationsCount());
    }

    @Test
    public void testHasPendingOperations_ReturnsFalseWhenNoOperations() {
        when(mockSharedPreferences.getString(anyString(), eq(null))).thenReturn("[]");
        assertFalse(syncManager.hasPendingOperations());
        assertEquals(0, syncManager.getPendingOperationsCount());
    }

    @Test
    public void testSyncPendingOperations_NoOperations_DoesNothing() {
        // Rely on default empty operations ("[]") from setUp.
        when(mockNetworkMonitor.isConnected()).thenReturn(true);
        syncManager.syncPendingOperations();
        verify(mockListener, never()).onSyncStatusChanged(any(SyncStatus.class), anyString());
    }

    @Test
    public void testSyncPendingOperations_NoNetwork_DoesNothing() {
        // No need to stub getString here since sync shouldn't run without network.
        when(mockNetworkMonitor.isConnected()).thenReturn(false);
        syncManager.syncPendingOperations();
        verify(mockListener, never()).onSyncStatusChanged(eq(SyncStatus.SYNCING), anyString());
    }

    @Test
    public void testSyncPendingOperations_WithNetwork_SyncsOperations() {
        String validJson = "[{\"id\":\"test-id\",\"type\":\"CREATE\",\"timestamp\":1000,\"postId\":null," +
                "\"postData\":\"{\\\"emotion\\\":\\\"Happy\\\",\\\"explanation\\\":\\\"Test\\\"," +
                "\\\"location\\\":\\\"Home\\\",\\\"username\\\":\\\"user\\\"}\"}]";
        when(mockSharedPreferences.getString(anyString(), eq(null))).thenReturn(validJson);
        when(mockNetworkMonitor.isConnected()).thenReturn(true);
        doAnswer(invocation -> {
            DocumentReference mockDocRef = mock(DocumentReference.class);
            OnSuccessListener<DocumentReference> successListener = invocation.getArgument(1);
            successListener.onSuccess(mockDocRef);
            return null;
        }).when(mockEmotionPostController).createPost(any(EmotionPost.class), any(), any());
        syncManager.syncPendingOperations();
        verify(mockListener).onSyncStatusChanged(eq(SyncStatus.SYNCING), anyString());
        verify(mockListener, times(1)).onSyncStatusChanged(eq(SyncStatus.SYNCED), anyString());
        verify(mockEmotionPostController).createPost(any(EmotionPost.class), any(), any());
    }

    @Test
    public void testNetworkAvailable_TriggersSync() {
        String validJson = "[{\"id\":\"test-id\",\"type\":\"UPDATE\",\"timestamp\":1000," +
                "\"postId\":\"post123\",\"postData\":\"{\\\"emotion\\\":\\\"Happy\\\",\\\"explanation\\\":\\\"Test\\\"," +
                "\\\"location\\\":\\\"Home\\\",\\\"username\\\":\\\"user\\\"}\"}]";
        when(mockSharedPreferences.getString(anyString(), eq(null))).thenReturn(validJson);
        when(mockNetworkMonitor.isConnected()).thenReturn(true);
        doAnswer(invocation -> {
            Runnable onSuccess = invocation.getArgument(2);
            onSuccess.run();
            return null;
        }).when(mockEmotionPostController).updateEmotionPost(anyString(), any(EmotionPost.class), any(), any());
        syncManager.onNetworkAvailable();
        verify(mockEmotionPostController).updateEmotionPost(anyString(), any(EmotionPost.class), any(), any());
    }

    @Test
    public void testClearPendingOperations_RemovesAllOperations() {
        // No need to stub getString; using the default value from setUp is sufficient.
        syncManager.clearPendingOperations();
        verify(mockEditor).remove(anyString());
        verify(mockEditor).apply();
    }

    @Test
    public void testMultipleOperations_ProcessedInOrder() {
        String multipleOpsJson = "[" +
                "{\"id\":\"id1\",\"type\":\"CREATE\",\"timestamp\":1000,\"postId\":null," +
                "\"postData\":\"{\\\"emotion\\\":\\\"Happy\\\",\\\"explanation\\\":\\\"Test1\\\"," +
                "\\\"location\\\":\\\"Home\\\",\\\"username\\\":\\\"user\\\"}\"}," +
                "{\"id\":\"id2\",\"type\":\"UPDATE\",\"timestamp\":2000,\"postId\":\"post123\"," +
                "\"postData\":\"{\\\"emotion\\\":\\\"Sad\\\",\\\"explanation\\\":\\\"Test2\\\"," +
                "\\\"location\\\":\\\"Work\\\",\\\"username\\\":\\\"user\\\"}\"}," +
                "{\"id\":\"id3\",\"type\":\"DELETE\",\"timestamp\":3000,\"postId\":\"post456\",\"postData\":null}" +
                "]";
        when(mockSharedPreferences.getString(anyString(), eq(null))).thenReturn(multipleOpsJson);
        when(mockNetworkMonitor.isConnected()).thenReturn(true);
        AtomicInteger operationCounter = new AtomicInteger(0);
        doAnswer(invocation -> {
            DocumentReference mockDocRef = mock(DocumentReference.class);
            OnSuccessListener<DocumentReference> successListener = invocation.getArgument(1);
            successListener.onSuccess(mockDocRef);
            int order = operationCounter.incrementAndGet();
            assertEquals("CREATE should be processed first", 1, order);
            return null;
        }).when(mockEmotionPostController).createPost(any(EmotionPost.class), any(), any());
        doAnswer(invocation -> {
            int order = operationCounter.incrementAndGet();
            assertEquals("UPDATE should be processed second", 2, order);
            Runnable onSuccess = invocation.getArgument(2);
            onSuccess.run();
            return null;
        }).when(mockEmotionPostController).updateEmotionPost(eq("post123"), any(EmotionPost.class), any(), any());
        doAnswer(invocation -> {
            int order = operationCounter.incrementAndGet();
            assertEquals("DELETE should be processed third", 3, order);
            Runnable onSuccess = invocation.getArgument(1);
            onSuccess.run();
            return null;
        }).when(mockEmotionPostController).deleteEmotionPost(eq("post456"), any(), any());
        syncManager.syncPendingOperations();
        verify(mockEmotionPostController).createPost(any(EmotionPost.class), any(), any());
        verify(mockEmotionPostController).updateEmotionPost(eq("post123"), any(EmotionPost.class), any(), any());
        verify(mockEmotionPostController).deleteEmotionPost(eq("post456"), any(), any());
        assertEquals("All three operations should be processed", 3, operationCounter.get());
    }
}
