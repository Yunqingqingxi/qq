package com.example.qq.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import com.example.qq.R;
import com.example.qq.activity.NewFriendActivity;
import com.example.qq.domain.FriendRequest;
import com.example.qq.event.FriendRequestEvent;
import com.example.qq.utils.SharedPreferencesManager;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

public class FriendsFragment extends Fragment {
    private TextView unreadCountView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_friends, container, false);
        
        // 初始化未读数视图
        unreadCountView = view.findViewById(R.id.unread_count);
        
        // 找到新朋友布局并设置点击事件
        View newFriendLayout = (View) view.findViewById(R.id.textview_new_friend).getParent();
        newFriendLayout.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), NewFriendActivity.class);
            startActivity(intent);
        });

        // 注册EventBus
        EventBus.getDefault().register(this);
        
        // 初始化时更新未读数
        updateUnreadCount();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateUnreadCount();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onFriendRequestEvent(FriendRequestEvent event) {
        updateUnreadCount();
    }

    private void updateUnreadCount() {
        List<FriendRequest> requests = SharedPreferencesManager.getInstance().getFriendRequests();
        int unreadCount = 0;
        for (FriendRequest request : requests) {
            if (request.getStatus() == 0) { // 未处理的请求
                unreadCount++;
            }
        }
        
        if (unreadCount > 0) {
            unreadCountView.setVisibility(View.VISIBLE);
            if (unreadCount > 99) {
                unreadCountView.setText("99+");
            } else {
                unreadCountView.setText(String.valueOf(unreadCount));
            }
        } else {
            unreadCountView.setVisibility(View.GONE);
        }
    }
} 
