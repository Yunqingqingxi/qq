package com.example.qq.fargments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.qq.R;
import com.example.qq.adapter.FriendAdapter;
import com.example.qq.pojo.Friend;

import java.util.List;

public class FriendsRecyclerViewFragment extends Fragment {

    private RecyclerView friendRecyclerView; // 用于显示好友列表的 RecyclerView
    private FriendAdapter friendAdapter; // 适配器，用于绑定数据
    private List<Friend> friends; // 好友数据列表

    // 构造函数，传入好友数据列表
    public FriendsRecyclerViewFragment(List<Friend> friends) {
        this.friends = friends;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 可以在这里进行一些初始化操作
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // 填充该Fragment的布局
        View view = inflater.inflate(R.layout.friends_list, container, false);

        // 初始化 RecyclerView 并设置布局管理器
        friendRecyclerView = view.findViewById(R.id.friendRecyclerView);
        friendRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));  // 使用线性布局管理器

        // 初始化并设置适配器
        friendAdapter = new FriendAdapter(getContext(), friends);
        friendRecyclerView.setAdapter(friendAdapter); // 将适配器设置给 RecyclerView

        return view; // 返回布局视图
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateFriends(List<Friend> friends) {
        if (friendAdapter != null) {
            // 更新适配器中的数据
            // 通知适配器数据已更改
            friendAdapter.notifyDataSetChanged();
        } else {
            // 如果适配器为空，创建一个新的适配器并设置
            friendAdapter = new FriendAdapter(getContext(), friends);
            friendRecyclerView.setAdapter(friendAdapter);
            friendAdapter.notifyDataSetChanged();
        }
    }

}