package com.example.qq.websocket.web;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okhttp3.Response;

public class WebClient {
    private static WebClient instance;
    private OkHttpClient client;
    private WebSocket webSocket;
    private WebSocketListener listener;

    // Private constructor for singleton
    // Private constructor for singleton
    private WebClient() {
        client = new OkHttpClient.Builder()
                // 设置超时时间等参数
                .callTimeout(10000, TimeUnit.SECONDS)
                .build();
    }

    // Get singleton instance
    public static synchronized WebClient getInstance() {
        if (instance == null) {
            instance = new WebClient();
        }
        return instance;
    }

    // Connect to WebSocket
    public void connect(String token, WebSocketListener listener) {
        this.listener = listener; // 保存监听器引用
        String url = "wss://web.yxdfirst.top/ws"; // WebSocket 连接的基础 URL
//        String url = "ws://10.0.2.2:8080/ws"; // WebSocket 连接的基础 URL
//        String url = "ws://192.168.3.1:8080/ws"; // WebSocket 连接的基础 123456URL
        // 创建请求
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", token) // 在请求头中添加 token
                .build();

        // 建立 WebSocket 连接
        this.webSocket = client.newWebSocket(request, listener);
        System.out.println("Success To Connect To WebSocket");
    }


    // Send message
    public void sendMessage(String message) {
        if (webSocket != null) {
            webSocket.send(message);
        }
    }

    // Disconnect
    public void disconnect() {
        if (webSocket != null) {
            webSocket.close(1000, "Client disconnected");
            webSocket = null;
        }
    }

    // Get WebSocket instance
    public WebSocket getWebSocket() {
        return webSocket;
    }

    // 设置现有连接的监听器
    public void setWebSocketListener(WebSocketListener listener) {
        if (webSocket != null) {
            // 先关闭现有的监听器
            webSocket.close(1000, "Replacing listener");
        }
        this.listener = listener;
        // 重新建立连接以使用新监听器
        assert webSocket != null;
        webSocket = client.newWebSocket(webSocket.request(), listener);
    }
}
