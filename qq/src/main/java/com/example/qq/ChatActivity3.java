package com.example.qq;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.qq.adapter.ChatMessageAdapter;
import com.example.qq.pojo.ChatMessage;
import com.example.qq.websocket.db.ChatDatabaseHelper;
import com.example.qq.websocket.domain.Message;
import com.example.qq.websocket.web.WebClient;
import com.example.qq.websocket.webResult.WebResult;
import com.example.qq.websocket.webUtils.GetNowUser;
import com.example.qq.websocket.webUtils.controller.Callback;
import com.google.gson.Gson;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.Map;

import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

/**
 * ChatActivity3 负责处理聊天界面功能，包括显示消息、发送消息和加载消息记录。
 */
public class ChatActivity3 extends BaseActivity {

    private EditText inputMessage; // 输入消息的 EditText
    private static String currentUsername; // 当前用户昵称
    private String friendId; // 好友 ID（昵称）
    private RecyclerView recyclerView; // RecyclerView 用于显示聊天记录
    private ChatDatabaseHelper dbHelper; // 数据库助手实例
    private ChatMessageAdapter messageAdapter; // 消息适配器
    private ArrayList<ChatMessage> messageList; // 消息列表
    private WebClient webClient;
    private String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // 初始化webClient
        webClient = WebClient.getInstance();


        // 获取token
        token = getSharedPreferences("MyPrefs", MODE_PRIVATE).getString("token", "");

