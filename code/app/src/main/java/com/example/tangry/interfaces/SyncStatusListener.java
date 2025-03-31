package com.example.tangry.interfaces;

import com.example.tangry.models.SyncStatus;

public interface SyncStatusListener {
    void onSyncStatusChanged(SyncStatus status, String message);
}