package com.example.qq;

import static com.example.qq.util.JsonUtil.parseMessage;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.qq.adapter.ChatMessageAdapter;
import com.example.qq.pojo.ChatMessage;
import com.example.qq.websocket.db.ChatDatabaseHelper;
import com.example.qq.websocket.domain.Message;
import com.example.qq.websocket.web.WebClient;
import com.example.qq.websocket.webUtils.controller.MessageFilter;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Map;

import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class ChatActivity3 extends BaseActivity {

    private EditText inputMessage;
    private static String currentUsername;
    private String friendId;
    private RecyclerView recyclerView;
    private ChatDatabaseHelper dbHelper;
    private ChatMessageAdapter messageAdapter;
    private ArrayList<ChatMessage> messageList;
    private WebClient webClient;
    private WebSocket webSocket;
    private MessageFilter messageFilter;
    private WebSocketListener webSocketListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        currentUsername = getSharedPreferences("MyRefs", MODE_PRIVATE).getString("current_username", "");

        // 初始化 WebSocket 客户端和消息过滤器
        initializeWebSocket();

        initialize(); // 初始化组件
        setupRecyclerView(); // 设置 RecyclerView
        loadMessagesFromDatabase(); // 加载消息记录
        setupListeners(); // 设置按钮点击事件
    }

    // 初始化 WebSocket 连接和消息过滤器
    private void initializeWebSocket() {
        webClient = WebClient.getInstance();
        messageFilter = message -> (Integer) message.get("system") == 1; // 仅接受 system == 1 的消息
        setupWebSocketListener();
        connectWebSocket();
    }

    private void setupWebSocketListener() {
        webSocketListener = new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                Log.d("WebSocket", "WebSocket connected.");
            }

            @Override
            public void onMessage(WebSocket webSocket, String text) {
                handleServerMessage(text);
            }

            @Override
            public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                Log.e("WebSocket", "Connection failed", t);
            }

            @Override
            public void onClosing(@NonNull WebSocket webSocket, int code, String reason) {
                Log.d("WebSocket", "Closing connection: " + reason);
            }
        };
    }

    private void connectWebSocket() {
        SharedPreferences sharedPreferences = getSharedPreferences("MyRefs", MODE_PRIVATE);
        String token = sharedPreferences.getString("token", "");
        webClient.connect(token, webSocketListener);
        webSocket = webClient.getWebSocket();
    }

    @SuppressLint("NotifyDataSetChanged")
    private void handleServerMessage(String text) {
        // 解析服务器发送的消息
        Map<String, Object> result = parseMessage(text);

        // 确保解析结果有效
        if (result != null) {
            String user = (String) result.get("user");
            String messageContent = (String) result.get("message");
            String targetname = (String) result.get("targetname");

            Log.d("ChatActivity3", "Received message: user=" + user + ", messageContent=" + messageContent + ", targetname=" + targetname);

            if (targetname == null || currentUsername == null) {
                Log.w("ChatActivity3", "Received message with null targetname or currentUsername.");
                return;
            }

            // 仅当消息的目标是当前用户时，才处理消息
            if (targetname.equals(currentUsername)) {
                // 构建并添加新的聊天消息
                ChatMessage chatMessage = new ChatMessage(
                        getAvatarResourceId(user), // 获取头像资源ID
                        user,                       // 发送者
                        targetname,                 // 接收者
                        messageContent,             // 消息内容
                        getCurrentTime(),           // 消息发送时间
                        R.drawable.p9               // 默认头像
                );

                // 使用 runOnUiThread() 将更新 UI 的操作放到主线程中
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // 将消息添加到数据库
                        dbHelper.insertMessage(chatMessage);

                        // 添加消息到消息列表
                        messageList.add(chatMessage);

                        // 通知适配器更新并插入新消息
                        messageAdapter.notifyItemInserted(messageList.size() - 1);

                        // 滚动到最新消息
                        recyclerView.scrollToPosition(messageList.size() - 1);
                    }
                });

            } else {
                Log.d("ChatActivity3", "Message not for current user: currentUsername=" + currentUsername + ", targetname=" + targetname);
            }
        } else {
            Log.w("ChatActivity3", "Failed to parse server message: " + text);
        }
    }

    private void initialize() {
        friendId = getIntent().getStringExtra("friendId");
        currentUsername = getSharedPreferences("MyRefs", MODE_PRIVATE).getString("current_username", "");
        dbHelper = new ChatDatabaseHelper(this);
        messageList = new ArrayList<>();
        inputMessage = findViewById(R.id.inputMessage);
        TextView nicknameTextView = findViewById(R.id.nickname);
        nicknameTextView.setText(friendId); // 显示好友昵称
    }

    private void setupRecyclerView() {
        recyclerView = findViewById(R.id.recyclerView);
        messageAdapter = new ChatMessageAdapter(this, messageList, currentUsername);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(messageAdapter);
    }

    private void loadMessagesFromDatabase() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;
        try {
            // SQL查询，查询两种消息（当前用户发送的消息和目标好友发送的消息）
            String query = "SELECT * FROM " + ChatDatabaseHelper.TABLE_MESSAGES + " WHERE (" +
                    ChatDatabaseHelper.COLUMN_MESSAGE_SENDER + " = ? AND " +
                    ChatDatabaseHelper.COLUMN_MESSAGE_RECEIVER + " = ?) OR (" +
                    ChatDatabaseHelper.COLUMN_MESSAGE_SENDER + " = ? AND " +
                    ChatDatabaseHelper.COLUMN_MESSAGE_RECEIVER + " = ?) ORDER BY " +
                    ChatDatabaseHelper.COLUMN_MESSAGE_TIMESTAMP + " ASC"; // 按时间排序
            String[] args = {currentUsername, friendId, friendId, currentUsername};
            cursor = db.rawQuery(query, args);

            if (cursor != null && cursor.moveToFirst()) {
                // 获取列索引
                int senderIndex = cursor.getColumnIndex(ChatDatabaseHelper.COLUMN_MESSAGE_SENDER);
                int receiverIndex = cursor.getColumnIndex(ChatDatabaseHelper.COLUMN_MESSAGE_RECEIVER);
                int contentIndex = cursor.getColumnIndex(ChatDatabaseHelper.COLUMN_MESSAGE_CONTENT);
                int timestampIndex = cursor.getColumnIndex(ChatDatabaseHelper.COLUMN_MESSAGE_TIMESTAMP);

                // 遍历查询结果
                do {
                    String sender = cursor.getString(senderIndex);
                    String receiver = cursor.getString(receiverIndex);
                    String messageContent = cursor.getString(contentIndex);
                    String timestamp = cursor.getString(timestampIndex);
                    String formattedTime = formatTimestamp(timestamp);  // 格式化时间戳
                    int avatarResId = getAvatarResourceId(sender);      // 获取头像资源ID

                    // 创建消息对象并添加到消息列表
                    messageList.add(new ChatMessage(avatarResId, sender, receiver, messageContent, formattedTime, R.drawable.p9));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("ChatActivity3", "Error loading messages", e);
        } finally {
            if (cursor != null) cursor.close();
            db.close();
        }
    }

    private String formatTimestamp(String timestamp) {
        // 假设数据库中存储的是 "HH:mm" 格式的时间
        try {
            if (timestamp.length() <= 5) {
                // 仅有时间部分，如 10:58
                DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
                LocalTime time = LocalTime.parse(timestamp, timeFormatter);
                return time.format(DateTimeFormatter.ofPattern("HH:mm"));  // 格式化为 "HH:mm"
            } else {
                // 如果时间戳是完整日期时间格式（例如: "2024-11-06 10:58:00"）
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                LocalDateTime dateTime = LocalDateTime.parse(timestamp, formatter);
                return dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));  // 格式化为 "yyyy-MM-dd HH:mm:ss"
            }
        } catch (DateTimeParseException e) {
            Log.e("ChatActivity3", "DateTimeParseException: " + e.getMessage());
            return timestamp;  // 如果解析失败，返回原始时间字符串
        }
    }

    private int getAvatarResourceId(String username) {
        return R.drawable.p9; // 目前只使用一个默认头像
    }

    private String getCurrentTime() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        return LocalDateTime.now().format(formatter);
    }

    private void setupListeners() {
        findViewById(R.id.backButton).setOnClickListener(v -> finish()); // 返回按钮
        findViewById(R.id.sendButton).setOnClickListener(v -> sendMessage()); // 发送按钮
    }

    private void sendMessage() {
        String messageContent = inputMessage.getText().toString().trim();
        if (!messageContent.isEmpty()) {
            insertMessageToDatabase(currentUsername, friendId, messageContent);
            ChatMessage message = new ChatMessage(getAvatarResourceId(currentUsername), currentUsername, friendId, messageContent, getCurrentTime(), R.drawable.p9);
            messageList.add(message);
            messageAdapter.notifyItemInserted(messageList.size() - 1);
            recyclerView.scrollToPosition(messageList.size() - 1); // 滚动到最后一条消息
            inputMessage.setText(""); // 清空输入框

            Message jsonMessage = new Message(1, currentUsername, friendId, messageContent);
            String jsonString = jsonMessage.toJson().toString();
            webSocket.send(jsonString); // 发送消息到服务器
        }
    }

    private void insertMessageToDatabase(String sender, String receiver, String message) {
        try (SQLiteDatabase db = dbHelper.getWritableDatabase()) {
            db.execSQL("INSERT INTO " + ChatDatabaseHelper.TABLE_MESSAGES +
                            " (" + ChatDatabaseHelper.COLUMN_MESSAGE_SENDER + ", " +
                            ChatDatabaseHelper.COLUMN_MESSAGE_RECEIVER + ", " +
                            ChatDatabaseHelper.COLUMN_MESSAGE_CONTENT + ") VALUES (?, ?, ?)",
                    new Object[]{sender, receiver, message}); // 使用参数化查询避免 SQL 注入
        } catch (Exception e) {
            Log.e("ChatActivity3", "Error inserting message", e);
        }
    }
}
