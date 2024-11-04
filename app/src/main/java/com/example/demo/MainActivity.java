package com.example.demo;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import java.net.URI;

public class MainActivity extends AppCompatActivity {
    private ChatWebSocketClient webSocketClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 连接到 WebSocket 服务器
        try {
            System.out.println("Start.....");
            URI uri = new URI("https://localhost:8080"); // 替换为你的服务器地址
            webSocketClient = new ChatWebSocketClient(uri);
            webSocketClient.connect();
            System.out.println("Connected.....");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (webSocketClient != null) {
            webSocketClient.close(); // 关闭连接
        }
    }

    // 发送消息的示例
    private void sendMessage() {
        if (webSocketClient != null && webSocketClient.isOpen()) {
            webSocketClient.sendMessage("Hello, server!");
        }
    }
}
