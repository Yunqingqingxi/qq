package com.example.qq;

import android.annotation.SuppressLint;
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

import java.util.ArrayList;

/**
 * ChatActivity3 负责处理聊天界面功能，包括显示消息、发送消息和加载消息记录。
 */
public class ChatActivity3 extends BaseActivity {

    private EditText inputMessage; // 输入消息的 EditText
    private String currentUsername; // 当前用户昵称
    private String friendId; // 好友 ID（昵称）
    private RecyclerView recyclerView; // RecyclerView 用于显示聊天记录
    private ChatDatabaseHelper dbHelper; // 数据库助手实例
    private ChatMessageAdapter messageAdapter; // 消息适配器
    private ArrayList<ChatMessage> messageList; // 消息列表

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        initialize(); // 初始化组件
        setupRecyclerView(); // 设置 RecyclerView
        loadMessagesFromDatabase(); // 加载消息记录
        setupListeners(); // 设置按钮点击事件
    }

    /**
     * 初始化界面组件
     */
    private void initialize() {
        friendId = getIntent().getStringExtra("friendId");
        currentUsername = getIntent().getStringExtra("nickname");
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
     */
    private void loadMessagesFromDatabase() {
        try (SQLiteDatabase db = dbHelper.getReadableDatabase();
             Cursor cursor = db.rawQuery(getChatHistoryQuery(), getChatHistoryArgs())) {

            while (cursor.moveToNext()) {
                @SuppressLint("Range") String sender = cursor.getString(cursor.getColumnIndex(ChatDatabaseHelper.COLUMN_MESSAGE_SENDER));
                @SuppressLint("Range") String messageContent = cursor.getString(cursor.getColumnIndex(ChatDatabaseHelper.COLUMN_MESSAGE_CONTENT));
                int avatarResId = getAvatarResourceId(sender); // 获取头像资源ID
                messageList.add(new ChatMessage(avatarResId, sender, messageContent, getCurrentTime(),friendId,R.drawable.p9)); // 添加聊天消息
            }
        } catch (Exception e) {
            Log.e("ChatActivity3", "Error loading messages", e);
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
     *
     * @return 查询语句
     */
    private String getChatHistoryQuery() {
        return "SELECT * FROM " + ChatDatabaseHelper.TABLE_MESSAGES + " WHERE " +
                "(" + ChatDatabaseHelper.COLUMN_MESSAGE_SENDER + " = ? AND " + ChatDatabaseHelper.COLUMN_MESSAGE_RECEIVER + " = ?) OR " +
                "(" + ChatDatabaseHelper.COLUMN_MESSAGE_SENDER + " = ? AND " + ChatDatabaseHelper.COLUMN_MESSAGE_RECEIVER + " = ?) " +
                "ORDER BY " + ChatDatabaseHelper.COLUMN_MESSAGE_TIMESTAMP + " ASC";
    }

    /**
     * 获取聊天记录查询参数
     *
     * @return 查询参数
     */
    private String[] getChatHistoryArgs() {
        return new String[]{currentUsername, friendId, friendId, currentUsername};
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
            ChatMessage message = new ChatMessage(avatarResId, currentUsername, messageContent, getCurrentTime(),friendId, R.drawable.p9);
            messageList.add(message);
            messageAdapter.notifyItemInserted(messageList.size() - 1);
            recyclerView.scrollToPosition(messageList.size() - 1); // 滚动到最后一条消息
            inputMessage.setText(""); // 清空输入框
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
