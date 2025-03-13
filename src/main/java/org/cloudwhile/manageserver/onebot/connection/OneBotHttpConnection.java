package org.cloudwhile.manageserver.onebot.connection;

import okhttp3.*;
import org.cloudwhile.manageserver.ManageServer;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.logging.Level;

/**
 * OneBot HTTP连接实现
 */
public class OneBotHttpConnection implements OneBotConnection {
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    
    private final ManageServer plugin;
    private final String baseUrl;
    private final String accessToken;
    private final OkHttpClient client;
    private Consumer<JSONObject> messageHandler;
    
    public OneBotHttpConnection(ManageServer plugin, String baseUrl, String accessToken) {
        this.plugin = plugin;
        this.baseUrl = baseUrl;
        this.accessToken = accessToken;
        
        // 创建HTTP客户端
        this.client = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build();
    }
    
    @Override
    public void init() {
        plugin.getLogger().info("初始化OneBot HTTP连接: " + baseUrl);
    }
    
    @Override
    public void setMessageHandler(Consumer<JSONObject> handler) {
        this.messageHandler = handler;
    }
    
    @Override
    public void sendPrivateMessage(long userId, String message) {
        JSONObject json = new JSONObject();
        json.put("user_id", userId);
        json.put("message", message);
        json.put("auto_escape", true);
        
        sendRequest("/send_private_msg", json);
    }
    
    @Override
    public void sendGroupMessage(long groupId, String message) {
        JSONObject json = new JSONObject();
        json.put("group_id", groupId);
        json.put("message", message);
        json.put("auto_escape", true);
        
        sendRequest("/send_group_msg", json);
    }
    
    @Override
    public void close() {
        // HTTP连接不需要特殊关闭
    }
    
    /**
     * 发送HTTP请求
     */
    private void sendRequest(String endpoint, JSONObject data) {
        String url = baseUrl + endpoint;
        
        RequestBody body = RequestBody.create(JSON, data.toString());
        Request.Builder requestBuilder = new Request.Builder()
            .url(url)
            .post(body);
            
        // 添加Token认证
        if (accessToken != null && !accessToken.isEmpty()) {
            requestBuilder.addHeader("Authorization", "Bearer " + accessToken);
        }
        
        Request request = requestBuilder.build();
        
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, IOException e) {
                plugin.getLogger().log(Level.WARNING, "发送OneBot请求失败: " + e.getMessage(), e);
            }
            
            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                try (ResponseBody responseBody = response.body()) {
                    if (!response.isSuccessful() || responseBody == null) {
                        plugin.getLogger().warning("OneBot请求失败: " + response.code());
                        return;
                    }
                    
                    String responseText = responseBody.string();
                    JSONObject json = new JSONObject(responseText);
                    
                    if (json.optInt("retcode", -1) != 0) {
                        plugin.getLogger().warning("OneBot请求返回错误: " + json.optString("msg", "未知错误"));
                    }
                } catch (Exception e) {
                    plugin.getLogger().log(Level.WARNING, "处理OneBot响应时出错", e);
                }
            }
        });
    }
} 