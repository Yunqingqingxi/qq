package com.example.qq.utils;

import android.util.Log;

import com.example.qq.domain.ChatMessage;
import com.example.qq.domain.NotificationMessage;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * JSON解析工具类
 * 提供JSON数据的序列化和反序列化功能，包括：
 * - 创建各类消息的JSON字符串
 * - 解析各类消息为对象
 * - JSON与Map的相互转换
 * - 通用的JSON操作方法
 */
public class JsonParser {
    private static final String TAG = "JsonParser";

    /**
     * 创建聊天消息的JSON字符串
     * @param sender 发送者用户名
     * @param receiver 接收者用户名
     * @param content 消息内容
     * @return 格式化的JSON字符串，失败返回null
     */
    public static String createChatMessage(String sender, String receiver, String content) {
        try {
            JSONObject json = new JSONObject();
            json.put("type", "chat");
            json.put("sender", sender);
            json.put("receiver", receiver);
            json.put("content", content);
            json.put("timestamp", System.currentTimeMillis());
            return json.toString();
        } catch (JSONException e) {
            Log.e(TAG, "Error creating chat message", e);
            return null;
        }
    }

    /**
     * 创建通知消息的JSON字符串
     * @param title 通知标题
     * @param content 通知内容
     * @return 格式化的JSON字符串，失败返回null
     */
    public static String createNotificationMessage(String title, String content) {
        try {
            JSONObject json = new JSONObject();
            json.put("type", "notification");
            json.put("title", title);
            json.put("content", content);
            json.put("timestamp", System.currentTimeMillis());
            return json.toString();
        } catch (JSONException e) {
            Log.e(TAG, "Error creating notification message", e);
            return null;
        }
    }

    /**
     * 解析消息类型
     * @param jsonStr JSON格式的消息字符串
     * @return 消息类型字符串，解析失败返回"unknown"
     */
    public static String parseMessageType(String jsonStr) {
        try {
            JSONObject json = new JSONObject(jsonStr);
            return json.optString("type", "unknown");
        } catch (JSONException e) {
            Log.e(TAG, "Error parsing message type", e);
            return "unknown";
        }
    }

    /**
     * 解析聊天消息
     * @param jsonStr JSON格式的消息字符串
     * @return ChatMessage对象，如果不是聊天消息或解析失败返回null
     */
    public static ChatMessage parseChatMessage(String jsonStr) {
        try {
            JSONObject json = new JSONObject(jsonStr);
            if (!"chat".equals(json.optString("type"))) {
                return null;
            }

            return new ChatMessage(
                json.optString("sender"),
                json.optString("receiver"),
                json.optString("content"),
                json.optLong("timestamp")
            );
        } catch (JSONException e) {
            Log.e(TAG, "Error parsing chat message", e);
            return null;
        }
    }

    /**
     * 解析通知消息
     * @param jsonStr JSON格式的消息字符串
     * @return NotificationMessage对象，如果不是通知消息或解析失败返回null
     */
    public static NotificationMessage parseNotificationMessage(String jsonStr) {
        try {
            JSONObject json = new JSONObject(jsonStr);
            if (!"notification".equals(json.optString("type"))) {
                return null;
            }

            return new NotificationMessage(
                json.optString("title"),
                json.optString("content"),
                json.optLong("timestamp")
            );
        } catch (JSONException e) {
            Log.e(TAG, "Error parsing notification message", e);
            return null;
        }
    }

    /**
     * 创建空的JSON对象
     * @return 新的JSONObject实例
     */
    public static JSONObject createJson() {
        return new JSONObject();
    }

    /**
     * 解析JSON字符串为JSONObject对象
     * @param jsonStr 要解析的JSON字符串
     * @return JSONObject对象，解析失败返回null
     */
    public static JSONObject parseJson(String jsonStr) {
        try {
            return new JSONObject(jsonStr);
        } catch (JSONException e) {
            Log.e(TAG, "Error parsing JSON string", e);
            return null;
        }
    }

    /**
     * 向JSON对象添加键值对
     * @param json 目标JSON对象
     * @param key 键名
     * @param value 值
     */
    public static void putValue(JSONObject json, String key, Object value) {
        try {
            json.put(key, value);
        } catch (JSONException e) {
            Log.e(TAG, "Error putting value to JSON", e);
        }
    }

    /**
     * 从JSON对象获取字符串值
     * @param json JSON对象
     * @param key 键名
     * @return 字符串值，如果不存在返回空字符串
     */
    public static String getString(JSONObject json, String key) {
        return json.optString(key);
    }

    /**
     * 从JSON对象获取整数值
     * @param json JSON对象
     * @param key 键名
     * @return 整数值，如果不存在返回0
     */
    public static int getInt(JSONObject json, String key) {
        return json.optInt(key);
    }

