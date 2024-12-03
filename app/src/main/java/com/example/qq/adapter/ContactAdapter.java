package com.example.qq.adapter;

// Android 框架

import android.annotation.SuppressLint;
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
import com.example.qq.domain.Contact;

import java.util.List;

/**
 * 联系人列表适配器
 * 负责处理联系人列表的显示，包括：
 * - 显示联系人头像和昵称
 * - 处理联系人项的点击事件
 * - 管理联系人列表的更新和删除
 * 
 * @author yunxi
 * @version 1.0
 */
public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ViewHolder> {
    private final Context context;
    private List<Contact> contacts;
    private OnItemClickListener onItemClickListener;

    /**
     * 构造函数
     * @param context 上下文
     * @param contacts 联系人列表
     */
    public ContactAdapter(Context context, List<Contact> contacts) {
        this.context = context;
        this.contacts = contacts;
    }

    /**
     * 更新联系人列表数据
     * @param newContacts 新的联系人列表
     */
    @SuppressLint("NotifyDataSetChanged")
    public void updateData(List<Contact> newContacts) {
        this.contacts = newContacts;
        notifyDataSetChanged();
    }

    /**
     * 联系人项点击事件监听器接口
     */
    public interface OnItemClickListener {
        /**
         * 当联系人项被点击时调用
         * @param contact 被点击的联系人
         * @param position 在列表中的位置
         */
        void onItemClick(Contact contact, int position);
    }

    /**
     * 设置点击事件监听器
     * @param listener 监听器实例
     */
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    /**
     * 创建ViewHolder
     * @param parent 父视图
     * @param viewType 视图类型
     * @return ViewHolder实例
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_contact, parent, false);
        return new ViewHolder(view);
    }

    /**
     * 绑定ViewHolder
     * @param holder ViewHolder实例
     * @param position 联系人位置
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Contact contact = contacts.get(position);
        holder.nickName.setText(contact.getNickName());
        
        if (contact.getAvatarUrl() != null && !contact.getAvatarUrl().isEmpty()) {
            Glide.with(context)
                .load(contact.getAvatarUrl())
                .placeholder(R.drawable.default_avatar)
                .into(holder.avatar);
        } else {
            holder.avatar.setImageResource(R.drawable.default_avatar);
        }

        holder.itemView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(contact, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return contacts.size();
    }

    /**
     * 移除指定用户名的联系人
     * @param username 要移除的联系人用户名
     */
    public void removeContact(String username) {
        if (username == null) return;
        
        for (int i = 0; i < contacts.size(); i++) {
            Contact contact = contacts.get(i);
            if (contact != null && username.equals(contact.getUsername())) {
                contacts.remove(i);
                notifyItemRemoved(i);
                break;
            }
        }
    }

    /**
     * 联系人ViewHolder
     * 持有联系人项视图中的各个组件
     */
    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView avatar;
        TextView nickName;

        ViewHolder(View itemView) {
            super(itemView);
            avatar = itemView.findViewById(R.id.imageViewAvatar);
            nickName = itemView.findViewById(R.id.textViewNickName);
        }
    }
} 