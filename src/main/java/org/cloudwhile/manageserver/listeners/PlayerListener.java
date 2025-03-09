package org.cloudwhile.manageserver.listeners;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.cloudwhile.manageserver.ManageServer;
import org.cloudwhile.manageserver.utils.MessageUtils;

public class PlayerListener implements Listener {

    private final ManageServer plugin;
    private final File banFile;
    private FileConfiguration banConfig;

    public PlayerListener(ManageServer plugin) {
        this.plugin = plugin;
        this.banFile = new File(plugin.getDataFolder(), "data/bans.yml");
        loadBanConfig();
    }

    private void loadBanConfig() {
        if (!banFile.exists()) {
            try {
                banFile.getParentFile().mkdirs();
                banFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe(String.format("无法创建封禁数据文件: %s", e.getMessage()));
            }
        }
        banConfig = YamlConfiguration.loadConfiguration(banFile);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerPreLogin(AsyncPlayerPreLoginEvent event) {
        UUID playerUUID = event.getUniqueId();

        // 检查玩家是否被封禁
        if (banConfig.contains(playerUUID.toString())) {
            String reason = banConfig.getString(playerUUID.toString() + ".reason", "未指定原因");
            String banMessage = plugin.getConfig().getString("messages.ban-message", "&c你已被服务器封禁!");
            if (banMessage != null) {
                banMessage = MessageUtils.colorize(banMessage.replace("%reason%", reason));
                event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED, banMessage);
            }
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        FileConfiguration config = plugin.getConfig();
        
        if (config.getBoolean("features.welcome-message", true)) {
            String message = !player.hasPlayedBefore() 
                ? config.getString("messages.player-first-join", "&e欢迎 &b%player% &e首次加入服务器!")
                : config.getString("messages.player-join", "&e%player% &a加入了服务器");
                
            if (message != null) {
                event.setJoinMessage(MessageUtils.colorize(message.replace("%player%", player.getName())));
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        FileConfiguration config = plugin.getConfig();
        
        if (config.getBoolean("features.quit-message", true)) {
            String quitMessage = config.getString("messages.player-quit", "&e%player% &c离开了服务器");
            if (quitMessage != null) {
                event.setQuitMessage(MessageUtils.colorize(quitMessage.replace("%player%", player.getName())));
            }
        }
    }

    public FileConfiguration getBanConfig() {
        return banConfig;
    }

    public void saveBanConfig() {
        try {
            banConfig.save(banFile);
        } catch (IOException e) {
            plugin.getLogger().severe(String.format("无法保存封禁数据: %s", e.getMessage()));
        }
    }
} 