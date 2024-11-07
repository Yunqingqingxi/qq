package com.example.qq.websocket.service;

import android.content.Context;


import com.example.qq.websocket.domain.Message;
import com.example.qq.websocket.web.WebClient;

import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class WebSocketService {
    private WebClient webClient;
    private WebSocket webSocket;
    private WebSocketListener webSocketListener;
    private String token;
    private Context context;

    public WebSocketService(Context context, String token) {
        this.context = context;
        this.token = token;
        this.webClient = WebClient.getInstance();
        setupWebSocketListener();
    }

    private void setupWebSocketListener() {
        webSocketListener = new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, okhttp3.Response response) {
                System.out.println("WebSocket connected.");
            }

            @Override
            public void onMessage(WebSocket webSocket, String text) {
                // 处理接收到的消息
            }

            @Override
            public void onFailure(WebSocket webSocket, Throwable t, okhttp3.Response response) {
                t.printStackTrace();
            }

            @Override
            public void onClosing(WebSocket webSocket, int code, String reason) {
                System.out.println("WebSocket closed: " + reason);
            }
        };
    }

    public void connect() {
        webClient.connect(token, webSocketListener);
        this.webSocket = webClient.getWebSocket();
    }

    public void sendMessage(Message message) {
        if (webSocket != null) {
            webSocket.send(message.toJson().toString());
        }
    }

    public WebSocket getWebSocket() {
        return webSocket;
    }

    public void close() {
        if (webSocket != null) {
            webSocket.close(1000, "Activity destroyed");
        }
    }
}
