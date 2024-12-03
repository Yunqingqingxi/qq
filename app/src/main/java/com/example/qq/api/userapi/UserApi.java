package com.example.qq.api.userapi;

import android.net.Uri;
import com.example.qq.domain.User;
import java.util.List;

/**
 * 用户相关的API接口
 * 提供用户账户相关的功能，包括：
 * - 用户登录和注册
 * - 好友关系管理
 * - 用户信息获取和更新
 * - 头像上传
 * - 邮箱验证
 * 
 * @author yunxi
 * @version 1.0
 */
public interface UserApi {

    /**
     * 用户登录
     * 验证用户身份并获取访问令牌
     *
     * @param json 登录信息的JSON字符串，格式如下：
     *            {
     *              "username": "用户名",
     *              "password": "密码"
     *            }
     * @return 登录是否成功
     */
    boolean login(String json);

    /**
     * 用户注册
     * 创建新用户账户
     *
     * @param json 注册信息的JSON字符串，格式如下：
     *            {
     *              "username": "用户名",
     *              "password": "密码",
     *              "email": "邮箱",
     *              "nickname": "昵称"
     *            }
     * @return 注册是否成功
     */
    boolean register(String json);

    /**
     * 接受好友请求
     * 同意添加对方为好友
     *
     * @param json 好友请求信息的JSON字符串，格式如下：
     *            {
     *              "requester": "请求者用户名",
     *              "target": "目标用户名"
     *            }
     * @return 操作是否成功
     */
    boolean acceptFriendRequest(String json);

    /**
     * 删除好友
     * 解除与指定用户的好友关系
     *
     * @param username 当前用户名
     * @param friendname 要删除的好友用户名
     * @return 删除是否成功
     */
    boolean deleteFriend(String username, String friendname);

    /**
     * 获取用户信息
     * 获取指定用户的详细信息
     *
     * @param username 要查询的用户名
     * @return 用户信息对象，如果未找到返回null
     */
    User getUserInfo(String username);
    
    /**
     * 修改用户信息
     * 修改指定用户的详细信息
     *
     * @param json 用户信息JSON字符串
     * @param callback 更新结果回调
     */
    void updateUserInfo(String json, UserInfoUpdateCallback callback);


    /**
     * 更新用户头像
     * 上传新的头像图片
     *
     * @param username 用户名
     * @param imageUri 图片的URI
     * @param callback 上传结果回调
     */
    void updateAvatar(String username, Uri imageUri, AvatarUpdateCallback callback);

    /**
     * 发送验证码
     * 向指定邮箱发送验证码
     *
     * @param email 目标邮箱地址
     * @param callback 发送结果回调
     */
    void sendVerificationCode(String email, VerificationCallback callback);

    /**
     * 验证验证码
     * 验证用户输入的验证码是否正确
     *
     * @param email 邮箱地址
     * @param code 验证码
     * @param callback 验证结果回调
     */
    void verifyCode(String email, String code, VerificationCallback callback);

    /**
     * 修改用户密码
     * @param json 包含用户名、旧密码和新密码的JSON字符串
     * @param callback 修改结果回调
     */
    void updatePassword(String json, PasswordUpdateCallback callback);

    /**
     * 头像更新回调接口
     */
    interface AvatarUpdateCallback {
        /**
         * 上传成功回调
         * @param newAvatarUrl 新头像的URL地址
         */
        void onSuccess(String newAvatarUrl);

        /**
         * 上传失败回调
         * @param errorMessage 错误信息
         */
        void onError(String errorMessage);

        /**
         * 上传进度回调
         * @param progress 上传进度（0-100）
         */
        void onProgress(int progress);
    }

    /**
     * 验证码操作回调接口
     */
    interface VerificationCallback {
        /**
         * 操作成功回调
         */
        void onSuccess();

        /**
         * 操作失败回调
         * @param errorMessage 错误信息
         */
        void onError(String errorMessage);
    }

    /**
     * 用户信息更新回调接口
     */
    interface UserInfoUpdateCallback {
        /**
         * 更新成功回调
         * @param user 更新后的用户信息
         */
        void onSuccess(User user);

        /**
         * 更新失败回调
         * @param message 错误信息
         */
        void onError(String message);
    }

    /**
     * 密码修改回调接口
     */
    interface PasswordUpdateCallback {
        /**
         * 修改成功回调
         */
        void onSuccess();

        /**
         * 修改失败回调
         * @param message 错误信息
         */
        void onError(String message);
    }
}
