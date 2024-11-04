package com.example.qq.websocket.webUtils.controller;

import java.util.Map;

public interface MessageFilter {
    boolean shouldProcessMessage(Map<String, Object> message);
}
