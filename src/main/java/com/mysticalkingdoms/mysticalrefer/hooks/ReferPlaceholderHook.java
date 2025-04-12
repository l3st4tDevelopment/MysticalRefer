package com.mysticalkingdoms.mysticalrefer.hooks;

import com.mysticalkingdoms.mysticalrefer.MysticalRefer;
import com.mysticalkingdoms.mysticalrefer.players.ReferPlayer;
import org.bukkit.OfflinePlayer;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;

public class ReferPlaceholderHook extends PlaceholderExpansion {

    private final MysticalRefer plugin;
    public ReferPlaceholderHook(MysticalRefer plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getAuthor() {
        return "l3st4t";
    }

    @Override
    public String getIdentifier() {
        return "mysticalrefer";
    }

    @Override
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer player, String params) {
        ReferPlayer referPlayer = plugin.getStorageManager().getPlayer(player);
        if (referPlayer == null) {
            return "";
        }

        return switch (params.toLowerCase()) {
            case "used_code" -> referPlayer.hasUsedCode() + "";
            case "referred_players" -> referPlayer.getReferredPlayers() + "";
            case "referral_code" -> referPlayer.getReferralCode();
            default -> null;
        };
    }
}
