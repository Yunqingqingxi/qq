package com.example.qq.api.friendlistapi.impl;

// Android 框架
import android.util.Log;

// 应用内部类
import com.example.qq.api.friendlistapi.FriendApi;
import com.example.qq.domain.Contact;
import com.example.qq.domain.FriendList;
import com.example.qq.network.RequestManager;
import com.example.qq.utils.JsonParser;
import com.example.qq.utils.SharedPreferencesManager;

// Java 标准库
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

// JSON 相关
import org.json.JSONArray;
import org.json.JSONObject;

// 静态导入
import static com.example.qq.network.RequestManager.get;
import static com.example.qq.utils.JsonParser.parseToMap;

/**
 * 好友API接口实现类
 * 实现好友列表相关的网络请求和数据处理，包括：
 * - 从服务器获取好友列表数据
 * - 解析好友信息和最近聊天记录
 * - 管理联系人数据的本地缓存
 * 
 * @author yunxi
 * @version 1.0
 */
public class FriendApiImpl implements FriendApi {
    private static final String TAG = "FriendApiImpl";

    /**
     * 获取首页好友列表
     * 从服务器获取包含最近聊天记录的好友列表
     *
     * @return 好友列表，包含每个好友的基本信息和最近聊天记录
     * @throws Exception 当网络请求失败或数据解析错误时抛出异常
     */
    @Override
    public List<FriendList> getFriendList() throws Exception {
        try {
            Log.d(TAG, "开始获取好友列表");
            List<FriendList> friendLists = new ArrayList<>();

            String currentUsername = SharedPreferencesManager.getInstance().getCurrentUsername();
            Map<String,Object> response = parseToMap(get("/getuserandmessage/" + currentUsername));

            if (response != null && response.containsKey("data")) {
                parseFriendListResponse(response, friendLists);
            }

            logFriendListResult(friendLists);
            return friendLists;
        } catch (Exception e) {
            Log.e(TAG, "获取好友列表失败: " + e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 解析好友列表响应数据
     * @param response 服务器响应数据
     * @param friendLists 用于存储解析结果的列表
     */
    private void parseFriendListResponse(Map<String,Object> response, List<FriendList> friendLists) {
        try {
            JSONArray friendsArray = (JSONArray) response.get("data");
            for (int i = 0; i < friendsArray.length(); i++) {
                JSONObject friend = friendsArray.getJSONObject(i);
                FriendList friendList = createFriendListFromJson(friend);
                friendLists.add(friendList);
            }
        } catch (Exception e) {
            Log.e(TAG, "解析好友列表数据失败", e);
        }
    }

    /**
     * 从JSON对象创建FriendList实例
     * @param friend JSON格式的好友数据
     * @return FriendList实例
     */
    private FriendList createFriendListFromJson(JSONObject friend) {
        FriendList friendList = new FriendList();
        friendList.setFriendUsername(friend.optString("username"));
        friendList.setFriendNickName(friend.optString("nickname"));
        friendList.setAvatarUrl(friend.optString("avatarUrl"));

        String content = friend.optString("content");
        if (content != null && !content.equals("null")) {
            friendList.setLastContext(content);
        }

        String timestamp = friend.optString("timestamp");
        if (timestamp != null && !timestamp.equals("null")) {
            friendList.setLastContextTime(timestamp);
        }

        return friendList;
    }

    /**
     * 记录好友列表获取结果
     * @param friendLists 获取到的好友列表
     */
    private void logFriendListResult(List<FriendList> friendLists) {
        if (!friendLists.isEmpty()) {
            Log.d(TAG, "成功获取好友列表，数量: " + friendLists.size());
        } else {
            Log.w(TAG, "获取到的好友列表为空");
        }
    }

    /**
     * 获取联系人列表
     * 从服务器获取所有好友的基本信息
     *
     * @return 联系人列表，包含所有好友的基本信息
     */
    @Override
    public List<Contact> getContactList() {
        List<Contact> contactList = new ArrayList<>();
        String currentUsername = SharedPreferencesManager.getInstance().getCurrentUsername();
        Map<String,Object> response = parseToMap(get("/friends/" + currentUsername));

        if (response != null && response.containsKey("data")) {
            parseContactListResponse(response, contactList);
        }

        return contactList;
    }

    /**
     * 解析联系人列表响应数据
     * @param response 服务器响应数据
     * @param contactList 用于存储解析结果的列表
     */
    private void parseContactListResponse(Map<String,Object> response, List<Contact> contactList) {
        try {
            Map<String, Object> data = (Map<String, Object>) response.get("data");
            JSONArray friendsArray = (JSONArray) data.get("friends");

            for (int i = 0; i < friendsArray.length(); i++) {
                JSONObject friend = friendsArray.getJSONObject(i);
                Contact contact = createContactFromJson(friend);
                contactList.add(contact);
            }

            // 保存联系人头像信息到本地缓存
            SharedPreferencesManager.getInstance().setFriendAvatars(contactList);
        } catch (Exception e) {
            Log.e(TAG, "解析联系人数据失败", e);
        }
    }

    /**
     * 从JSON对象创建Contact实例
     * @param friend JSON格式的好友数据
     * @return Contact实例
     */
    private Contact createContactFromJson(JSONObject friend) {
        Contact contact = new Contact();
        contact.setUsername(friend.optString("friend_id"));
        contact.setNickName(friend.optString("nickname"));
        contact.setAvatarUrl(friend.optString("avatar_url"));
        return contact;
    }
}
