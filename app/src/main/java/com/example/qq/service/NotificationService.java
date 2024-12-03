package com.example.qq.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class NotificationService {
    private static NotificationService instance;
    
    public static synchronized NotificationService getInstance() {
        if (instance == null) {
            instance = new NotificationService();
        }
        return instance;
    }
    
    private NotificationService() {
        // 私有构造函数
    }
    
    private final Map<String, Integer> unreadMessageCounts = new ConcurrentHashMap<>();
    private int friendRequestCount = 0;

    public void incrementUnreadCount(String username) {
        unreadMessageCounts.compute(username, (k, v) -> (v == null) ? 1 : v + 1);
    }

    public void setUnreadCount(String username, int count) {
        if (count > 0) {
            unreadMessageCounts.put(username, count);
        } else {
            unreadMessageCounts.remove(username);
        }
    }

    public void clearUnreadCount(String username) {
        unreadMessageCounts.remove(username);
    }

    public int getUnreadCount(String username) {
        return unreadMessageCounts.getOrDefault(username, 0);
    }

    public int getTotalUnreadCount() {
        return unreadMessageCounts.values().stream().mapToInt(Integer::intValue).sum();
    }

    public void setFriendRequestCount(int count) {
        this.friendRequestCount = count;
    }

    public int getFriendRequestCount() {
        return friendRequestCount;
    }

    public void incrementFriendRequestCount() {
        friendRequestCount++;
    }

    public void decrementFriendRequestCount() {
        if (friendRequestCount > 0) {
            friendRequestCount--;
        }
    }
} 