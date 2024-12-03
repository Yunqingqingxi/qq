package com.example.qq.adapter;

// Android 框架

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.qq.R;
import com.example.qq.domain.FriendRequest;

import java.util.ArrayList;
import java.util.List;

/**
 * 好友请求适配器
 * 负责处理好友请求列表的显示和交互，包括：
 * - 显示请求者的头像和昵称
 * - 显示请求消息和时间
 * - 处理接受和拒绝请求的操作
 * - 管理请求状态的更新
 */
public class FriendRequestAdapter extends RecyclerView.Adapter<FriendRequestAdapter.ViewHolder> {
    private static final String TAG = "FriendRequestAdapter";
    
    private final Context context;
    private final List<FriendRequest> requests;
    private OnRequestActionListener listener;

    /**
     * 构造函数
     * @param context 上下文
     * @param requests 好友请求列表
     */
    public FriendRequestAdapter(Context context, List<FriendRequest> requests) {
        this.context = context;
        this.requests = requests != null ? requests : new ArrayList<>();
    }

    /**
     * 好友请求操作监听器接口
     */
    public interface OnRequestActionListener {
        /**
         * 接受好友请求的回调
         * @param request 被接受的请求
         */
        void onAccept(FriendRequest request);

        /**
         * 拒绝好友请求的回调
         * @param request 被拒绝的请求
         */
        void onReject(FriendRequest request);
    }

    /**
     * 设置请求操作监听器
     * @param listener 监听器实例
     */
    public void setOnRequestActionListener(OnRequestActionListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
            .inflate(R.layout.item_friend_request, parent, false);
        return new ViewHolder(view);
    }

    /**
     * 绑定ViewHolder
     * 设置请求信息和按钮状态
     * @param holder ViewHolder实例
     * @param position 列表位置
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FriendRequest request = requests.get(position);
        
        // 设置头像
        if (request.getAvatarUrl() != null && !request.getAvatarUrl().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                .load(request.getAvatarUrl())
                .placeholder(R.drawable.default_avatar)
                .error(R.drawable.default_avatar)
                .circleCrop()
                .into(holder.avatar);
        } else {
            holder.avatar.setImageResource(R.drawable.default_avatar);
        }

        // 设置文本信息
        String displayName = request.getNickname();
        if (displayName == null || displayName.isEmpty()) {
            displayName = request.getUsername();
        }
        holder.username.setText(displayName);
        holder.message.setText(request.getMessage());
        holder.time.setText(request.getFormattedTime());

        // 根据状态更新UI
        updateViewByStatus(holder, request);
    }

    /**
     * 根据请求状态更新视图
     * @param holder ViewHolder实例
     * @param request 好友请求
     */
    private void updateViewByStatus(ViewHolder holder, FriendRequest request) {
        switch (request.getStatus()) {
            case 0: // 待处理状态
                configureForPendingStatus(holder, request);
                break;
            case 1: // 已接受
                configureForAcceptedStatus(holder);
                break;
            case 2: // 已拒绝
                configureForRejectedStatus(holder);
                break;
        }
    }

    /**
     * 配置待处理状态的视图
     */
    private void configureForPendingStatus(ViewHolder holder, FriendRequest request) {
        holder.pendingButtons.setVisibility(View.VISIBLE);
        holder.statusText.setVisibility(View.GONE);
        
        holder.btnAccept.setOnClickListener(v -> {
            if (listener != null) {
                listener.onAccept(request);
            }
        });
        
        holder.btnReject.setOnClickListener(v -> {
            if (listener != null) {
                listener.onReject(request);
            }
        });
    }

    /**
     * 配置已接受状态的视图
     */
    private void configureForAcceptedStatus(ViewHolder holder) {
        holder.pendingButtons.setVisibility(View.GONE);
        holder.statusText.setVisibility(View.VISIBLE);
        holder.statusText.setText("已添加");
        clearButtonListeners(holder);
    }

    /**
     * 配置已拒绝状态的视图
     */
    private void configureForRejectedStatus(ViewHolder holder) {
        holder.pendingButtons.setVisibility(View.GONE);
        holder.statusText.setVisibility(View.VISIBLE);
        holder.statusText.setText("已拒绝");
        clearButtonListeners(holder);
    }

    /**
     * 清除按钮监听器
     */
    private void clearButtonListeners(ViewHolder holder) {
        holder.btnAccept.setOnClickListener(null);
        holder.btnReject.setOnClickListener(null);
    }

    @Override
    public int getItemCount() {
        return requests.size();
    }

    /**
     * 更新请求状态
     * @param request 要更新的请求
     */
    public void updateRequestStatus(FriendRequest request) {
        int position = findRequestPosition(request);
        if (position != -1) {
            notifyItemChanged(position);
        }
    }

    /**
     * 查找请求在列表中的位置
     * @param request 要查找的请求
     * @return 请求的位置，未找到返回-1
     */
    private int findRequestPosition(FriendRequest request) {
        for (int i = 0; i < requests.size(); i++) {
            if (requests.get(i).getUsername().equals(request.getUsername())) {
                return i;
            }
        }
        return -1;
    }

    /**
     * 好友请求ViewHolder
     */
    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView avatar;
        TextView username;
        TextView message;
        TextView time;
        View pendingButtons;
        Button btnAccept;
        Button btnReject;
        TextView statusText;

        ViewHolder(View view) {
            super(view);
            avatar = view.findViewById(R.id.avatar);
            username = view.findViewById(R.id.username);
            message = view.findViewById(R.id.request_message);
            time = view.findViewById(R.id.request_time);
            pendingButtons = view.findViewById(R.id.pending_buttons);
            btnAccept = view.findViewById(R.id.btn_accept);
            btnReject = view.findViewById(R.id.btn_reject);
            statusText = view.findViewById(R.id.status_text);
        }
    }
} 