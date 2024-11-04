package com.example.as003;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;

public class MainActivity3 extends AppCompatActivity {
    private ArrayList<Message> messageList;
    private MessageAdapter messageAdapter;
    private EditText inputMessage;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);

        inputMessage = findViewById(R.id.inputMessage);
        Button sendButton = findViewById(R.id.sendButton);
        RecyclerView recyclerView = findViewById(R.id.recyclerView);

        messageList = new ArrayList<>();
        messageAdapter = new MessageAdapter(messageList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(messageAdapter);

        // 接收从MessageFragment传递过来的昵称
        if (getIntent().hasExtra("nickname")) {
            String nickname = getIntent().getStringExtra("nickname");
            updateNickname(nickname);
        }

        // 找到topBar并更新昵称
        View topBar = findViewById(R.id.topBar);
        TextView nicknameTextView = topBar.findViewById(R.id.nickname);

        // 设置返回按钮的点击事件
        ImageView backButton = topBar.findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 获取最后一条消息
                String lastMessage = messageList.isEmpty() ? "" : messageList.get(messageList.size() - 1).getContent();
                // 创建Intent启动MainActivity2，并传递最后一条消息
                Intent intent = new Intent(MainActivity3.this, MainActivity2.class);
                intent.putExtra("lastMessage", lastMessage);
                setResult(RESULT_OK, intent);
                finish();
            }
        });

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String messageContent = inputMessage.getText().toString().trim();
                if (!messageContent.isEmpty()) {
                    Message message = new Message(messageContent);
                    messageList.add(message);
                    messageAdapter.notifyItemInserted(messageList.size() - 1);
                    recyclerView.scrollToPosition(messageList.size() - 1); // 滚动到最后一条消息
                    inputMessage.setText(""); // 清空输入框
                }
            }
        });
    }

    private void updateNickname(String nickname) {
        View topBar = findViewById(R.id.topBar);
        TextView nicknameTextView = topBar.findViewById(R.id.nickname);
        nicknameTextView.setText(nickname);
    }
}