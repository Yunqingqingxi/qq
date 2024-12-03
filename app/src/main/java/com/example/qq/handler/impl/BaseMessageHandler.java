package com.example.qq.handler.impl;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.qq.domain.WebSocketMessage;
import com.example.qq.handler.MessageHandler;
import com.example.qq.utils.SharedPreferencesManager;
import com.example.qq.websocket.impl.WebSocketServiceImpl;
import com.google.gson.Gson;

/**
 * 消息处理器基类
 * 提供消息处理的基础功能，包括WebSocket服务、用户认证和消息序列化等
 *
 * @author yunxi
 * @version 1.0
 * @see WebSocketMessage
 * @see WebSocketServiceImpl
 * @see MessageHandler
 */
public abstract class BaseMessageHandler {
    /** 日志标签 */
    protected static final String TAG = "MessageHandler";
    
    /** WebSocket服务实例 */
    protected final WebSocketServiceImpl webSocketService;
    /** 当前用户名 */
    protected final String currentUsername;
    /** 应用上下文 */
    protected final Context context;
    /** SharedPreferences管理器 */
    protected final SharedPreferencesManager prefsManager;
    /** JSON序列化工具 */
    protected final Gson gson;
    /** 主线程Handler */
    protected final Handler mainHandler;

    /**
     * 构造函数
     * 初始化基础组件并验证用户登录状态
     *
     * @param context 应用上下文
     * @throws IllegalStateException 如果用户未登录或用户名为空
     */
    public BaseMessageHandler(Context context) {
        this.context = context;
        this.mainHandler = new Handler(Looper.getMainLooper());

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
    }

    /**
     * 发送WebSocket消息
     * 将消息对象序列化为JSON并通过WebSocket发送
     *
     * @param message 要发送的消息对象
     */
    protected void sendWebSocketMessage(WebSocketMessage message) {
        try {
            String jsonMessage = gson.toJson(message);
            webSocketService.sendMessage(jsonMessage);
        } catch (Exception e) {
            Log.e(TAG, "Error sending message", e);
        }
    }
}