    /**
     * 从JSON对象获取长整数值
     * @param json JSON对象
     * @param key 键名
     * @return 长整数值，如果不存在返回0
     */
    public static long getLong(JSONObject json, String key) {
        return json.optLong(key);
    }

    /**
     * 从JSON对象获取布尔值
     * @param json JSON对象
     * @param key 键名
     * @return 布尔值，如果不存在返回false
     */
    public static boolean getBoolean(JSONObject json, String key) {
        return json.optBoolean(key);
    }

    /**
     * 将JSON字符串解析为Map对象
     * @param jsonStr JSON字符串
     * @return Map对象，解析失败返回null
     */
    public static Map<String, Object> parseToMap(String jsonStr) {
        try {
            JSONObject json = new JSONObject(jsonStr);
            return jsonObjectToMap(json);
        } catch (JSONException e) {
            Log.e(TAG, "Error parsing JSON to Map", e);
            return null;
        }
    }

    /**
     * 将JSONObject对象转换为Map
     * @param json JSONObject对象
     * @return 转换后的Map对象
     */
    private static Map<String, Object> jsonObjectToMap(JSONObject json) {
        Map<String, Object> map = new HashMap<>();
        Iterator<String> keys = json.keys();
        
        while (keys.hasNext()) {
            String key = keys.next();
            Object value = json.opt(key);
            
            // 处理嵌套的JSONObject
            if (value instanceof JSONObject) {
                value = jsonObjectToMap((JSONObject) value);
            }
            
            map.put(key, value);
        }
        
        return map;
    }

    /**
     * 将Map对象转换为JSONObject
     * @param map 要转换的Map对象
     * @return JSONObject对象，转换失败返回null
     */
    public static JSONObject parseToJson(Map<String, Object> map) {
        try {
            JSONObject json = new JSONObject();
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                
                // 处理嵌套的Map
                if (value instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> nestedMap = (Map<String, Object>) value;
                    value = parseToJson(nestedMap);
                }
                
                json.put(key, value);
            }
            return json;
        } catch (JSONException e) {
            Log.e(TAG, "Error converting Map to JSONObject", e);
            return null;
        }
    }

    /**
     * 将Map对象安全地转换为JSONObject
     * @param map 要转换的Map对象
     * @return JSONObject对象，转换失败返回空的JSONObject
     */
    public static JSONObject parseToJsonSafe(Map<String, Object> map) {
        JSONObject result = parseToJson(map);
        return result != null ? result : new JSONObject();
    }

    /**
     * 创建登录请求的JSON字符串
     * @param username 用户名
     * @param password 密码
     * @return 登录请求的JSON字符串，失败返回null
     */
    public static String createLoginJson(String username, String password) {
        try {
            JSONObject json = new JSONObject();
            json.put("username", username);
            json.put("password", password);
            return json.toString();
        } catch (JSONException e) {
            Log.e(TAG, "Error creating login JSON", e);
            return null;
        }
    }

    /**
     * 创建注册请求的JSON字符串
     * @param username 用户名
     * @param password 密码
     * @param email 邮箱地址
     * @param nickname 用户昵称
     * @return 注册请求的JSON字符串，失败返回null
     */
    public static String createRegisterJson(String username, String password, String email, String nickname) {
        try {
            JSONObject json = new JSONObject();
            json.put("username", username);
            json.put("password", password);
            json.put("email", email);
            json.put("nickname", nickname);
            return json.toString();
        } catch (JSONException e) {
            Log.e(TAG, "Error creating register JSON", e);
            return null;
        }
    }

    /**
     * 解析登录/注册响应
     * @param jsonStr 响应的JSON字符串
     * @return 包含响应信息的Map对象，包含code(状态码)、message(消息)和data(数据，可选)字段，解析失败返回null
     */
    public static Map<String, Object> parseAuthResponse(String jsonStr) {
        try {
            Map<String, Object> resultMap = new HashMap<>();
            JSONObject json = new JSONObject(jsonStr);
            
            // 解析状态码
            int code = json.optInt("code", -1);
            resultMap.put("code", code);
            
            // 解析消息
            String message = json.optString("message", "");
            resultMap.put("message", message);
            
            // 如果有data字段，解析data中的信息
            if (json.has("data")) {
                JSONObject data = json.getJSONObject("data");
                Map<String, Object> dataMap = jsonObjectToMap(data);
                resultMap.put("data", dataMap);
            }
            
            return resultMap;
        } catch (JSONException e) {
            Log.e(TAG, "Error parsing auth response", e);
            return null;
        }
    }
}
