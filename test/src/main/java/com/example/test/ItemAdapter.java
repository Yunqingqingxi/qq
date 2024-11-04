package com.example.test;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class ItemAdapter extends ArrayAdapter<Friend> {
    private final Context context;
    private final ArrayList<Friend> friends;

    public ItemAdapter(Context context, ArrayList<Friend> friends) {
        super(context, R.layout.activity_main, friends); // 确保使用正确的布局文件
        this.context = context;
        this.friends = friends;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // 获取当前好友对象
        Friend friend = friends.get(position);

        // 使用 ViewHolder 优化性能
        ViewHolder holder;
        if (convertView == null) {

            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(R.layout.friend_item, parent, false);

            holder = new ViewHolder();
            holder.imageViewAvatar = convertView.findViewById(R.id.imageViewAvatar);
            holder.textViewNickname = convertView.findViewById(R.id.textViewNickname);
            holder.textViewMessage = convertView.findViewById(R.id.textViewMessage);
            holder.textViewTime = convertView.findViewById(R.id.textViewTime);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        // 设置好友数据
        holder.imageViewAvatar.setImageResource(friend.getAvatar());
        holder.textViewNickname.setText(friend.getNickname());
        holder.textViewMessage.setText(friend.getMessage());
        holder.textViewTime.setText(friend.getTime());

        return convertView;
    }

    static class ViewHolder {
        ImageView imageViewAvatar;
        TextView textViewNickname;
        TextView textViewMessage;
        TextView textViewTime;
    }
}