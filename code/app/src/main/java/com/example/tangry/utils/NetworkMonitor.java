// code/app/src/main/java/com/example/tangry/utils/NetworkMonitor.java
package com.example.tangry.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;

public class NetworkMonitor {
    private static final String TAG = "NetworkMonitor";
    private final ConnectivityManager connectivityManager;
    private final Handler mainHandler;
    private NetworkCallback networkCallback;
    private boolean isConnected = false;
    private NetworkChangeListener listener;

    public interface NetworkChangeListener {
        void onNetworkAvailable();
        void onNetworkUnavailable();
    }

    public NetworkMonitor(Context context) {
        connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        mainHandler = new Handler(Looper.getMainLooper());
        checkInitialConnectivity();
    }

    public void setNetworkChangeListener(NetworkChangeListener listener) {
        this.listener = listener;
        registerNetworkCallback();
    }

    public void removeNetworkChangeListener() {
        if (networkCallback != null) {
            try {
                connectivityManager.unregisterNetworkCallback(networkCallback);
            } catch (IllegalArgumentException e) {
                Log.e(TAG, "NetworkCallback was not registered or already unregistered", e);
            }
            networkCallback = null;
        }
        this.listener = null;
    }

    private void registerNetworkCallback() {
        if (networkCallback != null) {
            removeNetworkChangeListener();
        }

        networkCallback = new NetworkCallback();
        NetworkRequest networkRequest = new NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build();

        connectivityManager.registerNetworkCallback(networkRequest, networkCallback);
    }

    private void checkInitialConnectivity() {
        Network network = connectivityManager.getActiveNetwork();
        NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(network);
        isConnected = capabilities != null && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
    }

    public boolean isConnected() {
        return isConnected;
    }

    private class NetworkCallback extends ConnectivityManager.NetworkCallback {
        @Override
        public void onAvailable(@NonNull Network network) {
            mainHandler.post(() -> {
                isConnected = true;
                if (listener != null) {
                    listener.onNetworkAvailable();
                }
            });
        }

        @Override
        public void onLost(@NonNull Network network) {
            mainHandler.post(() -> {
                isConnected = false;
                if (listener != null) {
                    listener.onNetworkUnavailable();
                }
            });
        }
    }
}