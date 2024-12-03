package com.example.qq.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;
import android.util.Log;

/**
 * 网络状态广播接收器
 * 用于监听网络状态变化并通知观察者
 * 
 * @author yunxi
 * @version 1.0
 */
public class NetworkBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = "NetworkReceiver";
    private NetworkCallback networkCallback;

    public interface NetworkCallback {
        void onNetworkChanged(boolean isConnected, String networkType);
    }

    public NetworkBroadcastReceiver(NetworkCallback callback) {
        this.networkCallback = callback;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
            updateNetworkStatus(context);
        }
    }

    /**
     * 注册广播接收器
     */
    public static NetworkBroadcastReceiver register(Context context, NetworkCallback callback) {
        NetworkBroadcastReceiver receiver = new NetworkBroadcastReceiver(callback);
        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        context.registerReceiver(receiver, filter);
        
        // 立即检查一次当前网络状态
        receiver.updateNetworkStatus(context);
        
        return receiver;
    }

    /**
     * 注销广播接收器
     */
    public static void unregister(Context context, NetworkBroadcastReceiver receiver) {
        if (receiver != null) {
            try {
                context.unregisterReceiver(receiver);
            } catch (Exception e) {
                Log.e(TAG, "Error unregistering network receiver: " + e.getMessage());
            }
        }
    }

    /**
     * 更新网络状态
     */
    private void updateNetworkStatus(Context context) {
        ConnectivityManager cm = (ConnectivityManager) 
            context.getSystemService(Context.CONNECTIVITY_SERVICE);
        
        if (cm == null) {
            notifyNetworkStatus(false, "未知");
            return;
        }

        boolean isConnected = false;
        String networkType = "离线";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Network network = cm.getActiveNetwork();
            if (network != null) {
                NetworkCapabilities capabilities = cm.getNetworkCapabilities(network);
                if (capabilities != null) {
                    isConnected = true;
                    if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                        networkType = "WiFi在线";
                    } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                        networkType = "流量在线";
                    } else {
                        networkType = "在线";
                    }
                }
            }
        } else {
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            if (activeNetwork != null) {
                isConnected = true;
                if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
                    networkType = "WiFi在线";
                } else if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
                    networkType = "流量在线";
                } else {
                    networkType = "在线";
                }
            }
        }

        notifyNetworkStatus(isConnected, networkType);
    }

    /**
     * 通知网络状态变化
     */
    private void notifyNetworkStatus(boolean isConnected, String networkType) {
        if (networkCallback != null) {
            networkCallback.onNetworkChanged(isConnected, networkType);
        }
    }
} 