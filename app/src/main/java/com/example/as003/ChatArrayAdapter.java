package com.example.as003;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class ChatArrayAdapter extends ArrayAdapter<ChatMessage> implements Filterable {
    private List<ChatMessage> originalList;
    private ChatFilter filter;

    public ChatArrayAdapter(Context context, List<ChatMessage> objects) {
        super(context, 0, objects);
        originalList = new ArrayList<>(objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ChatMessage message = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item, parent, false);
        }

        // 通过findViewById获取ListView中的每一项的控件
        ImageView imageViewAvatar = convertView.findViewById(R.id.imageViewAvatar);
        TextView textViewNickname = convertView.findViewById(R.id.textViewNickname);
        TextView textViewMessage = convertView.findViewById(R.id.textViewMessage);
        TextView textViewTime = convertView.findViewById(R.id.textViewTime);

        // 设置数据到对应的控件上
        imageViewAvatar.setImageResource(message.getImageResId());
        textViewNickname.setText(message.getNickname());
        textViewMessage.setText(message.getMessage());
        textViewTime.setText(message.getTime());



        return convertView;
    }

    @Override
    public Filter getFilter() {
        if (filter == null) {
            filter = new ChatFilter();
        }
        return filter;
    }

    private class ChatFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();
            if (constraint == null || constraint.length() == 0) {
                results.count = originalList.size();
                results.values = originalList;
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();

                List<ChatMessage> filteredList = new ArrayList<>();
                for (ChatMessage chatMessage : originalList) {
                    if (chatMessage.getNickname().toLowerCase().contains(filterPattern) ||
                            chatMessage.getMessage().toLowerCase().contains(filterPattern)) {
                        filteredList.add(chatMessage);
                    }
                }

                results.count = filteredList.size();
                results.values = filteredList;
            }
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            ChatArrayAdapter.this.clear();
            ChatArrayAdapter.this.addAll((List<ChatMessage>) results.values);
            ChatArrayAdapter.this.notifyDataSetChanged();
        }
    }


}