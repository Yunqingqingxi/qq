package com.example.qq.activity;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.qq.R;
import com.example.qq.api.userapi.UserApi;
import com.example.qq.api.userapi.impl.UserApiImpl;
import com.example.qq.domain.User;

public class FriendProfileActivity extends AppCompatActivity {
    private ImageView imageAvatar;
    private TextView textNickname;
    private TextView textUsername;
    private TextView textEmail;
    private UserApi userApi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_profile);

        // 设置状态栏
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        getWindow().getDecorView().setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
            View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        // 初始化状态栏高度
        View statusBarBackground = findViewById(R.id.statusBarBackground);
        statusBarBackground.getLayoutParams().height = getStatusBarHeight();

        // 初始化视图
        initViews();

        // 获取好友用户名
        String friendUsername = getIntent().getStringExtra("friend_username");
        if (friendUsername != null) {
            // 加载好友信息
            loadFriendInfo(friendUsername);
        }
    }

    private void initViews() {
        // 初始化视图组件
        imageAvatar = findViewById(R.id.imageAvatar);
        textNickname = findViewById(R.id.textNickname);
        textUsername = findViewById(R.id.textUsername);
        textEmail = findViewById(R.id.textEmail);

        // 设置返回按钮
        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        // 初始化UserApi
        userApi = new UserApiImpl();
    }

    private void loadFriendInfo(String username) {
        new Thread(() -> {
            try {
                // 获取好友信息
                User friend = userApi.getUserInfo(username);
                
                // 在主线程更新UI
                runOnUiThread(() -> {
                    if (friend != null) {
                        // 设置头像
                        if (friend.getUserAvatarUrl() != null) {
                            Glide.with(this)
                                .load(friend.getUserAvatarUrl())
                                .placeholder(R.drawable.default_avatar)
                                .error(R.drawable.default_avatar)
                                .circleCrop()
                                .into(imageAvatar);
                        }

                        // 设置昵称
                        textNickname.setText(friend.getUserNickName());
                        
                        // 设置用户名
                        textUsername.setText(friend.getUserName());
                        
                        // 设置邮箱
                        textEmail.setText(friend.getEmail());
                    }
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(this, "加载好友信息失败", Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private int getStatusBarHeight() {
        int result = 0;
        @SuppressLint("DiscouragedApi") int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }
} 