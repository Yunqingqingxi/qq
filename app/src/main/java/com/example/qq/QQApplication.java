package com.example.qq;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.appcompat.app.AppCompatDelegate;

import com.bumptech.glide.Glide;
import com.bumptech.glide.MemoryCategory;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.qq.utils.SharedPreferencesManager;

/**
 * 应用程序入口类
 * 负责应用程序级别的初始化工作
 */
public class QQApplication extends Application {

    /** 单例实例 */
    private static QQApplication instance;

    /**
     * 获取Application实例
     * @return Application实例
     */
    public static QQApplication getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        
        // 应用保存的主题设置
        SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);
        boolean isDarkMode = prefs.getBoolean("dark_mode", false);
        AppCompatDelegate.setDefaultNightMode(
            isDarkMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
        );
        
        initApplication();
    }

    /**
     * 初始化应用程序
     * 在后台线程中执行初始化操作，避免阻塞主线程
     */
    private void initApplication() {
        // 在后台线程中执行初始化
        new Thread(() -> {
            // 初始化SharedPreferences管理器
            SharedPreferencesManager.init(this);

            // 在主线程中执行UI相关的初始化
            new Handler(Looper.getMainLooper()).post(() -> {
                // 配置Glide内存策略
                Glide.get(this).setMemoryCategory(MemoryCategory.HIGH);
                // 预加载资源
                preloadResources();
            });
        }).start();
    }

    /**
     * 预加载资源
     * 只预加载本地存在的图片资源，提高应用响应速度
     */
    @SuppressLint("DiscouragedApi")
    private void preloadResources() {
        try {
            // 使用 try-catch 分别处理每个资源的预加载
            try {
                // 预加载头像占位图
                Glide.with(this)
                    .load(R.drawable.p29)
                    .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                    .preload();
            } catch (Exception e) {
                Log.e("QQApplication", "Failed to preload p29: " + e.getMessage());
            }

            try {
                // 预加载其他常用资源
                Glide.with(this)
                    .load(R.drawable.p38)
                    .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                    .preload();
            } catch (Exception e) {
                Log.e("QQApplication", "Failed to preload p38: " + e.getMessage());
            }
        } catch (Exception e) {
            Log.e("QQApplication", "Failed to preload resources: " + e.getMessage());
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        // 清理Glide内存缓存
        Glide.get(this).clearMemory();
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        // 根据内存级别清理资源
        Glide.get(this).trimMemory(level);
    }
}