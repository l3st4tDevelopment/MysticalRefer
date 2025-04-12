package com.mysticalkingdoms.mysticalrefer.players;

import org.bukkit.entity.Player;

import java.util.UUID;

public class ReferPlayer {

    private final UUID uniqueId;

    private boolean usedCode;
    private int referredPlayers;
    private String referralCode;

    private boolean dirty;

    public ReferPlayer(UUID uniqueId, boolean usedCode, int referredPlayers, String referralCode) {
        this.uniqueId = uniqueId;
        this.usedCode = usedCode;
        this.referredPlayers = referredPlayers;
        this.referralCode = referralCode;
    }

    public UUID getUniqueId() {
        return uniqueId;
    }

    public void resetDirty() {
        this.dirty = false;
    }

    public boolean isDirty() {
        return dirty;
    }

    public boolean hasUsedCode() {
        return usedCode;
    }

    public void setUsedCode(boolean usedCode) {
        this.usedCode = usedCode;
        this.dirty = true;
    }

    public int getReferredPlayers() {
        return referredPlayers;
    }

    public void setReferredPlayers(int referredPlayers) {
        this.referredPlayers = referredPlayers;
        this.dirty = true;
    }

    public String getReferralCode() {
        return referralCode;
    }

    public void setReferralCode(String referralCode) {
        this.referralCode = referralCode;
        this.dirty = true;
    }

    public static ReferPlayer createNewPlayer(Player player, String code) {
        return new ReferPlayer(player.getUniqueId(), false, 0, code);
    }
}
