package com.example.qq.api.friendlistapi;

import com.example.qq.domain.Contact;
import com.example.qq.domain.FriendList;

import java.util.List;

/**
 * 好友相关的API接口
 * 提供好友列表相关的功能，包括：
 * - 获取首页好友列表
 * - 获取联系人页面的好友列表
 * 
 * @author yunxi
 * @version 1.0
 */
public interface FriendApi {
    /**
     * 获取首页好友列表
     * 返回包含最近聊天记录的好友列表
     * 
     * @return 好友列表，每个好友包含最近的聊天信息
     * @throws Exception 当网络请求失败或数据解析错误时抛出异常
     */
    List<FriendList> getFriendList() throws Exception;
    /**
     * 获取联系人页面的好友列表
     * 返回所有好友的基本信息列表
     * 
     * @return 联系人列表，包含所有好友的基本信息
     */
    List<Contact> getContactList();
}
