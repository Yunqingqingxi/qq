package com.example.qq.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.qq.ChatActivity3;
import com.example.qq.R;
import com.example.qq.pojo.ChatMessage;

import java.util.List;

/**
 * ChatMessageAdapter 适配器，用于显示聊天消息的列表。
 */
public class ChatMessageAdapter extends RecyclerView.Adapter<ChatMessageAdapter.MessageViewHolder> {
    private final List<ChatMessage> messages;  // 聊天消息列表
    private final LayoutInflater inflater;      // 布局填充器
    private final Context context;              // 上下文
    private final String currentUser;           // 当前用户的用户名或ID

    /**
     * 构造函数，初始化上下文和消息列表
     *
     * @param context 上下文
     * @param messages 聊天消息列表
     * @param currentUser 当前用户的用户名或ID
     */
    public ChatMessageAdapter(@NonNull Context context, @NonNull List<ChatMessage> messages, @NonNull String currentUser) {
        this.context = context;
        this.messages = messages;
        this.inflater = LayoutInflater.from(context);
        this.currentUser = currentUser;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // 根据消息类型加载不同的布局
        View view = inflater.inflate(viewType == 0 ? R.layout.item_sent_message : R.layout.item_received_message, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        holder.bind(messages.get(position));
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    @Override
    public int getItemViewType(int position) {
        // 根据消息发送者与当前用户匹配来确定消息类型
        return messages.get(position).getSender().equals(currentUser) ? 0 : 1;
    }

    /**
     * ViewHolder 用于缓存视图组件
     */
    class MessageViewHolder extends RecyclerView.ViewHolder {
        private final ImageView avatar;
        private final TextView messageText;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            // 直接通过视图ID获取组件
            avatar = itemView.findViewById(R.id.imageViewLeft); // 默认接收消息头像
            messageText = itemView.findViewById(R.id.messageTextLeft); // 默认接收消息内容

            // 根据当前 viewType 设置发送和接收的头像和消息视图
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    ChatMessage message = messages.get(position);
                    Intent intent = new Intent(context, ChatActivity3.class);
                    intent.putExtra("nickname", message.getSender());  // 传递消息发送者
                    intent.putExtra("message", message.getContent());  // 传递消息内容
                    context.startActivity(intent);
                }
            });
        }

        public void bind(ChatMessage message) {
            // 根据消息发送者设置头像资源
            avatar.setImageResource(message.getAvatarResId() != 0 ? message.getAvatarResId() : R.drawable.p9);  // 默认头像
            messageText.setText(message.getContent());

            // 根据发送者判断是否是发送消息，设置对应的布局视图
            if (getItemViewType() == 0) {  // 发送的消息
                avatar.setImageResource(message.getAvatarResId() != 0 ? message.getAvatarResId() : R.drawable.p9);
                messageText.setText(message.getContent());
            } else {  // 接收的消息
                avatar.setImageResource(message.getAvatarResId() != 0 ? message.getAvatarResId() : R.drawable.p9);
                messageText.setText(message.getContent());
            }
        }
    }
}
