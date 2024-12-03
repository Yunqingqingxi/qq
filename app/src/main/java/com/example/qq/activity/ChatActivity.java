package com.example.qq.activity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.qq.R;
import com.example.qq.adapter.ChatMessageAdapter;
import com.example.qq.adapter.EmojiAdapter;
import com.example.qq.api.messageapi.MessageApi;
import com.example.qq.api.messageapi.impl.MessageApiImpl;
import com.example.qq.api.userapi.UserApi;
import com.example.qq.api.userapi.impl.UserApiImpl;
import com.example.qq.domain.ChatMessage;
import com.example.qq.fragment.FriendListFragment;
import com.example.qq.handler.impl.MessageHandlerImpl;
import com.example.qq.service.NotificationService;
import com.example.qq.utils.SharedPreferencesManager;
import com.example.qq.websocket.WebSocketService;
import com.example.qq.websocket.impl.WebSocketServiceImpl;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * 聊天界面活动类
 * 负责处理用户之间的即时通讯功能
 *
 * @author yunxi
 * @version 1.0
 */
public class ChatActivity extends AppCompatActivity {
    private static final String TAG = "ChatActivity";
    private static final int SPEECH_REQUEST_CODE = 100;

    private RecyclerView recyclerView;
    private EditText inputMessage;
    private TextView sendButton;
    private ChatMessageAdapter messageAdapter;
    private List<ChatMessage> messageList;
    private String friendUsername;
    private String friendNickname;
    private String friendAvatar;
    private WebSocketService webSocketService;
    private MessageHandlerImpl messageHandler;
    private String currentUsername;
    private ImageView voiceButton;
    private ImageView emojiButton;
    private boolean isVoiceMode = false;

    private ActivityResultLauncher<Intent> voiceRecognitionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // 获取当前用户名
        currentUsername = SharedPreferencesManager.getInstance().getCurrentUsername();

        // 初始化 WebSocketService
        webSocketService = WebSocketServiceImpl.getInstance();

        // 使用 NotificationService 的单例实例创建 MessageHandlerImpl
        messageHandler = new MessageHandlerImpl(this, NotificationService.getInstance());

        messageHandler.setOnMessageReceivedListener((sender, content) -> {
            // 在主线程中更新UI
            runOnUiThread(() -> {
                Log.d(TAG, "收到消息: " + content + " 来自: " + sender);

                // 只有当消息是来自当前聊天的好友时才显示
                if (sender != null && sender.equals(friendUsername)) {
                    // 创建新消息对象
                    ChatMessage newMessage = new ChatMessage(
                        sender,
                        currentUsername,
                        content,
                        System.currentTimeMillis()
                    );

                    // 添加到消息列表并更新UI
                    messageList.add(newMessage);
                    messageAdapter.notifyItemInserted(messageList.size() - 1);
                    scrollToBottom();

                    // 更新本地缓存
                    updateLocalCache(newMessage);

                    // 清除该好友的未读消息计数
                    SharedPreferencesManager.getInstance().clearUnreadCount(sender);
                }
            });
        });

        // 获取传递的数据
        friendUsername = getIntent().getStringExtra("friend_username");
        friendNickname = getIntent().getStringExtra("friend_nickname");
        friendAvatar = getIntent().getStringExtra("friend_avatar");

        initChatView();
        setupClickListeners();

        // 初始化消息列表
        messageList = new ArrayList<>();
        messageAdapter = new ChatMessageAdapter(this, messageList, friendAvatar);
        recyclerView.setAdapter(messageAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // 加载历史消息
        loadMessages();

        // 除该好友的未读消息计数
        SharedPreferencesManager.getInstance().clearUnreadCount(friendUsername);

        setupInputButtons();
    }

    /**
     * 初始化聊天界面视图组件
     */
    private void initChatView() {
        recyclerView = findViewById(R.id.recyclerView);
        inputMessage = findViewById(R.id.inputMessage);
        sendButton = findViewById(R.id.sendButton);
        TextView userNickname = findViewById(R.id.userNickname);

        // 设置好友昵称
        if (friendNickname != null && !friendNickname.isEmpty()) {
            userNickname.setText(friendNickname);
        }

        ImageButton menuButton = findViewById(R.id.menuButton);
        ImageButton backButton = findViewById(R.id.backButton);

        // 设置RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        // TODO: 设置适配器
    }

