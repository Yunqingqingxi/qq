package com.example.qq.activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;

import com.bumptech.glide.Glide;
import com.example.qq.R;
import com.example.qq.utils.SharedPreferencesManager;

import java.io.File;

public class SettingsActivity extends AppCompatActivity {
    private static final String PREF_DARK_MODE = "dark_mode";
    private TextView textCacheSize;
    private SwitchCompat switchDarkMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // 设置状态栏
        updateStatusBar();

        // 初始化状态栏高度
        View statusBarBackground = findViewById(R.id.statusBarBackground);
        statusBarBackground.getLayoutParams().height = getStatusBarHeight();

        initViews();
        updateCacheSize();
    }

    private void updateStatusBar() {
        if (isDarkModeEnabled()) {
            getWindow().setStatusBarColor(Color.TRANSPARENT);
            getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            );
        } else {
            getWindow().setStatusBarColor(Color.TRANSPARENT);
            getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            );
        }
    }

    private void initViews() {
        // 返回按钮
        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        // 深色模式开关
        switchDarkMode = findViewById(R.id.switchDarkMode);
        switchDarkMode.setChecked(isDarkModeEnabled());
        switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            setDarkMode(isChecked);
            saveDarkModePreference(isChecked);
        });

        // 清除缓存
        textCacheSize = findViewById(R.id.textCacheSize);
        View layoutClearCache = findViewById(R.id.layoutClearCache);
        layoutClearCache.setOnClickListener(v -> showClearCacheDialog());
    }

    private boolean isDarkModeEnabled() {
        SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);
        return prefs.getBoolean(PREF_DARK_MODE, false);
    }

    private void saveDarkModePreference(boolean enabled) {
        SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);
        prefs.edit().putBoolean(PREF_DARK_MODE, enabled).apply();
    }

    private void setDarkMode(boolean enabled) {
        // 应用深色模式
        AppCompatDelegate.setDefaultNightMode(
            enabled ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
        );
        
        // 更新所有Activity
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    private void showClearCacheDialog() {
        new AlertDialog.Builder(this)
            .setTitle("清除缓存")
            .setMessage("确定要清除所有缓存数据吗？")
            .setPositiveButton("确定", (dialog, which) -> {
                clearCache();
            })
            .setNegativeButton("取消", null)
            .show();
    }

    private void clearCache() {
        // 清除图片缓存
        Glide.get(this).clearMemory();
        new Thread(() -> {
            Glide.get(this).clearDiskCache();
        }).start();

        // 清除SharedPreferences缓存
        SharedPreferencesManager manager = SharedPreferencesManager.getInstance();
        manager.clearFriendListCache();
        manager.clearAllUsersFriendListCache();

        // 清除应用缓存目录
        try {
            File cacheDir = getCacheDir();
            File externalCacheDir = getExternalCacheDir();
            deleteDir(cacheDir);
            if (externalCacheDir != null) {
                deleteDir(externalCacheDir);
            }
            Toast.makeText(this, "缓存清除成功", Toast.LENGTH_SHORT).show();
            updateCacheSize();
        } catch (Exception e) {
            Toast.makeText(this, "清除缓存失败", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            if (children != null) {
                for (String child : children) {
                    boolean success = deleteDir(new File(dir, child));
                    if (!success) {
                        return false;
                    }
                }
            }
        }
        return dir != null && dir.delete();
    }

    private void updateCacheSize() {
        long size = 0;
        try {
            size += getDirSize(getCacheDir());
            File externalCacheDir = getExternalCacheDir();
            if (externalCacheDir != null) {
                size += getDirSize(externalCacheDir);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        textCacheSize.setText(formatSize(size));
    }

    private long getDirSize(File dir) {
        long size = 0;
        if (dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File file : files) {
                    size += getDirSize(file);
                }
            }
        } else {
            size = dir.length();
        }
        return size;
    }

    private String formatSize(long size) {
        if (size < 1024) {
            return size + "B";
        } else if (size < 1024 * 1024) {
            return String.format("%.1fKB", size / 1024.0);
        } else if (size < 1024 * 1024 * 1024) {
            return String.format("%.1fMB", size / (1024.0 * 1024));
        } else {
            return String.format("%.1fGB", size / (1024.0 * 1024 * 1024));
        }
    }

    private int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }
} 