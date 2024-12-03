package com.example.qq.fragment;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.qq.R;

/**
 * 空状态Fragment
 * 用于显示空状态或占位内容的Fragment，支持自定义显示文本
 *
 * @author yunxi
 * @version 1.0
 */
public class EmptyFragment extends Fragment {

    /**
     * 创建Fragment的视图
     * 动态创建一个包含居中文本的布局
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
        // 创建根布局
        FrameLayout layout = createRootLayout();

        // 创建并配置文本视图
        TextView textView = createMessageTextView();

        // 设置文本视图的布局参数
        FrameLayout.LayoutParams params = createTextViewLayoutParams();
        layout.addView(textView, params);

        return layout;
    }

    /**
     * 创建根布局
     * @return 配置好的FrameLayout
     */
    private FrameLayout createRootLayout() {
        FrameLayout layout = new FrameLayout(requireContext());
        layout.setLayoutParams(new ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        ));
        layout.setBackgroundColor(getResources().getColor(R.color.white));
        return layout;
    }

    /**
     * 创建消息文本视图
     * @return 配置好的TextView
     */
    private TextView createMessageTextView() {
        TextView textView = new TextView(requireContext());
        String message = getArguments() != null ? 
            getArguments().getString("message") : "暂无内容";
        textView.setText(message);
        textView.setTextColor(getResources().getColor(R.color.textColorSecondary));
        textView.setTextSize(16);
        textView.setGravity(Gravity.CENTER);
        return textView;
    }

    /**
     * 创建文本视图的布局参数
     * @return 配置好的LayoutParams
     */
    private FrameLayout.LayoutParams createTextViewLayoutParams() {
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        );
        params.gravity = Gravity.CENTER;
        return params;
    }
} 