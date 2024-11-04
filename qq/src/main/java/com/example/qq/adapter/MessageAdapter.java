//package com.example.qq.adapter;
//
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.TextView;
//
//import androidx.annotation.NonNull;
//import androidx.recyclerview.widget.RecyclerView;
//
//import com.example.qq.R;
//import com.example.qq.pojo.Message;
//
//import java.util.List;
//
///**
// * MessageAdapter 用于显示聊天消息的 RecyclerView 适配器。
// */
//public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
//
//    // 消息视图类型
//    private static final int VIEW_TYPE_SENT = 1; // 发送的消息
//    private static final int VIEW_TYPE_RECEIVED = 2; // 接收的消息
//
//    private List<Message> messages; // 消息列表
//    private String currentUsername; // 当前用户名
//
//    /**
//     * 构造函数，初始化适配器
//     *
//     * @param messages       消息列表
//     * @param currentUsername 当前用户名
//     */
//    public MessageAdapter(List<Message> messages, String currentUsername) {
//        this.messages = messages;
//        this.currentUsername = currentUsername;
//    }
//
//    @Override
//    public int getItemViewType(int position) {
//        return messages.get(position).isSentByCurrentUser(currentUsername) ? VIEW_TYPE_SENT : VIEW_TYPE_RECEIVED; // 判断消息类型
//    }
//
//    @NonNull
//    @Override
//    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        View view = inflateView(parent, viewType);
//        return new MessageViewHolder(view);
//    }
//
//    @Override
//    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
//        Message message = messages.get(position);
//        ((MessageViewHolder) holder).bind(message); // 绑定消息
//    }
//
//    @Override
//    public int getItemCount() {
//        return messages.size();
//    }
//
//    /**
//     * 根据视图类型加载相应的布局
//     *
//     * @param parent   父视图组
//     * @param viewType 视图类型
//     * @return 加载的视图
//     */
//    private View inflateView(ViewGroup parent, int viewType) {
//        int layoutId = viewType == VIEW_TYPE_SENT ? R.layout.item_sent_message : R.layout.item_received_message; // 选择布局
//        return LayoutInflater.from(parent.getContext()).inflate(layoutId, parent, false);
//    }
//
//    /**
//     * ViewHolder 类，用于缓存视图组件并绑定数据
//     */
//    public static class MessageViewHolder extends RecyclerView.ViewHolder {
//        private TextView messageText;
//
//        public MessageViewHolder(View itemView) {
//            super(itemView);
//            messageText = itemView.findViewById(R.id.messageText); // 绑定消息文本视图
//        }
//
//        /**
//         * 绑定消息数据到视图
//         *
//         * @param message 消息对象
//         */
//        public void bind(Message message) {
//            messageText.setText(message.getContent()); // 设置消息内容
//        }
//    }
//}
