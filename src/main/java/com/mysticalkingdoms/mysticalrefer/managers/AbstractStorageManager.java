package com.mysticalkingdoms.mysticalrefer.managers;

import com.mysticalkingdoms.mysticalrefer.MysticalRefer;
import com.mysticalkingdoms.mysticalrefer.players.ReferPlayer;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.*;

public abstract class AbstractStorageManager {

    protected final MysticalRefer plugin;
    private final Map<UUID, ReferPlayer> playerMap = new HashMap<>();
    private final Map<String, ReferPlayer> codeMap = new HashMap<>();
    public AbstractStorageManager(MysticalRefer plugin) {
        this.plugin = plugin;
    }

    public void loadData() {
        plugin.getLogger().info("Loading player data into memory...");

        Map<UUID, ReferPlayer> map = this.loadPlayerData();
        for (Map.Entry<UUID, ReferPlayer> entry : map.entrySet()) {
            this.playerMap.put(entry.getKey(), entry.getValue());
            this.codeMap.put(entry.getValue().getReferralCode(), entry.getValue());
        }

        plugin.getLogger().info("Loaded " + map.size() + " players into memory.");
    }

    public void createPlayer(Player player) {
        if (playerMap.containsKey(player.getUniqueId())) return;

        ReferPlayer referPlayer = ReferPlayer.createNewPlayer(player, plugin.generateCode());
        playerMap.put(player.getUniqueId(), referPlayer);
        codeMap.put(referPlayer.getReferralCode(), referPlayer);
        createAccount(referPlayer);
    }

    public ReferPlayer getPlayerFromCode(String code) {
        return codeMap.get(code);
    }

    public ReferPlayer getPlayer(OfflinePlayer player) {
        return playerMap.get(player.getUniqueId());
    }

    public void saveAllPlayers() {
        saveBatch(playerMap);
    }

    public boolean isAccountCreated(OfflinePlayer player) {
        return playerMap.containsKey(player.getUniqueId());
    }

    public Collection<ReferPlayer> getPlayers() {
        return playerMap.values();
    }

    public abstract void init();
    public abstract void close();

    protected abstract Map<UUID, ReferPlayer> loadPlayerData();
    protected abstract void createAccount(ReferPlayer referPlayer);
    protected abstract void saveBatch(Map<UUID, ReferPlayer> batch);
}