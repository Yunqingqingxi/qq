package com.example.qq.adapter;

// Android 框架

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.qq.R;
import com.example.qq.domain.ChatMessage;
import com.example.qq.utils.SharedPreferencesManager;

import java.util.List;

/**
 * 聊天消息适配器
 * 负责处理聊天消息的显示，包括：
 * - 区分发送和接收的消息布局
 * - 显示消息内容和用户头像
 * - 管理消息列表的更新
 * 
 * @author yunxi
 * @version 1.0
 */
public class ChatMessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_SENT = 1;
    private static final int TYPE_RECEIVED = 2;

    private final Context context;
    private final List<ChatMessage> messages;
    private final String currentUsername;
    private final String friendAvatar;

    /**
     * 构造函数
     * @param context 上下文
     * @param messages 消息列表
     * @param friendAvatar 好友头像URL
     */
    public ChatMessageAdapter(Context context, List<ChatMessage> messages, String friendAvatar) {
        this.context = context;
        this.messages = messages;
        this.friendAvatar = friendAvatar;
        this.currentUsername = SharedPreferencesManager.getInstance().getCurrentUsername();
    }

    /**
     * 获取消息类型
     * @param position 消息位置
     * @return 消息类型（发送/接收）
     */
    @Override
    public int getItemViewType(int position) {
        ChatMessage message = messages.get(position);
        return message.getSender().equals(currentUsername) ? TYPE_SENT : TYPE_RECEIVED;
    }

    /**
     * 创建ViewHolder
     * @param parent 父视图
     * @param viewType 视图类型
     * @return 对应类型的ViewHolder
     */
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_SENT) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_sent_message, parent, false);
            return new SentMessageHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.item_received_message, parent, false);
            return new ReceivedMessageHolder(view);
        }
    }

    /**
     * 绑定ViewHolder
     * @param holder ViewHolder实例
     * @param position 消息位置
     */
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessage message = messages.get(position);
        if (holder.getItemViewType() == TYPE_SENT) {
            configureSentMessage((SentMessageHolder) holder, message);
        } else {
            configureReceivedMessage((ReceivedMessageHolder) holder, message);
        }
    }

    /**
     * 配置发送的消息视图
     * @param holder 发送消息的ViewHolder
     * @param message 消息对象
     */
    private void configureSentMessage(SentMessageHolder holder, ChatMessage message) {
        holder.messageText.setText(message.getContent());
        String currentUserAvatar = SharedPreferencesManager.getInstance().getUserInfo().getUserAvatarUrl();
        if (currentUserAvatar != null && !currentUserAvatar.isEmpty()) {
            Glide.with(context)
                .load(currentUserAvatar)
                .placeholder(R.drawable.default_avatar)
                .error(R.drawable.default_avatar)
                .circleCrop()
                .into(holder.avatar);
        }
    }

    /**
     * 配置接收的消息视图
     * @param holder 接收消息的ViewHolder
     * @param message 消息对象
     */
    private void configureReceivedMessage(ReceivedMessageHolder holder, ChatMessage message) {
        holder.messageText.setText(message.getContent());
        if (friendAvatar != null && !friendAvatar.isEmpty()) {
            Glide.with(context)
                .load(friendAvatar)
                .placeholder(R.drawable.default_avatar)
                .error(R.drawable.default_avatar)
                .circleCrop()
                .into(holder.avatar);
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    /**
     * 添加新消息
     * @param message 要添加的消息
     */
    public void addMessage(ChatMessage message) {
        messages.add(message);
        notifyItemInserted(messages.size() - 1);
    }

    /**
     * 更新消息列表
     * @param newMessages 新的消息列表
     */
    public void updateMessages(List<ChatMessage> newMessages) {
        messages.clear();
        messages.addAll(newMessages);
        notifyDataSetChanged();
    }

    /**
     * 发送消息的ViewHolder
     */
    static class SentMessageHolder extends RecyclerView.ViewHolder {
        TextView messageText;
        ImageView avatar;

        SentMessageHolder(View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.messageTextRight);
            avatar = itemView.findViewById(R.id.imageViewRight);
        }
    }

    /**
     * 接收消息的ViewHolder
     */
    static class ReceivedMessageHolder extends RecyclerView.ViewHolder {
        TextView messageText;
        ImageView avatar;

        ReceivedMessageHolder(View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.messageTextLeft);
            avatar = itemView.findViewById(R.id.imageViewLeft);
        }
    }
} 