package com.example.qq.websocket.webUtils.controller;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.qq.util.JsonUtil;
import com.example.qq.websocket.webResult.WebResult;
import com.example.qq.websocket.webUtils.Connect;


import org.json.JSONException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WebUtil {

    private static final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private static String urlString;
    private static String url = "https://web.yxdfirst.top/api";
//    private static String url = "http://10.0.2.2:8080/api";
//    private static String url = "http://192.168.3.1:8080/api";

    public static void login(String username, String password, Callback callback) {
        executorService.execute(() -> {
            WebResult<Map<String, Object>> result = performLogin(username, password);
            // 切换到主线程处理结果
            new Handler(Looper.getMainLooper()).post(() -> {
                if (callback != null) {
                    try {
                        callback.onResult(result);
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        });
    }

    public static WebResult<Map<String, Object>> performLogin(String username, String password) {
        urlString = url+"/login";
        String jsonInputString = String.format("{\"username\":\"%s\", \"password\":\"%s\"}", username, password);
        System.out.println("Json: " + jsonInputString);

        // 调用封装的 postConnect 方法
        String response = Connect.postConnect(urlString, jsonInputString);
        System.out.println("Response: " + response);
        Log.e("Response",response);

        // 处理响应结果
        WebResult<Map<String, Object>> result;

        try {
            // 创建 JsonParser 实例解析响应
            JsonUtil parser = new JsonUtil(response);
            result = parser.getWebResult();

            WebResult<Map<String,Object>> webResult;

            // 根据 code 判断结果
            if (result.getCode() == 200) {
                webResult = WebResult.success(null);
                webResult.setCode(200);
                webResult.setMessage("登录成功");
                webResult.setData(result.getData());
                return webResult; // 登录成功
            } else {
                return WebResult.error(null); // 返回错误信息
            }
        } catch (Exception e) {
            e.printStackTrace();
            return WebResult.error(null); // 处理解析异常
        }
    }

    public static void register(String nickname,String username, String password, Callback callback) {
        executorService.execute(() -> {
            WebResult<Map<String, Object>> result = performRegister(nickname,username, password);
            // 切换到主线程处理结果
            new Handler(Looper.getMainLooper()).post(() -> {
                if (callback != null) {
                    try {
                        callback.onResult(result);
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        });
    }

    public static WebResult<Map<String, Object>> performRegister(String nickname,String username, String password) {
         urlString = url+"/register";
        String jsonInputString = String.format("{\"nickname\":\"%s\",\"username\":\"%s\", \"password\":\"%s\"}", nickname, username, password);
        System.out.println("Json: " + jsonInputString);

        // 调用封装的 postConnect 方法
        String response = Connect.postConnect(urlString, jsonInputString);
        System.out.println("Response: " + response);

        // 处理响应结果
        WebResult<Map<String, Object>> result;

        try {
            // 创建 JsonParser 实例解析响应
            JsonUtil parser = new JsonUtil(response);
            result = parser.getWebResult();

            // 根据 code 判断结果
            if (result.getCode() == 200) {
                return WebResult.success(null); // 注册成功
            } else {
                return WebResult.error(null); // 返回错误信息
            }
        } catch (Exception e) {
            e.printStackTrace();
            return WebResult.error(null); // 处理解析异常
        }
    }
    public static void acceptFriend(String requester,String target,String token, Callback callback) {
        executorService.execute(() -> {
            WebResult<Map<String, Object>> result = performAcceptFriend(requester, target,token);
            // 切换到主线程处理结果
            new Handler(Looper.getMainLooper()).post(() -> {
                if (callback != null) {
                    try {
                        callback.onResult(result);
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        });


    }

    private static WebResult<Map<String, Object>> performAcceptFriend(String requester, String target, String token) {
        urlString = url+"/acceptFriend";
        String jsonInputString = String.format("{\"requester\":\"%s\", \"target\":\"%s\"}", requester, target);
        System.out.println("Json: " + jsonInputString);

        // 调用封装的 postConnect 方法
        String response = Connect.postConnect(urlString, jsonInputString, token);
        System.out.println("Response: " + response);
        // 处理响应结果
        WebResult<Map<String, Object>> result;
        try {
            // 创建 JsonParser 实例解析响应
            JsonUtil parser = new JsonUtil(response);
            result = parser.getWebResult();

            // 根据 code 判断结果
            if (result.getCode() == 200) {
                return WebResult.success(null); // 成功
            } else {
                return WebResult.error(null); // 返回错误信息
            }
        } catch (Exception e) {
            e.printStackTrace();
            return WebResult.error(null); // 处理解析异常
        }
    }
//    // 添加好友
//    public static void addFriend(String requester,String target,String token, Callback callback) {
//        executorService.execute(() -> {
//            WebResult<Map<String, Object>> result = performAddFriend(requester, target,token);
//            // 切换到主线程处理结果
//            new Handler(Looper.getMainLooper()).post(() -> {
//                if (callback != null) {
//                    callback.onResult(result);
//                }
//            });
//        });
//    }
//
//    private static WebResult<Map<String, Object>> performAddFriend(String requester, String target, String token) {
//        urlString = url + "/addFriend";
//        String jsonInputString = String.format("{\"requester\":\"%s\", \"target\":\"%s\"}", requester, target);
//        System.out.println("Json: " + jsonInputString);
//        // 调用封装的 postConnect 方法
//        String response = Connect.postConnect(urlString, jsonInputString, token);
//        System.out.println("Response: " + response);
//
//        // 处理响应结果
//        WebResult<Map<String, Object>> result;
//
//        try {
//            // 创建 JsonParser 实例解析响应
//            JsonUtil parser = new JsonUtil(response);
//            result = parser.getWebResult();
//            // 根据 code 判断结果
//            if (result.getCode() == 200) {
//                return WebResult.success(null); // 成功
//            } else {
//                return WebResult.error(null); // 返回错误信息
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return WebResult.error(null); // 处理解析异常
//    }
    // 转发用户同意添加好友

    // 获取当前好友的
    public static void getFriendList(String token,String username, Callback callback) {
        executorService.execute(() -> {
            WebResult<Map<String, Object>> result = performGetFriendList(token,username);
            // 切换到主线程处理结果
            new Handler(Looper.getMainLooper()).post(() -> {
                if (callback != null) {
                    try {
                        callback.onResult(result);
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        });
    }
    private static WebResult<Map<String, Object>> performGetFriendList(String token, String username) {
        // 使用 String.format 代替字符串拼接
        String urlString = String.format("%s/friends/%s", url, username);

        // 调用封装的 postConnect 方法
        String response = Connect.getConnect(urlString, token);
        System.out.println("Response: " + response);

        // 处理响应结果
        WebResult<Map<String, Object>> result;
        try {
            // 创建 JsonParser 实例解析响应
            JsonUtil parser = new JsonUtil(response);
            result = parser.getWebResult();

            // 从data中获取friends列表
            if (result.getCode() == 0) {

                // 直接获取result的data数据
                Map<String, Object> data = result.getData(); // 获取整个数据

                // 构建一个新的WebResult对象，并设置获取的data
                WebResult<Map<String, Object>> successResult = WebResult.success(null);
                successResult.setCode(200);
                successResult.setMessage("获取好友列表成功");
                successResult.setData(data); // 将封装后的Map作为data

                return successResult;  // 返回成功的WebResult
            } else {
                return WebResult.error(null); // 返回错误信息
            }
        } catch (Exception e) {
            e.printStackTrace();
            return WebResult.error(null); // 处理解析异常
        }
    }
    // 存储聊天信息
public static void saveChatInfo(String token, String sender, String receiver, String content , Callback callback) {
    executorService.execute(() -> {
        WebResult<Map<String, Object>> result = performSaveChatInfo(token, sender, receiver, content);
        // 切换到主线程处理结果
        new Handler(Looper.getMainLooper()).post(() -> {
            if (callback != null) {
                try {
                    callback.onResult(result);
                }catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    });
    }
    private static WebResult<Map<String, Object>> performSaveChatInfo(String token, String sender, String receiver, String content) {
        // 使用 String.format 代替字符串拼接
        String urlString = String.format("%s/addmessage", url);
        // 将数据封装为json
        String jsonInputString = "{\"sender\":\"" + sender + "\",\"receiver\":\"" + receiver + "\",\"content\":\"" + content + "\"}";


        // 调用封装的 postConnect 方法
        String response = Connect.postConnect(urlString, jsonInputString ,token);
        System.out.println("Response: " + response);

        // 处理响应结果
        WebResult<Map<String, Object>> result;
        try {
            // 创建 JsonParser 实例解析响应
            JsonUtil parser = new JsonUtil(response);
            result = parser.getWebResult();
            // 从data中获取friends列表
            if (result.getCode() == 0) {
                result = WebResult.success(null);
                result.setCode(200);
                result.setMessage("保存聊天信息成功");
                result.setData(null);
                return result;  // 返回成功的WebResult
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return WebResult.error(null); // 处理解析异常
    }
    // 获取聊天信息
    public static void getChatInfo(String token, String sender, String receiver, Callback callback) {
        executorService.execute(() -> {
            WebResult<Map<String, Object>> result = performGetChatInfo(token, sender, receiver);
            // 切换到主线程处理结果
            new Handler(Looper.getMainLooper()).post(() -> {
                if (callback != null) {
                    try {
                        callback.onResult(result);
                    }catch (Exception e){
                        throw new RuntimeException(e);
                    }
                }
            });
        });
    }
    private static WebResult<Map<String, Object>> performGetChatInfo(String token, String sender, String receiver) {
        // 使用 String.format 代替字符串拼接
        String urlString = String.format("%s/getmessage/%s/%s", url, sender, receiver);

        // 调用封装的 getConnect 方法
        String response = Connect.getConnect(urlString, token);
        System.out.println("Response: " + response);

        // 处理响应结果
        WebResult<Map<String, Object>> result;

        try {
            // 直接解析JSON响应，获取data部分
            JsonUtil parser = new JsonUtil(response);
            Map<String, Object> responseData = parser.parseToMap(response);

            // 获取"data"部分，提取出messages
            Map<String, Object> data = (Map<String, Object>) responseData.get("data");
            Map<String, Object> messages = (Map<String, Object>) data.get("messages");

            if (messages != null) {
                // 提取发送者的消息列表
                List<Map<String, Object>> senderMessages = (List<Map<String, Object>>) messages.get(sender);

                // 提取接收者的消息列表
                List<Map<String, Object>> receiverMessages = (List<Map<String, Object>>) messages.get(receiver);

                // 保存消息列表
                data.put("senderMessages", senderMessages);  // 保存发送者的消息列表
                data.put("receiverMessages", receiverMessages);  // 保存接收者的消息列表
            }

            // 返回成功的WebResult
            result = WebResult.success(null);
            result.setCode(200);
            result.setMessage("获取聊天信息成功");

            // 将更新后的data设置回WebResult
            result.setData(data);

            return result;  // 返回成功的WebResult
        } catch (Exception e) {
            e.printStackTrace();
            return WebResult.error(null);  // 处理解析异常
        }
    }
    // 获取用户信息
    public static void getUserInfo(String token, String userId, Callback callback) {
        executorService.execute(() -> {
            WebResult<Map<String, Object>> result = performGetUserInfo(token, userId);
            // 切换到主线程处理结果
            new Handler(Looper.getMainLooper()).post(() -> {
                if (callback != null) {
                    try {
                        callback.onResult(result);
                    }catch (Exception e){
                        throw new RuntimeException(e);
                    }
                }
            });
        });
    }

    private static WebResult<Map<String, Object>> performGetUserInfo(String token, String userId){
        // 使用 String.format 代替字符串拼接
        String urlString = String.format("%s/getuser/%s", url, userId);
        // 调用封装的 getConnect 方法
        String response = Connect.getConnect(urlString, token);
        System.out.println("Response: " + response);

        // 处理响应结果
        WebResult<Map<String, Object>> result;
        try {
            // 直接解析JSON响应，获取data部分
            JsonUtil parser = new JsonUtil(response);
            Map<String, Object> responseData = parser.parseToMap(response);
            // 获取"data"部分
            Map<String, Object> data = (Map<String, Object>) responseData.get("data");
            // 返回成功的WebResult
            result = WebResult.success(null);
            result.setCode(200);
            result.setMessage("获取用户信息成功");
            // 将更新后的data设置回WebResult
            result.setData(data);
            return result;  // 返回成功的WebResult
        }catch (Exception e){
            e.printStackTrace();
        }
        return WebResult.error(null);  // 处理解析异常
    }
    // 获取特定的好友列表
    public static void getSpaFriendList(String token, String username, Callback callback) {
        executorService.execute(() -> {
            WebResult<Map<String, Object>> result = performGetSpaFriendList(token, username);
            // 切换到主线程处理结果
            new Handler(Looper.getMainLooper()).post(() -> {
                if (callback != null) {
                    try {
                        callback.onResult(result);
                    }catch (Exception e){
                        throw new RuntimeException(e);
                    }
                }
            });
        });
    }

    private static WebResult<Map<String, Object>> performGetSpaFriendList(String token, String username) {
        // 使用 String.format 代替字符串拼接
        String urlString = String.format("%s/getuserandmessage/%s", url, username);

        // 调用封装的 getConnect 方法
        String response = Connect.getConnect(urlString, token);
        System.out.println("Response: " + response);

        // 处理响应结果
        WebResult<Map<String, Object>> result;
        try {
            // 直接使用 JsonUtil 解析 JSON 响应
            JsonUtil parser = new JsonUtil(response);
            WebResult<Map<String, Object>> webResult = parser.getWebResult();

            // 获取 "data" 部分
            Map<String, Object> data = (Map<String, Object>) webResult.getData();

            // 设置成功返回结果
            result = WebResult.success(null);
            result.setCode(200);
            result.setMessage("获取特定好友列表成功");

            // 将更新后的 data 设置回 WebResult
            result.setData(data);
            return result;

        } catch (Exception e) {
            // 异常处理
            e.printStackTrace();
            result = WebResult.error(null);
            result.setCode(500);
            result.setMessage("服务器解析错误");
            return result;
        }
    }

}
