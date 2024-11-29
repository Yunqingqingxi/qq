package com.example.qq.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.qq.R;
import com.example.qq.adapter.FriendAdapter;
import com.example.qq.domain.FriendList;
import com.example.qq.event.FriendRequestEvent;
import com.example.qq.utils.SharedPreferencesManager;
import com.example.qq.domain.WebSocketMessage;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class FriendListFragment extends Fragment {
    private RecyclerView recyclerView;
    private FriendAdapter friendAdapter;
    private List<FriendList> friendLists;
    private SwipeRefreshLayout swipeRefreshLayout;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private SharedPreferencesManager sharedPreferencesManager;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferencesManager = SharedPreferencesManager.getInstance();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onFriendRequestEvent(FriendRequestEvent event) {
        WebSocketMessage message = event.getMessage();
        // 处理好友请求事件
        if (message.getSystemType() == 3) { // 好友请求被接受
            // 立即刷新好友列表
            loadFriendData();
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
        loadFriendData();
    }

    private void initViews(View view) {
        recyclerView = view.findViewById(R.id.recyclerViewFriends);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
    }

    private void setupSwipeRefresh() {
        swipeRefreshLayout.setColorSchemeResources(R.color.purple_500);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            // 模拟刷新数据
            refreshFriendData();
        });
    }

    private void refreshFriendData() {
        // 模拟网络请求延迟
        handler.postDelayed(() -> {
            // TODO: 这里添加实际的刷新数据逻辑
            loadFriendData();
            // 停止刷新动画
            swipeRefreshLayout.setRefreshing(false);
            Toast.makeText(requireContext(), "刷新成功", Toast.LENGTH_SHORT).show();
        }, 1000); // 1秒后结束刷新
    }

    private void setupRecyclerView() {
        friendLists = new ArrayList<>();
        friendAdapter = new FriendAdapter(requireContext(), friendLists);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(friendAdapter);

        // 设置点击事件
        friendAdapter.setOnItemClickListener((friend, position) -> {
            Toast.makeText(requireContext(), "点击了: " + friend.getFriendNickName(), Toast.LENGTH_SHORT).show();
            // TODO: 处理好友点击事件，比如跳转到聊天界面
        });
    }

    private void loadFriendData() {
        // 从本地存储加载好友列表
        Set<String> friendUsernames = sharedPreferencesManager.getFriendList();
        List<FriendList> friendData = new ArrayList<>();

        for (String username : friendUsernames) {
            FriendList friend = new FriendList();
            friend.setFriendNickName(username);
            friend.setLastContext(sharedPreferencesManager.getLastMessage(username));
            friend.setLastContextTime(sharedPreferencesManager.getLastMessageTime(username));
            friend.setAvatarUrl(sharedPreferencesManager.getFriendAvatar(username));
            
            friendData.add(friend);
        }

        // 更新适配器数据
        if (friendAdapter != null) {
            friendAdapter.updateData(friendData);
            friendAdapter.notifyDataSetChanged();
        }
    }

    // 添加新好友到列表
    public void addNewFriend(String username) {
        FriendList newFriend = new FriendList();
        newFriend.setFriendNickName(username);
        // 设置初始值
        newFriend.setLastContext("");
        newFriend.setLastContextTime("");
        
        // 更新UI
        friendLists.add(newFriend);
        friendAdapter.notifyItemInserted(friendLists.size() - 1);
    }

    @Override
    public void onResume() {
        super.onResume();
        // 每次Fragment可见时刷新好友列表
        loadFriendData();
    }
} 