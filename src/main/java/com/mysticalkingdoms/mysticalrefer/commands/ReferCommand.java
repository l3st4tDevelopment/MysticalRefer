package com.mysticalkingdoms.mysticalrefer.commands;

import com.mysticalkingdoms.mysticalrefer.MysticalRefer;
import com.mysticalkingdoms.mysticalrefer.players.ReferPlayer;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.Optional;

public class ReferCommand {

    private final MysticalRefer plugin;
    public ReferCommand(MysticalRefer plugin) {
        this.plugin = plugin;
    }

    @Command("refer")
    public void onRefer(Player player, @Optional String code) {
        ReferPlayer referPlayer = plugin.getStorageManager().getPlayer(player);
        if (code == null) {
            plugin.getLocaleManager().getMessage("messages.referral-info")
                    .replace("%code%", referPlayer.getReferralCode())
                    .replace("%players%", referPlayer.getReferredPlayers() + "")
                    .sendMessage(player);
            return;
        }

        if (referPlayer.hasUsedCode()) {
            plugin.getLocaleManager().getMessage("messages.already-used-code").sendMessage(player);
            return;
        }

        if (!plugin.isValidCode(code)) {
            plugin.getLocaleManager().getMessage("messages.illegal-characters").sendMessage(player);
            return;
        }

        ReferPlayer otherRefer = plugin.getStorageManager().getPlayerFromCode(code);
        if (otherRefer == null) {
            plugin.getLocaleManager().getMessage("messages.invalid-code").sendMessage(player);
            return;
        }

        if (otherRefer.getUniqueId().equals(player.getUniqueId())) {
            plugin.getLocaleManager().getMessage("messages.cannot-refer-yourself").sendMessage(player);
            return;
        }

        int max = plugin.getMainConfig().getInt("code-settings.max-uses-per-code");
        if (max != -1 && otherRefer.getReferredPlayers() >= max) {
            plugin.getLocaleManager().getMessage("messages.max-players-exceeded").sendMessage(player);
            return;
        }

        referPlayer.setUsedCode(true);
        otherRefer.setReferredPlayers(otherRefer.getReferredPlayers() + 1);

        plugin.getLocaleManager().getMessage("messages.code-redeemed").sendMessage(player);

        for (String command : plugin.getMainConfig().getStringList("reward-settings.referred")) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("%player%", player.getName()));
        }

        OfflinePlayer other = Bukkit.getOfflinePlayer(otherRefer.getUniqueId());
        for (String command : plugin.getMainConfig().getStringList("reward-settings.referral")) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("%player%", other.getName()));
        }

        if (other.isOnline()) {
            plugin.getLocaleManager().getMessage("messages.referred-player")
                    .replace("%player%", player.getName())
                    .sendMessage(other.getPlayer());
        }
    }
}
