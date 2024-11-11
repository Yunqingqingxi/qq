package com.example.qq;

import static com.example.qq.util.JsonUtil.parseMessage;
import static com.example.qq.util.TimeUtil.formatToHHMM;
import static com.example.qq.util.TimeUtil.parseTime;
import static com.example.qq.websocket.webUtils.controller.WebUtil.acceptFriend;
import static com.example.qq.websocket.webUtils.controller.WebUtil.getSpaFriendList;
import static com.example.qq.websocket.webUtils.controller.WebUtil.getUserInfo;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.text.InputType;
import android.util.Base64;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.qq.adapter.FriendAdapter;
import com.example.qq.fargments.ContactFragment;
import com.example.qq.fargments.DynamicFragment;
import com.example.qq.fargments.FriendsRecyclerViewFragment;
import com.example.qq.pojo.Friend;
import com.example.qq.pojo.User;
import com.example.qq.service.MyService;
import com.example.qq.websocket.db.FriendDatabaseHelper;
import com.example.qq.websocket.domain.Message;
import com.example.qq.websocket.web.WebClient;
import com.example.qq.websocket.webResult.WebResult;
import com.example.qq.websocket.webUtils.AddFriendUtil;
import com.example.qq.websocket.webUtils.GetNowUser;
import com.example.qq.websocket.webUtils.controller.Callback;
import com.example.qq.websocket.webUtils.controller.MessageFilter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class FrameActivity extends BaseActivity {
    private FriendAdapter friendAdapter;
    private List<Friend> friends = new ArrayList<>();
    private FriendDatabaseHelper dbHelper;
    private WebClient webClient;
    private WebSocket webSocket;
    private GetNowUser getNowUser;
    private WebSocketListener webSocketListener;
    private String currentUsername;
    private MessageFilter messageFilter;
    private ImageView imageViewPlus;
    private String token;
    private ImageButton btnMsg,btnFri,btnAut;
    private FriendsRecyclerViewFragment friendsFragment;
    private TextView textViewNickname;
    private ImageView imageViewAvatar;
    private DynamicFragment dynamicFragment;
    private ContactFragment contactFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment);

        SharedPreferences sharedPreferences = getSharedPreferences("MyRefs", MODE_PRIVATE);
        token = sharedPreferences.getString("token", "");

