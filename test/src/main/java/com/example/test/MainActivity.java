package com.example.test;

import android.os.Bundle;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

public class MainActivity extends AppCompatActivity {

    private ImageButton btn1, btn2, btn3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        btn1 = findViewById(R.id.btn1);
        btn2 = findViewById(R.id.btn2);
        btn3 = findViewById(R.id.btn3);

        // btn1点击事件
        btn1.setOnClickListener(v -> {
            // 处理 btn1 的点击事件
        });

        // btn2点击事件，显示 RecyclerViewFragment
        btn2.setOnClickListener(v -> {
            // 切换到 RecyclerViewFragment
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, new RecyclerViewFragment());
            transaction.addToBackStack(null); // 如果需要返回栈
            transaction.commit();
        });

        // btn3点击事件
        btn3.setOnClickListener(v -> {
            // 处理 btn3 的点击事件
        });
    }
}
