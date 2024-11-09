package com.example.qq.websocket.webUtils;

import android.app.AlertDialog;
import android.content.Context;
import android.widget.EditText;

public class AddFriendUtil {

    public static void showAddFriendDialog(Context context, String username, FriendAddListener listener) {
        // 创建对话框构建器
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("添加好友");
        builder.setMessage("是否要添加 " + username + " 为好友？");

        // 创建一个可编辑的输入框，用于输入拒绝理由
        final EditText reasonEditText = new EditText(context);
        builder.setView(reasonEditText);

        // 添加确认按钮
        builder.setPositiveButton("添加", (dialog, which) -> {
            String wantSay = reasonEditText.getText().toString().trim();
            // 当用户选择添加时，调用 listener 的 onAdd 方法
            if (listener != null) {
                listener.onAdd(username,wantSay);
            }
            dialog.dismiss(); // 关闭对话框
        });

        // 添加拒绝按钮
        builder.setNegativeButton("拒绝", (dialog, which) -> {
            // 获取用户输入的拒绝理由
            String reason = reasonEditText.getText().toString().trim();
            if (listener != null) {
                listener.onReject(reason);
            }
            dialog.dismiss(); // 关闭对话框
        });

        // 添加取消按钮
        builder.setNeutralButton("取消", (dialog, which) -> dialog.dismiss());

        // 显示对话框
        builder.show();
    }

    // 定义一个接口，回调添加好友和拒绝的操作
    public interface FriendAddListener {
        void onAdd(String username,String wantSay);
        void onReject(String reason);
    }
}