//         启动服务
//        Intent serviceIntent = new Intent(this, MyService.class);
//        serviceIntent.putExtra("token", token);
//        startService(serviceIntent);

        getNowUser = new GetNowUser(FrameActivity.this);
        currentUsername = getNowUser.getCurrentUsername(); // 获取当前用户名


        initializeWebSocketClient();
        initializeUI();
        initializeDatabase();

        initFragments();

        loadFriends();

        // 加载好友列表Fragment
        loadFriendListFragment();

        btnMsg = findViewById(R.id.btnMsg);
        btnFri = findViewById(R.id.btnFri);
        btnAut = findViewById(R.id.btnAut);

        btnMsg.setOnClickListener(v -> {
            loadFriends();
            // 切换到 FriendsRecyclerViewFragment
            loadFriendListFragment();
            // 更新 ImageButton 的图片资源
            btnMsg.setImageResource(R.drawable.p32);
            // 重置其他 ImageButton 的图片资源
            btnFri.setImageResource(R.drawable.p3);
            btnAut.setImageResource(R.drawable.p4);
        });

        btnFri.setOnClickListener(v -> {
            // 切换到 ContactFragment
            loadContactFragment();
            // 更新 ImageButton 的图片资源
            btnFri.setImageResource(R.drawable.p5);
            // 重置其他 ImageButton 的图片资源
            btnMsg.setImageResource(R.drawable.p2);
            btnAut.setImageResource(R.drawable.p4);
        });

        btnAut.setOnClickListener(v -> {
            // 切换到 DynamicFragment
            loadDynamicFragment();
            // 更新 ImageButton 的图片资源
            btnAut.setImageResource(R.drawable.p6);
            // 重置其他 ImageButton 的图片资源
            btnMsg.setImageResource(R.drawable.p2);
            btnFri.setImageResource(R.drawable.p3);
        });

    }

    private void initFragments() {
        friendsFragment = new FriendsRecyclerViewFragment(friends);
        dynamicFragment = new DynamicFragment();
        contactFragment = new ContactFragment();
    }


    private void loadContactFragment() {
        showFragment(contactFragment);
    }

    private void loadDynamicFragment() {
        showFragment(dynamicFragment);
    }

    private void showFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.commit();
    }

    private void initializeWebSocketClient() {
        webClient = WebClient.getInstance();
        messageFilter = message -> {
            Integer systemValue = (Integer) message.get("system");
            return systemValue == 2 || systemValue == 3; // 过滤系统消息
        };
        setupWebSocketListener();
        connectWebSocket();
    }

    private void setupWebSocketListener() {
        webSocketListener = new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                System.out.println("WebSocket connected.");
            }

            @Override
            public void onMessage(WebSocket webSocket, String text) {
                handleServerMessage(text);
            }

            @Override
            public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                t.printStackTrace();
            }

            @Override
            public void onClosing(@NonNull WebSocket webSocket, int code, String reason) {
                System.out.println("WebSocket closed: " + reason);
            }
        };
    }

    private void connectWebSocket() {
        SharedPreferences sharedPreferences = getSharedPreferences("MyRefs", MODE_PRIVATE);
        token = sharedPreferences.getString("token", "");
        webClient.connect(token, webSocketListener);
        webSocket = webClient.getWebSocket();
    }

    private void initializeUI() {
        imageViewAvatar = findViewById(R.id.imageAvatar);
        textViewNickname = findViewById(R.id.textNickname);
        getUserInfo(token, currentUsername, new Callback() {
            @Override
            public void onResult(WebResult<Map<String, Object>> result) throws JSONException {
                if (result.getCode() == 200) {
                    Map<String, Object> data = result.getData();
                    Map<String,String> userInfo = (Map<String,String>) data.get("user");

                    String username = userInfo.get("username");
                    String nicknametemp = userInfo.get("nickname");
                    String avatar = userInfo.get("avatar");

                    User user = new User(username, nicknametemp, avatar);
                    getNowUser.rememberUser(user);
                    String nickname = userInfo.get("nickname");
                    if (nickname == null) {
                        nickname = currentUsername;
                    }
                    textViewNickname.setText(nickname);

                    // 获取 Base64 编码的头像
                    String avatarBase64 = userInfo.get("avatar");
                    if (avatarBase64 != null && !avatarBase64.isEmpty()) {
                        // 解码 Base64 字符串为字节数组
                        byte[] avatarBytes = Base64.decode(avatarBase64, Base64.DEFAULT);
                        if (avatarBytes != null) {
                            // 将字节数组转换为 Bitmap
                            Bitmap avatarBitmap = BitmapFactory.decodeByteArray(avatarBytes, 0, avatarBytes.length);
                            if (avatarBitmap != null) {
                                imageViewAvatar.setImageBitmap(avatarBitmap);
                            } else {
                                // 如果转换失败，使用默认头像
                                imageViewAvatar.setImageResource(R.drawable.p14);
                            }
                        }
                    } else {
                        // 如果没有头像，使用默认头像
                        imageViewAvatar.setImageResource(R.drawable.p14);
                    }
                }
            }
        });
        imageViewPlus = findViewById(R.id.imageViewPlus);
        imageViewPlus.setOnClickListener(this::showPopupMenu);
    }

    private void initializeDatabase() {
        dbHelper = new FriendDatabaseHelper(this);
    }

    private void loadFriendListFragment() {
        // 检查 Fragment 是否已经加载
        friendsFragment = (FriendsRecyclerViewFragment) getSupportFragmentManager()
                .findFragmentByTag(FriendsRecyclerViewFragment.class.getSimpleName());

        if (friendsFragment == null) {
            // 如果没有加载过，创建一个新的 Fragment
            friendsFragment = new FriendsRecyclerViewFragment(friends);
            // 开始 Fragment 事务
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            // 替换或添加 Fragment 到 FrameLayout 中
            transaction.replace(R.id.fragment_container, friendsFragment, FriendsRecyclerViewFragment.class.getSimpleName());
            // 提交事务
            transaction.commit();
        } else {
            // 如果已经加载过，直接更新数据
            friendsFragment.updateFriends(friends);
        }
    }


    private void loadFriends() {
        // 异步加载好友列表
        fetchFriendListFromServer();
    }

    private void fetchFriendListFromServer() {
        getSpaFriendList(token, currentUsername, new Callback() {
            @SuppressLint("SimpleDateFormat")
            @Override
            public void onResult(WebResult<Map<String, Object>> result) throws JSONException {
                if (result.getCode() == 200) {
                    Map<String, Object> data = result.getData();
                    Object friendsObj = data.get("friends");

                    if (friendsObj instanceof JSONArray) {
                        JSONArray friendsArray = (JSONArray) friendsObj;
                        List<Friend> friendDataList = new ArrayList<>();

                        // 解析 JSON 数据并创建 Friend 对象
                        for (int i = 0; i < friendsArray.length(); i++) {
                            JSONObject friendJson = friendsArray.getJSONObject(i);
                            Friend friend = new Friend();

                            // 设置好友的各项属性
                            friend.setUsername(friendJson.optString("username"));
                            String nickname = friendJson.optString("nickname");
                            if (nickname.equals("null")) {
                                nickname = friendJson.optString("username");  // 如果昵称为空，使用用户名
                            }
                            friend.setNickname(nickname);
                            String content = friendJson.optString("content");
                            if (content.equals("null")) {
                                content = "";  // 如果内容为空，使用默认签名
                            }
                            friend.setContent(content);
                            Date time = formatToHHMM(parseTime(friendJson.optString("timestamp")));
                            friend.setTime(time); // yyyy-MM-dd HH:mm:ss
                            String avatarBase = friendJson.optString("avatar");
                            friend.setAvatar(avatarBase);
                            // 将 Friend 对象添加到列表
                            friendDataList.add(friend);
                        }

                        // 更新好友列表
                        updateFriendListWithNetworkData(friendDataList);
                    }
                }
            }
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    private void handleServerMessage(String text) {
        try {
            Map<String, Object> result = parseMessage(text);
            if (messageFilter.shouldProcessMessage(result)) {
                Integer systemValue = (Integer) result.get("system");
                switch (systemValue) {
                    case 2: handleFriendRequest(result); break; // 好友请求
                    case 3: handleFriendAdded(result); break; // 好友已添加
                    default: handleUnknownSystemValue(result); break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleFriendRequest(Map<String, Object> result) {
        String user = (String) result.get("user");
        runOnUiThread(() -> {
            if (!currentUsername.equals(user)) {
                AddFriendUtil.showAddFriendDialog(FrameActivity.this, user, new AddFriendUtil.FriendAddListener() {
                    @Override
                    public void onAdd(String username, String wanSay) {
                        acceptFriendRequest(username);
                    }

                    @Override
                    public void onReject(String reason) {
                        showToast("好友请求已拒绝: " + reason);
                    }
                });
            }
        });
    }

    private void handleFriendAdded(Map<String, Object> result) {
        String user = (String) result.get("user");  // 目标好友的用户名
        String message = (String) result.get("message");
        runOnUiThread(() -> {
            showToast(message);  // 显示添加成功的消息

            // 更新好友列表显示
            loadFriends();
//            fetchFriendListFromServer();
        });
    }


    private void handleUnknownSystemValue(Map<String, Object> result) {
        String message = (String) result.get("message");
        runOnUiThread(() -> showToast("未知消息: " + message));
    }

    @SuppressLint("NotifyDataSetChanged")
    private void updateFriendListWithNetworkData(List<Friend> friendDataList) {
        friends.clear();  // 清空旧的数据
        friends.addAll(friendDataList);  // 添加新获取的好友数据

        // 更新适配器
        if (friendAdapter == null) {
            initFriendAdapter(friends);  // 如果适配器为空，初始化适配器
        } else {
            friendAdapter.notifyDataSetChanged();  // 如果适配器已经存在，刷新数据
        }

        // 确保在 UI 线程上更新 UI
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (friendsFragment != null) {
                    friendsFragment.updateFriends(friends);  // 更新好友列表显示
                } else {
                    System.out.println("friendsFragment is not null");
                }
            }
        });
    }


    // 更新
    private void initFriendAdapter(List<Friend> friends) {
        // 初始化FriendAdapter
        friendAdapter = new FriendAdapter(this, friends);
    }

    private void acceptFriendRequest(String username) {
        String token = getSharedPreferences("MyRefs", MODE_PRIVATE).getString("token", "");
        notifySender(currentUsername, username, token);
        acceptFriend(currentUsername, username, token, new Callback() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onResult(WebResult<Map<String, Object>> result) {
                if (result.getCode() == 200) {
                    runOnUiThread(() -> {
                        showToast("好友请求已接受");
                        // 刷新好友列表
                        fetchFriendListFromServer();  // 重新加载好友列表

                    });
                }
            }
        });
    }

    private void notifySender(String senderUsername, String receiverUsername, String token) {
        Message message = new Message(3, senderUsername, receiverUsername, "已接受好友申请");
        webSocket.send(message.toJson().toString());
    }

    private void showAddFriendDialog() {
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        new AlertDialog.Builder(this)
                .setTitle("添加好友")
                .setMessage("请输入好友的用户 ID:")
                .setView(input)
                .setPositiveButton("添加", (dialog, which) -> {
                    String friendId = input.getText().toString().trim();
                    if (!friendId.isEmpty()) {
                        sendAddFriendMessage(friendId);
                    } else {
                        showToast("用户 ID 不能为空");
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void sendAddFriendMessage(String friendId) {
        if (webSocket != null) {
            String currentUsername = new GetNowUser(FrameActivity.this).getCurrentUsername();
            Message addFriendMessage = new Message(2, currentUsername, friendId, "请求添加你为好友");
            webSocket.send(addFriendMessage.toJson().toString());
            showToast("已向 " + friendId + " 发出好友申请");
        }
    }

    private void showPopupMenu(View v) {
        PopupMenu popup = new PopupMenu(this, v);
        popup.setGravity(Gravity.END);
        popup.getMenuInflater().inflate(R.menu.popup_menu, popup.getMenu());
        popup.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.menu_add_friend) {
                showAddFriendDialog();
                return true;
            }
            if(item.getItemId() == R.id.menu_other_option){
                // 实现注销,清除当前存储的用户名和token，断开websocket
                SharedPreferences.Editor editor = getSharedPreferences("MyRefs", MODE_PRIVATE).edit();
                editor.remove("current_username");
                editor.remove("token");
                editor.apply();
                // 断开WebSocket连接
                if (webSocket != null) {
                    webSocket.close(1000, "User logged out");
                }
                // 回到登录界面
                Intent intent = new Intent(FrameActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
            return false;
        });
        popup.show();
    }

    private void showToast(String message) {
        Toast.makeText(FrameActivity.this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences sharedPreferences = getSharedPreferences("MyRefs", MODE_PRIVATE);
        boolean fromChatActivity = sharedPreferences.getBoolean("fromChatActivity", false);
        if (fromChatActivity) {
            loadFriends(); // 刷新好友列表
            sharedPreferences.edit().remove("fromChatActivity").apply(); // 清除标志
        }}

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (webSocket != null) {
            webSocket.close(1000, "Activity destroyed");
        }
    }
}