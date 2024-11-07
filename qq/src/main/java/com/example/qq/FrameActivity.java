package com.example.qq;

import static com.example.qq.util.JsonUtil.parseMessage;
import static com.example.qq.websocket.webUtils.controller.WebUtil.acceptFriend;
import static com.example.qq.websocket.webUtils.controller.WebUtil.getFriendList;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.qq.adapter.FriendAdapter;
import com.example.qq.websocket.db.FriendDatabaseHelper;
import com.example.qq.pojo.Friend;
import com.example.qq.websocket.domain.Message;
import com.example.qq.websocket.web.WebClient;
import com.example.qq.websocket.webResult.WebResult;
import com.example.qq.websocket.webUtils.AddFriendUtil;
import com.example.qq.websocket.webUtils.GetNowUser;
import com.example.qq.websocket.webUtils.controller.Callback;
import com.example.qq.websocket.webUtils.controller.MessageFilter;
import com.example.qq.websocket.webUtils.controller.WebUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.net.ssl.SSLContext;

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
    private RecyclerView friendRecyclerView;
    private String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment);

        getNowUser = new GetNowUser(FrameActivity.this);
        currentUsername = getNowUser.getCurrentUsername(); // 获取当前用户名

        initializeWebSocketClient();
        initializeUI();
        initializeDatabase();
        loadFriends();
        setupRecyclerView();
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
        imageViewPlus = findViewById(R.id.imageViewPlus);
        friendRecyclerView = findViewById(R.id.friendRecyclerView);
        friendRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        imageViewPlus.setOnClickListener(this::showPopupMenu);
    }

    private void initializeDatabase() {
        dbHelper = new FriendDatabaseHelper(this);
    }

    private void loadFriends() {
        currentUsername = getNowUser.getCurrentUsername();
        SharedPreferences sharedPreferences = getSharedPreferences("MyRefs", MODE_PRIVATE);
        token = sharedPreferences.getString("token", "");

//        // 从数据库加载好友列表
//        List<String> friendUsernames = dbHelper.getFriendsForUser(currentUsername);
//        for (String friendUsername : friendUsernames) {
//            Friend friend = new Friend();
//            friend.setUsername(friendUsername);
//            friend.setNickname(friendUsername);  // 假设昵称与用户名相同
//            friends.add(friend);
//        }

        // 从网络加载好友列表
        fetchFriendListFromServer();
    }

    private void fetchFriendListFromServer() {
        getFriendList(token, currentUsername, new Callback() {
            @Override
            public void onResult(WebResult<Map<String, Object>> result) throws JSONException {
                if (result.getCode() == 200) {
                    Map<String, Object> data = result.getData();
                    Object friendsObj = data.get("friends");

                    if (friendsObj instanceof JSONArray) {
                        JSONArray friendsArray = (JSONArray) friendsObj;
                        List<Map<String, Object>> friendDataList = new ArrayList<>();

                        // 将 JSONArray 转换为 List<Map<String, Object>> 以便后续处理
                        for (int i = 0; i < friendsArray.length(); i++) {
                            JSONObject friendJson = friendsArray.getJSONObject(i);
                            Map<String, Object> friendMap = new HashMap<>();
                            Iterator<String> keys = friendJson.keys();
                            while (keys.hasNext()) {
                                String key = keys.next();
                                friendMap.put(key, friendJson.get(key));
                            }
                            friendDataList.add(friendMap);
                        }

                        // 从网络数据更新好友列表
                        updateFriendListWithNetworkData(friendDataList);
                    }
                }
            }
        });
    }


    private void setupRecyclerView() {
        friendAdapter = new FriendAdapter(FrameActivity.this, friends);
        friendRecyclerView.setAdapter(friendAdapter);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(friendAdapter.getItemTouchHelperCallback());
        itemTouchHelper.attachToRecyclerView(friendRecyclerView);
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

            // 更新数据库，双向添加好友
//            dbHelper.addFriend(currentUsername, user);  // 将目标好友添加到发起者的好友列表中
//            dbHelper.addFriend(user, currentUsername);  // 将发起者添加到目标好友的好友列表中

            // 更新好友列表显示
            fetchFriendListFromServer();
        });
    }


    private void handleUnknownSystemValue(Map<String, Object> result) {
        String message = (String) result.get("message");
        runOnUiThread(() -> showToast("未知消息: " + message));
    }

    @SuppressLint("NotifyDataSetChanged")
    private void updateFriendListWithNetworkData(List<Map<String, Object>> friendDataList) {
        friends.clear();
        // 处理从网络请求中获取到的好友数据
        List<String> friendIds = new ArrayList<>();
        for (Map<String, Object> friend : friendDataList) {
            Object friendId = friend.get("friendId");
            if (friendId != null) {
                friendIds.add(String.valueOf(friendId));
            }
        }

        // 更新好友列表
        for (String friendId : friendIds) {
            Friend friend = new Friend();
            friend.setUsername(friendId);  // 假设 friendId 可以作为用户名
            friend.setNickname(friendId);  // 假设昵称与用户名相同
            friends.add(friend);
        }

        // 通知适配器更新数据
        friendAdapter.notifyDataSetChanged();
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

//                        // 如果有必要，也可以直接手动刷新数据
//                        friendAdapter.notifyDataSetChanged(); // 通知适配器更新
                        System.out.println("Succee");
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
            return false;
        });
        popup.show();
    }

    private void showToast(String message) {
        Toast.makeText(FrameActivity.this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (webSocket != null) {
            webSocket.close(1000, "Activity destroyed");
        }
    }
}
