package org.cloudwhile.manageserver.onebot;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.cloudwhile.manageserver.ManageServer;
import org.cloudwhile.manageserver.onebot.connection.OneBotConnection;
import org.cloudwhile.manageserver.onebot.connection.OneBotHttpConnection;
import org.cloudwhile.manageserver.onebot.connection.OneBotWebSocketConnection;
import org.cloudwhile.manageserver.utils.MessageUtils;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

/**
 * OneBot管理器，负责与QQ机器人通信
 */
public class OneBotManager {
    private final ManageServer plugin;
    private OneBotConnection connection;
    private boolean enabled;
    private long mainGroupId;
    private long adminGroupId;
    private final Map<Long, String> qqBindings = new HashMap<>();
    private final Map<String, Long> playerBindings = new HashMap<>();
    private boolean forwardServerToQQ;
    private boolean forwardQQToServer;
    private boolean forwardPlayerJoinQuit;
    private boolean forwardBanKick;
    private String commandPrefix;

    public OneBotManager(ManageServer plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    /**
     * 从配置文件加载OneBot设置
     */
    private void loadConfig() {
        ConfigurationSection config = plugin.getConfig().getConfigurationSection("onebot");
        if (config == null) {
            plugin.getLogger().warning("OneBot配置部分不存在，无法启用OneBot功能");
            return;
        }

        enabled = config.getBoolean("enabled", false);
        if (!enabled) {
            plugin.getLogger().info("OneBot功能已禁用");
            return;
        }

        String connectionType = config.getString("connection-type", "websocket");
        
        // 创建连接
        if ("http".equalsIgnoreCase(connectionType)) {
            ConfigurationSection httpConfig = config.getConfigurationSection("http");
            if (httpConfig != null) {
                String url = httpConfig.getString("url", "http://127.0.0.1:5700");
                String token = httpConfig.getString("access-token", "");
                connection = new OneBotHttpConnection(plugin, url, token);
            }
        } else if ("websocket".equalsIgnoreCase(connectionType)) {
            ConfigurationSection wsConfig = config.getConfigurationSection("websocket");
            if (wsConfig != null) {
                String url = wsConfig.getString("url", "ws://127.0.0.1:6700");
                String token = wsConfig.getString("access-token", "");
                connection = new OneBotWebSocketConnection(plugin, url, token);
            }
        } else {
            plugin.getLogger().warning("未知的OneBot连接类型: " + connectionType);
            enabled = false;
            return;
        }

        // 加载群组配置
        ConfigurationSection groupsConfig = config.getConfigurationSection("groups");
        if (groupsConfig != null) {
            mainGroupId = groupsConfig.getLong("main-group", 0);
            adminGroupId = groupsConfig.getLong("admin-group", 0);
        }

        // 加载转发配置
        ConfigurationSection forwardConfig = config.getConfigurationSection("forward");
        if (forwardConfig != null) {
            forwardServerToQQ = forwardConfig.getBoolean("server-to-qq", true);
            forwardQQToServer = forwardConfig.getBoolean("qq-to-server", true);
            forwardPlayerJoinQuit = forwardConfig.getBoolean("player-join-quit", true);
            forwardBanKick = forwardConfig.getBoolean("ban-kick", true);
        }

        // 加载绑定配置
        ConfigurationSection bindingsConfig = config.getConfigurationSection("bindings");
        if (bindingsConfig != null) {
            for (String qqIdStr : bindingsConfig.getKeys(false)) {
                try {
                    long qqId = Long.parseLong(qqIdStr);
                    String playerName = bindingsConfig.getString(qqIdStr);
                    if (playerName != null && !playerName.isEmpty()) {
                        qqBindings.put(qqId, playerName);
                        playerBindings.put(playerName.toLowerCase(), qqId);
                    }
                } catch (NumberFormatException e) {
                    plugin.getLogger().warning("无效的QQ号: " + qqIdStr);
                }
            }
        }

        // 加载命令前缀
        commandPrefix = config.getString("command-prefix", "!");

        // 初始化连接
        if (connection != null) {
            connection.init();
            connection.setMessageHandler(this::handleMessage);
            plugin.getLogger().info("OneBot功能已启用，连接类型: " + connectionType);
        } else {
            plugin.getLogger().warning("OneBot连接初始化失败");
            enabled = false;
        }
    }

    /**
     * 处理从QQ收到的消息
     */
    private void handleMessage(JSONObject message) {
        try {
            String postType = message.optString("post_type", "");
            
            if ("message".equals(postType)) {
                String messageType = message.optString("message_type", "");
                JSONObject sender = message.optJSONObject("sender");
                String content = message.optString("raw_message", "");
                long userId = sender != null ? sender.optLong("user_id", 0) : 0;
                
                if ("group".equals(messageType)) {
                    long groupId = message.optLong("group_id", 0);
                    handleGroupMessage(groupId, userId, content);
                } else if ("private".equals(messageType)) {
                    handlePrivateMessage(userId, content);
                }
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "处理QQ消息时出错", e);
        }
    }

    /**
     * 处理QQ群消息
     */
    private void handleGroupMessage(long groupId, long userId, String content) {
        // 检查是否是主群或管理群
        if (groupId != mainGroupId && groupId != adminGroupId) {
            return;
        }
        
        // 检查是否是命令
        if (content.startsWith(commandPrefix)) {
            handleCommand(userId, content.substring(commandPrefix.length()), groupId);
            return;
        }
        
        // 转发消息到服务器
        if (forwardQQToServer) {
            String playerName = qqBindings.get(userId);
            String displayName = playerName != null ? playerName : "QQ用户(" + userId + ")";
            String message = String.format("[QQ] %s: %s", displayName, content);
            Bukkit.broadcastMessage(MessageUtils.colorize("&b" + message));
        }
    }

    /**
     * 处理QQ私聊消息
     */
    private void handlePrivateMessage(long userId, String content) {
        // 检查是否是命令
        if (content.startsWith(commandPrefix)) {
            handleCommand(userId, content.substring(commandPrefix.length()), 0);
            return;
        }
        
        // 获取绑定的玩家名
        String playerName = qqBindings.get(userId);
        if (playerName != null) {
            Player player = Bukkit.getPlayerExact(playerName);
            if (player != null && player.isOnline()) {
                player.sendMessage(MessageUtils.colorize("&b[QQ私聊] &7" + content));
            }
        }
    }

    /**
     * 处理QQ中的命令
     */
    private void handleCommand(long userId, String command, long groupId) {
        // 检查命令权限
        String playerName = qqBindings.get(userId);
        if (playerName == null) {
            sendMessage("您需要先绑定Minecraft账号才能使用命令", userId, groupId);
            return;
        }
        
        // 处理特殊命令
        if (command.startsWith("bind ")) {
            // 绑定命令
            String newName = command.substring(5).trim();
            if (newName.isEmpty()) {
                sendMessage("请指定要绑定的Minecraft玩家名", userId, groupId);
                return;
            }
            
            // 更新绑定
            qqBindings.put(userId, newName);
            playerBindings.put(newName.toLowerCase(), userId);
            saveBindings();
            sendMessage("已将您的QQ与Minecraft玩家 " + newName + " 绑定", userId, groupId);
            return;
        } else if (command.equals("unbind")) {
            // 解绑命令
            qqBindings.remove(userId);
            playerBindings.remove(playerName.toLowerCase());
            saveBindings();
            sendMessage("已解除您的QQ与Minecraft玩家的绑定", userId, groupId);
            return;
        } else if (command.equals("list")) {
            // 列出在线玩家
            StringBuilder sb = new StringBuilder("在线玩家 (");
            sb.append(Bukkit.getOnlinePlayers().size()).append("/").append(Bukkit.getMaxPlayers()).append("):\n");
            for (Player player : Bukkit.getOnlinePlayers()) {
                sb.append("- ").append(player.getName()).append("\n");
            }
            sendMessage(sb.toString(), userId, groupId);
            return;
        } else if (command.equals("help")) {
            // 帮助命令
            String sb = "可用命令:\n" + commandPrefix + "bind <玩家名> - 绑定Minecraft玩家\n" +
                    commandPrefix + "unbind - 解除绑定\n" +
                    commandPrefix + "list - 查看在线玩家\n" +
                    commandPrefix + "help - 显示帮助\n";
            sendMessage(sb, userId, groupId);
            return;
        }
        
        // 执行Minecraft命令 (仅限管理员)
        if (groupId == adminGroupId) {
            // 在主线程执行命令
            Bukkit.getScheduler().runTask(plugin, () -> {
                boolean success = Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
                if (success) {
                    sendMessage("命令执行成功", userId, groupId);
                } else {
                    sendMessage("命令执行失败，请检查命令格式", userId, groupId);
                }
            });
        } else {
            sendMessage("您没有权限执行此命令", userId, groupId);
        }
    }

    /**
     * 保存绑定关系到配置文件
     */
    private void saveBindings() {
        ConfigurationSection config = plugin.getConfig().getConfigurationSection("onebot");
        if (config == null) return;
        
        ConfigurationSection bindingsConfig = config.createSection("bindings");
        for (Map.Entry<Long, String> entry : qqBindings.entrySet()) {
            bindingsConfig.set(entry.getKey().toString(), entry.getValue());
        }
        
        plugin.saveConfig();
    }

    /**
     * 发送消息到QQ
     */
    public void sendMessage(String message, long userId, long groupId) {
        if (!enabled || connection == null) return;
        
        if (groupId > 0) {
            connection.sendGroupMessage(groupId, message);
        } else if (userId > 0) {
            connection.sendPrivateMessage(userId, message);
        }
    }

    /**
     * 发送消息到主群
     */
    public void sendMessageToMainGroup(String message) {
        if (!enabled || connection == null || mainGroupId <= 0 || !forwardServerToQQ) return;
        connection.sendGroupMessage(mainGroupId, message);
    }

    /**
     * 发送消息到管理群
     */
    public void sendMessageToAdminGroup(String message) {
        if (!enabled || connection == null || adminGroupId <= 0) return;
        connection.sendGroupMessage(adminGroupId, message);
    }

    /**
     * 发送玩家加入消息到QQ
     */
    public void sendPlayerJoinMessage(String playerName, boolean isFirstJoin) {
        if (!enabled || !forwardPlayerJoinQuit) return;
        
        String message = isFirstJoin 
            ? playerName + " 首次加入服务器！" 
            : playerName + " 加入了服务器";
            
        sendMessageToMainGroup(message);
    }

    /**
     * 发送玩家退出消息到QQ
     */
    public void sendPlayerQuitMessage(String playerName) {
        if (!enabled || !forwardPlayerJoinQuit) return;
        sendMessageToMainGroup(playerName + " 离开了服务器");
    }

    /**
     * 发送玩家被封禁消息到QQ
     */
    public void sendPlayerBannedMessage(String playerName, String reason, String byWho) {
        if (!enabled || !forwardBanKick) return;
        String message = String.format("玩家 %s 被 %s 封禁，原因: %s", playerName, byWho, reason);
        sendMessageToMainGroup(message);
        sendMessageToAdminGroup(message);
    }

    /**
     * 发送玩家被踢出消息到QQ
     */
    public void sendPlayerKickedMessage(String playerName, String reason, String byWho) {
        if (!enabled || !forwardBanKick) return;
        String message = String.format("玩家 %s 被 %s 踢出，原因: %s", playerName, byWho, reason);
        sendMessageToMainGroup(message);
        sendMessageToAdminGroup(message);
    }

    /**
     * 发送玩家被解封消息到QQ
     */
    public void sendPlayerUnbannedMessage(String playerName, String byWho) {
        if (!enabled || !forwardBanKick) return;
        String message = String.format("玩家 %s 被 %s 解封", playerName, byWho);
        sendMessageToMainGroup(message);
        sendMessageToAdminGroup(message);
    }

    /**
     * 发送服务器聊天消息到QQ
     */
    public void sendChatMessage(String playerName, String message) {
        if (!enabled || !forwardServerToQQ) return;
        sendMessageToMainGroup(playerName + ": " + message);
    }

    /**
     * 关闭连接
     */
    public void shutdown() {
        if (connection != null) {
            connection.close();
        }
    }

    /**
     * 检查是否启用
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * 获取玩家绑定的QQ号
     */
    public Long getPlayerBindQQ(String playerName) {
        return playerBindings.get(playerName.toLowerCase());
    }

    /**
     * 获取QQ绑定的玩家名
     */
    public String getQQBindPlayer(long qqId) {
        return qqBindings.get(qqId);
    }
} 