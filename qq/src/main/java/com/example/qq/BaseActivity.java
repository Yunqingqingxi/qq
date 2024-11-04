package com.example.qq;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import com.example.qq.websocket.webUtils.AddFriendUtil;

public class BaseActivity extends AppCompatActivity {
    protected void showAddFriendDialog(String friendNickname, AddFriendUtil.FriendAddListener listener) {
        AddFriendUtil.showAddFriendDialog(this, friendNickname, listener);
    }
}
