package org.cloudwhile.manageserver.commands;

import org.cloudwhile.manageserver.ManageServer;
import org.cloudwhile.manageserver.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class KickCommand implements CommandExecutor {

    private final ManageServer plugin;

    public KickCommand(ManageServer plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
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
        StringBuilder reasonBuilder = new StringBuilder();
        if (args.length > 1) {
            for (int i = 1; i < args.length; i++) {
                reasonBuilder.append(args[i]).append(" ");
            }
        }
        String reason = reasonBuilder.length() > 0 ? reasonBuilder.toString().trim() : "未指定原因";

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
            MessageUtils.sendMessage(sender, "&a已踢出玩家 &e" + targetName + " &a，原因: &e" + reason);
        }

        return true;
    }
} 