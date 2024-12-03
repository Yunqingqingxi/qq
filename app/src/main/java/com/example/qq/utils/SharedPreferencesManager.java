package com.example.qq.utils;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.qq.api.userapi.impl.UserApiImpl;
import com.example.qq.domain.ChatMessage;
import com.example.qq.domain.Contact;
import com.example.qq.domain.FriendList;
import com.example.qq.domain.FriendRequest;
import com.example.qq.domain.User;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
    /** 存储保存的用户昵称的key */
    private static final String KEY_SAVED_USERNICKNAME = "saved_usernickname";
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
    
    private static final String CACHED_FRIEND_LIST = "cached_friend_list_%s";
    private static final String FRIEND_LIST_UPDATE_TIME = "friend_list_update_time_%s";
    
    private static final String CHAT_MESSAGES_PREFIX = "chat_messages_";
    
    private static final String FRIEND_NICKNAME_PREFIX = "friend_nickname_";
    
    private static final String UNREAD_COUNT_PREFIX = "unread_count_";
    
    /** 单例实例 */
    private static SharedPreferencesManager instance;
    /** SharedPreferences实例 */
    private final SharedPreferences preferences;
    /** SharedPreferences.Editor实例 */
    private final SharedPreferences.Editor editor;
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
        editor = preferences.edit();
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
        if(user.getUserNickName() != null){
            saveUserNickName(user.getUserNickName());
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
     * 保存用户昵称
     * @param username 用户名
     */
    public void saveUserNickName(String username) {
        preferences.edit().putString(KEY_SAVED_USERNICKNAME, username).apply();
    }

    /**
     * 获取用户昵称
     * @return 用户昵称，如果不存在则返回null
     */
    public String getUserNickName() {
        return preferences.getString(KEY_SAVED_USERNICKNAME, "");
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
     * 获取好友昵称
     * @param username 好友用户名
     * @return 好友昵称，如果未找到则返回用户名
     */
    public String getFriendNickname(String username) {
        if (username == null || username.isEmpty()) {
            return "";
        }
        // 先尝试从好友昵称缓存中获取
        String nickname = preferences.getString(FRIEND_NICKNAME_PREFIX + username, null);
        if (nickname != null && !nickname.isEmpty()) {
            return nickname;
        }
        
        // 如果没有昵称缓存，从好友列表缓存中查找
        List<FriendList> cachedFriends = getCachedFriendList();
        if (cachedFriends != null) {
            for (FriendList friend : cachedFriends) {
                if (username.equals(friend.getFriendUsername())) {
                    String friendNickname = friend.getFriendNickName();
                    if (friendNickname != null && !friendNickname.isEmpty()) {
                        // 找到后保存到昵称缓存
                        saveFriendNickname(username, friendNickname);
                        return friendNickname;
                    }
                    break;
                }
            }
        }
        
        // 如果都没找到，返回用户名
        return username;
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
     * 注销登录时，清除用户信息
     * 注意：不会清除保存的用户名，以便下次登录时自动填充
     */
    public void clearForLoginUserInfo() {
        preferences.edit()
            .remove(KEY_TOKEN)
            .remove(KEY_IS_LOGGED_IN)
            .remove(KEY_USER_INFO)
            .apply();
    }
    /**
     * 清除现有的用户信息
     */
    public void clearUserInfo() {
        preferences.edit().remove(KEY_USER_INFO).apply();

    }
    
    /**
     * 存网络状态
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
        if (request == null || request.getUsername() == null) {
            Log.e(TAG, "Invalid friend request");
            return;
        }

        List<FriendRequest> requests = getFriendRequests();
        boolean found = false;
        
        for (int i = 0; i < requests.size(); i++) {
            FriendRequest existingRequest = requests.get(i);
            if (existingRequest != null && 
                request.getUsername().equals(existingRequest.getUsername())) {
                requests.set(i, request);
                found = true;
                break;
            }
        }
        
        if (!found) {
            Log.w(TAG, "Friend request not found for update: " + request.getUsername());
            requests.add(request);
        }
        
        saveFriendRequests(requests);
    }

    public void saveFriendRequests(List<FriendRequest> requests) {
        String currentUsername = getCurrentUsername();
        if (currentUsername == null) {
            Log.e(TAG, "Cannot save friend requests: current username is null");
            return;
        }
        
        if (requests == null) {
            Log.e(TAG, "Cannot save null friend requests list");
            return;
        }
        
        String key = String.format(KEY_FRIEND_REQUESTS, currentUsername);
        String json = gson.toJson(requests);
        
        preferences.edit()
            .putString(key, json)
            .apply();
        
        Log.d(TAG, "Saved " + requests.size() + " friend requests for user: " + currentUsername);
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
     * @return 好友用户名合
     */
    public Set<String> getFriendList() {
        return preferences.getStringSet(FRIEND_LIST_KEY, new HashSet<>());
    }

    /**
     * 从列表中移除好友
     * @param username 要移除的好友用户名
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

//    /**
//     * 获取好友头像
//     * @param username 要获取头像的好友用户名
//     * @return 好友头像URL
//     */
//    public String getFriendAvatar(String username) {
//        return preferences.getString(FRIEND_AVATAR_PREFIX + username, "");
//    }
//
//    /**
//     * 设置好友头像
//     * @param username 要设置头像的好友用户名
//     * @param avatarUrl 要设置的头像URL
//     */
//    public void setFriendAvatar(String username, String avatarUrl) {
//        preferences.edit()
//                .putString(FRIEND_AVATAR_PREFIX + username, avatarUrl)
//                .apply();
//    }

    /**
     * 清除所有好友相关数据
     */
    public void clearAllFriendData(String username) {
        if (username == null || username.isEmpty()) {
            return;
        }

        Log.d(TAG, "Starting to clear all data for friend: " + username);

        String currentUser = getCurrentUsername();
        if (currentUser == null) return;

        SharedPreferences.Editor editor = preferences.edit();

        try {
            // 清除好友列表缓存
            String cacheKey = String.format(CACHED_FRIEND_LIST, currentUser);
            String timeKey = String.format(FRIEND_LIST_UPDATE_TIME, currentUser);
            
            // 从缓存的好友列表中移除该好友
            List<FriendList> cachedList = getCachedFriendList();
            if (cachedList != null) {
                boolean removed = cachedList.removeIf(friend -> username.equals(friend.getFriendUsername()));
                if (removed) {
                    // 如果列表为空，直接清除缓存
                    if (cachedList.isEmpty()) {
                        editor.remove(cacheKey);
                        editor.remove(timeKey);
                    } else {
                        // 否则更新缓存
                        String json = gson.toJson(cachedList);
                        editor.putString(cacheKey, json);
                    }
                    editor.putLong(timeKey, System.currentTimeMillis());
                }
            }

            // 清除所有相关的键
            editor.remove(FRIEND_NICKNAME_PREFIX + username)
                  .remove(FRIEND_AVATAR_PREFIX + username)
                  .remove(CHAT_MESSAGES_PREFIX + currentUser + "_" + username)
                  .remove(LAST_MESSAGE_PREFIX + username)
                  .remove(LAST_MESSAGE_TIME_PREFIX + username);

            // 清除好友请求
            String requestKey = String.format(KEY_FRIEND_REQUESTS, currentUser);
            List<FriendRequest> requests = getFriendRequests();
            if (requests != null) {
                requests.removeIf(request -> username.equals(request.getUsername()));
                editor.putString(requestKey, gson.toJson(requests));
            }

            // 从好友列表中移除
            Set<String> friendList = getFriendList();
            if (friendList != null && !friendList.isEmpty()) {
                friendList.remove(username);
                editor.putStringSet(FRIEND_LIST_KEY, friendList);
            }

            // 立即应用所有更改
            editor.apply();

            Log.d(TAG, "Successfully cleared all data for friend: " + username + ", including FriendList cache");
        } catch (Exception e) {
            Log.e(TAG, "Error clearing friend data for: " + username, e);
        }
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

    /**
     * 保存好友列表缓存
     * @param friendList 好友列表
     */
    public void cacheFriendList(List<com.example.qq.domain.FriendList> friendList) {
        if (friendList == null) return;
        
        String currentUsername = getCurrentUsername();
        if (currentUsername == null) return;
        
        String key = String.format(CACHED_FRIEND_LIST, currentUsername);
        String timeKey = String.format(FRIEND_LIST_UPDATE_TIME, currentUsername);
        
        String json = gson.toJson(friendList);
        preferences.edit()
                .putString(key, json)
                .putLong(timeKey, System.currentTimeMillis())
                .apply();
    }

    /**
     * 获取缓存的好友列表
     * @return 好友列表，如果没有缓存则返回null
     */
    public List<com.example.qq.domain.FriendList> getCachedFriendList() {
        String currentUsername = getCurrentUsername();
        if (currentUsername == null) return null;
        
        String key = String.format(CACHED_FRIEND_LIST, currentUsername);
        String json = preferences.getString(key, null);
        if (json == null) return null;
        
        Type type = new TypeToken<List<com.example.qq.domain.FriendList>>(){}.getType();
        return gson.fromJson(json, type);
    }

    /**
     * 获取好友列表最后更新间
     * @return 最后更新时间的时间戳
     */
    public long getFriendListUpdateTime() {
        String currentUsername = getCurrentUsername();
        if (currentUsername == null) return 0;
        
        String timeKey = String.format(FRIEND_LIST_UPDATE_TIME, currentUsername);
        return preferences.getLong(timeKey, 0);
    }

    /**
     * 清除好友列表缓存
     */
    public void clearFriendListCache() {
        String currentUsername = getCurrentUsername();
        if (currentUsername == null) return;
        
        String key = String.format(CACHED_FRIEND_LIST, currentUsername);
        String timeKey = String.format(FRIEND_LIST_UPDATE_TIME, currentUsername);
        
        preferences.edit()
                .remove(key)
                .remove(timeKey)
                .apply();
        
        Log.d(TAG, "Friend list cache cleared");
    }

    /**
     * 清除所有用户的好友列表缓存
     */
    public void clearAllUsersFriendListCache() {
        SharedPreferences.Editor editor = preferences.edit();
        
        // 获取所有键
        Map<String, ?> allPrefs = preferences.getAll();
        for (String key : allPrefs.keySet()) {
            if (key.startsWith("cached_friend_list_") || 
                key.startsWith("friend_list_update_time_")) {
                editor.remove(key);
            }
        }
        
        editor.apply();
    }

    /**
     * 设置好友列表的最后更新时间
     * @param timestamp 时间戳（毫秒）
     */
    public void setFriendListUpdateTime(long timestamp) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putLong("friend_list_update_time", timestamp);
        editor.apply();
    }

    // 保存聊天消息到本地
    public void cacheChatMessages(String friendUsername, List<ChatMessage> messages) {
        String currentUser = getCurrentUsername();
        if (currentUser == null) return;
        
        String key = CHAT_MESSAGES_PREFIX + currentUser + "_" + friendUsername;
        String json = gson.toJson(messages);
        editor.putString(key, json);
        editor.apply();
    }
    
    // 获取本地缓存的聊天消息
    public List<ChatMessage> getCachedChatMessages(String friendUsername) {
        String currentUser = getCurrentUsername();
        if (currentUser == null) return new ArrayList<>();
        
        String key = CHAT_MESSAGES_PREFIX + currentUser + "_" + friendUsername;
        String json = preferences.getString(key, null);
        if (json == null) {
            return new ArrayList<>();
        }
        Type type = new TypeToken<List<ChatMessage>>(){}.getType();
        return gson.fromJson(json, type);
    }
    
    // 清除指定好友的聊天消息缓存
    public void clearChatMessagesCache(String friendUsername) {
        String currentUser = getCurrentUsername();
        if (currentUser == null) return;
        
        String key = CHAT_MESSAGES_PREFIX + currentUser + "_" + friendUsername;
        editor.remove(key);
        editor.apply();
    }

    /**
     * 存好友昵称
     * @param username 好友用户名
     * @param nickname 好友昵称
     */
    public void saveFriendNickname(String username, String nickname) {
        if (username == null || username.isEmpty()) {
            return;
        }
        if (nickname == null || nickname.isEmpty()) {
            nickname = username;
        }
        preferences.edit()
                .putString(FRIEND_NICKNAME_PREFIX + username, nickname)
                .apply();
    }

    /**
     * 清除好友昵称
     * @param username 好友用户名
     */
    public void clearFriendNickname(String username) {
        editor.remove(FRIEND_NICKNAME_PREFIX + username);
        editor.apply();
    }

    public void clearFriendAvatar(String username) {
        if (username == null || username.isEmpty()) {
            return;
        }
        preferences.edit()
            .remove(FRIEND_AVATAR_PREFIX + username)
            .apply();
    }

    public synchronized void incrementUnreadCount(String username) {
        if (username == null || username.isEmpty()) {
            return;
        }
        int currentCount = getUnreadMessageCount(username);
        Log.d(TAG, "Incrementing unread count for " + username + 
            " from " + currentCount + " to " + (currentCount + 1));
        preferences.edit()
            .putInt(UNREAD_COUNT_PREFIX + username, currentCount + 1)
            .commit();
    }

    public synchronized int getUnreadMessageCount(String username) {
        if (username == null || username.isEmpty()) {
            return 0;
        }
        int count = preferences.getInt(UNREAD_COUNT_PREFIX + username, 0);
        Log.d(TAG, "Getting unread count for " + username + ": " + count);
        return count;
    }

    public synchronized void clearUnreadCount(String username) {
        if (username == null || username.isEmpty()) {
            return;
        }
        Log.d(TAG, "Clearing unread count for " + username);
        preferences.edit()
            .putInt(UNREAD_COUNT_PREFIX + username, 0)
            .commit();
    }

    /**
     * 清除用户登录状态和相关信息
     */
    public void clearLoginStatus() {
        SharedPreferences.Editor editor = preferences.edit();
        
        // 清除登录状态
        editor.putBoolean("is_logged_in", false);
        
        // 清除用户信息
        editor.remove("current_username");
        editor.remove("current_user_id");
        editor.remove("current_user_nickname");
        editor.remove("current_user_avatar");
        editor.remove("token");
        
        // 清除好友相关信息
        editor.remove("friend_requests");
        editor.remove("friend_list");
        editor.remove("chat_messages");
        editor.remove("unread_counts");
        editor.remove("last_messages");
        editor.remove("last_message_times");
        
        // 立即提交更改
        editor.apply();
    }

    /**
     * 批量保存好友头像
     * @param contacts 联系人列表
     */
    public void setFriendAvatars(List<Contact> contacts) {
        if (contacts == null || contacts.isEmpty()) {
            return;
        }

        SharedPreferences.Editor editor = preferences.edit();
        
        for (Contact contact : contacts) {
            String username = contact.getUsername();
            String avatarUrl = contact.getAvatarUrl();
            
            if (username != null && avatarUrl != null) {
                editor.putString(FRIEND_AVATAR_PREFIX + username, avatarUrl);
                Log.d(TAG, "Saving avatar for " + username + ": " + avatarUrl);
            }
        }
        
        editor.apply();
    }

    /**
     * 获取好友头像URL
     * @param username 用户名
     * @return 头像URL，如果不存在返回null
     */
    public String getFriendAvatar(String username) {
        return preferences.getString(FRIEND_AVATAR_PREFIX + username, null);
    }

    /**
     * 设置单个好友头像
     * @param username 用户名
     * @param avatarUrl 头像URL
     */
    public void setFriendAvatar(String username, String avatarUrl) {
        if (username != null && avatarUrl != null) {
            preferences.edit()
                .putString(FRIEND_AVATAR_PREFIX + username, avatarUrl)
                .apply();
            Log.d(TAG, "Saved avatar for " + username + ": " + avatarUrl);
        }
    }
} 