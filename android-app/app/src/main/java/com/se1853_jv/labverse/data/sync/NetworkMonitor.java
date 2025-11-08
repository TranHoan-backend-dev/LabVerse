package com.se1853_jv.labverse.data.sync;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;

import com.se1853_jv.labverse.data.utils.Connectivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Monitor network connectivity và notify khi có internet
 */
public class NetworkMonitor {
    private final Context context;
    private final ConnectivityManager connectivityManager;
    private final List<NetworkStateListener> listeners = new ArrayList<>();
    private ConnectivityManager.NetworkCallback networkCallback;

    public interface NetworkStateListener {
        void onNetworkAvailable();
        void onNetworkUnavailable();
    }

    public NetworkMonitor(Context context) {
        this.context = context.getApplicationContext();
        this.connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
    }

    public void startMonitoring() {
        if (connectivityManager == null) return;

        NetworkRequest networkRequest = new NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
                .build();

        networkCallback = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(Network network) {
                // Kiểm tra thực sự có internet không
                if (Connectivity.isInternetAvailable(context)) {
                    notifyNetworkAvailable();
                }
            }

            @Override
            public void onLost(Network network) {
                notifyNetworkUnavailable();
            }

            @Override
            public void onCapabilitiesChanged(Network network, NetworkCapabilities networkCapabilities) {
                if (networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                    networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)) {
                    notifyNetworkAvailable();
                } else {
                    notifyNetworkUnavailable();
                }
            }
        };

        connectivityManager.registerNetworkCallback(networkRequest, networkCallback);
    }

    public void stopMonitoring() {
        if (connectivityManager != null && networkCallback != null) {
            connectivityManager.unregisterNetworkCallback(networkCallback);
        }
    }

    public void addListener(NetworkStateListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void removeListener(NetworkStateListener listener) {
        listeners.remove(listener);
    }

    public boolean isNetworkAvailable() {
        return Connectivity.isInternetAvailable(context);
    }

    private void notifyNetworkAvailable() {
        for (NetworkStateListener listener : listeners) {
            listener.onNetworkAvailable();
        }
    }

    private void notifyNetworkUnavailable() {
        for (NetworkStateListener listener : listeners) {
            listener.onNetworkUnavailable();
        }
    }
}











