package com.example.qq.broadcast;

// Android 框架

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.example.qq.service.WebSocketForegroundService;
import com.example.qq.utils.SharedPreferencesManager;

/**
 * 开机启动广播接收器
 * 负责在设备启动完成后恢复应用服务，包括：
 * - 检查用户登录状态
 * - 启动WebSocket前台服务
 * - 恢复应用的连接状态
 * 
 * @author yunxi
 * @version 1.0
 */
public class BootCompletedReceiver extends BroadcastReceiver {
    private static final String TAG = "BootCompletedReceiver";

    /**
     * 接收开机完成广播
     * 在设备启动完成后检查用户状态并启动必要的服务
     *
     * @param context 应用上下文
     * @param intent 广播意图，包含 ACTION_BOOT_COMPLETED 动作
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            try {
                // 初始化 SharedPreferencesManager
                SharedPreferencesManager.init(context.getApplicationContext());
                
                // 检查用户是否已登录
                if (SharedPreferencesManager.getInstance().isLoggedIn()) {
                    Log.d(TAG, "Device booted, starting WebSocket service...");
                    startWebSocketService(context);
                } else {
                    Log.d(TAG, "User not logged in, skipping service start");
                }
            } catch (Exception e) {
                Log.e(TAG, "Error starting service after boot", e);
            }
        }
    }

    /**
     * 启动WebSocket前台服务
     * 根据Android版本选择适当的服务启动方式
     *
     * @param context 应用上下文
     */
    private void startWebSocketService(Context context) {
        Intent serviceIntent = new Intent(context, WebSocketForegroundService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent);
        } else {
            context.startService(serviceIntent);
        }
    }
} 