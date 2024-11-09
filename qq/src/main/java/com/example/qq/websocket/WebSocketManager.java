package com.example.qq.websocket;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class WebSocketManager {
    private static WebSocketManager instance;
    private WebSocket webSocket;
    private boolean isConnected = false;

    public static synchronized WebSocketManager getInstance() {
        if (instance == null) {
            instance = new WebSocketManager();
        }
        return instance;
    }

    public void connect(String token) {
        // 连接WebSocket
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("wss://web.yxdfirst.top/ws")
                .addHeader("Authorization", token)
                .build();
        webSocket = client.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                isConnected = true;
                // 可以发送一些初始化消息
            }

            @Override
            public void onMessage(WebSocket webSocket, String text) {
                // 处理接收到的消息
            }

            @Override
            public void onClosing(WebSocket webSocket, int code, String reason) {
                isConnected = false;
            }

            @Override
            public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                isConnected = false;
                // 尝试重连
                reconnect();
            }
        });
    }

    public void reconnect() {
        // 实现重连逻辑
    }

    public boolean isConnected() {
        return isConnected;
    }

    public void disconnect() {
        if (webSocket != null) {
            webSocket.close(1000, null);
        }
    }
}