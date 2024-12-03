package com.example.qq.network.callback;

import androidx.annotation.NonNull;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class MyCallback implements Callback {
    @Override
    public void onFailure(@NonNull Call call, @NonNull IOException e) {
        //请求失败
        System.out.println("请求失败");
    }

    @Override
    public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
        //请求成功
        System.out.println("请求成功");
    }
}
