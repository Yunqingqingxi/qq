package com.example.qq.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.qq.R;
import com.example.qq.activity.NewFriendActivity;
import com.example.qq.domain.FriendRequest;
import java.util.ArrayList;
import java.util.List;

public class FriendRequestAdapter extends RecyclerView.Adapter<FriendRequestAdapter.ViewHolder> {
    
    private List<FriendRequest> requests;
    private OnRequestActionListener listener;

    public interface OnRequestActionListener {
        void onAccept(FriendRequest request);
        void onReject(FriendRequest request);
    }

    public FriendRequestAdapter(NewFriendActivity newFriendActivity, List<FriendRequest> requests) {
        this.requests = requests != null ? requests : new ArrayList<>();
    }

    public void setOnRequestActionListener(OnRequestActionListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_friend_request, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FriendRequest request = requests.get(position);
        
        // 设置头像
        Glide.with(holder.itemView.getContext())
            .load(request.getAvatarUrl())
            .placeholder(R.drawable.default_avatar)
            .error(R.drawable.default_avatar)
            .circleCrop()
            .into(holder.avatar);

        // 设置文本信息
        holder.username.setText(request.getUsername());
        holder.message.setText(request.getMessage());
        holder.time.setText(request.getFormattedTime());

        // 根据状态显示不同的UI
        if (request.getStatus() == 0) {
            // 待处理状态
            holder.pendingButtons.setVisibility(View.VISIBLE);
            holder.statusText.setVisibility(View.GONE);
            
            holder.btnAccept.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onAccept(request);
                }
            });
            
            holder.btnReject.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onReject(request);
                }
            });
        } else {
            // 已处理状态
            holder.pendingButtons.setVisibility(View.GONE);
            holder.statusText.setVisibility(View.VISIBLE);
            holder.statusText.setText(request.getStatus() == 1 ? "已添加" : "已拒绝");
        }
    }

    @Override
    public int getItemCount() {
        return requests.size();
    }

    public void updateData(List<FriendRequest> newRequests) {
        if (newRequests == null) {
            return;
        }
        this.requests.clear();
        this.requests.addAll(newRequests);
        notifyDataSetChanged();
    }

    public void addRequest(FriendRequest request) {
        if (request != null) {
            this.requests.add(request);
            notifyItemInserted(requests.size() - 1);
        }
    }

    public void addRequests(List<FriendRequest> newRequests) {
        if (newRequests == null || newRequests.isEmpty()) {
            return;
        }
        int startPosition = requests.size();
        this.requests.addAll(newRequests);
        notifyItemRangeInserted(startPosition, newRequests.size());
    }

    public void removeRequest(FriendRequest request) {
        int position = requests.indexOf(request);
        if (position != -1) {
            requests.remove(position);
            notifyItemRemoved(position);
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView avatar;
        TextView username;
        TextView message;
        TextView time;
        View pendingButtons;
        Button btnAccept;
        Button btnReject;
        TextView statusText;

        ViewHolder(View view) {
            super(view);
            avatar = view.findViewById(R.id.avatar);
            username = view.findViewById(R.id.username);
            message = view.findViewById(R.id.request_message);
            time = view.findViewById(R.id.request_time);
            pendingButtons = view.findViewById(R.id.pending_buttons);
            btnAccept = view.findViewById(R.id.btn_accept);
            btnReject = view.findViewById(R.id.btn_reject);
            statusText = view.findViewById(R.id.status_text);
        }
    }
} 