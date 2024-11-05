package com.example.qq.fragment;

package com.example.qq;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.qq.adapter.FriendAdapter;
import com.example.qq.pojo.Friend;

import java.util.ArrayList;
import java.util.List;

public class FriendFragment extends Fragment {

    private FriendAdapter friendAdapter;
    private List<Friend> friends = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_friend, container, false);

        // 初始化RecyclerView
        RecyclerView friendRecyclerView = view.findViewById(R.id.friendRecyclerView);
        friendRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // 假设数据
        for (int i = 0; i < 10; i++) {
            Friend friend = new Friend();
            friend.setUsername("User" + i);
            friend.setNickname("Nickname" + i);
            friends.add(friend);
        }

        friendAdapter = new FriendAdapter(getContext(), friends);
        friendRecyclerView.setAdapter(friendAdapter);

        return view;
    }
}