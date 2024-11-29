package com.example.qq.activity;

import static com.example.qq.utils.JsonParser.parseToJson;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.qq.R;
import com.example.qq.adapter.FriendRequestAdapter;
import com.example.qq.api.userapi.UserApi;
import com.example.qq.api.userapi.impl.UserApiImpl;
import com.example.qq.domain.FriendRequest;
import com.example.qq.handler.MessageHandler;
import com.example.qq.handler.impl.MessageHandlerImpl;
import com.example.qq.utils.SharedPreferencesManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NewFriendActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private View emptyView;
    private List<FriendRequest> friendRequests = new ArrayList<>();
    private FriendRequestAdapter adapter;
    private MessageHandler messageHandler;
    private static final UserApi userApi = new UserApiImpl();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_friend);

        messageHandler = new MessageHandlerImpl(this);
        initViews();
        setupListeners();
        loadFriendRequests();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.friend_request_list);
        emptyView = findViewById(R.id.empty_view);

        // 设置RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new FriendRequestAdapter(this,friendRequests);
        adapter.setOnRequestActionListener(new FriendRequestAdapter.OnRequestActionListener() {
            @Override
            public void onAccept(FriendRequest request) {
                // 创建后台线程执行网络请求
                ExecutorService executor = Executors.newSingleThreadExecutor();
                Handler handler = new Handler(Looper.getMainLooper());

                executor.execute(() -> {
                    try {
                        // 处理接受好友请求
                        messageHandler.acceptFriendRequest(request.getUsername());
                        Map<String, Object> map = new HashMap<>();
                        map.put("requester", SharedPreferencesManager.getInstance().getCurrentUsername());
                        map.put("target", request.getUsername());

                        String jsonRequest = Objects.requireNonNull(parseToJson(map)).toString();
                        if (userApi.acceptFriendRequest(jsonRequest)) {
                            // 在主线程中更新UI
                            handler.post(() -> {
                                request.setStatus(1);
                                // 更新本地存储
                                SharedPreferencesManager.getInstance().updateFriendRequest(request);
                                // 添加到好友列表
                                SharedPreferencesManager.getInstance().addFriend(request.getUsername());
                                // 更新单个项而不是整个列表
                                adapter.notifyItemChanged(friendRequests.indexOf(request));
                                Toast.makeText(NewFriendActivity.this, "已接受好友请求", Toast.LENGTH_SHORT).show();
                            });
                        } else {
                            handler.post(() -> {
                                Toast.makeText(NewFriendActivity.this, "处理请求失败", Toast.LENGTH_SHORT).show();
                            });
                        }
                    } catch (Exception e) {
                        handler.post(() -> {
                            Toast.makeText(NewFriendActivity.this, "网络请求失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
                    }
                });
            }

            @Override
            public void onReject(FriendRequest request) {
                ExecutorService executor = Executors.newSingleThreadExecutor();
                Handler handler = new Handler(Looper.getMainLooper());

                executor.execute(() -> {
                    try {
                        // 处理拒绝好友请求
                        messageHandler.rejectFriendRequest(request.getUsername());
                        
                        // 在主线程中更新UI
                        handler.post(() -> {
                            request.setStatus(2);
                            // 更新本地存储
                            SharedPreferencesManager.getInstance().updateFriendRequest(request);
                            // 更新单个项而不是整个列表
                            adapter.notifyItemChanged(friendRequests.indexOf(request));
                            Toast.makeText(NewFriendActivity.this, "已拒绝好友请求", Toast.LENGTH_SHORT).show();
                        });
                    } catch (Exception e) {
                        handler.post(() -> {
                            Toast.makeText(NewFriendActivity.this, "处理请求失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
                    }
                });
            }
        });
        recyclerView.setAdapter(adapter);

        // 返回按钮
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
    }

    private void setupListeners() {
        // 可以添加搜索等其他功能的监听器
    }

    @SuppressLint("NotifyDataSetChanged")
    private void loadFriendRequests() {
        // 从SharedPreferences加载好友请求数据
        List<FriendRequest> requests = SharedPreferencesManager.getInstance().getFriendRequests();
        friendRequests.clear();
        friendRequests.addAll(requests);

        if (friendRequests.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 每次页面显示时刷新数据
        loadFriendRequests();
    }
} 