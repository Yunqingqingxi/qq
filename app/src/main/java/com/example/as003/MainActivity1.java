package com.example.as003;


import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.ListView;

import com.example.as003.R;
import com.example.as003.ChatMessage;
import com.example.as003.ChatArrayAdapter;
import java.util.ArrayList;
import java.util.List;

public class MainActivity1 extends AppCompatActivity {
    private ListView listView;
    private List<ChatMessage> chatMessages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main1); // 确保导入正确的布局文件

        listView = findViewById(R.id.listView);

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
       // ChatArrayAdapter adapter = new ChatArrayAdapter(this, chatMessages.toArray(new ChatMessage[0]));
        //listView.setAdapter(adapter);
    }
}