    /**
     * 设置各个按钮的点击事件监听器
     */
    private void setupClickListeners() {
        // 修改返回按钮点击事件
        findViewById(R.id.backButton).setOnClickListener(v -> {
            setResult(RESULT_OK); // 设置返回结果
            finish();
        });

        // 设置菜单按钮点击事件
        findViewById(R.id.menuButton).setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(this, v);
            popup.getMenuInflater().inflate(R.menu.chat_menu, popup.getMenu());

            popup.setOnMenuItemClickListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.action_delete_friend) {
                    showDeleteFriendDialog();
                    return true;
                } else if (itemId == R.id.action_view_profile) {
                    // 打开好友资料页面
                    Intent intent = new Intent(this, FriendProfileActivity.class);
                    intent.putExtra("friend_username", friendUsername);
                    startActivity(intent);
                    return true;
                }
                return false;
            });

            popup.show();
        });

        // 修改发送按钮点击事件
        sendButton.setOnClickListener(v -> {
            String content = inputMessage.getText().toString().trim();
            if (!content.isEmpty()) {
                String currentUsername = SharedPreferencesManager.getInstance().getCurrentUsername();

                // 创建消息对象
                ChatMessage message = new ChatMessage(
                    currentUsername,
                    friendUsername,
                    content,
                    System.currentTimeMillis()
                );

                // 创建新线程执行网络请求
                new Thread(() -> {
                    try {
                        // 使用 MessageApiImpl 保存消息到数据库
                        MessageApi messageApi = new MessageApiImpl();
                        JSONObject jsonMessage = new JSONObject();
                        jsonMessage.put("sender", message.getSender());
                        jsonMessage.put("receiver", message.getReceiver());
                        jsonMessage.put("content", message.getContent());

                        boolean success = messageApi.sendMessage(jsonMessage.toString());

                        if (success) {
                            // 使用 WebSocket 实时发送消息
                            messageHandler.sendChatMessage(friendUsername, content);

                            // 在主线程更新UI
                            runOnUiThread(() -> {
                                // 清空输入框
                                inputMessage.setText("");

                                // 将消息添加到本地消息列表并更新UI
                                messageList.add(message);
                                messageAdapter.notifyItemInserted(messageList.size() - 1);
                                scrollToBottom();

                                // 更新本地缓存
                                updateLocalCache(message);
                            });
                        } else {
                            runOnUiThread(() -> {
                                Toast.makeText(ChatActivity.this,
                                    "发送消息失败",
                                    Toast.LENGTH_SHORT).show();
                            });
                        }
                    } catch (Exception e) {
                        runOnUiThread(() -> {
                            String errorMsg = e.getMessage();
                            if (errorMsg != null) {
                                // 忽略 "Invalid system type" 错误
                                if (errorMsg.contains("Invalid system type")) {
                                    return;
                                }
                                // 忽略 JSON 解析错误
                                if (errorMsg.contains("Expected BEGIN_OBJECT")) {
                                    return;
                                }
                                // 处理其他错误
                                Toast.makeText(ChatActivity.this,
                                    "发送失败: " + errorMsg,
                                    Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }).start();
            }
        });
    }

    /**
     * 加载聊天消息历史记录
     * 首先从本地缓存加载，然后从服务器获取最新消息
     */
    @SuppressLint("NotifyDataSetChanged")
    private void loadMessages() {
        // 获取本地缓存消息
        List<ChatMessage> cachedMessages = SharedPreferencesManager.getInstance()
            .getCachedChatMessages(friendUsername);

        // 如果缓存为空，创建新的空列表
        if (cachedMessages == null) {
            cachedMessages = new ArrayList<>();
        }

        // 更新UI显示缓存的消息
        messageList.clear();
        messageList.addAll(cachedMessages);

        // 按时间戳排序消息
        Collections.sort(messageList, (m1, m2) ->
            Long.compare(m1.getTimestamp(), m2.getTimestamp()));

        messageAdapter.notifyDataSetChanged();
        scrollToBottom();

        // 后台加载新消息
        new Thread(() -> {
            try {
                Log.d("ChatActivity", "开始从服务器加载消息");
                MessageApi messageApi = new MessageApiImpl();
                String currentUsername = SharedPreferencesManager.getInstance().getCurrentUsername();
                Log.d("ChatActivity", "当前用户: " + currentUsername + ", 好友: " + friendUsername);

                List<ChatMessage> newMessages = messageApi.getMessageList(currentUsername, friendUsername);

                Log.d("ChatActivity", "服务器返回消息数量: " + (newMessages != null ? newMessages.size() : 0));

                // 确保newMessages不为空
                if (newMessages == null) {
                    Log.w("ChatActivity", "务器返回的消息列表为null");
                    newMessages = new ArrayList<>();
                }

                // 在主线程更新UI
                final List<ChatMessage> finalNewMessages = newMessages;
                runOnUiThread(() -> {
                    try {
                        // 更新本地缓存
                        SharedPreferencesManager.getInstance()
                            .cacheChatMessages(friendUsername, finalNewMessages);

                        // 更新消息列表
                        messageList.clear();
                        messageList.addAll(finalNewMessages);
                        messageAdapter.notifyDataSetChanged();
                        scrollToBottom();

                        Log.d("ChatActivity", "消息加载完成，更新UI成功");
                    } catch (Exception e) {
                        Log.e("ChatActivity", "UI更新失败", e);
                    }
                });
            } catch (Exception e) {
                Log.e("ChatActivity", "加载消息失败", e);
                runOnUiThread(() -> {
                    Toast.makeText(ChatActivity.this,
                        "加载新消息失败: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    /**
     * 比较两条消息是否相同
     * @param msg1 第一条消息
     * @param msg2 第二条消息
     * @return 如果消息完全相同返回true，否则返回false
     */
    private boolean areMessagesEqual(ChatMessage msg1, ChatMessage msg2) {
        if (msg1 == null || msg2 == null) {
            return false;
        }

        return Objects.equals(msg1.getSender(), msg2.getSender()) &&
               Objects.equals(msg1.getReceiver(), msg2.getReceiver()) &&
               Objects.equals(msg1.getContent(), msg2.getContent()) &&
               msg1.getTimestamp() == msg2.getTimestamp();
    }

    /**
     * 比较两个消息列表是否相同
     * @param list1 第一个消息列表
     * @param list2 第二个消息列表
     * @return 如果两个列表内容完全相同返回true，否则返回false
     */
    private boolean areMessageListsEqual(List<ChatMessage> list1, List<ChatMessage> list2) {
        if (list1 == null || list2 == null) {
            return list1 == list2;
        }

        if (list1.size() != list2.size()) {
            return false;
        }

        for (int i = 0; i < list1.size(); i++) {
            if (!areMessagesEqual(list1.get(i), list2.get(i))) {
                return false;
            }
        }

        return true;
    }

    /**
     * 更新本地消息缓存
     * @param newMessage 需要添加到缓存的新消息
     */
    private void updateLocalCache(ChatMessage newMessage) {
        if (newMessage == null) {
            return;
        }

        List<ChatMessage> cachedMessages = SharedPreferencesManager.getInstance()
            .getCachedChatMessages(friendUsername);

        if (cachedMessages == null) {
            cachedMessages = new ArrayList<>();
        }

        // 检查是否已存在相同消息
        boolean exists = false;
        for (ChatMessage msg : cachedMessages) {
            if (areMessagesEqual(msg, newMessage)) {
                exists = true;
                break;
            }
        }

        // 如果消息不存在，则添加
        if (!exists) {
            cachedMessages.add(newMessage);
            // 按时间戳排序
            Collections.sort(cachedMessages, (m1, m2) ->
                Long.compare(m1.getTimestamp(), m2.getTimestamp()));
            SharedPreferencesManager.getInstance()
                .cacheChatMessages(friendUsername, cachedMessages);
        }
    }

    /**
     * 将聊天界面滚动到最底部
     */
    private void scrollToBottom() {
        if (!messageList.isEmpty()) {
            recyclerView.scrollToPosition(messageList.size() - 1);
        }
    }

    /**
     * 显示删除好友确认对话框
     */
    private void showDeleteFriendDialog() {
        new AlertDialog.Builder(this)
            .setTitle("删除好友")
            .setMessage("确要删除好友 " + friendNickname + " 吗？")
            .setPositiveButton("确定", (dialog, which) -> {
                deleteFriend();
            })
            .setNegativeButton("取消", null)
            .show();
    }

    /**
     * 执行删除好友操作
     */
    private void deleteFriend() {
        // 显示加载对话框
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("正在删除好友...");
        progressDialog.show();

        // 在后台线程执行删除操作
        new Thread(() -> {
            try {
                String currentUsername = SharedPreferencesManager.getInstance().getCurrentUsername();
                UserApi userApi = new UserApiImpl();
                boolean success = userApi.deleteFriend(currentUsername, friendUsername);

                // 在主线程更新UI
                runOnUiThread(() -> {
                    progressDialog.dismiss();
                    if (success) {
                        Toast.makeText(this, "好友删除成功", Toast.LENGTH_SHORT).show();
                        // 刷新好友列表
                        FriendListFragment.refreshFriendList();
                        setResult(RESULT_OK); // 设置返回结果，通知好友列表刷新
                        finish(); // 关闭聊天界面
                    } else {
                        Toast.makeText(this, "好友删除失败", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    progressDialog.dismiss();
                    Toast.makeText(this,
                        "删除失败: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    /**
     * 检查指定用户名是否为当前聊天对象
     * @param username 要检查的用户名
     * @return 如果是当前聊天对象返回true，否则返回false
     */
    public boolean isCurrentChat(String username) {
        return friendUsername != null && friendUsername.equals(username);
    }

    /**
     * 获取当前聊天好友的用户名
     * @return 好友用户名
     */
    public String getFriendUsername() {
        return friendUsername;  // 假设您已经有这个字段存储了当前聊天的好友用户名
    }

    /**
     * 处理WebSocket消息
     * @param message WebSocket接收到的消息
     */
    private void handleWebSocketMessage(String message) {
        try {
            // 忽略 "Invalid system type" 消息
            if (message.contains("Invalid system type")) {
                return;
            }

            // 处理其他消息...
            JSONObject jsonMessage = new JSONObject(message);
            String type = jsonMessage.optString("type");

            if ("error".equals(type)) {
                // 忽略错误消息
                return;
            }

            // 处理正常消息...

        } catch (Exception e) {
            // 忽略解析异常
            Log.e("ChatActivity", "Error handling WebSocket message", e);
        }
    }

    /**
     * 刷新消息列表
     */
    public void refreshMessageList() {
        if (!isFinishing()) {
            loadMessages();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (webSocketService != null) {
            try {
                webSocketService.reconnectIfNeeded();
            } catch (Exception e) {
                // 忽略重连异常
                Log.e("ChatActivity", "WebSocket reconnect failed", e);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 移除消息监听器
        if (messageHandler != null) {
            messageHandler.setOnMessageReceivedListener(null);
        }
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_OK); // 设置返回结果
        super.onBackPressed();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupInputButtons() {
        voiceButton = findViewById(R.id.inputImageView);
        emojiButton = findViewById(R.id.inputImageView2);

        // 语音按钮点击事件
        voiceButton.setOnClickListener(v -> {
            // 直接启动语音识别
            startVoiceRecognition();
        });

        // 表情按钮点击事件
        emojiButton.setOnClickListener(v -> {
            showEmojiDialog();
        });
    }

    private void startVoiceRecognition() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.CHINESE.toString());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "请说话...");

        try {
            startActivityForResult(intent, SPEECH_REQUEST_CODE);
        } catch (Exception e) {
            Toast.makeText(this, "您的设备不支持语音识别", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SPEECH_REQUEST_CODE && resultCode == RESULT_OK) {
            ArrayList<String> results = data.getStringArrayListExtra(
                    RecognizerIntent.EXTRA_RESULTS);
            if (results != null && !results.isEmpty()) {
                String spokenText = results.get(0);
                // 将识别的文字填入输入框
                inputMessage.setText(spokenText);
                // 可以选择自动发送
                sendMessage();
            }
        }
    }

    private void sendMessage() {
        String content = inputMessage.getText().toString().trim();
        if (!content.isEmpty()) {
            String currentUsername = SharedPreferencesManager.getInstance().getCurrentUsername();

            // 创建消息对象
            ChatMessage message = new ChatMessage(
                currentUsername,
                friendUsername,
                content,
                System.currentTimeMillis()
            );

            // 创建新线程执行网络请求
            new Thread(() -> {
                try {
                    // 使用 MessageApiImpl 保存消息到数据库
                    MessageApi messageApi = new MessageApiImpl();
                    JSONObject jsonMessage = new JSONObject();
                    jsonMessage.put("sender", message.getSender());
                    jsonMessage.put("receiver", message.getReceiver());
                    jsonMessage.put("content", message.getContent());

                    boolean success = messageApi.sendMessage(jsonMessage.toString());

                    if (success) {
                        // 使用 WebSocket 实时发送消息
                        messageHandler.sendChatMessage(friendUsername, content);

                        // 在主线程更新UI
                        runOnUiThread(() -> {
                            // 清空输入框
                            inputMessage.setText("");

                            // 将消息添加到本地消息列表并更新UI
                            messageList.add(message);
                            messageAdapter.notifyItemInserted(messageList.size() - 1);
                            scrollToBottom();

                            // 更新本地缓存
                            updateLocalCache(message);
                        });
                    } else {
                        runOnUiThread(() -> {
                            Toast.makeText(ChatActivity.this,
                                "发送消息失败",
                                Toast.LENGTH_SHORT).show();
                        });
                    }
                } catch (Exception e) {
                    runOnUiThread(() -> {
                        String errorMsg = e.getMessage();
                        if (errorMsg != null && !errorMsg.contains("Invalid system type") 
                            && !errorMsg.contains("Expected BEGIN_OBJECT")) {
                            Toast.makeText(ChatActivity.this,
                                "发送失败: " + errorMsg,
                                Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }).start();
        }
    }

    private void showEmojiDialog() {
        // 创建表情选择对话框
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_emoji, null);
        RecyclerView emojiRecyclerView = dialogView.findViewById(R.id.emojiRecyclerView);

        // 设置表情网格
        GridLayoutManager layoutManager = new GridLayoutManager(this, 7);
        emojiRecyclerView.setLayoutManager(layoutManager);

        // 创建表情列表
        List<String> emojis = getEmojiList();
        EmojiAdapter adapter = new EmojiAdapter(emojis);

        // 设置表情点击事件
        adapter.setOnEmojiClickListener(emoji -> {
            // 在输入框中插入表情
            int cursorPosition = inputMessage.getSelectionStart();
            inputMessage.getText().insert(cursorPosition, emoji);
        });

        emojiRecyclerView.setAdapter(adapter);

        // 显示对话框
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private List<String> getEmojiList() {
        List<String> emojis = new ArrayList<>();
        // 添加一些常用表情
        emojis.add("😊");
        emojis.add("😂");
        emojis.add("🤣");
        emojis.add("😍");
        emojis.add("😘");
        emojis.add("🥰");
        emojis.add("😋");
        emojis.add("🤗");
        emojis.add("🤔");
        emojis.add("😮");
        emojis.add("😴");
        emojis.add("😭");
        emojis.add("😡");
        emojis.add("👍");
        emojis.add("👎");
        emojis.add("👏");
        emojis.add("🙏");
        emojis.add("🎉");
        emojis.add("❤️");
        emojis.add("💔");
        // 可以继续添加更多表情
        return emojis;
    }
} 