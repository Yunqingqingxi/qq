package com.example.qq.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.qq.R;

/**
 * 认证界面Fragment
 * 用于显示用户认证相关的界面内容，包括登录、注册等功能的入口
 *
 * @author yunxi
 * @version 1.0
 * @see com.example.qq.activity.MainActivity
 */
public class AuthFragment extends Fragment {

    /**
     * 创建Fragment的视图
     * 
     * @param inflater 用于填充视图的LayoutInflater对象
     * @param container 视图的父容器
     * @param savedInstanceState 保存的状态数据
     * @return 创建的视图对象
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, 
            @Nullable ViewGroup container, 
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_auth, container, false);
    }
} 