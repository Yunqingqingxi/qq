package com.example.qq.activity;

// Android 框架

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.example.qq.R;
import com.example.qq.utils.SharedPreferencesManager;

/**
 * 启动页活动类
 * 负责应用启动时的初始化工作，包括：
 * - 显示启动页面
 * - 初始化SharedPreferencesManager
 * - 检查登录状态
 * - 根据登录状态跳转到相应界面
 * 
 * @author yunxi
 * @version 1.0
 */
@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {
    private static final String TAG = "SplashActivity";
    private static final long SPLASH_DELAY = 1000; // 启动页显示时间，单位毫秒

    /**
     * 初始化活动
     * 设置布局、初始化SharedPreferencesManager、延迟跳转
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // 初始化SharedPreferencesManager
        SharedPreferencesManager.init(this);

        // 延迟跳转
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

    /**
     * 检查用户是否已登录
     * 通过检查登录状态、token和用户信息来判断
     * @return 如果用户已登录返回true，否则返回false
     */
    private boolean isUserLoggedIn() {
        // 检查登录状态和token是否存在
        SharedPreferencesManager prefsManager = SharedPreferencesManager.getInstance();
        return prefsManager.isLoggedIn() 
               && prefsManager.getToken() != null 
               && prefsManager.getUserInfo() != null;
    }
}