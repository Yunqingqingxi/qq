package com.example.qq.adapter;

// Android 框架

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.qq.R;
import com.example.qq.domain.FriendList;
import com.example.qq.utils.SharedPreferencesManager;
import com.example.qq.utils.TimeUtils;

import java.util.List;

/**
 * 好友列表适配器
 * 负责处理好友列表的显示和交互，包括：
 * - 显示好友头像、昵称和最后消息
 * - 管理未读消息计数和红点显示
 * - 处理好友项的点击事件
 * - 支持拖动消除未读消息红点
 * 
 * @author yunxi
 * @version 1.0
 */
public class FriendAdapter extends RecyclerView.Adapter<FriendAdapter.ViewHolder> {
    private static final String TAG = "FriendAdapter";
    private final Context context;
    private List<FriendList> friendLists;
    private OnItemClickListener onItemClickListener;

    /**
     * 构造函数
     * @param context 上下文
     * @param friendLists 好友列表数据
     */
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

    /**
     * 绑定ViewHolder
     * 设置好友信息、未读消息数和点击事件
     * @param holder ViewHolder实例
     * @param position 列表位置
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FriendList friend = friendLists.get(position);
        
        // 设置昵称
        holder.tvNickname.setText(friend.getFriendNickName());
        
        // 获取并设置最后一条消息和时间
        String lastMessage = friend.getLastContext();
        if (lastMessage != null && !lastMessage.isEmpty()) {
            holder.tvLastMessage.setText(lastMessage);
            holder.tvLastMessage.setVisibility(View.VISIBLE);
            Log.d("FriendAdapter", "显示消息: " + lastMessage + " 给好友: " + friend.getFriendUsername());
        } else {
            holder.tvLastMessage.setVisibility(View.GONE);
            Log.d("FriendAdapter", "没有消息显示给好友: " + friend.getFriendUsername());
        }
        
        // 设置时间
        String lastTime = friend.getLastContextTime();
        if (lastTime != null && !lastTime.isEmpty()) {
            String formattedTime = TimeUtils.formatTime(lastTime);
            holder.tvTime.setText(formattedTime);
            holder.tvTime.setVisibility(View.VISIBLE);
        } else {
            holder.tvTime.setVisibility(View.GONE);
        }

        // 设置头像
        if (friend.getAvatarUrl() != null && !friend.getAvatarUrl().isEmpty()) {
            Glide.with(context)
                .load(friend.getAvatarUrl())
                .placeholder(R.drawable.default_avatar)
                .error(R.drawable.default_avatar)
                .circleCrop()
                .into(holder.ivAvatar);
        } else {
            holder.ivAvatar.setImageResource(R.drawable.default_avatar);
        }

        // 设置未读消息数
        int unreadCount = SharedPreferencesManager.getInstance()
            .getUnreadMessageCount(friend.getFriendUsername());
        
        Log.d("FriendAdapter", "Setting unread count for " + friend.getFriendUsername() + 
            ": " + unreadCount + ", View visibility: " + 
            (unreadCount > 0 ? "VISIBLE" : "GONE"));
        
        if (unreadCount > 0) {
            holder.unreadCountBadge.setVisibility(View.VISIBLE);
            if (unreadCount > 99) {
                holder.unreadCountBadge.setText("99+");
            } else {
                holder.unreadCountBadge.setText(String.valueOf(unreadCount));
            }
            
            // 确保红点在最上层
            holder.unreadCountBadge.bringToFront();
            holder.unreadCountBadge.invalidate();
            
            // 设置红点样式
            holder.unreadCountBadge.setBackgroundResource(R.drawable.badge_background);
            holder.unreadCountBadge.setTextColor(Color.WHITE);
            holder.unreadCountBadge.setGravity(Gravity.CENTER);
            
            Log.d("FriendAdapter", "Badge set up for " + friend.getFriendUsername() + 
                " with count " + unreadCount);
            
            // 添加触摸处理
            holder.unreadCountBadge.setOnTouchListener(new BadgeTouchListener(
                holder.unreadCountBadge,
                () -> {
                    SharedPreferencesManager.getInstance()
                        .clearUnreadCount(friend.getFriendUsername());
                    notifyItemChanged(position);
                }
            ));
        } else {
            holder.unreadCountBadge.setVisibility(View.GONE);
            holder.unreadCountBadge.setOnTouchListener(null);
        }

        // 点击项目时清除未读消息并打开聊天
        holder.itemView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                // 清除未读消息计数
                if (unreadCount > 0) {
                    SharedPreferencesManager.getInstance()
                        .clearUnreadCount(friend.getFriendUsername());
                    notifyItemChanged(position);
                }
                // 触发点击回调
                onItemClickListener.onItemClick(friend, position);
            }
        });

        Log.d("FriendAdapter", "绑定好友数据 - 用户名: " + friend.getFriendUsername() + 
            ", 昵称: " + friend.getFriendNickName() +
            ", 最后消息: " + lastMessage + 
            ", 时间: " + lastTime);
    }

    @Override
    public int getItemCount() {
        return friendLists.size();
    }

    /**
     * 更新好友列表数据
     * @param newData 新的好友列表数据
     */
    public void updateData(List<FriendList> newData) {
        if (newData == null) {
            this.friendLists.clear();
        } else {
            this.friendLists.clear();
            this.friendLists.addAll(newData);
        }
    }

