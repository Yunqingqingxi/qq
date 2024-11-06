package com.example.qq.util;

import android.util.Log;

import com.example.qq.websocket.webResult.WebResult;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * JSON 解析工具类
 */
public class JsonUtil {
    private WebResult<Map<String, Object>> webResult;

    public JsonUtil(String json) {
        parseJson(json);
    }

    // 解析格式为：{"code":xxx,"msg":"xxx", "data":{...}}
    private void parseJson(String json) {
        try {
            JSONObject jsonObject = new JSONObject(json);
            Integer code = jsonObject.getInt("code");
            String message = jsonObject.getString("msg");

            // 处理 data 字段
            Map<String, Object> dataMap = new HashMap<>();
            if (!jsonObject.isNull("data")) {
                JSONObject dataObject = jsonObject.getJSONObject("data");
                // 将 token 提取到 dataMap 中
                if (dataObject.has("token")) {
                    dataMap.put("token", dataObject.getString("token"));
                }
                // 将其他数据提取到 dataMap 中 , 例如data":{"friends":[{"id":20,"userId":1,"friendId":2},{"id":25,"userId":1,"friendId":123456}]}
                if (dataObject.has("friends")) {
                    dataMap.put("friends", dataObject.getJSONArray("friends"));
                }
            }

            // 创建 WebResult 实例
            this.webResult = new WebResult<>(code, message, dataMap);
        } catch (Exception e) {
            throw new RuntimeException("JSON 解析失败: " + e.getMessage(), e);
        }
    }

    public static Map<String, Object> parseMessage(String json) {
        Map<String, Object> map = new HashMap<>();
        try {
            JSONObject jsonObject = new JSONObject(json);

            // 解析 system 字段来判断是否是系统消息
            int systemFlag = jsonObject.getInt("system");
            if (systemFlag == 0) {
                // 处理系统消息
                map.put("system", systemFlag);
//                map.put("user", jsonObject.getString("user"));
                map.put("message", jsonObject.getString("message"));
//                map.put("targetName", jsonObject.getString("targetName"));
//                map.put("isSystemMessage", true);  // 标记为系统消息
                return map;
            } else {
                // 非系统消息，返回常规消息
                map.put("system", systemFlag);
                map.put("user", jsonObject.getString("user"));
                map.put("message", jsonObject.getString("message"));
                map.put("targetname", jsonObject.getString("targetname"));
//                map.put("isSystemMessage", false); // 标记为普通消息
                return map;
            }
        } catch (Exception e) {
            // 在捕获异常时，避免直接抛出异常，返回 null 或记录错误日志
            Log.e("JsonParser", "Failed to parse message: " + json, e);
            return null; // 如果解析失败，返回 null
        }
    }
    // 解析 JSON 字符串并返回 Map
    public Map<String, Object> parseToMap(String jsonString) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        // 使用 ObjectMapper 将 JSON 字符串转换为 Map
        return objectMapper.readValue(jsonString, Map.class);
    }

    public WebResult<Map<String, Object>> getWebResult() {
        return webResult;
    }
}
