package com.example.qq.api.userapi;

import android.net.Uri;
import com.example.qq.domain.User;

/**
 * 用户的api方法
 * @Author: yunxi
 * @Date: 2024/11/28 21:59
 */
public interface UserApi {
    /**
     * 登录
     * @param json 登录的json数据
     * @return true表示登录成功，false表示登录失败
     */
    boolean login(String json);

    /**
     * 注册
     * @Param json 注册的json数据
     * @return true表示注册成功，false表示注册失败
     */
    boolean register(String json);


    /**
     * 接受好友请求
     * @param json 接受好友请求的json数据
     */
    boolean acceptFriendRequest(String json);


    /**
     * 获取用户信息
     * @param username 用户名
     * @return 用户信息
     */
    User getUserInfo(String username);

    /**
     * 更新用户头像
     * @param username 用户名
     * @param imageUri 头像图片的Uri
     * @param callback 上传结果回调
     */
    void updateAvatar(String username, Uri imageUri, AvatarUpdateCallback callback);



    /**
     * 头像更新回调接口
     */
    interface AvatarUpdateCallback {
        /**
         * 上传成功
         * @param newAvatarUrl 新头像的URL
         */
        void onSuccess(String newAvatarUrl);

        /**
         * 上传失败
         * @param errorMessage 错误信息
         */
        void onError(String errorMessage);

        /**
         * 上传进度
         * @param progress 进度值（0-100）
         */
        void onProgress(int progress);
    }
}
