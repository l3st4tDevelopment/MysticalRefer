package com.mysticalkingdoms.mysticalrefer.managers.impl;

import com.mysticalkingdoms.mysticalrefer.MysticalRefer;
import com.mysticalkingdoms.mysticalrefer.managers.AbstractStorageManager;
import com.mysticalkingdoms.mysticalrefer.players.ReferPlayer;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.*;
import java.util.*;

public class SQLStorage extends AbstractStorageManager {

    private final String prefix;

    private final String insert;
    private final String update;

    private HikariDataSource hikariDataSource;

    public SQLStorage(MysticalRefer plugin) {
        super(plugin);

        this.prefix = plugin.getConfig().getString("storage-settings.mysql.prefix");
        this.insert = "INSERT INTO " + this.prefix + "refers VALUES(?,?,?,?) ON DUPLICATE KEY UPDATE REFERRAL_CODE=?";
        this.update = "UPDATE " + this.prefix + "refers SET USED_CODE=?,REFERRED_PLAYERS=? WHERE UUID=?";
    }


    @Override
    protected Map<UUID, ReferPlayer> loadPlayerData() {
        Map<UUID, ReferPlayer> referPlayers = new HashMap<>();
        try (Connection connection = hikariDataSource.getConnection();
             Statement statement = connection.createStatement()) {
            ResultSet resultSet = statement.executeQuery("SELECT * FROM " + this.prefix + "refers");
            while (resultSet.next()) {
                UUID uuid = UUID.fromString(resultSet.getString("UUID"));
                boolean usedCode = resultSet.getBoolean("USED_CODE");
                int referredPlayers = resultSet.getInt("REFERRED_PLAYERS");
                String referralCode = resultSet.getString("REFERRAL_CODE");

                referPlayers.put(uuid, new ReferPlayer(uuid, usedCode, referredPlayers, referralCode));
            }
            resultSet.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return referPlayers;
    }

    @Override
    protected void createAccount(ReferPlayer referPlayer) {
        try (Connection connection = hikariDataSource.getConnection();
             PreparedStatement insert = connection.prepareStatement(this.insert)) {

            insert.setString(1, referPlayer.getUniqueId().toString());
            insert.setString(2, referPlayer.getReferralCode());
            insert.setBoolean(3, referPlayer.hasUsedCode());
            insert.setInt(4, referPlayer.getReferredPlayers());
            insert.setString(5, referPlayer.getReferralCode());
            insert.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void saveBatch(Map<UUID, ReferPlayer> batch) {
        try (Connection connection = hikariDataSource.getConnection();
             PreparedStatement update = connection.prepareStatement(this.update)) {
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
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void init() {
        setupHikari();
        createTable();
    }

    private void setupHikari() {
        String host = plugin.getConfig().getString("storage-settings.mysql.host");
        int port = plugin.getConfig().getInt("storage-settings.mysql.port");
        String username = plugin.getConfig().getString("storage-settings.mysql.username");
        String password = plugin.getConfig().getString("storage-settings.mysql.password");
        String database = plugin.getConfig().getString("storage-settings.mysql.database");

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + database);
        config.setUsername(username);
        config.setPassword(password);
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        this.hikariDataSource = new HikariDataSource(config);
    }

    private void createTable() {
        try (Connection connection = hikariDataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS " + this.prefix + "refers(UUID varchar(36) UNIQUE, REFERRAL_CODE varchar(32), USED_CODE boolean, REFERRED_PLAYERS integer)");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void close() {
        if (hikariDataSource != null) {
            hikariDataSource.close();
        }
    }
}