    /**
     * 移除指定好友
     * @param username 要移除的好友用户名
     */
    public void removeFriend(String username) {
        if (username == null) return;
        
        for (int i = 0; i < friendLists.size(); i++) {
            FriendList friend = friendLists.get(i);
            if (friend != null && username.equals(friend.getFriendUsername())) {
                friendLists.remove(i);
                notifyItemRemoved(i);  // 通知适配器移除了指定位置的数据
                Log.d("FriendAdapter", "移除好友: " + username);
                break;
            }
        }
    }

    /**
     * 添加新好友
     * @param friend 要添加的好友信息
     */
    public void addFriend(FriendList friend) {
        if (friend == null) return;
        
        friendLists.add(friend);
        notifyItemInserted(friendLists.size() - 1);  // 通知适配器在末尾添加了新数据
        Log.d("FriendAdapter", "添加好友: " + friend.getFriendUsername());
    }

    /**
     * 好友列表项的ViewHolder
     */
    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivAvatar;
        TextView tvNickname;
        TextView tvLastMessage;
        TextView tvTime;
        TextView unreadCountBadge;

        ViewHolder(View itemView) {
            super(itemView);
            ivAvatar = itemView.findViewById(R.id.imageViewAvatar);
            tvNickname = itemView.findViewById(R.id.textViewNickname);
            tvLastMessage = itemView.findViewById(R.id.textViewMessage);
            tvTime = itemView.findViewById(R.id.textViewTime);
            unreadCountBadge = itemView.findViewById(R.id.unreadCountBadge);
        }
    }

    /**
     * 好友项点击事件监听器接口
     */
    public interface OnItemClickListener {
        /**
         * 当好友项被点击时调用
         * @param friend 被点击的好友信息
         * @param position 在列表中的位置
         */
        void onItemClick(FriendList friend, int position);
    }

    /**
     * 设置点击事件监听器
     * @param listener 监听器实例
     */
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    /**
     * 未读消息红点的触摸处理类
     * 支持拖动消除红点功能
     */
    private static class BadgeTouchListener implements View.OnTouchListener {
        private static final float DISMISS_THRESHOLD = 100f; // 消失阈值
        private float dX, dY;
        private float startX, startY;
        private boolean isDragging = false;
        private final View badge;
        private final Runnable onDismiss;

        /**
         * 构造函数
         * @param badge 红点视图
         * @param onDismiss 红点消失时的回调
         */
        BadgeTouchListener(View badge, Runnable onDismiss) {
            this.badge = badge;
            this.onDismiss = onDismiss;
        }

        @SuppressLint("ClickableViewAccessibility")
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    dX = badge.getX() - event.getRawX();
                    dY = badge.getY() - event.getRawY();
                    startX = badge.getX();
                    startY = badge.getY();
                    isDragging = true;
                    badge.animate().scaleX(0.9f).scaleY(0.9f).setDuration(100).start();
                    return true;

                case MotionEvent.ACTION_MOVE:
                    if (isDragging) {
                        float newX = event.getRawX() + dX;
                        float newY = event.getRawY() + dY;
                        badge.setX(newX);
                        badge.setY(newY);
                        
                        // 计算移动距离
                        float distance = (float) Math.sqrt(
                            Math.pow(newX - startX, 2) + Math.pow(newY - startY, 2));
                        
                        // 根据距离设置透明度
                        float alpha = Math.max(0, 1 - distance / DISMISS_THRESHOLD);
                        badge.setAlpha(alpha);
                    }
                    return true;

                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    if (isDragging) {
                        isDragging = false;
                        float distance = (float) Math.sqrt(
                            Math.pow(badge.getX() - startX, 2) + 
                            Math.pow(badge.getY() - startY, 2));
                        
                        if (distance > DISMISS_THRESHOLD) {
                            // 超过阈值，消失动画
                            badge.animate()
                                .alpha(0f)
                                .scaleX(0f)
                                .scaleY(0f)
                                .setDuration(200)
                                .withEndAction(() -> {
                                    badge.setVisibility(View.GONE);
                                    onDismiss.run();
                                })
                                .start();
                        } else {
                            // 未超过阈值，返回原位
                            badge.animate()
                                .x(startX)
                                .y(startY)
                                .alpha(1f)
                                .scaleX(1f)
                                .scaleY(1f)
                                .setDuration(200)
                                .start();
                        }
                    }
                    return true;
            }
            return false;
        }
    }
}