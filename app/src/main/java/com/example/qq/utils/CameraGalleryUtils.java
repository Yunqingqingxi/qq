package com.example.qq.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

/**
 * 相机和图库工具类
 * 处理相机拍照和图库选择相关的功能
 */
public class CameraGalleryUtils {
    public static final int CAMERA_PERMISSION_REQUEST_CODE = 1002;
    public static final int GALLERY_PERMISSION_REQUEST_CODE = 1003;

    /**
     * 检查相机权限并启动相机
     */
    public static void checkCameraPermissionAndCapture(Activity activity, Uri photoUri, 
            ActivityResultLauncher<Intent> cameraLauncher) {
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA) 
                != PackageManager.PERMISSION_GRANTED) {
            // 请求相机权限
            ActivityCompat.requestPermissions(activity,
                new String[]{Manifest.permission.CAMERA},
                CAMERA_PERMISSION_REQUEST_CODE);
        } else {
            startCamera(activity, photoUri, cameraLauncher);
        }
    }

    /**
     * 检查存储权限并打开图库
     */
    public static void checkGalleryPermissionAndPick(Activity activity, 
            ActivityResultLauncher<Intent> galleryLauncher) {
        String[] requiredPermissions;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requiredPermissions = new String[]{
                Manifest.permission.READ_MEDIA_IMAGES
            };
        } else {
            requiredPermissions = new String[]{
                Manifest.permission.READ_EXTERNAL_STORAGE
            };
        }

        boolean allPermissionsGranted = true;
        for (String permission : requiredPermissions) {
            if (ContextCompat.checkSelfPermission(activity, permission) 
                    != PackageManager.PERMISSION_GRANTED) {
                allPermissionsGranted = false;
                break;
            }
        }

        if (allPermissionsGranted) {
            openGallery(activity, galleryLauncher);
        } else {
            ActivityCompat.requestPermissions(activity, requiredPermissions, 
                GALLERY_PERMISSION_REQUEST_CODE);
        }
    }

    /**
     * 启动相机
     */
    public static void startCamera(Activity activity, Uri photoUri, 
            ActivityResultLauncher<Intent> cameraLauncher) {
        try {
            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
            cameraLauncher.launch(cameraIntent);
        } catch (Exception e) {
            Toast.makeText(activity, "启动相机失败", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 打开图库
     */
    public static void openGallery(Activity activity, ActivityResultLauncher<Intent> galleryLauncher) {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        galleryLauncher.launch(intent);
    }

    /**
     * 处理权限请求结果
     */
    public static void handlePermissionResult(Activity activity, int requestCode, 
            String[] permissions, int[] grantResults,
            ActivityResultLauncher<Intent> cameraLauncher,
            ActivityResultLauncher<Intent> galleryLauncher,
            Uri photoUri) {
        
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera(activity, photoUri, cameraLauncher);
            } else {
                Toast.makeText(activity, "需要相机权限才能拍照", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == GALLERY_PERMISSION_REQUEST_CODE) {
            if (areAllPermissionsGranted(grantResults)) {
                openGallery(activity, galleryLauncher);
            } else {
                Toast.makeText(activity, "需要存储权限才能选择图片", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * 检查是否所有权限都已授予
     */
    private static boolean areAllPermissionsGranted(int[] grantResults) {
        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }
} 