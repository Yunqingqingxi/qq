package com.example.qq.utils;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class Debouncer {
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final ConcurrentHashMap<Object, ScheduledFuture<?>> delayedMap = new ConcurrentHashMap<>();

    /**
     * 执行防抖操作
     * @param key 防抖的键值，用于区分不同的防抖操作
     * @param runnable 需要执行的任务
     * @param delay 延迟时间
     * @param unit 时间单位
     */
    public void debounce(Object key, Runnable runnable, long delay, TimeUnit unit) {
        ScheduledFuture<?> previous = delayedMap.get(key);
        if (previous != null) {
            previous.cancel(true);
        }

        ScheduledFuture<?> future = scheduler.schedule(() -> {
            try {
                runnable.run();
            } finally {
                delayedMap.remove(key);
            }
        }, delay, unit);

        delayedMap.put(key, future);
    }

    /**
     * 关闭防抖器，释放资源
     */
    public void shutdown() {
        scheduler.shutdown();
    }
} 