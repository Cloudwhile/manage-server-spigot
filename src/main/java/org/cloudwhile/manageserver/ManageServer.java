package org.cloudwhile.manageserver;

import org.cloudwhile.manageserver.commands.BanCommand;
import org.cloudwhile.manageserver.commands.KickCommand;
import org.cloudwhile.manageserver.commands.UnbanCommand;
import org.cloudwhile.manageserver.listeners.PlayerListener;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.Objects;

public class ManageServer extends JavaPlugin {
    
    private static ManageServer instance;
    private FileConfiguration config;
    private PlayerListener playerListener;
    
    @Override
    public void onEnable() {
        // 保存实例
        instance = this;
        
        // 加载配置
        saveDefaultConfig();
        config = getConfig();
        
        // 创建数据文件夹
        File dataFolder = new File(getDataFolder(), "data");
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
        
        // 创建并注册事件监听器
        playerListener = new PlayerListener(this);
        getServer().getPluginManager().registerEvents(playerListener, this);
        
        // 注册命令
        Objects.requireNonNull(getCommand("ban")).setExecutor(new BanCommand(this));
        Objects.requireNonNull(getCommand("kick")).setExecutor(new KickCommand(this));
        Objects.requireNonNull(getCommand("unban")).setExecutor(new UnbanCommand(this));
        
        getLogger().info("ManageServer 插件已启用!");
    }
    
    @Override
    public void onDisable() {
        getLogger().info("ManageServer 插件已禁用!");
    }
    
    public static ManageServer getInstance() {
        return instance;
    }
    
    public PlayerListener getPlayerListener() {
        return playerListener;
    }
} 