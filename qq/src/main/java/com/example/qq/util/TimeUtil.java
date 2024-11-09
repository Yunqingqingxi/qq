package com.example.qq.util;

import android.annotation.SuppressLint;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
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
            // 使用 SimpleDateFormat 解析带时区的 ISO 8601 格式时间字符串
            @SuppressLint("SimpleDateFormat") SimpleDateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
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
            @SuppressLint("SimpleDateFormat") SimpleDateFormat defaultFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            return defaultFormat.parse(timeString);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;  // 解析失败返回 null
        }
    }

    // 格式化时间为指定的格式
    public static String formatTime(Date date) {
        if (date == null) return "格式错误";
        @SuppressLint("SimpleDateFormat") SimpleDateFormat timeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return timeFormat.format(date);  // 格式化为 "yyyy-MM-dd HH:mm:ss"
    }

    // 获取当前时间并格式化为指定的格式
    public static String getCurrentTime() {
        @SuppressLint("SimpleDateFormat") SimpleDateFormat timeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return timeFormat.format(new Date());
    }

    public static Date formatToHHMM(Date date) {
        if (date == null) {
            return null;  // 如果日期为null，返回null
        }

        // 使用Calendar来设置时间
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        // 创建一个新的Calendar对象，只设置小时和分钟
        Calendar newCalendar = Calendar.getInstance();
        newCalendar.set(Calendar.HOUR_OF_DAY, hour);
        newCalendar.set(Calendar.MINUTE, minute);
        newCalendar.set(Calendar.SECOND, 0);
        newCalendar.set(Calendar.MILLISECOND, 0);

        return newCalendar.getTime();
    }

    public static Date formatDateToHHMM(Date date) {
        if (date == null) return null;  // 如果日期为null，返回null

        // 使用Calendar来设置时间
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        // 创建一个新的Calendar对象，只设置小时和分钟
        Calendar newCalendar = Calendar.getInstance();
        newCalendar.set(Calendar.HOUR_OF_DAY, hour);
        newCalendar.set(Calendar.MINUTE, minute);
        newCalendar.set(Calendar.SECOND, 0);
        newCalendar.set(Calendar.MILLISECOND, 0);
        newCalendar.set(Calendar.YEAR, 1970);  // 设置一个默认的年份，以确保Date对象的有效性
        newCalendar.set(Calendar.MONTH, Calendar.JANUARY);
        newCalendar.set(Calendar.DAY_OF_MONTH, 1);

        return newCalendar.getTime();
    }


}
