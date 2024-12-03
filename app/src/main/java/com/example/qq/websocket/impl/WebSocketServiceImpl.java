package com.example.qq.websocket.impl;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.qq.domain.ChatMessage;
import com.example.qq.domain.WebSocketMessage;
import com.example.qq.utils.SharedPreferencesManager;
import com.example.qq.websocket.WebSocketService;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * WebSocket服务实现类，用于管理与服务器的WebSocket连接
 * 实现了单例模式，确保整个应用只有一个WebSocket连接实例
 */
public class WebSocketServiceImpl implements WebSocketService {
    private static final String TAG = "WebSocketServiceImpl";
    /** WebSocket服务器地址 */
    private static final String WS_URL = "wss://web.yxdfirst.top/ws";
    /** 重连间隔时间（毫秒） */
    private static final int RECONNECT_INTERVAL = 5000;
    /** 最大重连尝试次数 */
    private static final int MAX_RECONNECT_ATTEMPTS = 12;

    /** 单例实例 */
    private static WebSocketServiceImpl instance;
    /** WebSocket客户端实例 */
    private WebSocketClient webSocketClient;
    /** 主线程Handler */
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    /** WebSocket监听器列表 */
    private final List<WebSocketListener> listeners = new ArrayList<>();
    /** 是否正在连接中 */
    private boolean isConnecting = false;
    /** 重连尝试次数 */
    private int reconnectAttempts = 0;
    /** 连接状态 */
    private boolean isConnected = false;

    /**
     * 私有构造函数，防止外部实例化
     */
    private WebSocketServiceImpl() {
    }

    /**
     * 获取WebSocketService的单例实例
     * @return WebSocketService实例
     */
    public static synchronized WebSocketServiceImpl getInstance() {
        if (instance == null) {
            instance = new WebSocketServiceImpl();
        }
        return instance;
    }

    /**
     * 初始化WebSocket连接
     * 获取token，构建WebSocket连接并设置认证头
     */
    @Override
    public void init() {
        String token = SharedPreferencesManager.getInstance().getToken();
        
        if (token == null) {
            Log.e(TAG, "Token is null, cannot initialize WebSocket");
            return;
        }

        try {
            URI uri = URI.create(WS_URL);
            Log.d(TAG, "Connecting to WebSocket URL: " + WS_URL);
            
            Map<String, String> headers = new HashMap<>();
            headers.put("Authorization", token);
            
            connect(uri, headers);
        } catch (Exception e) {
            Log.e(TAG, "Error initializing WebSocket", e);
            notifyError("WebSocket初始化失败: " + e.getMessage());
        }
    }

    /**
     * 建立WebSocket连接
     * @param uri WebSocket服务器URI
     * @param headers 请求头
     */
    private synchronized void connect(URI uri, Map<String, String> headers) {
        if (isConnecting || (webSocketClient != null && webSocketClient.isOpen())) {
            return;
        }

        isConnecting = true;
        webSocketClient = new WebSocketClient(uri, headers) {
            @Override
            public void onOpen(ServerHandshake handshakedata) {
                Log.i(TAG, "WebSocket Connected with status: " + handshakedata.getHttpStatus() 
                    + " " + handshakedata.getHttpStatusMessage());
                mainHandler.post(() -> {
                    isConnecting = false;
                    reconnectAttempts = 0;
                    notifyConnected();
                });
            }

            @Override
            public void onMessage(String message) {
                Log.d(TAG, "Received message: " + message);
                mainHandler.post(() -> notifyMessageReceived(message));
            }

            @Override
            public void onClose(int code, String reason, boolean remote) {
                Log.w(TAG, "WebSocket Closed: " + reason);
                mainHandler.post(() -> {
                    isConnecting = false;
                    notifyDisconnected();
                    if (shouldReconnect()) {
                        scheduleReconnect();
                    }
                });
            }

            @Override
            public void onError(Exception ex) {
                Log.e(TAG, "WebSocket Error", ex);
                mainHandler.post(() -> {
                    isConnecting = false;
                    notifyError("WebSocket错误: " + ex.getMessage());
                });
            }
        };

        webSocketClient.setConnectionLostTimeout(60);
        webSocketClient.connect();
    }

    /**
     * 判断是否应该进行重连
     * @return 如果重连次数未超过最大限制，返回true
     */
    private boolean shouldReconnect() {
        return reconnectAttempts < MAX_RECONNECT_ATTEMPTS;
    }

