package com.example.qq.utils;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.content.SharedPreferences;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import android.util.Log;

import com.example.qq.api.userapi.impl.UserApiImpl;
import com.example.qq.domain.FriendRequest;
import com.example.qq.domain.User;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 * SharedPreferences管理类
 * 用于管理应用程序的本地存储数据，包括用户信息、登录状态、token等
 */
public class SharedPreferencesManager {
    /** SharedPreferences文件名 */
    private static final String PREF_NAME = "QQPrefs";
    /** 存储token的key */
    private static final String KEY_TOKEN = "token";
    /** 存储登录状态的key */
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    /** 存储用户信息的key */
    private static final String KEY_USER_INFO = "user_info";
    /** 存储保存的用户名的key */
    private static final String KEY_SAVED_USERNAME = "saved_username";
    /** 存储网络状态的key */
    private static final String KEY_NETWORK_STATUS = "network_status";
    /** 存储好友列表的key */
    private static final String KEY_FRIENDS = "friends";
    /** 存储好友请求的key */
    private static final String KEY_FRIEND_REQUESTS = "friend_requests_%s";
    /** 存储好友列表的key */
    private static final String FRIEND_LIST_KEY = "friend_list";
    /** 存储最后一条消息的key前缀 */
    private static final String LAST_MESSAGE_PREFIX = "last_message_";
    /** 存储最后一条消息时间的key前缀 */
    private static final String LAST_MESSAGE_TIME_PREFIX = "last_message_time_";
    /** 存储好友头像的key前缀 */
    private static final String FRIEND_AVATAR_PREFIX = "friend_avatar_";
    
    /** 单例实例 */
    private static SharedPreferencesManager instance;
    /** SharedPreferences实例 */
    private final SharedPreferences preferences;
    /** Gson实例，用于JSON序列化 */
    private final Gson gson;
    /** UserApi实例，用于获取用户信息 */
    private final UserApiImpl userApi;
    
    /**
     * 私有构造函数
     * @param context 应用程序上下文
     */
    private SharedPreferencesManager(Context context) {
        preferences = context.getApplicationContext()
            .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
        userApi = new UserApiImpl();
    }
    
    /**
     * 初始化SharedPreferencesManager
     * @param context 应用程序上下文
     */
    public static void init(Context context) {
        if (instance == null) {
            synchronized (SharedPreferencesManager.class) {
                if (instance == null) {
                    instance = new SharedPreferencesManager(context);
                }
            }
        }
    }
    
    /**
     * 获取SharedPreferencesManager实例
     * @return SharedPreferencesManager实例
     * @throws IllegalStateException 如果实例未初始化
     */
    public static SharedPreferencesManager getInstance() {
        if (instance == null) {
            throw new IllegalStateException("SharedPreferencesManager must be initialized first");
        }
        return instance;
    }
    
    /**
     * 保存登录状态
     * @param isLoggedIn 是否已登录
     */
    public void saveLoginState(boolean isLoggedIn) {
        preferences.edit().putBoolean(KEY_IS_LOGGED_IN, isLoggedIn).apply();
    }
    
    /**
     * 检查是否已登录
     * @return 是否已登录
     */
    public boolean isLoggedIn() {
        return preferences.getBoolean(KEY_IS_LOGGED_IN, false);
    }
    
    /**
     * 保存用户信息
     * @param user 用户对象
     */
    public void saveUserInfo(User user) {
        if (user == null) {
            Log.e(TAG, "Attempting to save null user info");
            return;
        }
        String userInfoJson = gson.toJson(user);
        preferences.edit()
            .putString(KEY_USER_INFO, userInfoJson)
            .apply();
        // 保存登录状态
        saveLoginState(true);
        // 保存用户名
        if (user.getUserName() != null) {
            saveUsername(user.getUserName());
        }
    }
    
    /**
     * 获取用户信息
     * @return 用户对象，如果不存在则返回null
     */
    public User getUserInfo() {
        String userInfoJson = preferences.getString(KEY_USER_INFO, null);
        if (userInfoJson != null) {
            return gson.fromJson(userInfoJson, User.class);
        }
        return null;
    }
    
    /**
     * 获取当前用户名
     * @return 当前用户名，如果不存在则返回null
     */
    public String getCurrentUsername() {
        User userInfo = getUserInfo();
        if (userInfo != null) {
            return userInfo.getUserName();
        }
        return null;
    }
    
