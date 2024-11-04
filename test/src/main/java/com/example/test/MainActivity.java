package com.example.test;

import android.os.Bundle;
import android.widget.ListView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private ListView friendListView;
    private ArrayList<Friend> friends;
    private ItemAdapter itemAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        List<String> dataList = new ArrayList<>();

        for (int i = 1; i <= 100; i++) {
            dataList.add("Item11882 " + i);
        }

        MyAdapter adapter = new MyAdapter(dataList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this)); // 设置布局管理器
        recyclerView.setAdapter(adapter); // 设置适配器

//        // 初始化好友列表
//        friends = new ArrayList<>();
//        friends.add(new Friend(R.drawable.p1, "张三", "你好，今天过得怎么样？", "昨天 22:00"));
//        friends.add(new Friend(R.drawable.p3, "李四", "准备好明天的会议了吗？", "今天 09:00"));
//        friends.add(new Friend(R.drawable.p7, "王五", "下班一起吃饭吧！", "今天 10:00"));
//        friends.add(new Friend(R.drawable.p8, "赵六", "今天天气不错！", "今天 11:00"));
//        friends.add(new Friend(R.drawable.p11, "钱七", "明天一起出去玩吧！", "今天 12:00"));
//
//        // 创建适配器并设置给 ListView
//        itemAdapter = new ItemAdapter(this, friends);
//        friendListView.setAdapter(itemAdapter);
    }
}