package com.example.qq.utils;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;

import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * 文件工具类
 * 提供文件操作相关的工具方法，包括文件URI获取和图片保存功能
 */
public class FileUtils {
    
    /**
     * 获取文件的 Uri
     * 根据 Android 版本选择合适的方式获取文件 Uri
     * 
     * @param context 应用程序上下文
     * @param file 需要获取 Uri 的文件
     * @return 文件对应的 Uri
     * @throws IllegalArgumentException 如果文件路径无效
     */
    public static Uri getUriForFile(Context context, File file) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            // Android 7.0及以上使用FileProvider
            return FileProvider.getUriForFile(context, 
                context.getPackageName() + ".fileprovider", file);
        } else {
            // Android 7.0以下直接使用文件Uri
            return Uri.fromFile(file);
        }
    }
    
    /**
     * 保存图片到设备存储
     * 根据 Android 版本选择合适的存储方式
     * 
     * @param context 应用程序上下文
     * @param imageData 图片数据字节数组
     * @param fileName 保存的文件名
     * @return 保存后的图片Uri
     * @throws IOException 如果保存过程中发生IO错误
     */
    public static Uri saveImage(Context context, byte[] imageData, String fileName) 
            throws IOException {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Android 10及以上使用MediaStore API
            ContentResolver resolver = context.getContentResolver();
            ContentValues contentValues = new ContentValues();
            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");
            contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, 
                Environment.DIRECTORY_PICTURES);

            Uri imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, 
                contentValues);
            if (imageUri != null) {
                try (OutputStream os = resolver.openOutputStream(imageUri)) {
                    if (os != null) {
                        os.write(imageData);
                    }
                }
            }
            return imageUri;
        } else {
            // Android 10以下使用传统文件系统
            File imagesDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
            File imageFile = new File(imagesDir, fileName);
            try (FileOutputStream fos = new FileOutputStream(imageFile)) {
                fos.write(imageData);
            }
            return getUriForFile(context, imageFile);
        }
    }

}   