package com.example.qq.websocket.webUtils.controller;

import android.os.Handler;
import android.os.Looper;

import com.example.qq.util.JsonUtil;
import com.example.qq.websocket.webResult.WebResult;
import com.example.qq.websocket.webUtils.Connect;


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
                    callback.onResult(result);
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

    public static void register(String username, String password, Callback callback) {
        executorService.execute(() -> {
            WebResult<Map<String, Object>> result = performRegister(username, password);
            // 切换到主线程处理结果
            new Handler(Looper.getMainLooper()).post(() -> {
                if (callback != null) {
                    callback.onResult(result);
                }
            });
        });
    }

    public static WebResult<Map<String, Object>> performRegister(String username, String password) {
         urlString = url+"/register";
        String jsonInputString = String.format("{\"username\":\"%s\", \"password\":\"%s\"}", username, password);
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
                    callback.onResult(result);
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
    public static void getFriendList(String username,String token, Callback callback) {
        executorService.execute(() -> {
            WebResult<Map<String, Object>> result = performGetFriendList(token,username);
            // 切换到主线程处理结果
            new Handler(Looper.getMainLooper()).post(() -> {
                if (callback != null) {
                    callback.onResult(result);
                }
            });
        });
    }
    private static WebResult<Map<String, Object>> performGetFriendList(String token, String username) {
        // 使用 String.format 代替字符串拼接
        urlString = String.format("%sfriends/%s", url, username);

        // 调用封装的 postConnect 方法
        String response = Connect.getConnect(urlString, token);
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
        }catch (Exception e) {
            e.printStackTrace();
            return WebResult.error(null); // 处理解析异常
        }
    }
}