        initialize(); // 初始化组件
        setupRecyclerView(); // 设置 RecyclerView
        loadMessagesFromDatabase(currentUsername); // 加载消息记录
        setupListeners(); // 设置按钮点击事件
    }

    /**
     * 初始化界面组件
     */
    private void initialize() {
        friendId = getIntent().getStringExtra("friendId");
        currentUsername = getSharedPreferences("MyPrefs", MODE_PRIVATE).getString("current_username", "");
        dbHelper = new ChatDatabaseHelper(this);
        messageList = new ArrayList<>();
        inputMessage = findViewById(R.id.inputMessage);
        TextView nicknameTextView = findViewById(R.id.nickname);
        nicknameTextView.setText(friendId); // 显示好友昵称
    }

    /**
     * 设置 RecyclerView 的布局管理器和适配器
     */
    private void setupRecyclerView() {
        recyclerView = findViewById(R.id.recyclerView);
        messageAdapter = new ChatMessageAdapter(ChatActivity3.this,messageList,currentUsername);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(messageAdapter);
    }

    /**
     * 加载消息记录
     * 根据当前用户和好友ID查询并加载消息
     */
    private void loadMessagesFromDatabase(String currentUsername) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;
        System.out.println("currentUsername: " + currentUsername);
        System.out.println("friendId: " + friendId);
        String[] args = new String[]{currentUsername,friendId};
        System.out.println(args[0]);
        try {


            // 获取聊天记录查询语句和参数
            String query = getChatHistoryQuery();
            System.out.println(query);

            // 执行查询
            cursor  = db.rawQuery(query,args);

            // 判断查询结果是否为空
            if (cursor != null && cursor.moveToFirst()) {
                // 获取列索引（避免多次调用 `cursor.getColumnIndex`）
                int senderColumnIndex = cursor.getColumnIndex(ChatDatabaseHelper.COLUMN_MESSAGE_SENDER);
                int receiverColumnIndex = cursor.getColumnIndex(ChatDatabaseHelper.COLUMN_MESSAGE_RECEIVER);
                int contentColumnIndex = cursor.getColumnIndex(ChatDatabaseHelper.COLUMN_MESSAGE_CONTENT);
                int timestampColumnIndex = cursor.getColumnIndex(ChatDatabaseHelper.COLUMN_MESSAGE_TIMESTAMP);

                // 遍历每一行记录并添加到消息列表
                do {
                    // 获取消息的发送者、接收者、内容、时间戳
                    String sender = cursor.getString(senderColumnIndex);
                    String receiver = cursor.getString(receiverColumnIndex);
                    String messageContent = cursor.getString(contentColumnIndex);
                    String timestamp = cursor.getString(timestampColumnIndex); // 获取时间戳

                    // 获取头像资源ID
                    int avatarResId = getAvatarResourceId(sender);

                    // 创建新的 ChatMessage 实例并添加到列表
                    messageList.add(new ChatMessage(avatarResId, sender, messageContent, timestamp, receiver, R.drawable.p9));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            // 处理异常情况
            Log.e("ChatActivity3", "Error loading messages", e);
        } finally {
            // 关闭 Cursor 和数据库
            if (cursor != null) {
                cursor.close();
            }
            if (db != null) {
                db.close();
            }
        }
    }


    /**
     * 获取用户头像资源ID
     * @param username 用户名
     * @return 头像资源ID
     */
    private int getAvatarResourceId(String username) {
        // 根据用户名返回对应的头像资源ID，默认值为某个资源ID
        return R.drawable.p9; // 假设有一个默认头像
    }

    /**
     * 获取当前时间字符串
     * @return 当前时间
     */
    private String getCurrentTime() {
        // 实现获取当前时间的逻辑
        return "12:00"; // 示例，实际应返回当前时间
    }

    /**
     * 构建聊天记录查询语句
     * 通过当前用户和好友ID来查询消息记录
     *
     * @return 查询语句
     */
    private String getChatHistoryQuery() {
        // 查询当前用户和好友之间的消息，按时间顺序排序
        return "SELECT * FROM " + ChatDatabaseHelper.TABLE_MESSAGES + " WHERE " +
                ChatDatabaseHelper.COLUMN_MESSAGE_SENDER + " = ? AND " + ChatDatabaseHelper.COLUMN_MESSAGE_RECEIVER + " = ? ";
    }

    /**
     * 设置按钮点击事件
     */
    private void setupListeners() {
        findViewById(R.id.backButton).setOnClickListener(v -> finish()); // 返回按钮
        findViewById(R.id.sendButton).setOnClickListener(v -> sendMessage()); // 发送按钮
    }

    /**
     * 发送消息
     */
    private void sendMessage() {
        String messageContent = inputMessage.getText().toString().trim();
        if (!messageContent.isEmpty()) {
            insertMessageToDatabase(currentUsername, friendId, messageContent);
            int avatarResId = getAvatarResourceId(currentUsername); // 获取当前用户头像资源ID
            ChatMessage message = new ChatMessage(currentUsername,friendId,messageContent,getCurrentTime(),R.drawable.p9/* 静态资源暂代 */);
            messageList.add(message);
            messageAdapter.notifyItemInserted(messageList.size() - 1);
            recyclerView.scrollToPosition(messageList.size() - 1); // 滚动到最后一条消息
            inputMessage.setText(""); // 清空输入框

            // 将消息解析为 {System:1, Sender:xxx, Receiver:xxxx, message:xxxxx}
            Message jsonMessage = new Message(1,currentUsername,friendId,messageContent);
            String jsonString = jsonMessage.toJson().toString();

            // 发送消息到服务器
            webClient.sendMessage(jsonString);
        }
    }

    /**
     * 将消息插入到数据库
     *
     * @param sender   发送者
     * @param receiver 接收者
     * @param message  消息内容
     */
    private void insertMessageToDatabase(String sender, String receiver, String message) {
        try (SQLiteDatabase db = dbHelper.getWritableDatabase()) {
            db.execSQL("INSERT INTO " + ChatDatabaseHelper.TABLE_MESSAGES +
                            " (" + ChatDatabaseHelper.COLUMN_MESSAGE_SENDER + ", " +
                            ChatDatabaseHelper.COLUMN_MESSAGE_RECEIVER + ", " +
                            ChatDatabaseHelper.COLUMN_MESSAGE_CONTENT + ") VALUES (?, ?, ?)",
                    new Object[]{sender, receiver, message}); // 使用参数化查询以避免 SQL 注入
        } catch (Exception e) {
            Log.e("ChatActivity3", "Error inserting message", e);
        }
    }
}
