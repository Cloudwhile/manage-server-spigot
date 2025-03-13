package org.cloudwhile.manageserver.commands;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.cloudwhile.manageserver.ManageServer;
import org.cloudwhile.manageserver.listeners.PlayerListener;
import org.cloudwhile.manageserver.onebot.OneBotManager;
import org.cloudwhile.manageserver.utils.MessageUtils;

public class UnbanCommand implements CommandExecutor {

    private final ManageServer plugin;
    private final PlayerListener playerListener;
    private final OneBotManager oneBotManager;

    public UnbanCommand(ManageServer plugin, PlayerListener playerListener, OneBotManager oneBotManager) {
        this.plugin = plugin;
        this.playerListener = playerListener;
        this.oneBotManager = oneBotManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 1) {
            MessageUtils.sendMessage(sender, "&c用法: /unban <玩家名>");
            return true;
        }

        String targetName = args[0];
        FileConfiguration banConfig = playerListener.getBanConfig();
        
        // 查找被封禁的玩家
        UUID targetUUID = null;
        for (String key : banConfig.getKeys(false)) {
            String name = banConfig.getString(key + ".name");
            if (name != null && name.equalsIgnoreCase(targetName)) {
                targetUUID = UUID.fromString(key);
                break;
            }
        }
        
        // 如果找不到玩家，尝试通过名称获取UUID
        if (targetUUID == null) {
            @SuppressWarnings("deprecation")
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(targetName);
            if (offlinePlayer.hasPlayedBefore() && banConfig.contains(offlinePlayer.getUniqueId().toString())) {
                targetUUID = offlinePlayer.getUniqueId();
            }
        }
        
        if (targetUUID == null) {
            MessageUtils.sendMessage(sender, String.format("&c找不到被封禁的玩家: %s", targetName));
            return true;
        }

        // 移除封禁
        banConfig.set(targetUUID.toString(), null);
        playerListener.saveBanConfig();

        // 发送解封消息
        String unbanMessage = plugin.getConfig().getString("messages.player-unbanned", "&a玩家 &e%player% &a已被解封");
        if (unbanMessage != null) {
            unbanMessage = unbanMessage.replace("%player%", targetName);
            MessageUtils.broadcast(unbanMessage);
        }
        
        // 发送消息到QQ
        if (oneBotManager != null && oneBotManager.isEnabled()) {
            oneBotManager.sendPlayerUnbannedMessage(targetName, sender.getName());
        }

        return true;
    }
} 