package com.example.qq.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.webkit.MimeTypeMap;

import androidx.activity.result.ActivityResultLauncher;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * 图片上传工具类
 * 处理图片选择、拍照和上传相关的功能
 */
public class ImageUploadUtils {
    private static final String TAG = "ImageUploadUtils";
    public static final int MAX_IMAGE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final String[] SUPPORTED_MIME_TYPES = {"image/jpeg", "image/png", "image/jpg"};

    /**
     * 打开图片选择器
     * @param activity 当前活动
     * @param launcher ActivityResultLauncher用于处理选择结果
     */
    @SuppressLint("IntentReset")
    public static void openImagePicker(Activity activity, ActivityResultLauncher<Intent> launcher) {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        launcher.launch(intent);
    }

    /**
     * 创建拍照意图
     * @param activity 当前活动
     * @param photoUri 保存照片的URI
     * @return 拍照意图
     */
    public static Intent createCameraIntent(Activity activity, Uri photoUri) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (photoUri != null) {
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
        }
        return intent;
    }

    /**
     * 创建临时图片文件
     * @param activity 当前活动
     * @return 临时文件的URI
     */
    public static Uri createTempImageFile(Activity activity) {
        try {
            String timeStamp = new SimpleDateFormat("yyyy:MMdd:HHmmss", Locale.getDefault()).format(new Date());
            String imageFileName = "JPEG_" + timeStamp + "_";
            File storageDir = activity.getExternalCacheDir();
            File imageFile = File.createTempFile(imageFileName, ".jpg", storageDir);
            return FileProvider.getUriForFile(activity,
                    activity.getPackageName() + ".fileprovider",
                    imageFile);
        } catch (IOException e) {
            Log.e(TAG, "Error creating image file", e);
            return null;
        }
    }

    /**
     * 检查文件类型是否支持
     * @param uri 文件URI
     * @param activity 当前活动
     * @return 是否支持该文件类型
     */
    public static boolean isSupportedFileType(Uri uri, Activity activity) {
        String mimeType = activity.getContentResolver().getType(uri);
        if (mimeType == null) {
            String extension = MimeTypeMap.getFileExtensionFromUrl(uri.toString());
            mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }

        for (String supportedType : SUPPORTED_MIME_TYPES) {
            if (supportedType.equals(mimeType)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 检查文件大小是否在限制范围内
     * @param uri 文件URI
     * @param activity 当前活动
     * @return 是否在限制范围内
     */
    public static boolean isFileSizeValid(Uri uri, Activity activity) {
        try {
            InputStream inputStream = activity.getContentResolver().openInputStream(uri);
            if (inputStream != null) {
                int fileSize = inputStream.available();
                inputStream.close();
                return fileSize <= MAX_IMAGE_SIZE;
            }
        } catch (IOException e) {
            Log.e(TAG, "Error checking file size", e);
        }
        return false;
    }

    /**
     * 压缩图片文件
     * @param sourceUri 源文件URI
     * @param activity 当前活动
     * @return 压缩后的文件URI
     */
    public static Uri compressImage(Uri sourceUri, Activity activity) {
        try {
            InputStream inputStream = activity.getContentResolver().openInputStream(sourceUri);
            if (inputStream == null) return null;

            File compressedFile = new File(activity.getCacheDir(), "compressed_image.jpg");
            FileOutputStream outputStream = new FileOutputStream(compressedFile);

            // TODO: 实现图片压缩逻辑
            // 这里可以使用 Bitmap 或其他图片处理库进行压缩

            outputStream.close();
            inputStream.close();

            return FileProvider.getUriForFile(activity,
                    activity.getPackageName() + ".fileprovider",
                    compressedFile);
        } catch (IOException e) {
            Log.e(TAG, "Error compressing image", e);
            return null;
        }
    }

    /**
     * 处理选择的图片
     */
    public static void handleSelectedImage(Uri uri, Activity activity, ImageUploadCallback callback) {
        Log.d("ImageUploadUtils", "Processing image: " + uri);
        
        if (!isSupportedFileType(uri, activity)) {
            Log.e("ImageUploadUtils", "Unsupported file type");
            callback.onError("不支持的文件类型");
            return;
        }

        if (!isFileSizeValid(uri, activity)) {
            Log.e("ImageUploadUtils", "File size exceeds limit");
            callback.onError("文件大小超过限制");
            return;
        }

        try {
            // 创建临时文件
            File tempFile = createTempFileFromUri(activity, uri);
            if (tempFile != null) {
                Uri tempUri = FileProvider.getUriForFile(activity,
                    activity.getPackageName() + ".fileprovider",
                    tempFile);
                Log.d("ImageUploadUtils", "Created temp file: " + tempUri);
                callback.onSuccess(tempUri);
            } else {
                Log.e("ImageUploadUtils", "Failed to create temp file");
                callback.onError("图片处理失败");
            }
        } catch (Exception e) {
            Log.e("ImageUploadUtils", "Error processing image", e);
            callback.onError("处理图片时出错：" + e.getMessage());
        }
    }

    /**
     * 从Uri创建临时文件
     */
    private static File createTempFileFromUri(Activity activity, Uri uri) throws IOException {
        InputStream inputStream = activity.getContentResolver().openInputStream(uri);
        if (inputStream == null) return null;

        File tempFile = new File(activity.getCacheDir(), "temp_" + System.currentTimeMillis() + ".jpg");
        FileOutputStream outputStream = new FileOutputStream(tempFile);

        byte[] buffer = new byte[4096];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
        }

        outputStream.close();
        inputStream.close();
        return tempFile;
    }

    /**
     * 图片上传结果回调接口
     */
    public interface ImageUploadCallback {
        void onSuccess(Uri imageUri);
        void onError(String message);
    }
} 