package com.example.qq.handler.impl;

import android.annotation.SuppressLint;
import android.content.Context;
import android.app.NotificationManager;
import android.app.NotificationChannel;
import android.app.Notification;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;
import android.content.Intent;
import android.app.PendingIntent;
import android.Manifest;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.example.qq.domain.WebSocketMessage;
import com.example.qq.handler.MessageHandler;
import com.example.qq.utils.SharedPreferencesManager;
import com.example.qq.event.FriendRequestEvent;
import com.example.qq.websocket.WebSocketService;
import com.example.qq.websocket.impl.WebSocketServiceImpl;
import com.google.gson.Gson;
import org.greenrobot.eventbus.EventBus;
import com.example.qq.domain.FriendRequest;
import com.example.qq.activity.NewFriendActivity;

import java.util.List;

/**
 * 消息处理器实现类
 * 负责处理WebSocket消息的发送和接收，包括好友请求、聊天消息等
 */
public class MessageHandlerImpl implements MessageHandler, WebSocketService.WebSocketListener {
    
    private static final String TAG = "MessageHandlerImpl";
    private final WebSocketServiceImpl webSocketService;
    private final String currentUsername;
    private final Context context;
    private final SharedPreferencesManager prefsManager;
    private final Gson gson;
    private static final String FRIEND_REQUEST_CHANNEL_ID = "friend_requests";
    private static final int NOTIFICATION_ID = 1001;

    public MessageHandlerImpl(Context context) {
        this.context = context;
        
        SharedPreferencesManager.init(context);
        this.prefsManager = SharedPreferencesManager.getInstance();
        
        if (!prefsManager.isLoggedIn()) {
            throw new IllegalStateException("Current user not logged in");
        }
        
        this.currentUsername = prefsManager.getCurrentUsername();
        if (this.currentUsername == null) {
            throw new IllegalStateException("Current username is null");
        }
        
        this.webSocketService = WebSocketServiceImpl.getInstance();
        this.gson = new Gson();
        
        createNotificationChannel();
        initWebSocket();
    }

