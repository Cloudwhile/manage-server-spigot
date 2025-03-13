package org.cloudwhile.manageserver.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.cloudwhile.manageserver.ManageServer;
import org.cloudwhile.manageserver.onebot.OneBotManager;
import org.cloudwhile.manageserver.utils.MessageUtils;
import org.jetbrains.annotations.NotNull;

public class KickCommand implements CommandExecutor {

    private final ManageServer plugin;
    private final OneBotManager oneBotManager;

    public KickCommand(ManageServer plugin, OneBotManager oneBotManager) {
        this.plugin = plugin;
        this.oneBotManager = oneBotManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (args.length < 1) {
            MessageUtils.sendMessage(sender, "&c用法: /kick <玩家名> [原因]");
            return true;
        }

        String targetName = args[0];
        Player targetPlayer = Bukkit.getPlayerExact(targetName);
        
        if (targetPlayer == null) {
            MessageUtils.sendMessage(sender, "&c找不到在线玩家: " + targetName);
            return true;
        }

        // 构建踢出原因
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

        // 踢出玩家
        String kickMessage = plugin.getConfig().getString("messages.kick-message", "&c你已被服务器踢出!");
        kickMessage = MessageUtils.colorize(kickMessage.replace("%reason%", reason));
        targetPlayer.kickPlayer(kickMessage);

        // 广播踢出消息
        if (plugin.getConfig().getBoolean("features.broadcast-kick", true)) {
            String kickBroadcast = plugin.getConfig().getString("messages.player-kicked", "&c玩家 &e%player% &c已被踢出，原因: &e%reason%");
            kickBroadcast = kickBroadcast.replace("%player%", targetName).replace("%reason%", reason);
            MessageUtils.broadcast(kickBroadcast);
        } else {
            MessageUtils.sendMessage(sender, String.format("&a已踢出玩家 &e%s &a，原因: &e%s", targetName, reason));
        }
        
        // 发送消息到QQ
        if (oneBotManager != null && oneBotManager.isEnabled()) {
            oneBotManager.sendPlayerKickedMessage(targetName, reason, sender.getName());
        }

        return true;
    }
} 