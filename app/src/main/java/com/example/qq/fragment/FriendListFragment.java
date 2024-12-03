package com.example.qq.fragment;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Canvas;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.qq.R;
import com.example.qq.activity.ChatActivity;
import com.example.qq.adapter.FriendAdapter;
import com.example.qq.api.friendlistapi.FriendApi;
import com.example.qq.api.friendlistapi.impl.FriendApiImpl;
import com.example.qq.domain.FriendList;
import com.example.qq.domain.WebSocketMessage;
import com.example.qq.event.FriendDeletedEvent;
import com.example.qq.event.FriendListUpdateEvent;
import com.example.qq.event.FriendRequestEvent;
import com.example.qq.utils.SharedPreferencesManager;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FriendListFragment extends Fragment {
    // 静态变量
    private static FriendListFragment instance;
    private static boolean isRefreshing = false;
    private static final long REFRESH_COOLDOWN = 1000; // 1秒冷却时间
    private static long lastRefreshTime = 0;  // 改为静态变量

    // 实例变量
    private RecyclerView recyclerView;
    private FriendAdapter friendAdapter;
    private List<FriendList> friendLists;
    private SwipeRefreshLayout swipeRefreshLayout;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private SharedPreferencesManager sharedPreferencesManager;
    private FriendApi friendApi;
    private static final long REFRESH_TIMEOUT = 2000;
    private final Handler timeoutHandler = new Handler(Looper.getMainLooper());
    private Runnable timeoutRunnable;
    private boolean isFirstLoad = true;
    private static final long MIN_REFRESH_INTERVAL = 1000;
    private static final long CACHE_VALID_TIME = 30L * 24 * 60 * 60 * 1000;
    private String currentUserId;
    private static final int CHAT_REQUEST_CODE = 1001;

    // 声明 ActivityResultLauncher
    private ActivityResultLauncher<Intent> chatActivityLauncher;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        instance = this;  // 保存实例引用
        sharedPreferencesManager = SharedPreferencesManager.getInstance();
        friendApi = new FriendApiImpl();
        setHasOptionsMenu(true);  // 启用选项菜单

        // 初始化 ActivityResultLauncher
        chatActivityLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                // 聊天界面返回时的处理（如果需要）
                if (result.getResultCode() == Activity.RESULT_OK) {
                    // 刷新列表
                    loadFriendData();
                }
            }
        );
    }

    @Override
    public void onStart() {
        super.onStart();
        // 添加检查，避免重复注册
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        // 添加检查，确保注销时已经注册
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (timeoutRunnable != null) {
            timeoutHandler.removeCallbacks(timeoutRunnable);
        }
        if (instance == this) {
            instance = null;  // 清除实例引用
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onFriendRequestEvent(FriendRequestEvent event) {
        WebSocketMessage message = event.getMessage();
        Log.d(TAG, "收到好友请求事件: type=" + message.getSystemType());

        // 处理好友请求事件
        if (message.getSystemType() == 3) { // 好友请求被接受
            Log.d(TAG, "好友请求被接受，刷新列表");
            // 清除缓存并刷新数据
            sharedPreferencesManager.clearFriendListCache();
            loadFriendData();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onFriendDeleted(FriendDeletedEvent event) {
        // 直接从适配器中移除该好友
        if (friendAdapter != null) {
            friendAdapter.removeFriend(event.getFriendUsername());
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_message, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
        setupRecyclerView();
        setupSwipeRefresh();

        // 先尝试加载缓存数据
        List<FriendList> cachedList = sharedPreferencesManager.getCachedFriendList();
        if (cachedList != null && !cachedList.isEmpty()) {
            // 如果有缓存数据，直接显示
            friendAdapter.updateData(cachedList);
        } else {
            // 如果没有缓存数据，才去加载网络数据
            loadFriendData();
        }
    }

    private void initViews(View view) {
        recyclerView = view.findViewById(R.id.recyclerViewFriends);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
    }

    private void setupSwipeRefresh() {
        swipeRefreshLayout.setColorSchemeResources(R.color.purple_500);
        // 设置刷新监听
        swipeRefreshLayout.setOnRefreshListener(() -> {
            Log.d("FriendListFragment", "触发下拉刷新");

            // 强制结束上一次可能未完成的刷新状态
            swipeRefreshLayout.setRefreshing(false);

            // 检查刷新间隔
            if (!canRefresh()) {
                showToast("刷新太频繁，请稍后再试");
                return;
            }

            // 开始新的刷新
            swipeRefreshLayout.setRefreshing(true);
            loadFriendData();
        });
    }

    private void refreshFriendData() {
        // 如果正在刷新，直接返回
        if (swipeRefreshLayout.isRefreshing()) {
            return;
        }

        // 检查刷新间隔
        if (!canRefresh()) {
            showToast("刷新太频繁，请稍后再试");
            return;
        }

        // 开始刷新
        loadFriendData();
    }

    private boolean canRefresh() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastRefreshTime > MIN_REFRESH_INTERVAL) {
            lastRefreshTime = currentTime;
            return true;
        }
        return false;
    }

    private void setupRecyclerView() {
        friendLists = new ArrayList<>();
        friendAdapter = new FriendAdapter(requireContext(), friendLists);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(friendAdapter);

        // 添加滑动处理
        ItemTouchHelper.SimpleCallback swipeCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            private float initialX;
            private float initialY;
            private boolean isSwipeStarted = false;

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView,
                                @NonNull RecyclerView.ViewHolder viewHolder,
                                @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSelectedChanged(@Nullable RecyclerView.ViewHolder viewHolder, int actionState) {
                super.onSelectedChanged(viewHolder, actionState);
                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                    if (viewHolder != null) {
                        View itemView = viewHolder.itemView;
                        initialX = itemView.getX();
                        initialY = itemView.getY();
                        isSwipeStarted = true;
                    }
                }
            }

            @Override
            public void clearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                super.clearView(recyclerView, viewHolder);
                if (isSwipeStarted) {
                    View itemView = viewHolder.itemView;
                    // 如果滑动距离超过阈值，清除未读消息
                    float swipeDistance = Math.abs(itemView.getX() - initialX);
                    if (swipeDistance > itemView.getWidth() * 0.3f) {
                        int position = viewHolder.getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            FriendList friend = friendLists.get(position);
                            SharedPreferencesManager.getInstance()
                                .clearUnreadCount(friend.getFriendUsername());
                            friendAdapter.notifyItemChanged(position);
                        }
                    }
                    // 恢复原位
                    itemView.animate()
                        .x(initialX)
                        .y(initialY)
                        .setDuration(200)
                        .start();
                    isSwipeStarted = false;
                }
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                // 不实际删除项目，只处理滑动事件
                int position = viewHolder.getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    friendAdapter.notifyItemChanged(position);
                }
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView,
                                  @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY,
                                  int actionState, boolean isCurrentlyActive) {
                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                    View itemView = viewHolder.itemView;
                    // 限制最大滑动距离
                    float maxSwipe = itemView.getWidth() * 0.3f;
                    float swipeX = Math.min(Math.abs(dX), maxSwipe) * Math.signum(dX);

                    // 应用滑动
                    itemView.setTranslationX(swipeX);

                    // 根据滑动距离改变透明度
                    float alpha = 1.0f - Math.abs(swipeX) / maxSwipe;
                    itemView.setAlpha(alpha);
                }
            }
        };

        new ItemTouchHelper(swipeCallback).attachToRecyclerView(recyclerView);

        // 设置点击事件
        friendAdapter.setOnItemClickListener((friend, position) -> {
            Intent intent = new Intent(getActivity(), ChatActivity.class);
            intent.putExtra("friend_username", friend.getFriendUsername());
            intent.putExtra("friend_nickname", friend.getFriendNickName());
            intent.putExtra("friend_avatar", friend.getAvatarUrl());
            chatActivityLauncher.launch(intent);
        });
    }

    private void loadCachedData() {
        List<FriendList> cachedList = sharedPreferencesManager.getCachedFriendList();
        if (cachedList != null && !cachedList.isEmpty()) {
            friendAdapter.updateData(cachedList);
            friendAdapter.notifyDataSetChanged();
        }
    }

    private boolean shouldRefreshData() {
        // 如果需要强制刷新，接返回true
        if (needForceRefresh()) {
            return true;
        }

        long lastUpdateTime = sharedPreferencesManager.getFriendListUpdateTime();
        long currentTime = System.currentTimeMillis();
        return currentTime - lastUpdateTime > CACHE_VALID_TIME ||
               sharedPreferencesManager.getCachedFriendList() == null;
    }

    @SuppressLint("NotifyDataSetChanged")
    private void loadFriendData() {
        Log.d(TAG, "开始加载好友数据...");

        // 显示刷新动画
        if (swipeRefreshLayout != null) {
            requireActivity().runOnUiThread(() ->
                swipeRefreshLayout.setRefreshing(true));
        }

        new Thread(() -> {
            try {
                // 获取新数据
                List<FriendList> newFriendData = friendApi.getFriendList();
                Log.d(TAG, "获取到新数据: " + (newFriendData != null ? newFriendData.size() : 0) + "个好友");

                if (!isAdded()) return;

                // 更新最后一条消息和时间
                if (newFriendData != null) {
                    SharedPreferencesManager prefs = SharedPreferencesManager.getInstance();
                    for (FriendList friend : newFriendData) {
                        String username = friend.getFriendUsername();
                        String serverMessage = friend.getLastContext();
                        String serverTime = friend.getLastContextTime();

                        String localMessage = prefs.getLastMessage(username);
                        String localTime = prefs.getLastMessageTime(username);

                        if (serverMessage != null && !serverMessage.isEmpty()) {
                            prefs.setLastMessage(username, serverMessage);
                            prefs.setLastMessageTime(username, serverTime);
                            friend.setLastContext(serverMessage);
                            friend.setLastContextTime(serverTime);
                        } else if (localMessage != null && !localMessage.isEmpty()) {
                            friend.setLastContext(localMessage);
                            friend.setLastContextTime(localTime);
                        }
                    }

                    // 保存到缓存
                    prefs.cacheFriendList(newFriendData);
                }

                // 在主线程更新UI
                requireActivity().runOnUiThread(() -> {
                    try {
                        if (swipeRefreshLayout != null) {
                            swipeRefreshLayout.setRefreshing(false);
                        }

                        if (friendAdapter != null) {
                            if (newFriendData != null) {
                                friendAdapter.updateData(newFriendData);
                                friendAdapter.notifyDataSetChanged();
                                Log.d(TAG, "好友列表更新成功，数量: " + newFriendData.size());
                            } else {
                                friendAdapter.updateData(new ArrayList<>());
                                friendAdapter.notifyDataSetChanged();
                                Log.d(TAG, "清空好友列表");
                                if (friendAdapter.getItemCount() == 0) {
                                    showToast("暂无好友");
                                }
                            }
                        } else {
                            Log.e(TAG, "friendAdapter为空");
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "UI更新失败: " + e.getMessage(), e);
                        showToast("更新列表失败");
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "加载好友数据失败: " + e.getMessage(), e);
                if (!isAdded()) return;

                requireActivity().runOnUiThread(() -> {
                    if (swipeRefreshLayout != null) {
                        swipeRefreshLayout.setRefreshing(false);
                    }
                    showToast("加载失败: " + e.getMessage());
                });
            }
        }).start();
    }

    // 比较两个好友列表是否相同
    private boolean compareFriendLists(List<FriendList> list1, List<FriendList> list2) {
        if (list1 == null || list2 == null) {
            Log.d("FriendListFragment", "其中一个列表为空: list1=" + (list1 != null) + ", list2=" + (list2 != null));
            return list1 == list2;
        }

        if (list1.size() != list2.size()) {
            Log.d("FriendListFragment", "列表大小不同: list1.size=" + list1.size() + ", list2.size=" + list2.size());
            return false;
        }

        // 先对列表进行排序（按用户名）
        list1.sort((a, b) -> a.getFriendUsername().compareTo(b.getFriendUsername()));
        list2.sort((a, b) -> a.getFriendUsername().compareTo(b.getFriendUsername()));

        // 比较每个好友的关键数据
        for (int i = 0; i < list1.size(); i++) {
            FriendList friend1 = list1.get(i);
            FriendList friend2 = list2.get(i);

            boolean usernameSame = friend1.getFriendUsername().equals(friend2.getFriendUsername());
            boolean nicknameSame = Objects.equals(friend1.getFriendNickName(), friend2.getFriendNickName());
            boolean avatarSame = Objects.equals(friend1.getAvatarUrl(), friend2.getAvatarUrl());
            boolean contentSame = Objects.equals(friend1.getLastContext(), friend2.getLastContext());

            if (!usernameSame || !nicknameSame || !avatarSame || !contentSame) {
                Log.d("FriendListFragment", String.format(
                    "好友数据不同: username=%b, nickname=%b, avatar=%b, content=%b, friend=%s",
                    usernameSame, nicknameSame, avatarSame, contentSame, friend1.getFriendUsername()
                ));
                return false;
            }
        }

        Log.d("FriendListFragment", "两个列表完全相同");
        return true;
    }

    private void showToast(String message) {
        if (isAdded() && getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }

    // 修改addNewFriend方法
    public void addNewFriend(String username) {
        // 添加好友后清除缓存并强制刷新
        sharedPreferencesManager.clearFriendListCache();
        loadFriendData();
    }

    @Override
    public void onResume() {
        super.onResume();
        // 只在列表为空时才自动刷新
        if (friendAdapter.getItemCount() == 0) {
            loadFriendData();
        }
    }

    // 添加一个方法来判断是否需要强制刷新
    private boolean needForceRefresh() {
        // 检查是否有新消息或其他需要强制刷新的条件
        return false; // TODO: 实现具体的检查逻辑
    }

    private void openChatActivity(String friendUsername, String friendNickname, String friendAvatar) {
        Intent intent = new Intent(getActivity(), ChatActivity.class);
        intent.putExtra("friend_username", friendUsername);
        intent.putExtra("friend_nickname", friendNickname);
        intent.putExtra("friend_avatar", friendAvatar);
        chatActivityLauncher.launch(intent);  // 使用新的启动方式
    }

    public static void refreshFriendList() {
        // 使用Handler确保在主线程中执行
        new Handler(Looper.getMainLooper()).post(() -> {
            try {
                long currentTime = System.currentTimeMillis();
                if (isRefreshing || (currentTime - lastRefreshTime) < REFRESH_COOLDOWN) {
                    Log.d(TAG, "跳过刷新：正在刷新或冷却时间未到");
                    return;
                }

                if (instance != null && instance.isAdded()) {
                    isRefreshing = true;
                    Log.d(TAG, "开始刷新好友列表");

                    instance.sharedPreferencesManager.clearFriendListCache();
                    instance.loadFriendData();

                    lastRefreshTime = currentTime;
                    Log.d(TAG, "好友列表刷新请求已发送");
                } else {
                    Log.w(TAG, "无法刷新好友列表：Fragment实例不可用或未添加到Activity");
                }
            } catch (Exception e) {
                Log.e(TAG, "刷新好友列表失败: " + e.getMessage(), e);
            } finally {
                isRefreshing = false;
            }
        });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onFriendListUpdate(FriendListUpdateEvent event) {
        Log.d(TAG, "收到好友列表更新事件，立即刷新");
        // 强制刷新好友列表
        sharedPreferencesManager.clearFriendListCache();
        loadFriendData();
    }
} 