    private void initWebSocket() {
        webSocketService.addListener(this);
        webSocketService.init();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                NotificationManager notificationManager = 
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                
                if (notificationManager == null) {
                    Log.e(TAG, "NotificationManager is null");
                    return;
                }

                NotificationChannel channel = new NotificationChannel(
                    FRIEND_REQUEST_CHANNEL_ID,
                    "好友请求",
                    NotificationManager.IMPORTANCE_HIGH
                );
                
                channel.setDescription("显示新的好友请求通知");
                channel.enableLights(true);
                channel.setLightColor(0xFF0000FF);
                channel.enableVibration(true);
                channel.setVibrationPattern(new long[]{0, 500, 1000});
                channel.setShowBadge(true);
                
                notificationManager.createNotificationChannel(channel);
                Log.d(TAG, "Notification channel created");
            } catch (Exception e) {
                Log.e(TAG, "Error creating notification channel", e);
            }
        }
    }

    @Override
    public void sendFriendRequest(String targetUsername, String message) {
        WebSocketMessage friendRequest = new WebSocketMessage(
            2,  // systemType = 2 表示好友请求
            currentUsername,
            targetUsername,
            message
        );
        sendMessage(friendRequest);
    }

    @Override
    public void acceptFriendRequest(String fromUsername) {
        WebSocketMessage acceptMessage = new WebSocketMessage(
            3,  // systemType = 3 表示接受好友请求
            currentUsername,
            fromUsername,
            "接受好友请求"
        );
        sendMessage(acceptMessage);
        prefsManager.addFriend(fromUsername);
    }

    @Override
    public void rejectFriendRequest(String fromUsername) {
        WebSocketMessage rejectMessage = new WebSocketMessage(
            4,  // systemType = 4 表示拒绝好友请求
            currentUsername,
            fromUsername,
            "拒绝好友请求"
        );
        sendMessage(rejectMessage);
        
        // 更新本地存储中的好友请求状态为已拒绝
        FriendRequest request = findFriendRequest(fromUsername);
        if (request != null) {
            request.setStatus(2); // 设置状态为已拒绝
            prefsManager.updateFriendRequest(request);
        }
    }

    @Override
    public void sendMessage(WebSocketMessage message) {
        try {
            String jsonMessage = gson.toJson(message);
            webSocketService.sendMessage(jsonMessage);
        } catch (Exception e) {
            Log.e(TAG, "Error sending message", e);
        }
    }

    @Override
    public void sendChatMessage(String toUsername, String content) {
        WebSocketMessage chatMessage = new WebSocketMessage(
            1,  // systemType = 1 表示普通聊天消息
            currentUsername,
            toUsername,
            content
        );
        sendMessage(chatMessage);
    }

    @Override
    public void onConnected() {
        Log.i(TAG, "WebSocket connected");
    }

    @Override
    public void onDisconnected() {
        Log.i(TAG, "WebSocket disconnected");
    }

    @Override
    public void onError(String error) {
        Log.e(TAG, "WebSocket error: " + error);
    }

    @Override
    public void onMessageReceived(String messageJson) {
        try {
            WebSocketMessage message = gson.fromJson(messageJson, WebSocketMessage.class);
            handleReceivedMessage(message);
        } catch (Exception e) {
            Log.e(TAG, "Error parsing message", e);
        }
    }

    @Override
    public void handleReceivedMessage(WebSocketMessage message) {
        switch (message.getSystemType()) {
            case 2: // 收到好友请求
                handleFriendRequest(message);
                break;
            case 3: // 好友请求被接受
                handleFriendRequestAccepted(message);
                break;
            case 4: // 好友请求被拒绝
                handleFriendRequestRejected(message);
                break;
            case 1: // 普通聊天消息
                handleChatMessage(message);
                break;
            default:
                Log.w(TAG, "Unknown message type: " + message.getSystemType());
                break;
        }
    }

    @SuppressLint("ObsoleteSdkInt")
    private void handleFriendRequest(WebSocketMessage message) {
        if ("system".equals(message.getUser())) {
            new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                Toast.makeText(context, message.getMessage(), Toast.LENGTH_SHORT).show();
            });
            return;
        }

        String target = message.getTarget();
        if (target != null && target.equals(currentUsername)) {
            FriendRequest friendRequest = FriendRequest.fromWebSocketMessage(message);
            SharedPreferencesManager.getInstance().saveFriendRequest(friendRequest);

            // 检查通知权限
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(context, 
                        Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                    Log.w(TAG, "Notification permission not granted");
                    return;
                }
            }

            try {
                // 创建通知
                NotificationManager notificationManager = 
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

                if (notificationManager == null) {
                    Log.e(TAG, "NotificationManager is null");
                    return;
                }

                // 创建跳转Intent
                Intent intent = new Intent(context, NewFriendActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                
                PendingIntent pendingIntent = PendingIntent.getActivity(
                    context, 
                    0, 
                    intent, 
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
                );

                // 创建通知
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    Notification.Builder builder = new Notification.Builder(context, FRIEND_REQUEST_CHANNEL_ID)
                        .setSmallIcon(android.R.drawable.ic_dialog_info)
                        .setContentTitle("新的好友请求")
                        .setContentText(message.getUser() + "想添加您为好友")
                        .setAutoCancel(true)
                        .setContentIntent(pendingIntent)
                        .setBadgeIconType(NotificationCompat.BADGE_ICON_SMALL)
                        .setCategory(NotificationCompat.CATEGORY_MESSAGE);

                    notificationManager.notify(NOTIFICATION_ID, builder.build());
                    Log.d(TAG, "Notification sent for Android O and above");
                } else {
                    NotificationCompat.Builder builder = new NotificationCompat.Builder(context, FRIEND_REQUEST_CHANNEL_ID)
                        .setSmallIcon(android.R.drawable.ic_dialog_info)
                        .setContentTitle("新的好友请求")
                        .setContentText(message.getUser() + "想添加您为好友")
                        .setAutoCancel(true)
                        .setContentIntent(pendingIntent)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setDefaults(NotificationCompat.DEFAULT_ALL);

                    notificationManager.notify(NOTIFICATION_ID, builder.build());
                    Log.d(TAG, "Notification sent for Android below O");
                }
            } catch (Exception e) {
                Log.e(TAG, "Error sending notification", e);
            }

            // 发送EventBus事件
            EventBus.getDefault().post(new FriendRequestEvent(message));
        }
    }

    private FriendRequest findFriendRequest(String username) {
        List<FriendRequest> requests = prefsManager.getFriendRequests();
        for (FriendRequest request : requests) {
            if (request.getUsername().equals(username)) {
                return request;
            }
        }
        return null;
    }

    private void handleFriendRequestAccepted(WebSocketMessage message) {
        // 只有发送方接受了请求，才添加为好友
        if (message.getUser().equals(currentUsername)) {
            prefsManager.addFriend(message.getTarget());
        } else {
            prefsManager.addFriend(message.getUser());
        }
        
        // 更新本地存储中的好友请求状态为已接受
        FriendRequest request = findFriendRequest(
            message.getUser().equals(currentUsername) ? message.getTarget() : message.getUser()
        );
        if (request != null) {
            request.setStatus(1); // 设置状态为已接受
            prefsManager.updateFriendRequest(request);
        }
        
        EventBus.getDefault().post(new FriendRequestEvent(message));
    }

    private void handleFriendRequestRejected(WebSocketMessage message) {
        // 不添加好友，只更新请求状态
        FriendRequest request = findFriendRequest(
            message.getUser().equals(currentUsername) ? message.getTarget() : message.getUser()
        );
        if (request != null) {
            request.setStatus(2); // 设置状态为已拒绝
            prefsManager.updateFriendRequest(request);
        }
        
        EventBus.getDefault().post(new FriendRequestEvent(message));
    }

    private void handleChatMessage(WebSocketMessage message) {
        EventBus.getDefault().post(message);
    }

    public void destroy() {
        webSocketService.removeListener(this);
    }
} 