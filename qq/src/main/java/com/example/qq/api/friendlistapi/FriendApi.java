package com.example.qq.api.friendlistapi;

import com.example.qq.domain.FriendList;

import java.util.List;

/**
 * 关于好友的api方法
 * @Author: yunxi
 * @Date: 2024/11/28 21:59
 */
public interface FriendApi {
    /**
     * 获取好友列表
     * @return
     */
    List<FriendList> getFriendList();
}
