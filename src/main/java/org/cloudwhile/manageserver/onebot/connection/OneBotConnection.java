package org.cloudwhile.manageserver.onebot.connection;

import org.json.JSONObject;

import java.util.function.Consumer;

/**
 * OneBot连接接口
 */
public interface OneBotConnection {
    /**
     * 初始化连接
     */
    void init();
    
    /**
     * 设置消息处理器
     */
    void setMessageHandler(Consumer<JSONObject> handler);
    
    /**
     * 发送私聊消息
     */
    void sendPrivateMessage(long userId, String message);
    
    /**
     * 发送群聊消息
     */
    void sendGroupMessage(long groupId, String message);
    
    /**
     * 关闭连接
     */
    void close();
} 