package org.cloudwhile.manageserver;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.cloudwhile.manageserver.commands.BanCommand;
import org.cloudwhile.manageserver.commands.KickCommand;
import org.cloudwhile.manageserver.commands.UnbanCommand;
import org.cloudwhile.manageserver.listeners.ChatListener;
import org.cloudwhile.manageserver.listeners.PlayerListener;
import org.cloudwhile.manageserver.onebot.OneBotManager;

import java.io.File;

public class ManageServer extends JavaPlugin {
    
    private static ManageServer instance;
    private PlayerListener playerListener;
    private OneBotManager oneBotManager;
    
    @Override
    public void onEnable() {
        // 保存实例
        instance = this;
        
        // 加载配置
        saveDefaultConfig();
        
        // 创建数据文件夹
        File dataFolder = new File(getDataFolder(), "data");
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
        
        // 初始化OneBot管理器
        oneBotManager = new OneBotManager(this);
        
        // 创建并注册事件监听器
        playerListener = new PlayerListener(this, oneBotManager);
        getServer().getPluginManager().registerEvents(playerListener, this);
        
        // 注册聊天监听器
        getServer().getPluginManager().registerEvents(new ChatListener(this, oneBotManager), this);
        
        // 注册命令
        getCommand("ban").setExecutor(new BanCommand(this, playerListener, oneBotManager));
        getCommand("kick").setExecutor(new KickCommand(this, oneBotManager));
        getCommand("unban").setExecutor(new UnbanCommand(this, playerListener, oneBotManager));
        
        getLogger().info("ManageServer 插件已启用!");
    }
    
    @Override
    public void onDisable() {
        // 关闭OneBot连接
        if (oneBotManager != null) {
            oneBotManager.shutdown();
        }
        
        getLogger().info("ManageServer 插件已禁用!");
    }
    
    public static ManageServer getInstance() {
        return instance;
    }
    
    public PlayerListener getPlayerListener() {
        return playerListener;
    }
    
    public OneBotManager getOneBotManager() {
        return oneBotManager;
    }
} 