    /**
     * 获取并保存用户信息
     * @param username 用户名
     */
    public void fetchAndSaveUserInfo(String username) {
        try {
            User user = userApi.getUserInfo(username);
            if (user != null) {
                saveUserInfo(user);
            } else {
                Log.e(TAG, "Failed to fetch user info for: " + username);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error fetching user info", e);
        }
    }
    
    /**
     * 保存用户名
     * @param username 用户名
     */
    public void saveUsername(String username) {
        preferences.edit().putString(KEY_SAVED_USERNAME, username).apply();
    }
    
    /**
     * 获取保存的用户名
     * @return 保存的用户名
     */
    public String getSavedUsername() {
        return preferences.getString(KEY_SAVED_USERNAME, "");
    }
    
    /**
     * 保存token
     * @param token 认证token
     */
    public void saveToken(String token) {
        preferences.edit().putString(KEY_TOKEN, token).apply();
    }
    
    /**
     * 获取token
     * @return token，如果不存在则返回null
     */
    public String getToken() {
        return preferences.getString(KEY_TOKEN, null);
    }
    
    /**
     * 清除token
     */
    public void clearToken() {
        preferences.edit().remove(KEY_TOKEN).apply();
    }
    
    /**
     * 清除所有数据
     */
    public void clearAll() {
        preferences.edit()
            .remove(KEY_TOKEN)
            .remove(KEY_IS_LOGGED_IN)
            .remove(KEY_USER_INFO)
            .apply();
    }

    /**
     * 清除用户信息
     * 注意：不会清除保存的用户名，以便下次登录时自动填充
     */
    public void clearUserInfo() {
        preferences.edit()
            .remove(KEY_TOKEN)
            .remove(KEY_IS_LOGGED_IN)
            .remove(KEY_USER_INFO)
            .apply();
    }
    
    /**
     * 保存网络状态
     * @param status 网络状态描述
     */
    public void saveNetworkStatus(String status) {
        preferences.edit().putString(KEY_NETWORK_STATUS, status).apply();
    }
    
    /**
     * 获取网络状态
     * @return 网络状态描述
     */
    public String getNetworkStatus() {
        return preferences.getString(KEY_NETWORK_STATUS, "离线");
    }

    /**
     * 添加好友
     * @param username 要添加的好友用户名
     */
    public void addFriend(String username) {
        Set<String> friends = getFriends();
        friends.add(username);
        preferences.edit()
            .putStringSet(KEY_FRIENDS, friends)
            .apply();
    }

    /**
     * 移除好友
     * @param username 要移除的好友用户名
     */
    public void removeFriend(String username) {
        Set<String> friends = getFriends();
        friends.remove(username);
        preferences.edit()
            .putStringSet(KEY_FRIENDS, friends)
            .apply();
    }

    /**
     * 获取好友列表
     * @return 好友用户名集合
     */
    public Set<String> getFriends() {
        return new HashSet<>(preferences.getStringSet(KEY_FRIENDS, new HashSet<>()));
    }

    /**
     * 检查是否为好友
     * @param username 要检查的用户名
     * @return 是否为好友
     */
    public boolean isFriend(String username) {
        return getFriends().contains(username);
    }

    /**
     * 清除好友列表
     */
    public void clearFriends() {
        preferences.edit()
            .remove(KEY_FRIENDS)
            .apply();
    }

    /**
     * 保存好友请求
     * @param request 好友请求对象
     */
    public void saveFriendRequest(FriendRequest request) {
        String currentUsername = getCurrentUsername();
        if (currentUsername == null || request == null) {
            return;
        }
        
        String key = String.format(KEY_FRIEND_REQUESTS, currentUsername);
        List<FriendRequest> requests = getFriendRequests();
        
        // 检查是否已存在相同的请求
        boolean exists = false;
        for (int i = 0; i < requests.size(); i++) {
            if (requests.get(i).getUserId().equals(request.getUserId())) {
                requests.set(i, request); // 更新现有请求
                exists = true;
                break;
            }
        }
        
        if (!exists) {
            requests.add(request);
        }
        
        String json = gson.toJson(requests);
        preferences.edit()
            .putString(key, json)
            .apply();
    }

    /**
     * 获取当前用户的好友请求列表
     * @return 好友请求列表
     */
    public List<FriendRequest> getFriendRequests() {
        String currentUsername = getCurrentUsername();
        if (currentUsername == null) {
            return new ArrayList<>();
        }
        
        String key = String.format(KEY_FRIEND_REQUESTS, currentUsername);
        String json = preferences.getString(key, "[]");
        Type type = new TypeToken<List<FriendRequest>>(){}.getType();
        return gson.fromJson(json, type);
    }

    /**
     * 更新好友请求
     * @param request 好友请求对象
     */
    public void updateFriendRequest(FriendRequest request) {
        String currentUsername = getCurrentUsername();
        if (currentUsername == null || request == null) {
            return;
        }
        
        String key = String.format(KEY_FRIEND_REQUESTS, currentUsername);
        List<FriendRequest> requests = getFriendRequests();
        
        boolean updated = false;
        for (int i = 0; i < requests.size(); i++) {
            if (requests.get(i).getUserId().equals(request.getUserId())) {
                requests.set(i, request);
                updated = true;
                break;
            }
        }
        
        if (updated) {
            String json = gson.toJson(requests);
            preferences.edit()
                .putString(key, json)
                .apply();
        }
    }

    /**
     * 清除当前用户的所有好友请求
     */
    public void clearFriendRequests() {
        String currentUsername = getCurrentUsername();
        if (currentUsername == null) {
            return;
        }
        
        String key = String.format(KEY_FRIEND_REQUESTS, currentUsername);
        preferences.edit()
            .remove(key)
            .apply();
    }

    /**
     * 删除指定的好友请求
     * @param userId 要删除的请求的用户ID
     */
    public void removeFriendRequest(String userId) {
        String currentUsername = getCurrentUsername();
        if (currentUsername == null || userId == null) {
            return;
        }
        
        String key = String.format(KEY_FRIEND_REQUESTS, currentUsername);
        List<FriendRequest> requests = getFriendRequests();
        
        requests.removeIf(request -> request.getUserId().equals(userId));
        
        String json = gson.toJson(requests);
        preferences.edit()
            .putString(key, json)
            .apply();
    }

    /**
     * 获取好友列表
     * @return 好友用户名集合
     */
    public Set<String> getFriendList() {
        return preferences.getStringSet(FRIEND_LIST_KEY, new HashSet<>());
    }

    /**
     * 从列表中移除好友
     * @param username ��移除的好友用户名
     */
    public void removeFriendFromList(String username) {
        Set<String> currentFriends = new HashSet<>(getFriendList());
        currentFriends.remove(username);
        preferences.edit()
                .putStringSet(FRIEND_LIST_KEY, currentFriends)
                .apply();
        
        // 同时清除该好友的相关信息
        preferences.edit()
                .remove(LAST_MESSAGE_PREFIX + username)
                .remove(LAST_MESSAGE_TIME_PREFIX + username)
                .remove(FRIEND_AVATAR_PREFIX + username)
                .apply();
    }

    /**
     * 获取最后一条消息
     * @param username 要获取消息的好友用户名
     * @return 最后一条消息
     */
    public String getLastMessage(String username) {
        return preferences.getString(LAST_MESSAGE_PREFIX + username, "");
    }

    /**
     * 设置最后一条消息
     * @param username 要设置消息的好友用户名
     * @param message 要设置的消息
     */
    public void setLastMessage(String username, String message) {
        preferences.edit()
                .putString(LAST_MESSAGE_PREFIX + username, message)
                .apply();
    }

    /**
     * 获取最后一条消息的时间
     * @param username 要获取时间的好友用户名
     * @return 最后一条消息的时间
     */
    public String getLastMessageTime(String username) {
        return preferences.getString(LAST_MESSAGE_TIME_PREFIX + username, "");
    }

    /**
     * 设置最后一条消息的时间
     * @param username 要设置时间的好友用户名
     * @param time 要设置的时间
     */
    public void setLastMessageTime(String username, String time) {
        preferences.edit()
                .putString(LAST_MESSAGE_TIME_PREFIX + username, time)
                .apply();
    }

    /**
     * 获取好友头像
     * @param username 要获取头像的好友用户名
     * @return 好友头像URL
     */
    public String getFriendAvatar(String username) {
        return preferences.getString(FRIEND_AVATAR_PREFIX + username, "");
    }

    /**
     * 设置好友头像
     * @param username 要设置头像的好友用户名
     * @param avatarUrl 要设置的头像URL
     */
    public void setFriendAvatar(String username, String avatarUrl) {
        preferences.edit()
                .putString(FRIEND_AVATAR_PREFIX + username, avatarUrl)
                .apply();
    }

    /**
     * 清除所有好友相关数据
     */
    public void clearAllFriendData() {
        Set<String> friends = getFriendList();
        SharedPreferences.Editor editor = preferences.edit();
        
        // 清除所有好友的相关数据
        for (String username : friends) {
            editor.remove(LAST_MESSAGE_PREFIX + username)
                  .remove(LAST_MESSAGE_TIME_PREFIX + username)
                  .remove(FRIEND_AVATAR_PREFIX + username);
        }
        
        // 清除好友列表
        editor.remove(FRIEND_LIST_KEY)
              .apply();
    }

    /**
     * 检查是否已经存在未处理的好友请求
     * @param username 要检查的用户名
     * @return true 如果存在未处理的请求，false 否则
     */
    public boolean hasPendingFriendRequest(String username) {
        List<FriendRequest> requests = getFriendRequests();
        for (FriendRequest request : requests) {
            if (request.getUsername().equals(username) && request.getStatus() == 0) {
                return true;
            }
        }
        return false;
    }
} 