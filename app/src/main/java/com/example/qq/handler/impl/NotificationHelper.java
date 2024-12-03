package com.example.qq.handler.impl;

import android.Manifest;
import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.qq.R;
import com.example.qq.activity.ChatActivity;
import com.example.qq.activity.NewFriendActivity;
import com.example.qq.domain.FriendRequest;
import com.example.qq.domain.WebSocketMessage;

/**
 * 通知管理助手类
 * 用于处理应用内所有的通知消息，包括好友请求、聊天消息等通知的创建和显示
 *
 * @author yunxi
 * @version 1.0
 * @see NotificationManager
 * @see NotificationCompat
 */
public class NotificationHelper {
    private static final String TAG = "NotificationHelper";
    
    /** 好友请求通知渠道ID */
    private static final String FRIEND_REQUEST_CHANNEL_ID = "friend_requests";
    /** 聊天消息通知渠道ID */
    private static final String CHAT_CHANNEL_ID = "chat_messages";
    /** 好友请求通知ID */
    private static final int FRIEND_REQUEST_NOTIFICATION_ID = 1001;
    
    /** 上下文对象 */
    private final Context context;

    /**
     * 构造函数
     * @param context 应用上下文
     */
    public NotificationHelper(Context context) {
        this.context = context;
        createNotificationChannels();
    }

    /**
     * 创建通知渠道
     * 针对Android 8.0及以上版本创建通知渠道
     */
    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = 
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                
            // 创建好友请求通道
            NotificationChannel friendRequestChannel = new NotificationChannel(
                FRIEND_REQUEST_CHANNEL_ID,
                "好友请求",
                NotificationManager.IMPORTANCE_HIGH
            );
            friendRequestChannel.setDescription("显示新的好友请求通知");
            friendRequestChannel.enableLights(true);
            friendRequestChannel.setLightColor(0xFF0000FF);
            friendRequestChannel.enableVibration(true);
            friendRequestChannel.setVibrationPattern(new long[]{0, 500, 1000});
            friendRequestChannel.setShowBadge(true);
            
            // 创建聊天消息通道
            NotificationChannel chatChannel = new NotificationChannel(
                CHAT_CHANNEL_ID,
                "聊天消息",
                NotificationManager.IMPORTANCE_HIGH
            );
            chatChannel.setDescription("显示新的聊天消息通知");
            chatChannel.enableLights(true);
            chatChannel.setLightColor(0xFF0000FF);
            chatChannel.enableVibration(true);
            chatChannel.setVibrationPattern(new long[]{0, 300, 500});
            chatChannel.setShowBadge(true);
            
            notificationManager.createNotificationChannel(friendRequestChannel);
            notificationManager.createNotificationChannel(chatChannel);
        }
    }

    /**
     * 显示好友请求通知
     * @param request 好友请求对象
     */
    public void showFriendRequestNotification(FriendRequest request) {
        if (!checkNotificationPermission()) {
            Log.d(TAG, "没有通知权限");
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

        // 创建通知内容
        String notificationText = String.format("%s(%s)想添加您为好友", 
            request.getNickname(), request.getUsername());

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, FRIEND_REQUEST_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("新的好友请求")
            .setContentText(notificationText)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL);

        showNotification(builder, FRIEND_REQUEST_NOTIFICATION_ID);
    }

    /**
     * 显示聊天消息通知
     * @param message WebSocket消息对象
     * @param senderNickname 发送者昵称
     * @param senderAvatar 发送者头像URL
     */
    public void showChatNotification(WebSocketMessage message, String senderNickname, String senderAvatar) {
        if (!checkNotificationPermission()) {
            Log.d(TAG, "没有通知权限");
            return;
        }

        String sender = message.getUser();
        String displayName = senderNickname != null ? senderNickname : sender;

        // 创建通知意图
        Intent intent = new Intent(context, ChatActivity.class);
        intent.putExtra("friend_username", sender);
        intent.putExtra("friend_nickname", displayName);
        intent.putExtra("friend_avatar", senderAvatar);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        int requestCode = sender.hashCode();
        PendingIntent pendingIntent = PendingIntent.getActivity(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHAT_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(displayName)
            .setContentText(message.getMessage())
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setDefaults(NotificationCompat.DEFAULT_ALL);

        if (senderAvatar != null && !senderAvatar.isEmpty()) {
            Glide.with(context.getApplicationContext())
                .asBitmap()
                .load(senderAvatar)
                .circleCrop()
                .into(new CustomTarget<Bitmap>(128, 128) {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, 
                            @Nullable Transition<? super Bitmap> transition) {
                        builder.setLargeIcon(resource)
                            .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(message.getMessage())
                                .setBigContentTitle(displayName));
                        showNotification(builder, requestCode);
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {
                        showNotification(builder, requestCode);
                    }
                });
        } else {
            builder.setStyle(new NotificationCompat.BigTextStyle()
                .bigText(message.getMessage())
                .setBigContentTitle(displayName));
            showNotification(builder, requestCode);
        }
    }

    /**
     * 显示通知
     * @param builder 通知构建器
     * @param notificationId 通知ID
     */
    private void showNotification(NotificationCompat.Builder builder, int notificationId) {
        try {
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            if (checkNotificationPermission()) {
                notificationManager.notify(notificationId, builder.build());
                Log.d(TAG, "通知已发送: id=" + notificationId);
            }
        } catch (SecurityException e) {
            Log.e(TAG, "显示通知失败: " + e.getMessage());
        }
    }

    /**
     * 检查通知权限
     * @return 是否有通知权限
     */
    private boolean checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, 
                    Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                if (context instanceof Activity) {
                    ActivityCompat.requestPermissions(
                        (Activity) context,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        1002
                    );
                }
                return false;
            }
        }
        return true;
    }

    /**
     * 显示好友删除通知
     * @param username 被删除好友的用户名
     */
    public void showFriendDeletedNotification(String username) {
        if (!checkNotificationPermission()) {
            return;
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, FRIEND_REQUEST_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("好友关系解除")
            .setContentText(username + " 已将您从好友列表中删除")
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL);

        showNotification(builder, FRIEND_REQUEST_NOTIFICATION_ID + 2);  // 使用不同的通知ID
    }

    /**
     * 显示好友请求接受通知
     * @param friendUsername 接受请求的好友用户名
     * @param message 通知消息内容
     */
    public void showFriendRequestAcceptedNotification(String friendUsername, String message) {
        if (!checkNotificationPermission()) {
            return;
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, FRIEND_REQUEST_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("好友请求已接受")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setDefaults(NotificationCompat.DEFAULT_ALL);

        showNotification(builder, FRIEND_REQUEST_NOTIFICATION_ID + 1);  // 使用不同的通知ID
    }
} 