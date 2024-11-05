package com.example.qq.websocket.webUtils;

import android.content.Context;
import android.content.SharedPreferences;

public class GetNowUser {

    // 定义 SharedPreferences 名称和键
    private static final String PREFS_NAME = "MyRefs";
    private static final String KEY_USERNAME = "current_username";

    private SharedPreferences sharedPreferences;

    // 构造函数
    public GetNowUser(Context context) {
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    /**
     * 获取当前登录用户的用户名
     *
     * @return 当前用户名，如果没有则返回 null
     */
    public String getCurrentUsername() {
        return sharedPreferences.getString(KEY_USERNAME, null); // 返回用户名，如果没有则返回 null
    }

    /**
     * 设置当前登录用户的用户名
     *
     * @param username 用户名
     */
    public void setCurrentUsername(String username) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_USERNAME, username);
        editor.apply(); // 提交更改
    }
}
