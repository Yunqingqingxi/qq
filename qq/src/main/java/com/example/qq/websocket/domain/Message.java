package com.example.qq.websocket.domain;

import org.json.JSONException;
import org.json.JSONObject;

public class Message {
    private int system; // 0 for system message, 1 for user message
    private String user;
    private String targetName;
    private String message;

    // Constructor for system messages
    public Message(int system, String message) {
        this.system = system;
        this.user = "system"; // default user for system messages
        this.message = message;
    }

    // Constructor for user messages
    public Message(int system, String user, String targetName, String message) {
        this.system = system;
        this.user = user; // current logged-in user
        this.targetName = targetName; // target user
        this.message = message;
    }

    // Convert the message to JSON format
    public JSONObject toJson()  {

        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("system", system);
            jsonObject.put("user", user);
            jsonObject.put("targetname", targetName);
            jsonObject.put("message", message);
            return jsonObject;
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    // Getters and setters (optional, depending on your needs)
    public int getSystem() {
        return system;
    }

    public void setSystem(int system) {
        this.system = system;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getTargetName() {
        return targetName;
    }

    public void setTargetName(String targetName) {
        this.targetName = targetName;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
