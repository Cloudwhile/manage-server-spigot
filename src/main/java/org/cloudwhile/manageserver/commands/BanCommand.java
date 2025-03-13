package org.cloudwhile.manageserver.commands;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.cloudwhile.manageserver.ManageServer;
import org.cloudwhile.manageserver.listeners.PlayerListener;
import org.cloudwhile.manageserver.onebot.OneBotManager;
import org.cloudwhile.manageserver.utils.MessageUtils;

public class BanCommand implements CommandExecutor {

    private final ManageServer plugin;
    private final PlayerListener playerListener;
    private final OneBotManager oneBotManager;

    public BanCommand(ManageServer plugin, PlayerListener playerListener, OneBotManager oneBotManager) {
        this.plugin = plugin;
        this.playerListener = playerListener;
        this.oneBotManager = oneBotManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 1) {
            MessageUtils.sendMessage(sender, "&c用法: /ban <玩家名> [原因]");
            return true;
        }

        String targetName = args[0];
        Player targetPlayer = Bukkit.getPlayerExact(targetName);
        
        // 获取玩家UUID
        @SuppressWarnings("deprecation")
        OfflinePlayer offlinePlayer = targetPlayer != null ? targetPlayer 
            : Bukkit.getOfflinePlayer(targetName);
            
        // 检查玩家是否存在
        if (!offlinePlayer.hasPlayedBefore() && targetPlayer == null) {
            MessageUtils.sendMessage(sender, "&c找不到玩家: " + targetName);
            return true;
        }
        
        UUID targetUUID = offlinePlayer.getUniqueId();

        // 检查玩家是否已被封禁
        FileConfiguration banConfig = playerListener.getBanConfig();
        if (banConfig.contains(targetUUID.toString())) {
            MessageUtils.sendMessage(sender, "&c该玩家已经被封禁了");
            return true;
        }

        // 构建封禁原因
        String reason;
        if (args.length > 1) {
            StringBuilder reasonBuilder = new StringBuilder();
            for (int i = 1; i < args.length; i++) {
                reasonBuilder.append(args[i]).append(" ");
            }
            reason = reasonBuilder.toString().trim();
        } else {
            reason = "未指定原因";
        }

        // 保存封禁信息
        banConfig.set(targetUUID.toString() + ".name", targetName);
        banConfig.set(targetUUID.toString() + ".reason", reason);
        banConfig.set(targetUUID.toString() + ".by", sender.getName());
        banConfig.set(targetUUID.toString() + ".time", System.currentTimeMillis());
        playerListener.saveBanConfig();

        // 如果玩家在线，踢出玩家
        if (targetPlayer != null && targetPlayer.isOnline()) {
            String kickMessage = plugin.getConfig().getString("messages.ban-message", "&c你已被服务器封禁!");
            if (kickMessage != null) {
                kickMessage = MessageUtils.colorize(kickMessage.replace("%reason%", reason));
                targetPlayer.kickPlayer(kickMessage);
            }
        }

        // 广播封禁消息
        if (plugin.getConfig().getBoolean("features.broadcast-ban", true)) {
            String banMessage = plugin.getConfig().getString("messages.player-banned", "&c玩家 &e%player% &c已被封禁，原因: &e%reason%");
            if (banMessage != null) {
                banMessage = banMessage.replace("%player%", targetName).replace("%reason%", reason);
                MessageUtils.broadcast(banMessage);
            }
        } else {
            MessageUtils.sendMessage(sender, String.format("&a已封禁玩家 &e%s &a，原因: &e%s", targetName, reason));
        }
        
        // 发送消息到QQ
        if (oneBotManager != null && oneBotManager.isEnabled()) {
            oneBotManager.sendPlayerBannedMessage(targetName, reason, sender.getName());
        }

        return true;
    }
} 