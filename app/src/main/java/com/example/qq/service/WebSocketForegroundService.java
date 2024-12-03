package com.example.qq.service;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.qq.R;
import com.example.qq.activity.MainActivity;
import com.example.qq.websocket.impl.WebSocketServiceImpl;

/**
 * WebSocket前台服务
 * 负责保持WebSocket连接的前台服务，确保应用在后台时也能保持消息连接
 */
public class WebSocketForegroundService extends Service {
    private static final int NOTIFICATION_ID = 1;
    private static final String CHANNEL_ID = "websocket_service";
    private WebSocketServiceImpl webSocketService;

    @SuppressLint("ForegroundServiceType")
    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        webSocketService = WebSocketServiceImpl.getInstance();
        startForeground(NOTIFICATION_ID, createNotification());
    }

    /**
     * 创建通知渠道
     * 适配Android 8.0及以上版本的通知渠道要求
     */
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "WebSocket Service",
                NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("保持消息连接");
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    /**
     * 创建前台服务通知
     * @return 通知对象
     */
    private Notification createNotification() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent,
            PendingIntent.FLAG_IMMUTABLE
        );

        return new NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("QQ正在运行")
            .setContentText("保持消息连接")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(pendingIntent)
            .build();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // 尝试重启服务
        Intent restartServiceIntent = new Intent(getApplicationContext(), WebSocketForegroundService.class);
        startService(restartServiceIntent);
    }
} 