package com.example.qq.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.example.qq.R;
import com.example.qq.utils.SharedPreferencesManager;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {

    private static final long SPLASH_DELAY = 1000; // 启动页显示时间，单位毫秒

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // 初始化SharedPreferencesManager
        SharedPreferencesManager.init(this);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            // 检查是否已登录
            if (isUserLoggedIn()) {
                // 已登录，直接进入主界面
                startActivity(new Intent(SplashActivity.this, MainActivity.class));
            } else {
                // 未登录，进入登录界面
                startActivity(new Intent(SplashActivity.this, LoginActivity.class));
            }
            finish();
        }, SPLASH_DELAY);
    }

    private boolean isUserLoggedIn() {
        // 检查登录状态和token是否存在
        SharedPreferencesManager prefsManager = SharedPreferencesManager.getInstance();
        return prefsManager.isLoggedIn() 
               && prefsManager.getToken() != null 
               && prefsManager.getUserInfo() != null;
    }
}