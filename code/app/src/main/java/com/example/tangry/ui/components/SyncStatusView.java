/**
 * SyncStatusView.java
 * 
 * Custom view that displays the current synchronization status.
 * Shows an icon and text indicating whether data is synced, pending sync,
 * or if there are sync errors. Listens for status changes from OfflineSyncManager.
 */
package com.example.tangry.ui.components;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.tangry.R;
import com.example.tangry.interfaces.SyncStatusListener;
import com.example.tangry.models.SyncStatus;
import com.example.tangry.utils.OfflineSyncManager;

public class SyncStatusView extends FrameLayout implements SyncStatusListener {

    private ImageView statusIcon;
    private TextView statusText;
    private OfflineSyncManager syncManager;

    public SyncStatusView(@NonNull Context context) {
        super(context);
        init(context);
    }

    public SyncStatusView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public SyncStatusView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.view_sync_status, this, true);

        statusIcon = findViewById(R.id.sync_status_icon);
        statusText = findViewById(R.id.sync_status_text);

        syncManager = OfflineSyncManager.getInstance(context);
        syncManager.registerSyncStatusListener(this);

        updateUI(syncManager.getCurrentStatus(), null);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (syncManager != null) {
            syncManager.registerSyncStatusListener(this);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (syncManager != null) {
            syncManager.unregisterSyncStatusListener(this);
        }
    }

    @Override
    public void onSyncStatusChanged(SyncStatus status, String message) {
        updateUI(status, message);
    }

    private void updateUI(SyncStatus status, String message) {
        switch (status) {
            case PENDING:
                statusIcon.setImageResource(android.R.drawable.ic_dialog_info);
                statusText.setText(message != null ? message
                        : "Changes will sync when online (" +
                                syncManager.getPendingOperationsCount() + ")");
                setVisibility(VISIBLE);
                break;

            case SYNCING:
                statusIcon.setImageResource(android.R.drawable.ic_popup_sync);
                statusText.setText(message != null ? message : "Syncing changes...");
                setVisibility(VISIBLE);
                break;

            case SYNCED:
                statusIcon.setImageResource(android.R.drawable.ic_dialog_info);
                statusText.setText(message != null ? message : "All changes synced");
                // Auto-hide after 3 seconds when synced
                postDelayed(() -> setVisibility(GONE), 3000);
                break;

            case FAILED:
                statusIcon.setImageResource(android.R.drawable.ic_dialog_alert);
                statusText.setText(message != null ? message : "Sync failed");
                setVisibility(VISIBLE);
                break;
        }
    }

    public void triggerSync() {
        if (syncManager != null) {
            syncManager.syncPendingOperations();
        }
    }
}