package com.example.qq.api.messageapi;

import com.example.qq.domain.ChatMessage;
import java.util.List;

/**
 * 消息相关的API接口
 * 提供消息处理相关的功能，包括：
 * - 获取聊天消息历史记录
 * - 发送新消息
 * - 管理消息的存储和传输
 * 
 * @author yunxi
 * @version 1.0
 */
public interface MessageApi {
    /**
     * 获取消息列表
     * 获取指定用户之间的聊天记录
     *
     * @param sender 发送者用户名
     * @param receiver 接收者用户名
     * @return 包含聊天记录的消息列表，按时间排序
     */
    List<ChatMessage> getMessageList(String sender, String receiver);

    /**
     * 发送消息
     * 将消息发送给指定用户
     *
     * @param json 包含消息内容的JSON字符串，格式如下：
     *            {
     *              "sender": "发送者用户名",
     *              "receiver": "接收者用户名",
     *              "content": "消息内容",
     *              "timestamp": "发送时间"
     *            }
     * @return 发送是否成功
     */
    boolean sendMessage(String json);
}
