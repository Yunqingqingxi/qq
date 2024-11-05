package com.example.qq.websocket.webUtils.controller;

import com.example.qq.websocket.webResult.WebResult;

import org.json.JSONException;

import java.util.Map;

public interface Callback {
    void onResult(WebResult<Map<String, Object>> result) throws JSONException;
}