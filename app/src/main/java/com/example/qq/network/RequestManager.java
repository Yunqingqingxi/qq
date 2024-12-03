package com.example.qq.network;

// Android 框架

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;
import android.util.Log;

import com.example.qq.QQApplication;
import com.example.qq.utils.SharedPreferencesManager;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * 网络请求管理器
 * 负责处理所有HTTP请求，包括：
 * - 网络状态检查
 * - 请求头处理
 * - 统一的请求发送逻辑
 */
public class RequestManager {
    private static final String TAG = "RequestManager";
    private static final String BASE_URL = "https://web.yxdfirst.top/api";
    private static final String NO_NETWORK_MESSAGE = "网络连接不可用，请检查网络设置";
    
    private static final OkHttpClient client = new OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build();

    /**
     * 检查网络连接状态
     * @return 如果网络可用返回true，否则返回false
     */
    private static boolean isNetworkAvailable() {
        Context context = QQApplication.getInstance();
        ConnectivityManager cm = (ConnectivityManager) 
            context.getSystemService(Context.CONNECTIVITY_SERVICE);
        
        if (cm == null) {
            return false;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            NetworkCapabilities capabilities = cm.getNetworkCapabilities(cm.getActiveNetwork());
            return capabilities != null && (
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET));
        } else {
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            return activeNetwork != null && activeNetwork.isConnected();
        }
    }

    /**
     * 检查网络状态并抛出异常
     * @throws IOException 当网络不可用时抛出
     */
    private static void checkNetworkConnection() throws IOException {
        if (!isNetworkAvailable()) {
            Log.e(TAG, NO_NETWORK_MESSAGE);
            throw new IOException(NO_NETWORK_MESSAGE);
        }
    }

    /**
     * 获取带有token的请求构建器
     */
    private static Request.Builder getRequestBuilderWithToken() {
        Request.Builder builder = new Request.Builder();
        String token = SharedPreferencesManager.getInstance().getToken();
        if (token != null && !token.isEmpty()) {
            builder.addHeader("Authorization", token);
        }
        return builder;
    }

    /**
     * 发送POST请求
     */
    public static String post(String url, String json) {
        try {
            checkNetworkConnection();
            
            String finalUrl = BASE_URL + url;
            Log.d(TAG, "POST请求: " + finalUrl);
            
            RequestBody requestBody = RequestBody.create(
                json,
                MediaType.parse("application/json; charset=utf-8"));
                
            Request request = getRequestBuilderWithToken()
                .url(finalUrl)
                .post(requestBody)
                .build();
                
            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new IOException("请求失败: " + response.code());
                }
                return response.body() != null ? response.body().string() : "";
            }
        } catch (IOException e) {
            Log.e(TAG, "POST请求失败: " + e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 发送GET请求
     */
    public static String get(String url) {
        try {
            checkNetworkConnection();
            
            String finalUrl = BASE_URL + url;
            Log.d(TAG, "GET请求: " + finalUrl);
            
            Request request = getRequestBuilderWithToken()
                .url(finalUrl)
                .build();
                
            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new IOException("请求失败: " + response.code());
                }
                return response.body() != null ? response.body().string() : "";
            }
        } catch (IOException e) {
            Log.e(TAG, "GET请求失败: " + e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 发送Multipart POST请求
     */
    public static String postMultipart(String url, RequestBody requestBody) {
        try {
            checkNetworkConnection();
            
            String finalUrl = BASE_URL + url;
            Log.d(TAG, "Multipart POST请求: " + finalUrl);
            
            Request request = getRequestBuilderWithToken()
                .url(finalUrl)
                .post(requestBody)
                .build();
                
            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new IOException("请求失败: " + response.code());
                }
                return response.body() != null ? response.body().string() : "";
            }
        } catch (IOException e) {
            Log.e(TAG, "Multipart POST请求失败: " + e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 发送PUT请求
     */
    public static String put(String url, String json) {
        try {
            checkNetworkConnection();
            
            String finalUrl = BASE_URL + url;
            Log.d(TAG, "PUT请求: " + finalUrl);
            
            RequestBody requestBody = RequestBody.create(
                json,
                MediaType.parse("application/json; charset=utf-8"));
                
            Request request = getRequestBuilderWithToken()
                .url(finalUrl)
                .put(requestBody)
                .build();
                
            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new IOException("请求失败: " + response.code());
                }
                return response.body() != null ? response.body().string() : "";
            }
        } catch (IOException e) {
            Log.e(TAG, "PUT请求失败: " + e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 发送DELETE请求
     */
    public static String delete(String url) {
        try {
            checkNetworkConnection();
            
            String finalUrl = BASE_URL + url;
            Log.d(TAG, "DELETE请求: " + finalUrl);
            
            Request request = getRequestBuilderWithToken()
                .url(finalUrl)
                .delete()
                .build();
                
            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new IOException("请求失败: " + response.code());
                }
                return response.body() != null ? response.body().string() : "";
            }
        } catch (IOException e) {
            Log.e(TAG, "DELETE请求失败: " + e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    // ... 其他请求方法也类似添加网络检查
} 