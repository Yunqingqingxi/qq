package com.example.qq.api.messageapi.impl;

// Android 框架
import android.util.Log;

// 应用内部类
import com.example.qq.api.messageapi.MessageApi;
import com.example.qq.domain.ChatMessage;
import com.example.qq.network.RequestManager;
import com.example.qq.utils.JsonParser;

// Java 标准库
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

// JSON 相关
import org.json.JSONException;

// 静态导入
import static com.example.qq.network.RequestManager.get;
import static com.example.qq.network.RequestManager.post;
import static com.example.qq.utils.JsonParser.parseToMap;

/**
 * 消息API接口实现类
 * 实现消息的发送和获取功能，包括：
 * - 获取历史聊天记录
 * - 发送新消息到服务器
 * - 解析和处理消息数据
 * 
 * @author yunxi
 * @version 1.0
 */
public class MessageApiImpl implements MessageApi {
    private static final String TAG = "MessageApiImpl";

    /**
     * 获取消息列表
     * 从服务器获取两个用户之间的聊天记录
     *
     * @param currentUsername 当前用户名
     * @param friendUsername 好友用户名
     * @return 按时间排序的聊天消息列表
     * @throws Exception 当网络请求失败或数据解析错误时抛出异常
     */
    @Override
    public List<ChatMessage> getMessageList(String currentUsername, String friendUsername) {
        try {
            Log.d(TAG, "开始获取消息列表");
            List<ChatMessage> chatMessages = new ArrayList<>();
            
            // 获取消息数据
            String response = fetchMessagesFromServer(currentUsername, friendUsername);
            Map<String,Object> map = parseResponse(response);
            if (map == null) return chatMessages;

            // 解析消息数据
            parseMessageData(map, chatMessages);
            
            // 对消息进行时间排序
            sortMessagesByTime(chatMessages);
            
            Log.d(TAG, "成功获取消息列表，共 " + chatMessages.size() + " 条消息");
            return chatMessages;
            
        } catch (Exception e) {
            Log.e(TAG, "获取消息列表失败", e);
            throw e;
        }
    }

    /**
     * 从服务器获取消息数据
     */
    private String fetchMessagesFromServer(String currentUsername, String friendUsername) {
        Log.d(TAG, "请求URL: /getmessage/" + currentUsername + "/" + friendUsername);
        String response = get("/getmessage/" + currentUsername + "/" + friendUsername);
        Log.d(TAG, "收到响应: " + response);
        return response;
    }

    /**
     * 解析服务器响应
     */
    private Map<String,Object> parseResponse(String response) {
        Map<String,Object> map = parseToMap(response);
        if (map == null) {
            Log.e(TAG, "解析响应失败：返回数据为空");
            return null;
        }

        // 检查返回码
        Object code = map.get("code");
        if (code == null || !String.valueOf(code).equals("200")) {
            Log.e(TAG, "获取消息失败：" + map.get("msg"));
            return null;
        }

        return map;
    }

    /**
     * 解析消息数据并添加到列表
     */
    private void parseMessageData(Map<String,Object> map, List<ChatMessage> chatMessages) {
        Object data = map.get("data");
        if (data == null) {
            Log.e(TAG, "获取消息失败：data为空");
            return;
        }

        Map<String, Object> dataMap = (Map<String, Object>) data;
        Object messagesObj = dataMap.get("messages");
        if (messagesObj == null || !(messagesObj instanceof Map)) {
            Log.e(TAG, "获取消息失败：messages无效");
            return;
        }

        parseMessageArray((Map<String, Object>) messagesObj, chatMessages);
    }

    /**
     * 解析消息数组
     */
    private void parseMessageArray(Map<String, Object> messagesMap, List<ChatMessage> chatMessages) {
        for (Map.Entry<String, Object> entry : messagesMap.entrySet()) {
            if (entry.getValue() instanceof org.json.JSONArray) {
                org.json.JSONArray messageArray = (org.json.JSONArray) entry.getValue();
                try {
                    for (int i = 0; i < messageArray.length(); i++) {
                        ChatMessage message = parseMessageObject(messageArray.getJSONObject(i));
                        if (message != null) {
                            chatMessages.add(message);
                        }
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "解析消息数据失败：" + e.getMessage(), e);
                }
            }
        }
    }

    /**
     * 解析单个消息对象
     */
    private ChatMessage parseMessageObject(org.json.JSONObject msg) throws JSONException {
        String msgSender = msg.getString("sender");
        String msgReceiver = msg.getString("receiver");
        String content = msg.getString("content");
        String timestampStr = msg.getString("timestamp");
        
        long timestamp = parseTimestamp(timestampStr);
        return new ChatMessage(msgSender, msgReceiver, content, timestamp);
    }

    /**
     * 解析时间戳
     */
    private long parseTimestamp(String timestampStr) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
                .withZone(ZoneOffset.UTC);
            return Instant.from(formatter.parse(timestampStr)).toEpochMilli();
        } catch (Exception e) {
            Log.w(TAG, "时间戳解析失败，使用当前时间: " + e.getMessage());
            return System.currentTimeMillis();
        }
    }

    /**
     * 对消息列表按时间排序
     */
    private void sortMessagesByTime(List<ChatMessage> chatMessages) {
        Collections.sort(chatMessages, 
            (msg1, msg2) -> Long.compare(msg1.getTimestamp(), msg2.getTimestamp()));
    }

    /**
     * 发送消息
     * 将消息发送到服务器
     *
     * @param json 消息的JSON字符串
     * @return 发送是否成功
     */
    @Override
    public boolean sendMessage(String json) {
        Map<String,Object> map = parseToMap(post("/addmessage", json));
        Object code = map.get("code");

        if (code == null || !String.valueOf(code).equals("200")) {
            Log.e(TAG, "发送消息失败：" + map.get("msg"));
            return false;
        }
        return true;
    }
}
