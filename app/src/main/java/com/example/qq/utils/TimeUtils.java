package com.example.qq.utils;

import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class TimeUtils {
    private static final String TAG = "TimeUtils";
    
    // 输入格式：处理多种可能的时间格式
    private static final SimpleDateFormat[] INPUT_FORMATS = {
        new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.getDefault()),
        new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()),
        new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
    };
    
    // 输出格式
    private static final SimpleDateFormat TIME_FORMAT = 
        new SimpleDateFormat("HH:mm", Locale.getDefault());
    private static final SimpleDateFormat DATE_TIME_FORMAT = 
        new SimpleDateFormat("MM-dd HH:mm", Locale.getDefault());
    private static final SimpleDateFormat FULL_DATE_FORMAT = 
        new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

    public static String formatTime(String timestamp) {
        if (timestamp == null || timestamp.isEmpty()) {
            return "";
        }

        try {
            Date date = parseDate(timestamp);
            if (date == null) {
                Log.e(TAG, "Failed to parse date: " + timestamp);
                return timestamp;
            }

            Calendar now = Calendar.getInstance();
            Calendar msgTime = Calendar.getInstance();
            msgTime.setTime(date);

            // 今天的消息只显示时间
            if (isSameDay(now, msgTime)) {
                return TIME_FORMAT.format(date);
            }

            // 昨天的消息显示"昨天 HH:mm"
            Calendar yesterday = Calendar.getInstance();
            yesterday.add(Calendar.DAY_OF_YEAR, -1);
            if (isSameDay(yesterday, msgTime)) {
                return "昨天 " + TIME_FORMAT.format(date);
            }

            // 今年的消息显示"MM-dd HH:mm"
            if (now.get(Calendar.YEAR) == msgTime.get(Calendar.YEAR)) {
                return DATE_TIME_FORMAT.format(date);
            }

            // 往年的消息显示完整日期"yyyy-MM-dd HH:mm"
            return FULL_DATE_FORMAT.format(date);

        } catch (Exception e) {
            Log.e(TAG, "Error formatting time: " + e.getMessage());
            return timestamp;
        }
    }

    private static Date parseDate(String timestamp) {
        for (SimpleDateFormat format : INPUT_FORMATS) {
            try {
                return format.parse(timestamp);
            } catch (ParseException e) {
                // 尝试下一个格式
                continue;
            }
        }
        return null;
    }

    private static boolean isSameDay(Calendar cal1, Calendar cal2) {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
               cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }

    public static String getCurrentTime() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            .format(new Date());
    }

    public static String formatTimeForChat(String timestamp) {
        try {
            Date date = parseDate(timestamp);
            if (date == null) return timestamp;

            Calendar now = Calendar.getInstance();
            Calendar msgTime = Calendar.getInstance();
            msgTime.setTime(date);

            // 今天的消息只显示时间
            if (isSameDay(now, msgTime)) {
                return TIME_FORMAT.format(date);
            }

            // 昨天的消息显示"昨天 HH:mm"
            Calendar yesterday = Calendar.getInstance();
            yesterday.add(Calendar.DAY_OF_YEAR, -1);
            if (isSameDay(yesterday, msgTime)) {
                return "昨天 " + TIME_FORMAT.format(date);
            }

            // 一周内的消息显示星期几
            if (isWithinWeek(now, msgTime)) {
                String[] weekDays = {"周日", "周一", "周二", "周三", "周四", "周五", "周六"};
                return weekDays[msgTime.get(Calendar.DAY_OF_WEEK) - 1] + " " + 
                    TIME_FORMAT.format(date);
            }

            // 其他情况显示完整日期
            return DATE_TIME_FORMAT.format(date);
        } catch (Exception e) {
            Log.e(TAG, "Error formatting chat time: " + e.getMessage());
            return timestamp;
        }
    }

    private static boolean isWithinWeek(Calendar now, Calendar msgTime) {
        Calendar weekAgo = (Calendar) now.clone();
        weekAgo.add(Calendar.DAY_OF_YEAR, -7);
        return msgTime.after(weekAgo) && msgTime.before(now);
    }
} 