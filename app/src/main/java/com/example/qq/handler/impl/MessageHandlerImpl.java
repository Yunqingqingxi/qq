package com.example.qq.handler.impl;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.qq.R;
import com.example.qq.activity.ChatActivity;
import com.example.qq.activity.LoginActivity;
import com.example.qq.activity.MainActivity;
import com.example.qq.activity.NewFriendActivity;
import com.example.qq.api.userapi.UserApi;
import com.example.qq.api.userapi.impl.UserApiImpl;
import com.example.qq.constant.MessageType;
import com.example.qq.domain.FriendRequest;
import com.example.qq.domain.User;
import com.example.qq.domain.WebSocketMessage;
import com.example.qq.event.FriendDeletedEvent;
import com.example.qq.event.FriendListUpdateEvent;
import com.example.qq.event.FriendRequestEvent;
import com.example.qq.fragment.ContactListFragment;
import com.example.qq.fragment.FriendListFragment;
import com.example.qq.fragment.FriendsFragment;
import com.example.qq.handler.MessageHandler;
import com.example.qq.service.NotificationService;
import com.example.qq.utils.SharedPreferencesManager;
import com.example.qq.utils.TimeUtils;
import com.example.qq.websocket.WebSocketService.WebSocketListener;
import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import okhttp3.Response;
import okhttp3.WebSocket;

/**
 * 消息处理器实现类
 * 负责处理WebSocket消息的发送和接收，包括好友请求、聊天消息等
 */
public class MessageHandlerImpl extends BaseMessageHandler implements MessageHandler, WebSocketListener {

    // 定义消息监听器接口
    public interface OnMessageReceivedListener {
        void onMessageReceived(String sender, String content);
    }

    private static final String TAG = "MessageHandlerImpl";
    private final NotificationHelper notificationHelper;
    private final MessageProcessor messageProcessor;
    private OnMessageReceivedListener messageListener;
    private UserApi userApi = new UserApiImpl();
    private static final int MESSAGE_TYPE_FORCE_OFFLINE = 6; // 新增消息类型：强制下线
    private static final int MESSAGE_TYPE_ONLINE_CHECK = 7;  // 新增消息类型：在线检测
    private final NotificationService notificationService;
    private Gson gson = new Gson();

    public MessageHandlerImpl(Context context, NotificationService notificationService) {
        super(context);
        this.notificationHelper = new NotificationHelper(context);
        this.messageProcessor = new MessageProcessor(this, this.notificationHelper);
        this.notificationService = notificationService;
        
        // 设置 MessageHandler 到 UserApiImpl
        UserApiImpl.setMessageHandler(this);
        
        initWebSocket();
    }

    private void initWebSocket() {
        webSocketService.addListener(this);
        webSocketService.init();
        
        // 发送上线通知
        WebSocketMessage onlineMessage = new WebSocketMessage(
            MessageType.ONLINE_CHECK.getValue(),
            currentUsername,
            "server",
            "login"
        );
        sendWebSocketMessage(onlineMessage);
    }

    @Override
    public void sendFriendRequest(String targetUsername, String message) {
        WebSocketMessage friendRequest = new WebSocketMessage(
            MessageType.FRIEND_REQUEST.getValue(),
            currentUsername,
            targetUsername,
            message
        );
        sendWebSocketMessage(friendRequest);
    }

    @Override
    public void acceptFriendRequest(String fromUsername) {
        WebSocketMessage acceptMessage = new WebSocketMessage(
            MessageType.FRIEND_ACCEPT.getValue(),
            currentUsername,
            fromUsername,
            "接受好友请求"
        );
        sendWebSocketMessage(acceptMessage);
        notificationService.decrementFriendRequestCount();
    }

    @Override
    public void rejectFriendRequest(String fromUsername) {
        WebSocketMessage rejectMessage = new WebSocketMessage(
            MessageType.FRIEND_REJECT.getValue(),
            currentUsername,
            fromUsername,
            "拒绝好友请求"
        );
        sendWebSocketMessage(rejectMessage);
        notificationService.decrementFriendRequestCount();
    }

