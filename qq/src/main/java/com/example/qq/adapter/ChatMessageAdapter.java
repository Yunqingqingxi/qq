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
        View view;
        if (viewType == 0) {
            view = inflater.inflate(R.layout.item_sent_message, parent, false);
        } else {
            view = inflater.inflate(R.layout.item_received_message, parent, false);
        }
        return new MessageViewHolder(view, viewType);
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
        // Check if the sender matches the current user
        return messages.get(position).getSender().equals(currentUser) ? 0 : 1; // 判断消息类型
    }

    /**
     * ViewHolder 用于缓存视图组件
     */
    class MessageViewHolder extends RecyclerView.ViewHolder {
        private final ImageView avatar;
        private final TextView messageText;
//        private final TextView nicknameText;

        public MessageViewHolder(@NonNull View itemView, int viewType) {
            super(itemView);
            avatar = itemView.findViewById(viewType == 0 ? R.id.imageViewRight : R.id.imageViewLeft);
            messageText = itemView.findViewById(viewType == 0 ? R.id.messageTextRight : R.id.messageTextLeft);
//            nicknameText = itemView.findViewById(viewType == 0 ? R.id.nicknameRight : R.id.nicknameLeft);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    ChatMessage message = messages.get(position);
                    Intent intent = new Intent(context, ChatActivity3.class);
                    intent.putExtra("nickname", message.getSender());
                    intent.putExtra("message", message.getContent());
                    context.startActivity(intent);
                }
            });
        }

        public void bind(ChatMessage message) {
            avatar.setImageResource(message.getAvatarResId() != 0 ? message.getAvatarResId() : R.drawable.p9); // 使用默认头像资源
            messageText.setText(message.getContent());
//            nicknameText.setText(message.getNickname());
        }
    }
}
