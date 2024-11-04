package com.example.demo;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class FourthActivity extends AppCompatActivity {

    private EditText editText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fourth); // 确保这里的布局文件名与您的XML文件名相匹配

        // 初始化组件
        editText = findViewById(R.id.editText);
        Button buttonReturn = findViewById(R.id.button5);
        TextView textViewMessage = findViewById(R.id.textView2);

        // 设置返回按钮的点击事件
        buttonReturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 获取用户输入的内容
                String message = editText.getText().toString();

                // 创建Intent以返回结果
                Intent resultIntent = new Intent();
                resultIntent.putExtra("content", message);

                // 设置结果码并结束活动
                setResult(RESULT_OK, resultIntent);
                finish();
            }
        });
    }
}