    @Override
    public void sendMessage(WebSocketMessage message) {
        sendWebSocketMessage(message);
    }

    @Override
    public void sendChatMessage(String toUsername, String content) {
        WebSocketMessage chatMessage = new WebSocketMessage(
            MessageType.CHAT.getValue(),
            currentUsername,
            toUsername,
            content
        );
        sendWebSocketMessage(chatMessage);
    }

    @Override
    public void handleReceivedMessage(WebSocketMessage message) {
        messageProcessor.processMessage(message);
    }

    @Override
    public void deleteFriend(String username, boolean notifyPeer) {
        if (notifyPeer) {
            WebSocketMessage deleteMessage = new WebSocketMessage(
                MessageType.FRIEND_DELETED.getValue(),
                currentUsername,
                username,
                "删除好友"
            );
            sendWebSocketMessage(deleteMessage);
        }
        // 清理本地数据
        prefsManager.clearAllFriendData(username);
    }

    @SuppressLint("ObsoleteSdkInt")
    protected void handleFriendRequest(WebSocketMessage message) {
        if ("system".equals(message.getUser())) {
            mainHandler.post(() -> {
                Toast.makeText(context, message.getMessage(), Toast.LENGTH_SHORT).show();
            });
            return;
        }

        String target = message.getTargetname();
        if (target != null && target.equals(currentUsername)) {
            // 获取发送请求用户的详细信息
            new Thread(() -> {
                try {
                    User senderInfo = userApi.getUserInfo(message.getUser());
                    if (senderInfo == null) {
                        Log.e(TAG, "Failed to get sender user info");
                        return;
                    }

                    Log.d(TAG, "Got sender info - nickname: " + senderInfo.getUserNickName() 
                        + ", avatar: " + senderInfo.getUserAvatarUrl());

                    // 创建好友请求对象
                    FriendRequest friendRequest = new FriendRequest(
                        message.getUser(),  // userId
                        message.getUser(),  // username
                        senderInfo.getUserNickName(), // nickname
                        senderInfo.getUserAvatarUrl(), // avatarUrl
                        message.getMessage(), // 验证消息
                        System.currentTimeMillis(), // timestamp
                        0  // status = 0 表示待处理
                    );

                    mainHandler.post(() -> {
                        try {
                            SharedPreferencesManager.getInstance().saveFriendRequest(friendRequest);

                            // 使用 NotificationHelper 显示通知
                            notificationHelper.showFriendRequestNotification(friendRequest);

                            // 发送EventBus事件
                            EventBus.getDefault().post(new FriendRequestEvent(message));

                            // 如果当前在新朋友页面，刷新列表
                            if (context instanceof NewFriendActivity) {
                                ((NewFriendActivity) context).loadFriendRequests();
                            }

                            Log.d(TAG, "Friend request handled successfully - User: " + message.getUser() 
                                + ", Nickname: " + senderInfo.getUserNickName() 
                                + ", Avatar: " + senderInfo.getUserAvatarUrl());

                        } catch (Exception e) {
                            Log.e(TAG, "Error handling friend request", e);
                        }
                    });
                } catch (Exception e) {
                    Log.e(TAG, "Error getting sender info", e);
                }
            }).start();
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

    protected void handleFriendRequestAccepted(WebSocketMessage message) {
        try {
            Log.d(TAG, "处理好友请求接受消息");
            String friendUsername = message.getUser();
            String notificationMessage = message.getMessage();
            
            // 发送通知
            notificationHelper.showFriendRequestAcceptedNotification(
                friendUsername,
                notificationMessage
            );
            
            // 发送事件通知好友列表更新
            EventBus.getDefault().post(new FriendListUpdateEvent());
            
            // 清除缓存
            SharedPreferencesManager.getInstance().clearFriendListCache();
            
            // 直接调用刷新方法
            mainHandler.post(() -> {
                FriendListFragment.refreshFriendList();
                Log.d(TAG, "已触发好友列表手动刷新");
            });
            
            Log.d(TAG, "好友请求接受处理完成: " + friendUsername);
        } catch (Exception e) {
            Log.e(TAG, "处理好友请求接受消息失败: " + e.getMessage(), e);
        }
    }

    // 新增方法：获取并保存好友信息
    private void fetchAndSaveFriendInfo(String username) {
        try {
            // 获取好友的详细信息
            User friendInfo = userApi.getUserInfo(username);
            if (friendInfo == null) {
                Log.e(TAG, "Failed to get friend info");
                return;
            }

            Log.d(TAG, "Got friend info - User: " + username 
                + ", Nickname: " + friendInfo.getUserNickName() 
                + ", Avatar: " + friendInfo.getUserAvatarUrl());

            // 在主线程中保存信息
            mainHandler.post(() -> {
                try {
                    // 保存好友信息到本地存储
                    SharedPreferencesManager.getInstance().saveFriendNickname(username, friendInfo.getUserNickName());
                    SharedPreferencesManager.getInstance().setFriendAvatar(username, friendInfo.getUserAvatarUrl());
                    
                    // 刷新好友列表
                    FriendListFragment.refreshFriendList();

                    Log.d(TAG, "Friend info saved successfully");
                } catch (Exception e) {
                    Log.e(TAG, "Error saving friend info", e);
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error fetching friend info", e);
        }
    }

    protected void handleFriendRequestRejected(WebSocketMessage message) {
        try {
            String currentUser = SharedPreferencesManager.getInstance().getCurrentUsername();
            String friendUsername;
            
            if (message.getUser().equals(currentUser)) {
                friendUsername = message.getTargetname();
            } else {
                friendUsername = message.getUser();
            }

            // 检查参数
            if (friendUsername == null) {
                Log.e(TAG, "Friend username is null");
                return;
            }

            // 更新好友请求状态
            List<FriendRequest> requests = SharedPreferencesManager.getInstance().getFriendRequests();
            if (requests != null) {
                boolean found = false;
                for (FriendRequest request : requests) {
                    if (request != null && friendUsername.equals(request.getUsername())) {
                        request.setStatus(2); // 设置状态为已拒绝
                        SharedPreferencesManager.getInstance().updateFriendRequest(request);
                        found = true;
                        break;
                    }
                }
                
                if (!found) {
                    Log.w(TAG, "Friend request not found for user: " + friendUsername);
                }
            } else {
                Log.w(TAG, "No friend requests found");
            }
            
            // 发送事通UI更新
            EventBus.getDefault().post(new FriendRequestEvent(message));
            
            // 在主线程中更新UI
            new Handler(Looper.getMainLooper()).post(() -> {
                try {
                    if (context instanceof NewFriendActivity) {
                        ((NewFriendActivity) context).loadFriendRequests();
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error updating UI", e);
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error handling friend request rejected", e);
        }
    }

    protected void handleFriendDeleted(WebSocketMessage message) {
        try {
            String currentUser = SharedPreferencesManager.getInstance().getCurrentUsername();
            String friendUsername;
            
            if (message.getUser().equals(currentUser)) {
                friendUsername = message.getTargetname();
            } else {
                friendUsername = message.getUser();
            }

            // 在主线程中更新UI
            mainHandler.post(() -> {
                try {
                    // 关闭聊天窗口
                    if (context != null && context instanceof ChatActivity) {
                        String chatFriend = ((ChatActivity) context).getFriendUsername();
                        if (chatFriend != null && chatFriend.equals(friendUsername)) {
                            ((ChatActivity) context).finish();
                        }
                    }

                    // 清除所有相关数据
                    SharedPreferencesManager.getInstance().clearAllFriendData(friendUsername);
                    
                    // 强制刷新好友列表数据
                    if (context instanceof MainActivity) {
                        MainActivity activity = (MainActivity) context;
                        Fragment fragment = activity.getSupportFragmentManager()
                            .findFragmentById(R.id.fragment_container);
                        if (fragment instanceof FriendsFragment) {
                            Fragment contactFragment = ((FriendsFragment) fragment)
                                .getChildFragmentManager()
                                .findFragmentById(R.id.fragment_container2);
                            if (contactFragment instanceof ContactListFragment) {
                                ((ContactListFragment) contactFragment).loadContacts();
                            }
                        }
                    }
                    
                    // 发送事件通知其他界面更新
                    EventBus.getDefault().post(new FriendDeletedEvent(friendUsername));
                    
                    // 显示通知
                    if (!message.getUser().equals(currentUser)) {
                        // 如果是被删除的一方，显示通知
                        String notificationText = message.getUser() + " 已将您从好友列表中删除";
                        Toast.makeText(context, notificationText, Toast.LENGTH_SHORT).show();
                        // 可以添加系统通知
                        notificationHelper.showFriendDeletedNotification(message.getUser());
                    }

                    Log.d(TAG, "Friend deleted and all data cleared - Username: " + friendUsername);
                } catch (Exception e) {
                    Log.e(TAG, "Error handling friend deleted", e);
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error handling friend deleted message", e);
        }
    }

    protected void handleChatMessage(WebSocketMessage message) {
        try {
            // 如果消息是发给当前用户的
            if (message.getTargetname().equals(currentUsername)) {
                String sender = message.getUser();
                Log.d(TAG, "收到来自 " + sender + " 的聊天消息: " + message.getMessage());

                // 在主线程中更新UI和数据
                mainHandler.post(() -> {
                    try {
                        SharedPreferencesManager prefs = SharedPreferencesManager.getInstance();
                        
                        // 1. 更新最后一条消息和时间
                        prefs.setLastMessage(sender, message.getMessage());
                        String currentTime = TimeUtils.getCurrentTime();
                        prefs.setLastMessageTime(sender, currentTime);
                        Log.d(TAG, "更新最后一条消息: " + message.getMessage() + ", 时间: " + currentTime);

                        // 2. 通知消息监听器
                        if (messageListener != null) {
                            messageListener.onMessageReceived(sender, message.getMessage());
                        }

                        // 3. 发送好友列表更新事件
                        EventBus.getDefault().post(new FriendListUpdateEvent());
                        
                        // 4. 检查是否在聊天界面
                        boolean isInChatWithSender = false;
                        if (context instanceof ChatActivity) {
                            isInChatWithSender = ((ChatActivity) context).isCurrentChat(sender);
                        }

                        // 5. 如果不是当前聊天的好友，增加未读计数并显示通知
                        if (!isInChatWithSender) {
                            prefs.incrementUnreadCount(sender);
                            int newCount = prefs.getUnreadMessageCount(sender);
                            Log.d(TAG, "增加未读消息计数 " + sender + ": " + newCount);

                            // 获取发送者信息并显示通知
                            String senderNickname = prefs.getFriendNickname(sender);
                            String senderAvatar = prefs.getFriendAvatar(sender);
                            notificationHelper.showChatNotification(message, senderNickname, senderAvatar);
                        }

                    } catch (Exception e) {
                        Log.e(TAG, "更新UI失败: " + e.getMessage(), e);
                    }
                });
            }
        } catch (Exception e) {
            Log.e(TAG, "处理聊天消息失败: " + e.getMessage(), e);
        }
    }

    private void showNotification(NotificationCompat.Builder builder, int notificationId) {
        try {
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            
            // 检查通知权限
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(context, 
                        Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
            }

            notificationManager.notify(notificationId, builder.build());
        } catch (SecurityException e) {
            Log.e(TAG, "显示通知失败: " + e.getMessage());
        }
    }

    // 新增方法：检查通知权限
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

    public void destroy() {
        webSocketService.removeListener(this);
    }

    public void setOnMessageReceivedListener(OnMessageReceivedListener listener) {
        this.messageListener = listener;
    }

    @Override
    public void deleteFriend(String username) {
        WebSocketMessage deleteMessage = new WebSocketMessage(
            MessageType.FRIEND_DELETED.getValue(),
            currentUsername,
            username,
            "删除好友"
        );
        sendWebSocketMessage(deleteMessage);
    }

    @Override
    public void onOpen(WebSocket webSocket, Response response) {

    }

    @Override
    public void onFailure(WebSocket webSocket, Throwable t, Response response) {

    }

    // WebSocketListener 接口方法
    @Override
    public void onConnected() {
        Log.d(TAG, "WebSocket连接成功");
    }

    @Override
    public void onDisconnected() {
        Log.d(TAG, "WebSocket连接断开");
    }

    @Override
    public void onMessageReceived(String message) {
        try {
            Log.d(TAG, "收到WebSocket消息: " + message);
            
            // 忽略特定的系统消息
            if (message.equals("Invalid system type.") || 
                message.contains("Invalid") || 
                message.trim().isEmpty()) {
                Log.d(TAG, "忽略系统消息: " + message);
                return;
            }

            // 尝试解析为 JSON
            try {
                JSONObject jsonMessage = new JSONObject(message);
                
                // 修改这里的验证逻辑
                if (jsonMessage.has("system") && jsonMessage.has("user")) {
                    String targetname = jsonMessage.optString("targetname", "");  // 使用optString提供默认值
                    
                    WebSocketMessage wsMessage = new WebSocketMessage(
                        jsonMessage.getInt("system"),
                        jsonMessage.getString("user"),
                        targetname,  // 可能为空字符串
                        jsonMessage.optString("message", "")
                    );
                    wsMessage.setTimestamp(System.currentTimeMillis());
                    
                    Log.d(TAG, "解析消息成功: type=" + wsMessage.getSystemType() + 
                        ", from=" + wsMessage.getUser() + 
                        ", content=" + wsMessage.getMessage());

                    handleReceivedMessage(wsMessage);
                } else {
                    Log.w(TAG, "消息格式不完整: " + message);
                }
            } catch (JSONException e) {
                Log.e(TAG, "JSON解析失败: " + e.getMessage());
            }
        } catch (Exception e) {
            Log.e(TAG, "消息处理失败: " + e.getMessage(), e);
        }
    }

    @Override
    public void onError(String error) {
        Log.e(TAG, "WebSocket错误: " + error);
    }

    // 添加处理强制下线的方法
    protected void handleForceOffline(WebSocketMessage message) {
        mainHandler.post(() -> {
            try {
                // 清除登录状态
                SharedPreferencesManager.getInstance().clearLoginStatus();
                
                // 关闭所有Activity返回登录界面
                if (context instanceof Activity) {
                    Activity currentActivity = (Activity) context;
                    
                    // 显示提示对话框
                    new AlertDialog.Builder(currentActivity)
                        .setTitle("下线通知")
                        .setMessage("您的账号在其他设备上登录，您已被迫下线")
                        .setCancelable(false)
                        .setPositiveButton("确定", (dialog, which) -> {
                            // 关闭WebSocket连接
                            webSocketService.disconnect();
                            
                            // 跳转到登录界面
                            Intent intent = new Intent(currentActivity, LoginActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            currentActivity.startActivity(intent);
                            currentActivity.finish();
                        })
                        .show();
                }
            } catch (Exception e) {
                Log.e(TAG, "处理强制下线失败", e);
            }
        });
    }

    // 添加处理在线检测的
    protected void handleOnlineCheck(WebSocketMessage message) {
        WebSocketMessage response = new WebSocketMessage(
            MessageType.ONLINE_CHECK.getValue(),
            currentUsername,
            "server",
            "online"
        );
        sendWebSocketMessage(response);
    }

    @Override
    public void updateUnreadCount(String fromUsername, int count) {
        notificationService.setUnreadCount(fromUsername, count);
    }

    @Override
    public void clearUnreadCount(String username) {
        notificationService.clearUnreadCount(username);
    }

    @Override
    public int getUnreadCount(String username) {
        return notificationService.getUnreadCount(username);
    }

    @Override
    public void updateFriendRequestCount(int count) {
        notificationService.setFriendRequestCount(count);
    }

    protected void sendWebSocketMessage(WebSocketMessage message) {
        try {
            String jsonMessage = gson.toJson(message);
            Log.d(TAG, "发送WebSocket消息: " + jsonMessage);  // 添加日志
            webSocketService.sendMessage(jsonMessage);
        } catch (Exception e) {
            Log.e(TAG, "发送消息失败: " + e.getMessage(), e);
        }
    }
}