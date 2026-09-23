package com.geocraft.maintenance;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.plugin.java.JavaPlugin;

public final class GeoCraftMaintenance extends JavaPlugin implements Listener, CommandExecutor {

    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    @Override
    public void onEnable() {
        saveDefaultConfig();
        getServer().getPluginManager().registerEvents(this, this);

        if (getCommand("smpmaintenance") != null) {
            getCommand("smpmaintenance").setExecutor(this);
        }

        getLogger().info("GeoCraft Maintenance Plugin enabled successfully!");
    }

    @Override
    public void onDisable() {
        getLogger().info("GeoCraft Maintenance Plugin disabled.");
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerLogin(PlayerLoginEvent event) {
        boolean isMaintenance = getConfig().getBoolean("maintenance-enabled", false);

        if (isMaintenance) {
            Player player = event.getPlayer();
            if (!player.hasPermission("geocraft.bypass")) {
                String kickMsgRaw = getConfig().getString("messages.kick-reason", "<red>Server under maintenance.</red>");
                Component kickMsg = miniMessage.deserialize(kickMsgRaw);
                event.disallow(PlayerLoginEvent.Result.KICK_OTHER, kickMsg);
            }
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("geocraft.admin")) {
            String noPermRaw = getConfig().getString("messages.no-permission", "<red>No permission.</red>");
            sender.sendMessage(miniMessage.deserialize(noPermRaw));
            return true;
        }

        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            reloadConfig();
            String reloadMsgRaw = getConfig().getString("messages.reloaded", "<green>Config reloaded.</green>");
            sender.sendMessage(miniMessage.deserialize(reloadMsgRaw));
            return true;
        }

        boolean currentState = getConfig().getBoolean("maintenance-enabled", false);
        boolean newState = !currentState;

        if (args.length > 0) {
            if (args[0].equalsIgnoreCase("on")) {
                newState = true;
            } else if (args[0].equalsIgnoreCase("off")) {
                newState = false;
            }
        }

        getConfig().set("maintenance-enabled", newState);
        saveConfig();

        if (newState) {
            String broadcastRaw = getConfig().getString("messages.enabled-broadcast", "<green>Maintenance enabled.</green>");
            Bukkit.broadcast(miniMessage.deserialize(broadcastRaw));

            String kickMsgRaw = getConfig().getString("messages.kick-reason", "<red>Server under maintenance.</red>");
            Component kickMsg = miniMessage.deserialize(kickMsgRaw);

            for (Player player : Bukkit.getOnlinePlayers()) {
                if (!player.hasPermission("geocraft.bypass")) {
                    player.kick(kickMsg);
                }
            }
        } else {
            String broadcastRaw = getConfig().getString("messages.disabled-broadcast", "<red>Maintenance disabled.</red>");
            Bukkit.broadcast(miniMessage.deserialize(broadcastRaw));
        }

        return true;
    }
}
