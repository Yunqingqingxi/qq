package com.example.test;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

public class RecyclerViewFragment extends Fragment {

    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private List<String> dataList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.recyleview, container, false);

        // 初始化 RecyclerView
        recyclerView = rootView.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // 创建数据
        dataList = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            dataList.add("Item " + (i + 1));
        }

        // 设置Adapter
        adapter = new MyAdapter(dataList);
        recyclerView.setAdapter(adapter);

        return rootView;
    }
}
