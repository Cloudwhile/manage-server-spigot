package org.cloudwhile.manageserver.commands;

import org.cloudwhile.manageserver.ManageServer;
import org.cloudwhile.manageserver.listeners.PlayerListener;
import org.cloudwhile.manageserver.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.UUID;

public class BanCommand implements CommandExecutor {

    private final ManageServer plugin;
    private final PlayerListener playerListener;

    public BanCommand(ManageServer plugin) {
        this.plugin = plugin;
        this.playerListener = plugin.getPlayerListener();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 1) {
            MessageUtils.sendMessage(sender, "&c用法: /ban <玩家名> [原因]");
            return true;
        }

        String targetName = args[0];
        Player targetPlayer = Bukkit.getPlayerExact(targetName);
        UUID targetUUID;
        
        // 获取玩家UUID
        if (targetPlayer != null) {
            targetUUID = targetPlayer.getUniqueId();
        } else {
            // 尝试获取离线玩家的UUID
            @SuppressWarnings("deprecation")
            UUID offlineUUID = Bukkit.getOfflinePlayer(targetName).getUniqueId();
            if (offlineUUID == null) {
                MessageUtils.sendMessage(sender, "&c找不到玩家: " + targetName);
                return true;
            }
            targetUUID = offlineUUID;
        }

        // 构建封禁原因
        StringBuilder reasonBuilder = new StringBuilder();
        if (args.length > 1) {
            for (int i = 1; i < args.length; i++) {
                reasonBuilder.append(args[i]).append(" ");
            }
        }
        String reason = reasonBuilder.length() > 0 ? reasonBuilder.toString().trim() : "未指定原因";

        // 保存封禁信息
        FileConfiguration banConfig = playerListener.getBanConfig();
        banConfig.set(targetUUID.toString() + ".name", targetName);
        banConfig.set(targetUUID.toString() + ".reason", reason);
        banConfig.set(targetUUID.toString() + ".by", sender.getName());
        banConfig.set(targetUUID.toString() + ".time", System.currentTimeMillis());
        playerListener.saveBanConfig();

        // 如果玩家在线，踢出玩家
        if (targetPlayer != null) {
            String kickMessage = plugin.getConfig().getString("messages.ban-message", "&c你已被服务器封禁!");
            kickMessage = MessageUtils.colorize(kickMessage.replace("%reason%", reason));
            targetPlayer.kickPlayer(kickMessage);
        }

        // 广播封禁消息
        if (plugin.getConfig().getBoolean("features.broadcast-ban", true)) {
            String banMessage = plugin.getConfig().getString("messages.player-banned", "&c玩家 &e%player% &c已被封禁，原因: &e%reason%");
            banMessage = banMessage.replace("%player%", targetName).replace("%reason%", reason);
            MessageUtils.broadcast(banMessage);
        } else {
            MessageUtils.sendMessage(sender, "&a已封禁玩家 &e" + targetName + " &a，原因: &e" + reason);
        }

        return true;
    }
} 