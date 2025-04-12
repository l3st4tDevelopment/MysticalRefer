package com.mysticalkingdoms.mysticalrefer.commands;

import com.mysticalkingdoms.mysticalrefer.MysticalRefer;
import com.mysticalkingdoms.mysticalrefer.players.ReferPlayer;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.Subcommand;
import revxrsal.commands.bukkit.annotation.CommandPermission;

@Command("referadmin")
@CommandPermission("mysticalrefer.admin")
public class ReferAdminCommand {

    private final MysticalRefer plugin;
    public ReferAdminCommand(MysticalRefer plugin) {
        this.plugin = plugin;
    }

    @Subcommand("help")
    public void sendHelpMenu(CommandSender sender) {
        plugin.getLocaleManager().getMessage("messages.admin-help").sendMessage(sender);
    }

    @Subcommand("reset")
    public void onReset(CommandSender sender, OfflinePlayer player) {
        ReferPlayer referPlayer = plugin.getStorageManager().getPlayer(player);
        if (referPlayer == null) {
            plugin.getLocaleManager().getMessage("messages.invalid-player").sendMessage(sender);
            return;
        }

        referPlayer.setReferredPlayers(0);
        referPlayer.setUsedCode(false);

        plugin.getLocaleManager().getMessage("messages.player-reset").sendMessage(sender);
    }
}
