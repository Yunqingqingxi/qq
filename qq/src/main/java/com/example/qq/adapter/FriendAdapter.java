package com.example.qq.adapter;

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
import com.example.qq.domain.FriendList;

import java.util.List;

public class FriendAdapter extends RecyclerView.Adapter<FriendAdapter.ViewHolder> {
    private final List<FriendList> friendLists;
    private final Context context;
    private OnItemClickListener onItemClickListener;

    public FriendAdapter(Context context, List<FriendList> friendLists) {
        this.context = context;
        this.friendLists = friendLists;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_friend, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FriendList friend = friendLists.get(position);

        // 设置头像
        Glide.with(context)
            .load(friend.getAvatarUrl())
            .placeholder(R.drawable.default_avatar)
            .error(R.drawable.default_avatar)
            .circleCrop()
            .into(holder.imageViewAvatar);

        // 设置昵称
        holder.textViewNickname.setText(friend.getFriendNickName());
        
        // 设置最后消息
        holder.textViewMessage.setText(friend.getLastContext());
        
        // 设置时间
        holder.textViewTime.setText(friend.getLastContextTime());

        // 设置点击事件
        holder.itemView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(friend, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return friendLists.size();
    }

    public void updateData(List<FriendList> newData) {
        friendLists.clear();
        friendLists.addAll(newData);
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageViewAvatar;
        TextView textViewNickname;
        TextView textViewMessage;
        TextView textViewTime;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewAvatar = itemView.findViewById(R.id.imageViewAvatar1);
            textViewNickname = itemView.findViewById(R.id.textViewNickname1);
            textViewMessage = itemView.findViewById(R.id.textViewMessage);
            textViewTime = itemView.findViewById(R.id.textViewTime);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(FriendList friend, int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }
}