package com.example.demo;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import java.net.URI;

public class ChatWebSocketClient extends WebSocketClient {

    public ChatWebSocketClient(URI serverUri) {
        super(serverUri);
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        // 连接成功
        System.out.println("Connected to server");
    }

    @Override
    public void onMessage(String message) {
        // 接收到消息
        System.out.println("Message from server: " + message);
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        // 连接关闭
        System.out.println("Connection closed: " + reason);
    }

    @Override
    public void onError(Exception ex) {
        System.out.println("error.......");
        // 出现错误
        ex.printStackTrace();
    }

    public void sendMessage(String message) {
        send(message); // 发送消息
    }
}
