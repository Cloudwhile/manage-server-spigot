package org.cloudwhile.manageserver.onebot.connection;

import org.cloudwhile.manageserver.ManageServer;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.function.Consumer;
import java.util.logging.Level;

/**
 * OneBot WebSocket连接实现
 */
public class OneBotWebSocketConnection implements OneBotConnection {
    private final ManageServer plugin;
    private final String url;
    private final String accessToken;
    private WebSocketClient client;
    private Consumer<JSONObject> messageHandler;
    private boolean connected = false;
    
    public OneBotWebSocketConnection(ManageServer plugin, String url, String accessToken) {
        this.plugin = plugin;
        this.url = url;
        this.accessToken = accessToken;
    }
    
    @Override
    public void init() {
        try {
            URI serverUri = new URI(url);
            
            client = new WebSocketClient(serverUri) {
                @Override
                public void onOpen(ServerHandshake handshakedata) {
                    plugin.getLogger().info("OneBot WebSocket连接已建立");
                    connected = true;
                }
                
                @Override
                public void onMessage(String message) {
                    try {
                        JSONObject json = new JSONObject(message);
                        if (messageHandler != null) {
                            messageHandler.accept(json);
                        }
                    } catch (Exception e) {
                        plugin.getLogger().log(Level.WARNING, "处理WebSocket消息时出错", e);
                    }
                }
                
                @Override
                public void onClose(int code, String reason, boolean remote) {
                    plugin.getLogger().info("OneBot WebSocket连接已关闭: " + reason);
                    connected = false;
                    
                    // 尝试重连
                    plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                        if (!connected) {
                            plugin.getLogger().info("尝试重新连接OneBot WebSocket...");
                            try {
                                client.reconnect();
                            } catch (Exception e) {
                                plugin.getLogger().log(Level.WARNING, "重连OneBot WebSocket失败", e);
                            }
                        }
                    }, 200L); // 10秒后重连
                }
                
                @Override
                public void onError(Exception ex) {
                    plugin.getLogger().log(Level.WARNING, "OneBot WebSocket连接错误", ex);
                }
            };
            
            // 添加Token认证
            if (accessToken != null && !accessToken.isEmpty()) {
                client.addHeader("Authorization", "Bearer " + accessToken);
            }
            
            // 连接WebSocket
            client.connect();
            
        } catch (URISyntaxException e) {
            plugin.getLogger().log(Level.SEVERE, "OneBot WebSocket URL格式错误: " + url, e);
        }
    }
    
    @Override
    public void setMessageHandler(Consumer<JSONObject> handler) {
        this.messageHandler = handler;
    }
    
    @Override
    public void sendPrivateMessage(long userId, String message) {
        if (!connected || client == null) return;
        
        JSONObject action = new JSONObject();
        action.put("action", "send_private_msg");
        
        JSONObject params = new JSONObject();
        params.put("user_id", userId);
        params.put("message", message);
        params.put("auto_escape", true);
        
        action.put("params", params);
        
        client.send(action.toString());
    }
    
    @Override
    public void sendGroupMessage(long groupId, String message) {
        if (!connected || client == null) return;
        
        JSONObject action = new JSONObject();
        action.put("action", "send_group_msg");
        
        JSONObject params = new JSONObject();
        params.put("group_id", groupId);
        params.put("message", message);
        params.put("auto_escape", true);
        
        action.put("params", params);
        
        client.send(action.toString());
    }
    
    @Override
    public void close() {
        if (client != null) {
            try {
                client.close();
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING, "关闭OneBot WebSocket连接时出错", e);
            }
        }
    }
} 