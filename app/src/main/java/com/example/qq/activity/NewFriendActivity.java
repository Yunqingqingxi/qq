package com.example.qq.activity;

// Android 框架

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
import com.example.qq.domain.User;
import com.example.qq.handler.MessageHandler;
import com.example.qq.handler.impl.MessageHandlerImpl;
import com.example.qq.service.NotificationService;
import com.example.qq.utils.SharedPreferencesManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 新朋友界面活动类
 * 负责处理好友请求相关功能，包括：
 * - 显示好友请求列表
 * - 处理好友请求的接受和拒绝
 * - 更新好友请求状态
 * - 管理空视图的显示
 * 
 * @author yunxi
 * @version 1.0
 */
public class NewFriendActivity extends AppCompatActivity {
    private static final String TAG = "NewFriendActivity";

    private RecyclerView recyclerView;
    private View emptyView;
    private List<FriendRequest> friendRequests = new ArrayList<>();
    private FriendRequestAdapter adapter;
    private MessageHandler messageHandler;
    private NotificationService notificationService;
    private static final UserApi userApi = new UserApiImpl();

    /**
     * 初始化活动
     * 设置布局、初始化服务和视图组件
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_friend);

        // 初始化 NotificationService
        notificationService = NotificationService.getInstance();
        
        // 创建 MessageHandlerImpl 实例
        messageHandler = new MessageHandlerImpl(this, NotificationService.getInstance());
        initViews();
        setupListeners();
        loadFriendRequests();
    }

    /**
     * 初始化视图组件
     * 设置RecyclerView和其适配器，配置好友请求的处理逻辑
     */
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
                        // 获取对方的用户信息
                        User friendInfo = userApi.getUserInfo(request.getUsername());
                        if (friendInfo == null) {
                            handler.post(() -> Toast.makeText(NewFriendActivity.this, 
                                "获取用户信息失败", Toast.LENGTH_SHORT).show());
                            return;
                        }

                        // 处理接受好友请求
                        messageHandler.acceptFriendRequest(request.getUsername());
                        Map<String, Object> map = new HashMap<>();
                        map.put("requester", SharedPreferencesManager.getInstance().getCurrentUsername());
                        map.put("target", request.getUsername());

                        String jsonRequest = Objects.requireNonNull(parseToJson(map)).toString();
                        if (userApi.acceptFriendRequest(jsonRequest)) {
                            handler.post(() -> {
                                request.setStatus(1);
                                request.setNickname(friendInfo.getUserNickName());
                                request.setAvatarUrl(friendInfo.getUserAvatarUrl());
                                
                                // 更新本地存储
                                SharedPreferencesManager.getInstance().updateFriendRequest(request);
                                
                                // 更新列表项
                                adapter.updateRequestStatus(request);
                                Toast.makeText(NewFriendActivity.this, "已接受好友请求", Toast.LENGTH_SHORT).show();
                            });
                        } else {
                            handler.post(() -> Toast.makeText(NewFriendActivity.this, 
                                "处理请求失败", Toast.LENGTH_SHORT).show());
                        }
                    } catch (Exception e) {
                        handler.post(() -> Toast.makeText(NewFriendActivity.this, 
                            "网络请求失败: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    }
                });
            }

            @Override
            public void onReject(FriendRequest request) {
                ExecutorService executor = Executors.newSingleThreadExecutor();
                Handler handler = new Handler(Looper.getMainLooper());

                executor.execute(() -> {
                    try {
                        messageHandler.rejectFriendRequest(request.getUsername());
                        handler.post(() -> {
                            request.setStatus(2);
                            SharedPreferencesManager.getInstance().updateFriendRequest(request);
                            adapter.updateRequestStatus(request);
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

    /**
     * 设置事件监听器
     * 可以添加搜索等其他功能的监听器
     */
    private void setupListeners() {
        // 可以添加搜索等其他功能的监听器
    }

    /**
     * 加载好友请求列表
     * 从本地存储加载好友请求数据并更新界面
     */
    @SuppressLint("NotifyDataSetChanged")
    public void loadFriendRequests() {
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

    /**
     * 活动恢复时刷新数据
     * 每次页面显示时重新加载好友请求列表
     */
    @Override
    protected void onResume() {
        super.onResume();
        loadFriendRequests();
    }
} 