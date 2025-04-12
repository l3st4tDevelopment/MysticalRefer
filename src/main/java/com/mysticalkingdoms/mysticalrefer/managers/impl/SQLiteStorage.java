package com.mysticalkingdoms.mysticalrefer.managers.impl;

import com.mysticalkingdoms.mysticalrefer.MysticalRefer;
import com.mysticalkingdoms.mysticalrefer.managers.AbstractStorageManager;
import com.mysticalkingdoms.mysticalrefer.players.ReferPlayer;

import java.io.File;
import java.sql.*;
import java.util.*;

public class SQLiteStorage extends AbstractStorageManager {

    private Connection connection;
    private final String connectionUrl;

    private static final String INSERT = "INSERT INTO MysticalRefer VALUES(?,?,?,?) ON CONFLICT(UUID) DO UPDATE SET REFERRAL_CODE=?";
    private static final String SAVE = "UPDATE MysticalRefer SET USED_CODE=?,REFERRED_PLAYERS=? WHERE UUID=?";

    public SQLiteStorage(MysticalRefer plugin) {
        super(plugin);
        this.connectionUrl = "jdbc:sqlite:" + plugin.getDataFolder().getAbsolutePath() + File.separator + "mysticalrefer.db";
    }

    @Override
    public void init() {
        try {
            this.connection = DriverManager.getConnection(this.connectionUrl);
        } catch (SQLException e) {
            plugin.getLogger().severe("An error occurred retrieving the SQLite database connection: " + e.getMessage());
        }

        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS MysticalRefer(UUID varchar(36) UNIQUE, REFERRAL_CODE varchar(32), USED_CODE boolean, REFERRED_PLAYERS integer)");
        }catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected Map<UUID, ReferPlayer> loadPlayerData() {
        Map<UUID, ReferPlayer> referPlayers = new HashMap<>();
        try (Statement statement = connection.createStatement()) {
            ResultSet resultSet = statement.executeQuery("SELECT * FROM MysticalRefer");
            while (resultSet.next()) {
                UUID uuid = UUID.fromString(resultSet.getString("UUID"));
                boolean usedCode = resultSet.getBoolean("USED_CODE");
                int referredPlayers = resultSet.getInt("REFERRED_PLAYERS");
                String referralCode = resultSet.getString("REFERRAL_CODE");

                referPlayers.put(uuid, new ReferPlayer(uuid, usedCode, referredPlayers, referralCode));
            }
            resultSet.close();
        }catch (SQLException e) {
            e.printStackTrace();
        }

        return referPlayers;
    }

    @Override
    protected void createAccount(ReferPlayer referPlayer) {
        try (PreparedStatement insert = connection.prepareStatement(INSERT)) {
            insert.setString(1, referPlayer.getUniqueId().toString());
            insert.setString(2, referPlayer.getReferralCode());
            insert.setBoolean(3, referPlayer.hasUsedCode());
            insert.setInt(4, referPlayer.getReferredPlayers());
            insert.setString(5, referPlayer.getReferralCode());
            insert.execute();
        }catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void saveBatch(Map<UUID, ReferPlayer> batch) {
        try (PreparedStatement update = connection.prepareStatement(SAVE)) {
            for (Map.Entry<UUID, ReferPlayer> entry : batch.entrySet()) {
                if (entry.getValue().isDirty()) {
                    update.setBoolean(1, entry.getValue().hasUsedCode());
                    update.setInt(2, entry.getValue().getReferredPlayers());
                    update.setString(3, entry.getKey().toString());
                    update.addBatch();

                    entry.getValue().resetDirty();
                }
            }

            update.executeBatch();
        }catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void close() {
        try {
            if (this.connection != null) {
                this.connection.close();
            }
        } catch (SQLException ex) {
            this.plugin.getLogger().severe("An error occurred closing the SQLite database connection: " + ex.getMessage());
        }
    }
}