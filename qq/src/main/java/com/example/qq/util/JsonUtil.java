package com.example.qq.util;

import com.example.qq.websocket.webResult.WebResult;
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
            }

            // 创建 WebResult 实例
            this.webResult = new WebResult<>(code, message, dataMap);
        } catch (Exception e) {
            throw new RuntimeException("JSON 解析失败: " + e.getMessage(), e);
        }
    }

    // 解析格式为：{"system":(int),"user":"(String)","targetName":"(String)","message":"(String)"}
    public static Map<String, Object> parseMessage(String json) {
        Map<String, Object> map = new HashMap<>();
        try {
            JSONObject jsonObject = new JSONObject(json);
            if(!jsonObject.getString("system").equals("0")){
                map.put("system", jsonObject.getInt("system"));
                map.put("user", jsonObject.getString("user"));
                map.put("message", jsonObject.getString("message"));
                return map;
            }
            return map;

        } catch (Exception e) {
            throw new RuntimeException("消息已发送",e);
        }
    }

    public WebResult<Map<String, Object>> getWebResult() {
        return webResult;
    }
}
