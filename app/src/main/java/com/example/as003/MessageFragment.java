package com.example.as003;

import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

public class MessageFragment extends Fragment {

    private ListView listView;
    private EditText searchBox;
    private List<ChatMessage> chatMessages;
    private ChatArrayAdapter adapter;

    public MessageFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_message, container, false);

        // 初始化组件
        listView = view.findViewById(R.id.listView);
        searchBox = view.findViewById(R.id.searchBox);

        // 初始化数据
        chatMessages = new ArrayList<>();
        chatMessages.add(new ChatMessage(R.drawable.p8, "余董威", "加入了群聊。", "08:05"));
        chatMessages.add(new ChatMessage(R.drawable.p9, "洪坤", "加入了群聊。", "09:50"));
        chatMessages.add(new ChatMessage(R.drawable.p10, "廖志博", "加入了群聊。", "10:33"));
        chatMessages.add(new ChatMessage(R.drawable.p11, "余英贝", "加入了群聊。", "14:06"));
        chatMessages.add(new ChatMessage(R.drawable.p12, "饶承一", "加入了群聊。", "15:17"));
        chatMessages.add(new ChatMessage(R.drawable.p13, "王英槐", "加入了群聊。", "16:02"));
        chatMessages.add(new ChatMessage(R.drawable.p14, "魏泽琛", "加入了群聊。", "16:55"));
        chatMessages.add(new ChatMessage(R.drawable.p15, "詹基军", "加入了群聊。", "17:24"));
        chatMessages.add(new ChatMessage(R.drawable.p16, "盧羊琴", "加入了群聊。", "17:25"));
        chatMessages.add(new ChatMessage(R.drawable.p17, "柳仕昊", "加入了群聊。", "17:43"));
        chatMessages.add(new ChatMessage(R.drawable.p18, "麥嘉兴", "加入了群聊。", "17:58"));
        chatMessages.add(new ChatMessage(R.drawable.p19, "雷柳园", "加入了群聊。", "18:59"));
        chatMessages.add(new ChatMessage(R.drawable.p20, "姚桢琼", "加入了群聊。", "18:20"));
        chatMessages.add(new ChatMessage(R.drawable.p21, "盧林峯", "加入了群聊。", "19:19"));
        chatMessages.add(new ChatMessage(R.drawable.p22, "黄语墨", "加入了群聊。", "21:20"));
        chatMessages.add(new ChatMessage(R.drawable.p23, "關宇晟", "加入了群聊。", "昨天01:40"));

        // 创建适配器并设置到ListView
        adapter = new ChatArrayAdapter(getContext(), chatMessages);
        listView.setAdapter(adapter);

        // 设置搜索框的过滤功能
        searchBox.addTextChangedListener(new SearchFilter());
        // 设置ListView的点击监听器
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ChatMessage clickedItem = (ChatMessage) parent.getItemAtPosition(position);
                String nickname = clickedItem.getNickname();
                Intent intent = new Intent(getActivity(), MainActivity3.class);
                intent.putExtra("nickname", nickname);
                startActivity(intent);
            }
        });
        return view;
    }

    private class SearchFilter implements android.text.TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            adapter.getFilter().filter(charSequence);
        }

        @Override
        public void afterTextChanged(android.text.Editable editable) {}
    }

    public void updateChatMessage(String message) {
        // 这里假设你的ChatMessage类有一个构造函数接收消息内容
        chatMessages.add(new ChatMessage(message)); // 添加新消息到列表
        adapter.notifyDataSetChanged(); // 通知适配器数据已更改
    }

}