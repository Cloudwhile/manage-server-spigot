package org.cloudwhile.manageserver.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.cloudwhile.manageserver.ManageServer;
import org.cloudwhile.manageserver.onebot.OneBotManager;

/**
 * 聊天监听器，用于将玩家聊天消息转发到QQ
 */
public class ChatListener implements Listener {
    
    private final ManageServer plugin;
    private final OneBotManager oneBotManager;
    
    public ChatListener(ManageServer plugin, OneBotManager oneBotManager) {
        this.plugin = plugin;
        this.oneBotManager = oneBotManager;
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        if (!oneBotManager.isEnabled()) return;
        
        Player player = event.getPlayer();
        String message = event.getMessage();
        
        // 转发消息到QQ
        oneBotManager.sendChatMessage(player.getName(), message);
    }
} 