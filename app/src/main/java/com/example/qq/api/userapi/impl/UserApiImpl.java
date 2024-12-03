package com.example.qq.api.userapi.impl;

// Android 框架
import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

// 应用内部类
import com.example.qq.QQApplication;
import com.example.qq.api.userapi.UserApi;
import com.example.qq.constant.MessageType;
import com.example.qq.domain.User;
import com.example.qq.domain.WebSocketMessage;
import com.example.qq.handler.MessageHandler;
import com.example.qq.utils.SharedPreferencesManager;

// OkHttp 相关
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

// Java 标准库
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Objects;

// 静态导入
import static com.example.qq.network.RequestManager.get;
import static com.example.qq.network.RequestManager.post;
import static com.example.qq.network.RequestManager.postMultipart;
import static com.example.qq.utils.JsonParser.parseJson;
import static com.example.qq.utils.JsonParser.parseToMap;

/**
 * 用户API接口实现类
 * 实现用户相关的所有功能，包括：
 * - 用户认证（登录、注册）
 * - 好友关系管理
 * - 户信息维护
 * - 头像上传
 * - 邮箱验证
 * 
 * @author yunxi
 * @version 1.0
 */
public class UserApiImpl implements UserApi {
    private static final String TAG = "UserApiImpl";
    private static MessageHandler messageHandler;

    /**
     * 设置消息处理器
     * @param handler 消息处理器实例
     */
    public static void setMessageHandler(MessageHandler handler) {
        messageHandler = handler;
    }

