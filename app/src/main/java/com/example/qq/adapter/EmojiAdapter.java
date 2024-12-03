package com.example.qq.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.qq.R;

import java.util.List;

public class EmojiAdapter extends RecyclerView.Adapter<EmojiAdapter.EmojiViewHolder> {
    private List<String> emojiList;
    private OnEmojiClickListener listener;

    public interface OnEmojiClickListener {
        void onEmojiClick(String emoji);
    }

    public EmojiAdapter(List<String> emojiList) {
        this.emojiList = emojiList;
    }

    public void setOnEmojiClickListener(OnEmojiClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public EmojiViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_emoji, parent, false);
        return new EmojiViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EmojiViewHolder holder, int position) {
        String emoji = emojiList.get(position);
        holder.emojiText.setText(emoji);
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEmojiClick(emoji);
            }
        });
    }

    @Override
    public int getItemCount() {
        return emojiList.size();
    }

    static class EmojiViewHolder extends RecyclerView.ViewHolder {
        TextView emojiText;

        public EmojiViewHolder(@NonNull View itemView) {
            super(itemView);
            emojiText = itemView.findViewById(R.id.emojiText);
        }
    }
} 