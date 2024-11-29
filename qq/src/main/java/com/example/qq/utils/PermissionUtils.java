package com.example.qq.utils;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

/**
 * 权限工具类
 * 用于处理Android运行时权限的请求和检查
 */
public class PermissionUtils {
    /** 日志标签 */
    private static final String TAG = "PermissionUtils";
    /** 权限请求码 */
    public static final int PERMISSION_REQUEST_CODE = 1001;
    
    /** 
     * 必需权限列表
     * 应用核心功能需要的权限
     */
    private static final String[] ESSENTIAL_PERMISSIONS = {
        "android.permission.CAMERA",
        "android.permission.READ_PHONE_STATE"
    };
    
    /**
     * 媒体权限列表
     * Android 13及以上版本需要的媒体访问权限
     */
    private static final String[] MEDIA_PERMISSIONS = {
        "android.permission.READ_MEDIA_IMAGES",
        "android.permission.READ_MEDIA_VIDEO",
        "android.permission.READ_MEDIA_AUDIO"
    };
    
    /**
     * 可选权限列表
     * 非核心功能需要的权限
     */
    private static final String[] OPTIONAL_PERMISSIONS = {
        "android.permission.POST_NOTIFICATIONS",
        "android.permission.FOREGROUND_SERVICE",
        "android.permission.FOREGROUND_SERVICE_MEDIA_PLAYBACK"
    };

    /**
     * 检查并请求所需权限
     * 
     * @param activity 当前活动
     * @return 如果所有权限都已授予返回true，否则返回false
     */
    public static boolean checkAndRequestPermissions(Activity activity) {
        List<String> permissionsNeeded = new ArrayList<>();
        
        // 检查必需权限
        for (String permission : ESSENTIAL_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(activity, permission) 
                    != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(permission);
            }
        }
        
        // 检查媒体权限（Android 13及以上）
        if (Build.VERSION.SDK_INT >= 33) {
            for (String permission : MEDIA_PERMISSIONS) {
                if (ContextCompat.checkSelfPermission(activity, permission) 
                        != PackageManager.PERMISSION_GRANTED) {
                    permissionsNeeded.add(permission);
                }
            }
        }
        
        // 检查可选权限
        for (String permission : OPTIONAL_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(activity, permission) 
                    != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(permission);
            }
        }

        if (!permissionsNeeded.isEmpty()) {
            Log.d(TAG, "Requesting permissions: " + permissionsNeeded);
            ActivityCompat.requestPermissions(activity,
                    permissionsNeeded.toArray(new String[0]),
                    PERMISSION_REQUEST_CODE);
            return false;
        }
        return true;
    }

    /**
     * 检查必需权限是否已全部授予
     * 
     * @param activity 当前活动
     * @return 如果所有必需权限都已授予返回true，否则返回false
     */
    public static boolean areEssentialPermissionsGranted(Activity activity) {
        for (String permission : ESSENTIAL_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(activity, permission) 
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    /**
     * 判断指定权限是否为必需权限
     * 
     * @param permission 要检查的权限
     * @return 如果是必需权限返回true，否则返回false
     */
    public static boolean isEssentialPermission(String permission) {
        for (String essentialPermission : ESSENTIAL_PERMISSIONS) {
            if (essentialPermission.equals(permission)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 记录权限请求结果
     * 
     * @param permissions 请求的权限数组
     * @param grantResults 权限授予结果数组
     */
    public static void logPermissionResults(String[] permissions, int[] grantResults) {
        for (int i = 0; i < permissions.length; i++) {
            String permissionName = permissions[i].substring(permissions[i].lastIndexOf(".") + 1);
            String result = grantResults[i] == PackageManager.PERMISSION_GRANTED ? "GRANTED" : "DENIED";
            Log.d(TAG, "Permission result - " + permissionName + ": " + result);
        }
    }

    /**
     * 检查所有请求的权限是否都已授予
     * 
     * @param grantResults 权限授予结果数组
     * @return 如果所有权限都已授予返回true，否则返回false
     */
    public static boolean areAllPermissionsGranted(int[] grantResults) {
        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Found denied permission");
                return false;
            }
        }
        return true;
    }
} 