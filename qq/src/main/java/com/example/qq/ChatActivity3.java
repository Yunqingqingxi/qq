package com.example.qq;

import static com.example.qq.util.JsonUtil.parseMessage;
import static com.example.qq.websocket.webUtils.controller.WebUtil.getChatInfo;
import static com.example.qq.websocket.webUtils.controller.WebUtil.saveChatInfo;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.qq.adapter.ChatMessageAdapter;
import com.example.qq.pojo.ChatMessage;
import com.example.qq.pojo.User;
import com.example.qq.websocket.domain.Message;
import com.example.qq.websocket.web.WebClient;
import com.example.qq.websocket.webResult.WebResult;
import com.example.qq.websocket.webUtils.GetNowUser;
import com.example.qq.websocket.webUtils.controller.Callback;
import com.example.qq.websocket.webUtils.controller.MessageFilter;

import org.json.JSONException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class ChatActivity3 extends BaseActivity {

    private EditText inputMessage;
    private static String currentUsername;
    private String friendId;
    private String friendNickname;
    private RecyclerView recyclerView;
//    private ChatDatabaseHelper dbHelper;
    private ChatMessageAdapter messageAdapter;
    private ArrayList<ChatMessage> messageList;
    private WebClient webClient;
    private WebSocket webSocket;
    private MessageFilter messageFilter;
    private WebSocketListener webSocketListener;
    private String token;
    private GetNowUser getNowUser;

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
                        null             // 默认头像
                );

                // 使用 runOnUiThread() 将更新 UI 的操作放到主线程中
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
//                        // 将消息添加到数据库
//                        dbHelper.insertMessage(chatMessage);

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
        friendNickname = getIntent().getStringExtra("friendNickname");
        currentUsername = getSharedPreferences("MyRefs", MODE_PRIVATE).getString("current_username", "");
        messageList = new ArrayList<>();
        inputMessage = findViewById(R.id.inputMessage);
        TextView nicknameTextView = findViewById(R.id.nickname);
        nicknameTextView.setText(friendNickname); // 显示好友昵称
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
                    if (data != null) {
                        // 获取发送者和接收者的消息列表，可能没有消息
                        List<Map<String, Object>> senderMessages = (List<Map<String, Object>>) data.get("senderMessages");
                        List<Map<String, Object>> receiverMessages = (List<Map<String, Object>>) data.get("receiverMessages");

                        // 清空现有的消息列表，防止重复添加
                        messageList.clear();

                        // 处理发送者的消息（如果有的话）
                        if (senderMessages != null) {
                            for (Map<String, Object> messageData : senderMessages) {
                                String sender = (String) messageData.get("sender");
                                String receiver = (String) messageData.get("receiver");
                                String content = (String) messageData.get("content");
                                String timestamp = (String) messageData.get("timestamp");
                                String avatarResId =(String) messageData.get("avatarResId");  // 根据发送者获取头像资源ID

                                // 格式化时间戳为需要的格式
                                String formattedTime = formatTimestamp(timestamp);  // 你可以定义这个方法来格式化时间

                                // 创建 ChatMessage 对象并添加到消息列表
                                messageList.add(new ChatMessage(sender, receiver, content, formattedTime, avatarResId));
                            }
                        }

                        // 处理接收者的消息（如果有的话）
                        if (receiverMessages != null) {
                            for (Map<String, Object> messageData : receiverMessages) {
                                String sender = (String) messageData.get("sender");
                                String receiver = (String) messageData.get("receiver");
                                String content = (String) messageData.get("content");
                                String timestamp = (String) messageData.get("timestamp");
                                String avatarResId =(String) messageData.get("avatarResId"); // 根据接收者获取头像资源ID

                                // 格式化时间戳为需要的格式
                                String formattedTime = formatTimestamp(timestamp);  // 你可以定义这个方法来格式化时间

                                // 创建 ChatMessage 对象并添加到消息列表
                                messageList.add(new ChatMessage(sender, receiver, content, formattedTime, avatarResId));
                            }
                        }

                        // 按照时间戳进行排序（升序），确保从最早的消息开始
                        messageList.sort(new Comparator<ChatMessage>() {
                            @Override
                            public int compare(ChatMessage o1, ChatMessage o2) {
                                // 假设 timestamp 是 ISO 8601 格式的字符串，直接进行比较
                                return o1.getFormattedTime().compareTo(o2.getFormattedTime());
                            }
                        });

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                messageAdapter.notifyDataSetChanged();
                                recyclerView.scrollToPosition(messageList.size() - 1);  // 滚动到最新的消息
                            }
                        });
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
            @SuppressLint("SimpleDateFormat") SimpleDateFormat originalFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            Date date = originalFormat.parse(timestamp);
            @SuppressLint("SimpleDateFormat") SimpleDateFormat targetFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
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
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return LocalDateTime.now().format(formatter);
    }


    private void setupListeners() {
        findViewById(R.id.backButton).setOnClickListener(v -> finish());  // 关闭当前 ChatActivity
        findViewById(R.id.sendButton).setOnClickListener(v -> sendMessage()); // 发送按钮
    }

    private void sendMessage() {
        String messageContent = inputMessage.getText().toString().trim();
        if (!messageContent.isEmpty()) {
            // 检查发送者和接收者是否有效
            if (currentUsername == null || currentUsername.isEmpty() || friendId == null || friendId.isEmpty()) {
                Log.e("ChatActivity3", "Invalid sender or receiver information.");
                return;
            }

            getNowUser = new GetNowUser(this);
            User user = getNowUser.getRememberedUser();

            // 创建消息对象
            ChatMessage message = new ChatMessage(currentUsername, friendId, messageContent, getCurrentTime(),user.getAvatar());
            messageList.add(message);

            // 保存消息到服务器
            saveChatInfo(token, currentUsername, friendId, messageContent, new Callback() {
                @Override
                public void onResult(WebResult<Map<String, Object>> result) throws JSONException {
                    if (result.getCode() == 200) {
                        // 保存成功，继续处理
                        Log.d("ChatActivity3", "Message saved successfully");
                    }
                }
            });

            // 更新消息列表
            messageAdapter.notifyItemInserted(messageList.size() - 1);
            recyclerView.scrollToPosition(messageList.size() - 1); // 滚动到最后一条消息
            inputMessage.setText(""); // 清空输入框

            // 发送消息到服务器（检查是否有问题）
            Message jsonMessage = new Message(1, currentUsername, friendId, messageContent);
            try {
                String jsonString = jsonMessage.toJson().toString();
                webSocket.send(jsonString); // 发送消息到服务器
            } catch (Exception e) {
                Log.e("ChatActivity3", "Error sending message: " + e.getMessage());
            }
        }
    }

    }
