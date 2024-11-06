package com.example.qq;

import static com.example.qq.util.JsonUtil.parseMessage;
import static com.example.qq.websocket.webUtils.controller.WebUtil.getChatInfo;
import static com.example.qq.websocket.webUtils.controller.WebUtil.saveChatInfo;

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
import com.example.qq.websocket.webResult.WebResult;
import com.example.qq.websocket.webUtils.controller.Callback;
import com.example.qq.websocket.webUtils.controller.MessageFilter;

import org.json.JSONArray;
import org.json.JSONException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
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
    private String token;

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
        token = sharedPreferences.getString("token", "");
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

    @SuppressLint("NotifyDataSetChanged")
    private void loadMessagesFromDatabase() {
        // 从服务器加载聊天记录
        getChatInfo(token, currentUsername, friendId, new Callback() {
            @Override
            public void onResult(WebResult<Map<String, Object>> result) throws JSONException {
                if (result.getCode() == 200) {
                    // 获取返回的数据
                    Map<String, Object> data = result.getData();
                    if (data != null && data.containsKey("messages")) {
                        // 获取消息列表
                        Object messagesObj = data.get("messages");

                        // 检查 messagesObj 是否为空
                        if (messagesObj == null) {
                            // 如果 messages 为空，直接处理为空列表
                            Log.d("ChatActivity", "No messages available");
                            messageList.clear();  // 清空现有消息
                            messageAdapter.notifyDataSetChanged();  // 通知适配器更新界面
                            return;  // 直接返回，避免进一步处理
                        }

                        // 检查是否是一个 List 类型
                        if (messagesObj instanceof List) {
                            // 处理 List 类型的数据
                            List<Map<String, Object>> messageListFromServer = (List<Map<String, Object>>) messagesObj;

                            // 清空现有的消息列表，防止重复添加
                            messageList.clear();

                            // 遍历每条消息并创建 ChatMessage 对象
                            for (Map<String, Object> messageData : messageListFromServer) {
                                String sender = (String) messageData.get("sender");
                                String receiver = (String) messageData.get("receiver");
                                String content = (String) messageData.get("content");
                                String timestamp = (String) messageData.get("timestamp");
                                int avatarResId = getAvatarResourceId(sender);  // 根据发送者获取头像资源ID

                                // 格式化时间戳为需要的格式
                                String formattedTime = formatTimestamp(timestamp);  // 你可以定义这个方法来格式化时间

                                // 创建 ChatMessage 对象并添加到消息列表
                                messageList.add(new ChatMessage(sender, receiver, content, formattedTime, avatarResId));
                            }

                            // 通知适配器更新界面
                            messageAdapter.notifyDataSetChanged();
                        } else {
                            Log.e("ChatActivity", "Invalid messages format: expected a List, got " + messagesObj.getClass().getName());
                        }
                    }
                } else {
                    // 处理查询失败的情况
                    Log.e("ChatActivity", "Failed to load messages");
                }
            }
        });
    }

    // 格式化时间戳
    private String formatTimestamp(String timestamp) {
        // 假设时间戳是字符串格式的 ISO 8601 时间，使用 SimpleDateFormat 来格式化
        try {
            @SuppressLint("SimpleDateFormat") SimpleDateFormat originalFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX");
            Date date = originalFormat.parse(timestamp);
            @SuppressLint("SimpleDateFormat") SimpleDateFormat targetFormat = new SimpleDateFormat("HH:mm");
            return targetFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return timestamp; // 如果格式化失败，返回原始时间戳
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

            // 双向添加
//            insertMessageToDatabase(friendId, currentUsername, messageContent); // 当前用户向好友发送
//            insertMessageToDatabase(currentUsername, friendId, messageContent); // 好友向当前用户发送

            // 添加到服务器
            saveChatInfo(token, currentUsername, friendId, messageContent,getAvatarResourceId(currentUsername), new Callback() {
                @Override
                public void onResult(WebResult<Map<String, Object>> result) throws JSONException {
                    if (result.getCode()  == 200) {
                        System.out.println(currentUsername+"to"+friendId+"Success");
                    }
                }
            });

            ChatMessage message = new ChatMessage( currentUsername, friendId, messageContent, getCurrentTime(), R.drawable.p9);
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
            Log.d("ChatActivity3", "Message inserted: " + sender + " -> " + receiver + ": " + message);
        } catch (Exception e) {
            Log.e("ChatActivity3", "Error inserting message", e);
        }
    }

}
