package com.mysticalkingdoms.mysticalrefer;

import com.mysticalkingdoms.mysticalrefer.commands.ReferAdminCommand;
import com.mysticalkingdoms.mysticalrefer.commands.ReferCommand;
import com.mysticalkingdoms.mysticalrefer.enums.GeneratorType;
import com.mysticalkingdoms.mysticalrefer.hooks.ReferPlaceholderHook;
import com.mysticalkingdoms.mysticalrefer.listeners.ConnectionListeners;
import com.mysticalkingdoms.mysticalrefer.locale.LocaleManager;
import com.mysticalkingdoms.mysticalrefer.managers.AbstractStorageManager;
import com.mysticalkingdoms.mysticalrefer.managers.impl.SQLStorage;
import com.mysticalkingdoms.mysticalrefer.managers.impl.SQLiteStorage;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.dvs.versioning.BasicVersioning;
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import revxrsal.commands.Lamp;
import revxrsal.commands.bukkit.BukkitLamp;
import revxrsal.commands.bukkit.actor.BukkitCommandActor;

import java.io.File;
import java.io.IOException;
import java.util.regex.Pattern;

public class MysticalRefer extends JavaPlugin {

    private BukkitAudiences adventure;
    private YamlDocument mainConfig;
    private LocaleManager localeManager;
    private AbstractStorageManager storageManager;
    private GeneratorType generatorType;

    @Override
    public void onEnable() {
        this.adventure = BukkitAudiences.create(this);
        this.mainConfig = this.createUpdatingConfig(new File(this.getDataFolder(), "config.yml"));
        this.localeManager = new LocaleManager(this);
        this.generatorType = GeneratorType.valueOf(this.mainConfig.getString("code-settings.type"));
        if (this.mainConfig.getString("storage-settings.type").equalsIgnoreCase("mysql")) {
            this.storageManager = new SQLStorage(this);
        } else {
            this.storageManager = new SQLiteStorage(this);
        }
        this.storageManager.init();
        this.storageManager.loadData();

        Bukkit.getPluginManager().registerEvents(new ConnectionListeners(this), this);

        Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> storageManager.saveAllPlayers(),
                0L, mainConfig.getLong("time-settings.auto-save") * 20);

        Lamp<BukkitCommandActor> lamp = BukkitLamp.builder(this).build();
        lamp.register(new ReferCommand(this));
        lamp.register(new ReferAdminCommand(this));

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new ReferPlaceholderHook(this).register();
        }
    }

    private static final Pattern PATTERN = Pattern.compile("[a-zA-Z0-9]");
    public boolean isValidCode(String code) {
        return !PATTERN.matcher(code).matches();
    }

    public String generateCode() {
        return generatorType.generateCode(this.mainConfig.getInt("code-settings.length"));
    }

    @Override
    public void onDisable() {
        this.storageManager.saveAllPlayers();
        this.storageManager.close();
    }

    public BukkitAudiences getAdventure() {
        return adventure;
    }

    public YamlDocument getMainConfig() {
        return mainConfig;
    }

    public LocaleManager getLocaleManager() {
        return localeManager;
    }

    public AbstractStorageManager getStorageManager() {
        return storageManager;
    }

    /**
     * Create a configuration file that does NOT update.
     * @param file File to create.
     * @return The configuration file created.
     */
    public YamlDocument createConfig(File file) {
        try {
            return YamlDocument.create(file, getResource(file.getName()));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Create a configuration file that automatically updates. Requires config-version to be defined.
     * @param file File to create.
     * @return The configuration file created.
     */
    public YamlDocument createUpdatingConfig(File file) {
        try {
            return YamlDocument.create(file, getResource(file.getName()),
                    GeneralSettings.DEFAULT,
                    LoaderSettings.builder().setAutoUpdate(true).build(),
                    DumperSettings.DEFAULT,
                    UpdaterSettings.builder().setVersioning(new BasicVersioning("config-version")).build());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
