package com.example.qq.network;

import com.example.qq.utils.SharedPreferencesManager;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * 封装的请求方法
 */
public class RequestManager {
    private static final String BASE_URL = "http://10.0.2.2:7078/api";     // 安卓模拟器连接本地服务器的地址
    private static final OkHttpClient client = new OkHttpClient();

    /**
     * 获取带有token的请求构建器
     */
    private static Request.Builder getRequestBuilderWithToken() {
        Request.Builder builder = new Request.Builder();
        String token = SharedPreferencesManager.getInstance().getToken();
        if (token != null && !token.isEmpty()) {
            builder.addHeader("Authorization",token);
        }
        return builder;
    }

    /**
     * 发送POST请求
     *
     * @param url  请求的URL
     * @param json 请求体（JSON格式）
     * @return 返回服务器响应的字符串
     */
    public static String post(String url, String json) {
        // 得到最终请求地址
        String finalUrl = BASE_URL + url;
        System.out.println("Success send to " + finalUrl);

        // TODO: 2024/11/28 请求体
        RequestBody requestBody = RequestBody.create(
                json,
                MediaType.parse("application/json; charset=utf-8"));
                
        // TODO: 2024/11/28 发送请求
        Request request = getRequestBuilderWithToken()
                .url(finalUrl)
                .post(requestBody)
                .build();
                
        try {
            Response response = client.newCall(request).execute();
            if (!response.isSuccessful()) {
                throw new IOException("请求失败: " + response.code());
            }
            assert response.body() != null;
            return response.body().string();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 发起GET请求
     * @param url 请求的URL
     * @return 返回服务器响应的字符串
     */
    public static String get(String url) {
        // 得到最终请求地址
        String finalUrl = BASE_URL + url;
        
        // TODO: 2024/11/28 发送请求
        Request request = getRequestBuilderWithToken()
                .url(finalUrl)
                .build();
                
        try {
            Response response = client.newCall(request).execute();
            assert response.body() != null;
            return response.body().string();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 发起PUT请求
     * @param url 请求的URL
     * @param json 请求体（JSON格式）
     * @return 返回服务器响应的字符串
     */
    public static String put(String url, String json) {
        // 得到最终请求地址
        String finalUrl = BASE_URL + url;
        
        // TODO: 2024/11/28 请求体
        RequestBody requestBody = RequestBody.create(
                json,
                MediaType.parse("application/json; charset=utf-8"));
                
        // TODO: 2024/11/28 发送请求
        Request request = getRequestBuilderWithToken()
                .url(finalUrl)
                .put(requestBody)
                .build();
                
        try {
            Response response = client.newCall(request).execute();
            assert response.body() != null;
            return response.body().string();
        } catch (Exception e) {
            throw new RuntimeException();
        }
    }

    /**
     * 发起DELETE请求
     * @param url 请求的URL
     * @return 返回服务器响应的字符串
     */
    public static String delete(String url) {
        // 得到最终请求地址
        String finalUrl = BASE_URL + url;
        
        // TODO: 2024/11/28 发送请求
        Request request = getRequestBuilderWithToken()
                .url(finalUrl)
                .delete()
                .build();
                
        try {
            Response response = client.newCall(request).execute();
            assert response.body() != null;
            return response.body().string();
        } catch (Exception e) {
            throw new RuntimeException();
        }
    }

    /**
     * 发送Multipart POST请求
     * @param url 请求的URL
     * @param requestBody Multipart请求体
     * @return 返回服务器响应的字符串
     */
    public static String postMultipart(String url, RequestBody requestBody) {
        // 得到最终请求地址
        String finalUrl = BASE_URL + url;
        
        // 构建请求
        Request request = getRequestBuilderWithToken()
                .url(finalUrl)
                .post(requestBody)
                .build();
                
        try {
            Response response = client.newCall(request).execute();
            if (!response.isSuccessful()) {
                throw new IOException("请求失败: " + response.code());
            }
            assert response.body() != null;
            return response.body().string();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
} 