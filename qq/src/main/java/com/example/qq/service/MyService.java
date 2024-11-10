package com.example.qq.service;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.IBinder;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.qq.ChatActivity3;
import com.example.qq.LoginActivity;
import com.example.qq.R;
import com.example.qq.websocket.web.WebClient;

import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.WebSocketListener;

public class MyService extends Service {
    private static final String CHANNEL_ID = "my_service_channel";
    private Handler handler;
    private WebClient webClient;
    private String token; // 用于存储token
    private final int NOTIFICATION_ID = 1; // 通知ID
    private static final int PERMISSION_REQUEST_CODE = 1;

    @Override
    public void onCreate() {
        super.onCreate();
        handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                // 处理消息
                System.out.println("接收到了消息");
            }
        };
    }

    private void showNotification(String message) {
        // 创建通知渠道（Android 8.0及以上）
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "My Service Channel";
            String description = "Channel for My Service notifications";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            channel.enableLights(true);
            channel.setLightColor(Color.RED);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

        // 构建通知
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.img) // 设置通知小图标
                .setContentTitle("收到一条新消息") // 设置通知标题
                .setContentText(message) // 设置通知内容
                .setPriority(NotificationCompat.PRIORITY_DEFAULT) // 设置通知优先级
                .setAutoCancel(true); // 点击后自动取消

        // 构建PendingIntent
        Intent intent = new Intent(this, ChatActivity3.class); // 点击通知后打开的Activity
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        // 设置点击事件
        builder.setContentIntent(pendingIntent);

        // 发送通知
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            notificationManager.notify(NOTIFICATION_ID, builder.build());
        } else {
            requestNotificationPermission();
        }
    }

    private void requestNotificationPermission() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    private void sendNotification() {
        // 这里可以发送通知，因为已经获得了权限
        showNotification("定期通知");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "服务已启动", Toast.LENGTH_SHORT).show();
        // 这里可以处理启动服务时传递的Intent
        if (intent != null) {
            token = intent.getStringExtra("token");
        }

        // 每隔一秒发送一个通知
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                showNotification("定期通知");
                // 每隔一秒发送一次通知
                handler.postDelayed(this, 1000);
            }
        }, 2000);

        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // 如果服务需要被绑定，则返回一个Binder对象
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // 服务销毁时的清理工作
        if (webClient != null) {
            webClient.disconnect();
        }
        System.out.println("服务关闭");
    }
}