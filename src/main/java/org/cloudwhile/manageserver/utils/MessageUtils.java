package org.cloudwhile.manageserver.utils;

import org.cloudwhile.manageserver.ManageServer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class MessageUtils {

    /**
     * 将颜色代码转换为实际颜色
     * @param message 包含颜色代码的消息
     * @return 格式化后的消息
     */
    public static String colorize(String message) {
        if (message == null) return "";
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    /**
     * 向玩家发送带前缀的消息
     * @param sender 接收消息的对象
     * @param message 消息内容
     */
    public static void sendMessage(CommandSender sender, String message) {
        String prefix = ManageServer.getInstance().getConfig().getString("messages.prefix", "&7[&6ManageServer&7] ");
        sender.sendMessage(colorize(prefix + message));
    }

    /**
     * 向所有玩家广播消息
     * @param message 消息内容
     */
    public static void broadcast(String message) {
        String prefix = ManageServer.getInstance().getConfig().getString("messages.prefix", "&7[&6ManageServer&7] ");
        Bukkit.broadcastMessage(colorize(prefix + message));
    }

    /**
     * 向有权限的玩家广播消息
     * @param message 消息内容
     * @param permission 所需权限
     */
    public static void broadcastPermission(String message, String permission) {
        String prefix = ManageServer.getInstance().getConfig().getString("messages.prefix", "&7[&6ManageServer&7] ");
        Bukkit.broadcast(colorize(prefix + message), permission);
    }
} 