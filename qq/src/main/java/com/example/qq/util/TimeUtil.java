package com.example.qq.util;

import android.annotation.SuppressLint;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TimeUtil {

    // 统一时间格式解析方法
    public static Date parseTime(String timeString) {
        // 优先解析 ISO 8601 格式带时区
        String iso8601Regex = "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}(\\.\\d+)?([+-]\\d{2}:\\d{2}|Z)$";
        Pattern pattern = Pattern.compile(iso8601Regex);
        Matcher matcher = pattern.matcher(timeString);

        if (matcher.matches()) {
            // 解析带时区的 ISO 8601 格式
            return parseISO8601(timeString);
        } else {
            // 处理其他时间格式
            return parseDefaultTimeFormat(timeString);
        }
    }

    // 解析 ISO 8601 格式时间字符串（带时区）
    public static Date parseISO8601(String timeString) {
        try {
            // 使用 DateTimeFormatter 解析带时区的 ISO 8601 格式时间字符串
            SimpleDateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
            return iso8601Format.parse(timeString);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;  // 解析失败返回 null
        }
    }

    // 处理其他常见的时间格式
    private static Date parseDefaultTimeFormat(String timeString) {
        try {
            // 比如处理不带时区的时间格式
            SimpleDateFormat defaultFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            return defaultFormat.parse(timeString);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;  // 解析失败返回 null
        }
    }

    // 格式化时间为指定的格式
    public static String formatTime(Date date) {
        if (date == null) return "格式错误";
        @SuppressLint("SimpleDateFormat") SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
        return timeFormat.format(date);  // 格式化为 "HH:mm"
    }
}
