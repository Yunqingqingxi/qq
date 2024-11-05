package com.example.qq.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.example.qq.ChatActivity3;
import com.example.qq.pojo.Friend;
import com.example.qq.R;

import java.util.Date;
import java.util.List;

/**
 * 好友列表适配器，用于显示好友信息的 RecyclerView
 */
public class FriendAdapter extends RecyclerView.Adapter<FriendAdapter.FriendViewHolder> {
    private List<Friend> friendList;  // 好友列表
    private Context context;  // 上下文

    /**
     * 构造函数
     *
     * @param context    上下文
     * @param friendList 好友列表
     */
    public FriendAdapter(Context context, List<Friend> friendList) {
        this.context = context;
        this.friendList = friendList;
    }

    @NonNull
    @Override
    public FriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // 使用布局填充器加载 item_friend 布局
        View view = LayoutInflater.from(context).inflate(R.layout.item_friend, parent, false);
        return new FriendViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FriendViewHolder holder, int position) {
        // 绑定好友数据到视图
        Friend friend = friendList.get(position);
        holder.bind(friend);
    }

    @Override
    public int getItemCount() {
        return friendList.size();  // 返回好友数量
    }

    /**
     * 更新好友列表
     *
     * @param newFriendList 新的好友列表
     */
    @SuppressLint("NotifyDataSetChanged")
    public void updateFriendList(List<Friend> newFriendList) {
        this.friendList.clear();  // 清空旧数据
        this.friendList.addAll(newFriendList);  // 添加新数据
        notifyDataSetChanged();  // 通知适配器更新视图
    }

    /**
     * 获取 ItemTouchHelper 的回调
     *
     * @return ItemTouchHelper.Callback
     */
    public ItemTouchHelper.Callback getItemTouchHelperCallback() {
        return new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView,
                                  @NonNull RecyclerView.ViewHolder viewHolder,
                                  @NonNull RecyclerView.ViewHolder target) {
                // 处理拖拽移动
                int fromPosition = viewHolder.getBindingAdapterPosition();
                int toPosition = target.getBindingAdapterPosition();
                // 更新数据源
                Friend movedFriend = friendList.remove(fromPosition);
                friendList.add(toPosition, movedFriend);
                notifyItemMoved(fromPosition, toPosition);
                return true;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                // 处理侧滑删除
                int position = viewHolder.getBindingAdapterPosition();
                // 从数据源中移除该好友
                friendList.remove(position);
                notifyItemRemoved(position);
            }
        };
    }

    /**
     * ViewHolder 类，表示好友列表中每个项的视图
     */
    class FriendViewHolder extends RecyclerView.ViewHolder {
        private ImageView imageViewAvatar;  // 头像视图
        private TextView textViewNickname;  // 昵称视图
        private TextView textViewMessage;    // 消息视图
        private TextView textViewTime;       // 时间视图

        public FriendViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewAvatar = itemView.findViewById(R.id.imageViewAvatar1);
            textViewNickname = itemView.findViewById(R.id.textViewNickname1);
            textViewMessage = itemView.findViewById(R.id.textViewMessage);
            textViewTime = itemView.findViewById(R.id.textViewTime);

            // itemView 的点击事件
            itemView.setOnClickListener(v -> {
                int position = getBindingAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    Friend friend = friendList.get(position);
                    Intent intent = new Intent(context, ChatActivity3.class); // 替换为 ChatActivity 的类名
                    intent.putExtra("friendNickname", friend.getNickname()); // 传递好友昵称
                    intent.putExtra("friendId", friend.getUsername()); // 传递好友 ID（假设你有这个字段）
                    context.startActivity(intent);
                }
            });
        }

        public void bind(Friend friend) {
            imageViewAvatar.setImageResource(friend.getAvatar());
            textViewNickname.setText(friend.getNickname());
            textViewMessage.setText(friend.getMessage());
            textViewTime.setText(formatTime(friend.getTime()));
        }

        private String formatTime(Date time) {
            return "昨天 22:00"; // 示例返回
        }
    }
}