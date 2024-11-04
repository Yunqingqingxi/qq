package com.example.qq.websocket.webUtils.controller;

import com.example.qq.websocket.webResult.WebResult;

import java.util.Map;

public interface Callback {
    void onResult(WebResult<Map<String, Object>> result);
}