    /**
     * 安排重连任务
     * 使用指数退避策略增加重连间隔
     */
    private void scheduleReconnect() {
        reconnectAttempts++;
        long delay = getReconnectDelay();
        Log.i(TAG, "Scheduling reconnect attempt " + reconnectAttempts + " in " + delay + "ms");
        mainHandler.postDelayed(this::init, delay);
    }

    /**
     * 计算重连延迟时间
     * @return 重连延迟时间（毫秒）
     */
    private long getReconnectDelay() {
        return Math.min(RECONNECT_INTERVAL * (long) Math.pow(1.5, reconnectAttempts - 1),
                TimeUnit.MINUTES.toMillis(2));
    }

    /**
     * 发送消息到WebSocket服务器
     * @param message 要发送的消息
     */
    @Override
    public void sendMessage(String message) {
        if (webSocketClient != null && webSocketClient.isOpen()) {
            try {
                Log.d(TAG, "WebSocket发送消息: " + message);
                webSocketClient.send(message);
            } catch (Exception e) {
                Log.e(TAG, "Error sending message", e);
                notifyError("发送消息失败: " + e.getMessage());
            }
        } else {
            Log.e(TAG, "WebSocket未连接");
            if (!isConnecting) {
                // 如果未连接且不在连接过程中，尝试重新连接
                init();
            }
        }
    }

    /**
     * 添加WebSocket监听器
     * @param listener 要添加的监听器
     */
    @Override
    public void addListener(WebSocketListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    /**
     * 移除WebSocket监听器
     * @param listener 要移除的监听器
     */
    @Override
    public void removeListener(WebSocketListener listener) {
        listeners.remove(listener);
    }

    /**
     * 断开WebSocket连接
     * 清理相关资源
     */
    @Override
    public void disconnect() {
        if (webSocketClient != null) {
            try {
                webSocketClient.close();
            } catch (Exception e) {
                Log.e(TAG, "Error closing WebSocket", e);
            }
        }
        mainHandler.removeCallbacksAndMessages(null);
        isConnecting = false;
        reconnectAttempts = 0;
    }

    @Override
    public void onMessage(String message) {
        Log.d(TAG, "收到WebSocket消息: " + message);
        try {
            JSONObject jsonMessage = new JSONObject(message);
            int systemType = jsonMessage.getInt("system");
            String user = jsonMessage.getString("user");
            String targetname = jsonMessage.getString("targetname");
            String content = jsonMessage.getString("message");
            
            // 创建 WebSocketMessage 对象
            WebSocketMessage wsMessage = new WebSocketMessage(systemType, user, targetname, content);
            
            // 通知所有监听器
            for (WebSocketListener listener : new ArrayList<>(listeners)) {
                listener.onMessageReceived(message);
            }
        } catch (JSONException e) {
            Log.e(TAG, "解析消息失败: " + e.getMessage());
        }
    }

    private void notifyConnected() {
        for (WebSocketListener listener : new ArrayList<>(listeners)) {
            listener.onConnected();
        }
    }

    private void notifyDisconnected() {
        for (WebSocketListener listener : new ArrayList<>(listeners)) {
            listener.onDisconnected();
        }
    }

    private void notifyMessageReceived(String message) {
        for (WebSocketListener listener : new ArrayList<>(listeners)) {
            listener.onMessageReceived(message);
        }
    }

    private void notifyError(String error) {
        for (WebSocketListener listener : new ArrayList<>(listeners)) {
            listener.onError(error);
        }
    }

    @Override
    public void sendMessage(ChatMessage message) {
        try {
            // 使用 WebSocketMessage 格式创建消息
            JSONObject jsonMessage = new JSONObject();
            jsonMessage.put("systemType", 1);  // 普通聊天消息
            jsonMessage.put("fromUsername", message.getSender());
            jsonMessage.put("toUsername", message.getReceiver());
            jsonMessage.put("content", message.getContent());
            
            // 发送消息
            if (webSocketClient != null && webSocketClient.isOpen()) {
                String messageStr = jsonMessage.toString();
                Log.d(TAG, "发送聊天消息: " + messageStr);
                webSocketClient.send(messageStr);
            } else {
                Log.w(TAG, "WebSocket连接未建立或已关闭");
                if (!isConnecting) {
                    init();
                }
            }
        } catch (JSONException e) {
            Log.e(TAG, "发送消息失败：" + e.getMessage());
        }
    }

    @Override
    public void reconnectIfNeeded() {
        if (webSocketClient == null || !webSocketClient.isOpen()) {
            Log.d(TAG, "WebSocket连接已断开，尝试重新连接");
            init();
        } else {
            Log.d(TAG, "WebSocket连接正常，无需重连");
        }
    }
} 