package com.example.qq.handler.impl;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.qq.constant.MessageType;
import com.example.qq.domain.WebSocketMessage;
import com.example.qq.event.FriendListUpdateEvent;
import com.example.qq.fragment.FriendListFragment;
import com.example.qq.utils.SharedPreferencesManager;

import org.greenrobot.eventbus.EventBus;

public class MessageProcessor {
    private static final String TAG = "MessageProcessor";
    private final MessageHandlerImpl messageHandler;
    private final NotificationHelper notificationHelper;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public MessageProcessor(MessageHandlerImpl messageHandler, NotificationHelper notificationHelper) {
        this.messageHandler = messageHandler;
        this.notificationHelper = notificationHelper;
    }

    public void processMessage(WebSocketMessage message) {
        try {
            int messageType = message.getSystemType();
            Log.d(TAG, "处理消息: type=" + messageType);

            if (messageType == MessageType.CHAT.getValue()) {
                Log.d(TAG, "处理聊天消息");
                messageHandler.handleChatMessage(message);
            } else if (messageType == MessageType.FRIEND_REQUEST.getValue()) {
                Log.d(TAG, "处理好友请求");
                messageHandler.handleFriendRequest(message);
            } else if (messageType == MessageType.FRIEND_ACCEPT.getValue()) {
                Log.d(TAG, "处理好友请求接受");
                messageHandler.handleFriendRequestAccepted(message);

                // 延迟一点时间后刷新，确保服务器数据已更新
                mainHandler.postDelayed(() -> {
                    try {
                        // 先发送事件
                        EventBus.getDefault().post(new FriendListUpdateEvent());

                        // 直接调用刷新
                        FriendListFragment.refreshFriendList();

                        // 强制清除缓存
                        SharedPreferencesManager.getInstance().clearFriendListCache();

                        Log.d(TAG, "已触发好友列表刷新");
                    } catch (Exception e) {
                        Log.e(TAG, "刷新好友列表失败: " + e.getMessage(), e);
                    }
                }, 1000); // 延迟1秒执行

            } else if (messageType == MessageType.FRIEND_DELETED.getValue()) {
                Log.d(TAG, "处理好友删除");
                messageHandler.handleFriendDeleted(message);
                EventBus.getDefault().post(new FriendListUpdateEvent());
            } else if (messageType == MessageType.FRIEND_REJECT.getValue()) {
                Log.d(TAG, "处理好友请求拒绝");
                messageHandler.handleFriendRequestRejected(message);
            } else if (messageType == MessageType.FORCE_OFFLINE.getValue()) {
                Log.d(TAG, "处理强制下线");
                messageHandler.handleForceOffline(message);
            } else if (messageType == MessageType.ONLINE_CHECK.getValue()) {
                Log.d(TAG, "处理在线检测");
                messageHandler.handleOnlineCheck(message);
            } else {
                Log.w(TAG, "未知的消息类型: " + messageType);
            }
        } catch (Exception e) {
            Log.e(TAG, "处理消息失败: " + e.getMessage(), e);
        }
    }
}