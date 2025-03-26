package com.example.tangry.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.tangry.controllers.EmotionPostController;
import com.example.tangry.interfaces.SyncStatusListener;
import com.example.tangry.models.EmotionPost;
import com.example.tangry.models.PendingOperation;
import com.example.tangry.models.SyncStatus;
import com.google.firebase.firestore.DocumentReference;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class OfflineSyncManager implements NetworkMonitor.NetworkChangeListener {
    private static final String TAG = "OfflineSyncManager";
    private static final String PREFS_NAME = "offline_sync_prefs";
    private static final String PENDING_OPS_KEY = "pending_operations";

    private static OfflineSyncManager instance;

    private final Context context;
    private final NetworkMonitor networkMonitor;
    private final SharedPreferences sharedPreferences;
    private final EmotionPostController emotionPostController;
    private final List<SyncStatusListener> listeners = new ArrayList<>();
    private boolean isSyncing = false;

    public OfflineSyncManager(Context context) {
        this.context = context.getApplicationContext();
        this.sharedPreferences = this.context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.networkMonitor = new NetworkMonitor(this.context);
        this.emotionPostController = new EmotionPostController();
        this.networkMonitor.setNetworkChangeListener(this);
    }

    public OfflineSyncManager(Context context, NetworkMonitor networkMonitor, EmotionPostController emotionPostController) {
        this.context = context.getApplicationContext();
        this.sharedPreferences = this.context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.networkMonitor = networkMonitor;
        this.emotionPostController = emotionPostController;
        this.networkMonitor.setNetworkChangeListener(this);
    }

    public static synchronized OfflineSyncManager getInstance(Context context) {
        if (instance == null) {
            instance = new OfflineSyncManager(context);
        }
        return instance;
    }

    public void registerSyncStatusListener(SyncStatusListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void unregisterSyncStatusListener(SyncStatusListener listener) {
        listeners.remove(listener);
    }

    private void notifyListeners(SyncStatus status, String message) {
        for (SyncStatusListener listener : listeners) {
            listener.onSyncStatusChanged(status, message);
        }
    }

    public void addPendingCreate(EmotionPost post) {
        PendingOperation operation = new PendingOperation(PendingOperation.OperationType.CREATE, null, post);
        addOperation(operation);
        notifyListeners(SyncStatus.PENDING, "New post will sync when online");
    }

    public void addPendingUpdate(String postId, EmotionPost post) {
        PendingOperation operation = new PendingOperation(PendingOperation.OperationType.UPDATE, postId, post);
        addOperation(operation);
        notifyListeners(SyncStatus.PENDING, "Post update will sync when online");
    }

    public void addPendingDelete(String postId) {
        PendingOperation operation = new PendingOperation(PendingOperation.OperationType.DELETE, postId, null);
        addOperation(operation);
        notifyListeners(SyncStatus.PENDING, "Post deletion will sync when online");
    }

    private void addOperation(PendingOperation operation) {
        List<PendingOperation> operations = getPendingOperations();
        operations.add(operation);
        savePendingOperations(operations);
    }

    private List<PendingOperation> getPendingOperations() {
        String json = sharedPreferences.getString(PENDING_OPS_KEY, null);
        if (json == null) {
            return new ArrayList<>();
        }
        Type type = new TypeToken<ArrayList<PendingOperation>>() {
        }.getType();
        return new Gson().fromJson(json, type);
    }

    private void savePendingOperations(List<PendingOperation> operations) {
        String json = new Gson().toJson(operations);
        sharedPreferences.edit().putString(PENDING_OPS_KEY, json).apply();
    }

    public boolean hasPendingOperations() {
        return !getPendingOperations().isEmpty();
    }

    public int getPendingOperationsCount() {
        return getPendingOperations().size();
    }

    public SyncStatus getCurrentStatus() {
        if (isSyncing) {
            return SyncStatus.SYNCING;
        } else if (hasPendingOperations()) {
            return SyncStatus.PENDING;
        } else {
            return SyncStatus.SYNCED;
        }
    }

    @Override
    public void onNetworkAvailable() {
        Log.d(TAG, "Network available, attempting to sync pending operations");
        if (hasPendingOperations() && !isSyncing) {
            syncPendingOperations();
        }
    }

    @Override
    public void onNetworkUnavailable() {
        Log.d(TAG, "Network unavailable, sync paused");
    }

    public void syncPendingOperations() {
        if (!networkMonitor.isConnected()) {
            Log.d(TAG, "Cannot sync - no network connection");
            return;
        }

        if (isSyncing) {
            Log.d(TAG, "Sync already in progress, skipping");
            return;
        }

        List<PendingOperation> operations = getPendingOperations();
        if (operations.isEmpty()) {
            Log.d(TAG, "No pending operations to sync");
            return;
        }

        isSyncing = true;
        notifyListeners(SyncStatus.SYNCING, "Syncing " + operations.size() + " changes...");

        Log.d(TAG, "Starting sync of " + operations.size() + " operations");
        AtomicInteger completed = new AtomicInteger(0);
        AtomicInteger failed = new AtomicInteger(0);

        for (PendingOperation operation : new ArrayList<>(operations)) {
            processOperation(operation, operations, completed, failed);
        }
    }

    private void processOperation(PendingOperation operation, List<PendingOperation> operations,
            AtomicInteger completed, AtomicInteger failed) {
        switch (operation.getType()) {
            case CREATE:
                emotionPostController.createPost(operation.getPost(),
                        (DocumentReference docRef) -> {
                            Log.d(TAG, "Successfully synced CREATE operation");
                            operations.remove(operation);
                            savePendingOperations(operations);
                            checkSyncCompletion(operations, completed.incrementAndGet(), failed.get());
                        },
                        e -> {
                            Log.e(TAG, "Failed to sync CREATE operation", e);
                            failed.incrementAndGet();
                            checkSyncCompletion(operations, completed.get(), failed.get());
                        });
                break;

            case UPDATE:
                emotionPostController.updateEmotionPost(operation.getPostId(), operation.getPost(),
                        () -> { // Using Runnable instead of OnSuccessListener<Void>
                            Log.d(TAG, "Successfully synced UPDATE operation");
                            operations.remove(operation);
                            savePendingOperations(operations);
                            checkSyncCompletion(operations, completed.incrementAndGet(), failed.get());
                        },
                        e -> {
                            Log.e(TAG, "Failed to sync UPDATE operation", e);
                            failed.incrementAndGet();
                            checkSyncCompletion(operations, completed.get(), failed.get());
                        });
                break;

            case DELETE:
                emotionPostController.deleteEmotionPost(operation.getPostId(),
                        () -> { // Using Runnable instead of OnSuccessListener<Void>
                            Log.d(TAG, "Successfully synced DELETE operation");
                            operations.remove(operation);
                            savePendingOperations(operations);
                            checkSyncCompletion(operations, completed.incrementAndGet(), failed.get());
                        },
                        e -> {
                            Log.e(TAG, "Failed to sync DELETE operation", e);
                            failed.incrementAndGet();
                            checkSyncCompletion(operations, completed.get(), failed.get());
                        });
                break;
        }
    }

    private void checkSyncCompletion(List<PendingOperation> operations, int completed, int failed) {
        if (completed + failed >= operations.size()) {
            isSyncing = false;
            if (failed > 0) {
                notifyListeners(SyncStatus.FAILED, "Sync completed with " + failed + " errors");
            } else {
                notifyListeners(SyncStatus.SYNCED, "All changes synchronized successfully");
            }
        }
    }

    public void clearPendingOperations() {
        sharedPreferences.edit().remove(PENDING_OPS_KEY).apply();
    }

    public void destroy() {
        networkMonitor.removeNetworkChangeListener();
        listeners.clear();
    }
}