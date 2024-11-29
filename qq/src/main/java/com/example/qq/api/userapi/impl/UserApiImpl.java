package com.example.qq.api.userapi.impl;

import static com.example.qq.network.RequestManager.get;
import static com.example.qq.network.RequestManager.post;
import static com.example.qq.network.RequestManager.postMultipart;
import static com.example.qq.utils.JsonParser.parseToMap;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.example.qq.QQApplication;
import com.example.qq.api.userapi.UserApi;
import com.example.qq.domain.User;
import com.example.qq.utils.SharedPreferencesManager;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

/**
 * 实现用户api的方法
 */
public class UserApiImpl implements UserApi {

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
}