    /**
     * 用户登录
     * 处理登录请求并保存认证信息
     *
     * @param json 登录信息JSON
     * @return 登录是否成功
     */
    @Override
    public boolean login(String json) {
        try {
            Map<String,Object> map = parseToMap(post("/login", json));
            Object code = map.get("code");
            if (code instanceof Number && ((Number) code).intValue() == 200) {
                // 获取data中的token
                Map<String, Object> data = (Map<String, Object>) map.get("data");
                if (data != null && data.containsKey("token")) {
                    String token = (String) data.get("token");
                    // 保存token到SharedPreferences
                    SharedPreferencesManager.getInstance().saveToken(token);

                    // 获取用户名并保存用户信息
                    Map<String, Object> requestMap = parseToMap(json);
                    String username = (String) requestMap.get("username");
                    if (username != null) {
                        SharedPreferencesManager.getInstance().fetchAndSaveUserInfo(username);
                    }
                }
                return true;
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 用户注册
     * 创建新用户账户
     *
     * @param json 注册信息JSON
     * @return 注册是否成功
     */
    @Override
    public boolean register(String json) {
        try {
            Map<String,Object> map = parseToMap(post("/register", json));
            Object code = map.get("code");
            if (code instanceof Number) {
                return ((Number) code).intValue() == 200;
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 接受好友请求
     * 处理好友请求的接受操作
     *
     * @param json 好友请求信息JSON
     * @return 操作是否成功
     */
    @Override
    public boolean acceptFriendRequest(String json) {
        try {
            Map<String,Object> map = parseToMap(post("/acceptFriend", json));
            Object code = map.get("code");
            if (code instanceof Number) {
                return ((Number) code).intValue() == 200;
            }
            return false;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }

    }

    /**
     * 删除好友
     * 处理好友删除操作并清理相关缓存
     *
     * @param username 当前用户名
     * @param friendname 要删除的好友用户名
     * @return 删除是否成功
     */
    @Override
    public boolean deleteFriend(String username, String friendname) {
        try {
            // 构建请求URL
            Map<String,Object> map = parseToMap(get("/deletefriend/"+username+"/"+friendname));

            // 检查响应状态码
            Object code = map.get("code");
            if (code != null && String.valueOf(code).equals("200")) {
                if (messageHandler != null) {  // 添加空检查
                    // 通知另一个好友被删除
                    WebSocketMessage notifyMessage = new WebSocketMessage(
                        MessageType.FRIEND_DELETED,
                        username,
                        friendname,
                        "已将您好友列表中删除"
                    );

                    // 通过WebSocket发送通知
                    messageHandler.sendMessage(notifyMessage);
                } else {
                    Log.w(TAG, "MessageHandler is not set");
                }

                // 删除成功，清除所有相关的本地缓存
                SharedPreferencesManager manager = SharedPreferencesManager.getInstance();
                // 清除好友列表缓存
                manager.clearFriendListCache();
                // 清除聊天消息缓存
                manager.clearChatMessagesCache(friendname);
                // 清除好友头像缓存
                manager.setFriendAvatar(friendname, null);
                // 清除好友状态
                manager.removeFriend(friendname);
                // 清除好友昵称
                manager.clearFriendNickname(friendname);
                // 清除最后一条消息和时间
                manager.setLastMessage(friendname, null);
                // 清除最后一条消息时间
                manager.setLastMessageTime(friendname, null);
                // 从好友列表中移除
                manager.removeFriendFromList(friendname);

                return true;
            }

            Log.e(TAG, "删除好友失败：" + map.get("msg"));
            return false;
        } catch (Exception e) {
            Log.e(TAG, "删除好友失败：" + e.getMessage(), e);
            return false;
        }
    }

    /**
     * 获取用户信息
     * 从服务器获取用户详细信息
     *
     * @param username 要查询的用户名
     * @return 用户信息对象，失败返回null
     */
    @Override
    public User getUserInfo(String username) {
        try {
            // 发送GET请求获取用户信息
            Map<String, Object> responseMap = parseToMap(get("/getuser/" + username));

            // 检查响应状态码
            Object code = responseMap.get("code");
            if (code instanceof Number && ((Number) code).intValue() == 200) {
                // 获取data中的user对象
                Map<String, Object> data = (Map<String, Object>) responseMap.get("data");
                Map<String, Object> userMap = (Map<String, Object>) data.get("user");

                // 构造User对象
                User user = new User();
                assert userMap != null;
                user.setUserName((String) userMap.get("username"));
                user.setEmail((String) userMap.get("email") == null ? " " : (String) userMap.get("email"));
                user.setUserNickName((String) userMap.get("nickname"));
                user.setUserAvatarUrl((String) userMap.get("avatarUrl"));
                // 根据需要设置其他用户属性

                return user;
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 更新用户信息
     * @param json 用户信息JSON字符串
     * @param callback 更新结果回调
     */
    @Override
    public void updateUserInfo(String json, UserInfoUpdateCallback callback) {
        new Thread(() -> {
            try {
                // 发送POST请求更新用户信息
                String response = post("/updateUserInfo", json);

                // 解析响应
                Map<String, Object> map = parseToMap(response);
                int code = (int) map.get("code");
                String msg = (String) map.get("msg");
                
                if (code == 200) {
                    // 获取当前用户的完整信息
                    Map<String, Object> requestMap = parseToMap(json);
                    String username = (String) requestMap.get("username");
                    
                    // 获取最新的用户信息
                    User updatedUser = getUserInfo(username);
                    
                    if (updatedUser != null) {
                        // 先清除旧的缓存
                        SharedPreferencesManager manager = SharedPreferencesManager.getInstance();
                        manager.clearUserInfo();
                        
                        // 写入新的用户信息
                        manager.saveUserInfo(updatedUser);
                        
                        // 回调成功
                        if (callback != null) {
                            Handler mainHandler = new Handler(Looper.getMainLooper());
                            mainHandler.post(() -> callback.onSuccess(updatedUser));
                        }
                        
                        Log.d(TAG, "用户信息更新成功");
                    } else {
                        // 获取用户信息失败
                        if (callback != null) {
                            Handler mainHandler = new Handler(Looper.getMainLooper());
                            mainHandler.post(() -> callback.onError("获取更新后的用户信息失败"));
                        }
                        Log.e(TAG, "获取更新后的用户信息失败");
                    }
                } else {
                    // 更新失败
                    if (callback != null) {
                        Handler mainHandler = new Handler(Looper.getMainLooper());
                        mainHandler.post(() -> callback.onError(msg));
                    }
                    Log.e(TAG, "用户信息更新失败: " + msg);
                }
            } catch (Exception e) {
                Log.e(TAG, "更新用户信息时发生错误", e);
                if (callback != null) {
                    Handler mainHandler = new Handler(Looper.getMainLooper());
                    mainHandler.post(() -> callback.onError("网络错误: " + e.getMessage()));
                }
            }
        }).start();
    }

    /**
     * 更新用户头像
     * 上传新头像并更新用户信息
     *
     * @param username 用户名
     * @param imageUri 头像图片URI
     * @param callback 上传结果回调
     */
    @Override
    public void updateAvatar(String username, Uri imageUri, AvatarUpdateCallback callback) {
        new Thread(() -> {
            try {
                Log.d("UserApiImpl", "Starting avatar upload for user: " + username);
                Log.d("UserApiImpl", "Image URI: " + imageUri);

                // 构建multipart请求体
                MultipartBody.Builder builder = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("username", username);

                // 从ContentResolver获取文件输入流
                Context context = QQApplication.getInstance();
                InputStream inputStream = context.getContentResolver().openInputStream(imageUri);
                if (inputStream == null) {
                    throw new IOException("Cannot open input stream for URI: " + imageUri);
                }

                // 读取文件数据
                byte[] fileBytes = new byte[inputStream.available()];
                inputStream.read(fileBytes);
                inputStream.close();

                // 创建RequestBody
                RequestBody imageBody = RequestBody.create(
                    fileBytes,
                    MediaType.get("image/*")
                );

                // 使用时间戳作为文件名
                String fileName = "avatar_" + System.currentTimeMillis() + ".jpg";
                builder.addFormDataPart("file", fileName, imageBody);

                // 使用RequestManager发送请求
                Log.d("UserApiImpl", "Sending request to server...");
                String response = postMultipart("/upload/avatar", builder.build());
                Log.d("UserApiImpl", "Server response: " + response);
                
                // 解析响应
                Map<String, Object> responseMap = parseToMap(response);
                int code = ((Number) responseMap.get("code")).intValue();
                Log.d("UserApiImpl", "Response code: " + code);

                if (code == 200) {
                    Map<String, Object> data = (Map<String, Object>) responseMap.get("data");
                    String newAvatarUrl = (String) data.get("avatarUrl");
                    Log.d("UserApiImpl", "New avatar URL: " + newAvatarUrl);
                    
                    // 更新本地保存的用户信息
                    User currentUser = SharedPreferencesManager.getInstance().getUserInfo();
                    if (currentUser != null) {
                        currentUser.setUserAvatarUrl(newAvatarUrl);
                        SharedPreferencesManager.getInstance().saveUserInfo(currentUser);
                    }
                    
                    // 通知上传成功
                    callback.onSuccess(newAvatarUrl);
                } else {
                    String message = (String) responseMap.get("message");
                    Log.e("UserApiImpl", "Server error: " + message);
                    callback.onError("服务器返回错误：" + message);
                }
            } catch (Exception e) {
                Log.e("UserApiImpl", "Upload failed", e);
                callback.onError("上传失败：" + e.getMessage() + "\n" + Log.getStackTraceString(e));
            }
        }).start();
    }

    /**
     * 发送验证码
     * 向指定邮箱发送验证码
     *
     * @param email 目标邮箱地址
     * @param callback 发送结果回调
     */
    @Override
    public void sendVerificationCode(String email, VerificationCallback callback) {
        new Thread(() -> {
            try {
                Log.d("UserApiImpl", "Sending verification code to email: " + email);
                
                String encodedEmail = java.net.URLEncoder.encode(email, "UTF-8");
                String response = post("/send-captcha?email=" + encodedEmail, "");
                Log.d("UserApiImpl", "Server response: " + response);
                
                // 解析JSON响应
                Map<String, Object> responseMap = parseToMap(response);
                int code = ((Number) responseMap.get("code")).intValue();
                
                if (code == 200) {
                    // 成功发送
                    new Handler(Looper.getMainLooper()).post(callback::onSuccess);
                } else {
                    // 发送失败
                    String message = (String) responseMap.get("msg");
                    Log.e("UserApiImpl", "Failed to send verification code: " + message);
                    new Handler(Looper.getMainLooper()).post(() -> {
                        callback.onError(message);
                    });
                }
            } catch (Exception e) {
                Log.e("UserApiImpl", "Error sending verification code", e);
                new Handler(Looper.getMainLooper()).post(() -> {
                    callback.onError(e.getMessage());
                });
            }
        }).start();
    }

    /**
     * 验证验证码
     * 验证用户输入的验证码
     *
     * @param email 邮箱地址
     * @param code 验证码
     * @param callback 验证结果回调
     */
    @Override
    public void verifyCode(String email, String code, VerificationCallback callback) {
        new Thread(() -> {
            try {
                Log.d("UserApiImpl", "Verifying code for email: " + email);
                
                String encodedEmail = java.net.URLEncoder.encode(email, "UTF-8");
                String encodedCode = java.net.URLEncoder.encode(code, "UTF-8");
                String response = post("/verify-captcha?email=" + encodedEmail + "&captcha=" + encodedCode, "");
                Log.d("UserApiImpl", "Server response: " + response);
                
                // 解析JSON响应
                Map<String, Object> responseMap = parseToMap(response);
                int responseCode = ((Number) responseMap.get("code")).intValue();
                
                if (responseCode == 200) {
                    new Handler(Looper.getMainLooper()).post(() -> {
                        callback.onSuccess();
                    });
                } else {
                    String message = (String) responseMap.get("msg");
                    Log.e("UserApiImpl", "Code verification failed: " + message);
                    new Handler(Looper.getMainLooper()).post(() -> {
                        callback.onError(message);
                    });
                }
            } catch (Exception e) {
                Log.e("UserApiImpl", "Error verifying code", e);
                new Handler(Looper.getMainLooper()).post(() -> {
                    callback.onError(e.getMessage());
                });
            }
        }).start();
    }

    @Override
    public void updatePassword(String json, PasswordUpdateCallback callback) {
        new Thread(() -> {
            try {
                Log.d(TAG, "Updating password for user");
                
                // 发送POST请求更新密码
                String response = post("/updatePassword", json);
                Log.d(TAG, "Server response: " + response);
                
                // 解析响应
                Map<String, Object> responseMap = parseToMap(response);
                int code = ((Number) responseMap.get("code")).intValue();
                String msg = (String) responseMap.get("msg");

                Handler mainHandler = new Handler(Looper.getMainLooper());
                if (code == 200) {
                    // 密码更新成功
                    mainHandler.post(() -> {
                        if (callback != null) {
                            callback.onSuccess();
                        }
                    });
                    Log.d(TAG, "Password updated successfully");
                } else {
                    // 密码更新失败
                    mainHandler.post(() -> {
                        if (callback != null) {
                            callback.onError(msg);
                        }
                    });
                    Log.e(TAG, "Failed to update password: " + msg);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error updating password", e);
                Handler mainHandler = new Handler(Looper.getMainLooper());
                mainHandler.post(() -> {
                    if (callback != null) {
                        callback.onError("网络错误: " + e.getMessage());
                    }
                });
            }
        }).start();
    }
}
