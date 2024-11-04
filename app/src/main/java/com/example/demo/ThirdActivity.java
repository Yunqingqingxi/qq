package com.example.demo;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class ThirdActivity extends AppCompatActivity {

    TextView textViewName, textViewNameValue, textViewAge, textViewAgeValue;
    Button buttonReturn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_third); // 确保这里的布局文件名与您的XML文件名相匹配

        // 初始化组件
        textViewName = findViewById(R.id.textViewName);
        textViewNameValue = findViewById(R.id.textViewNameValue);
        textViewAge = findViewById(R.id.textViewAge);
        textViewAgeValue = findViewById(R.id.textViewAgeValue);
        buttonReturn = findViewById(R.id.buttonReturn);

        // 从Intent获取数据
        String name = getIntent().getStringExtra("name");
        String age = getIntent().getStringExtra("age");

        // 将获取到的数据设置到TextView中
        textViewNameValue.setText(name);
        textViewAgeValue.setText(age);

        // 设置返回按钮的点击事件
        buttonReturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // 这将关闭当前Activity，返回到上一个Activity
            }
        });
    }
}