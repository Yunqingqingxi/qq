package com.example.qq.views;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.recyclerview.widget.ItemTouchHelper;

import com.example.qq.R;

public class SwipeActionsView extends LinearLayout {
    private OnActionClickListener actionClickListener;

    public SwipeActionsView(Context context, ItemTouchHelper.SimpleCallback listener) {
        super(context);
        this.actionClickListener = (OnActionClickListener) listener;
        init(context);
    }

    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.swipe_actions_view, this, true);

        Button topButton = findViewById(R.id.top_button);
        Button unreadButton = findViewById(R.id.unread_button);
        Button deleteButton = findViewById(R.id.delete_button);

        topButton.setOnClickListener(v -> {
            if (actionClickListener != null) actionClickListener.onTopClick();
        });

        unreadButton.setOnClickListener(v -> {
            if (actionClickListener != null) actionClickListener.onUnreadClick();
        });

        deleteButton.setOnClickListener(v -> {
            if (actionClickListener != null) actionClickListener.onDeleteClick();
        });
    }

    public interface OnActionClickListener {
        void onTopClick();
        void onUnreadClick();
        void onDeleteClick();
    }
}
