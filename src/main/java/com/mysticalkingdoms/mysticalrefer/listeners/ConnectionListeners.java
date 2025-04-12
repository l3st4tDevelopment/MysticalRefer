package com.mysticalkingdoms.mysticalrefer.listeners;

import com.mysticalkingdoms.mysticalrefer.MysticalRefer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class ConnectionListeners implements Listener {

    private final MysticalRefer plugin;
    public ConnectionListeners(MysticalRefer plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        plugin.getStorageManager().createPlayer(event.getPlayer());
    }
}
