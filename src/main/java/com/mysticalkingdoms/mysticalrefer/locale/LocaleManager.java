package com.mysticalkingdoms.mysticalrefer.locale;

import com.mysticalkingdoms.mysticalrefer.MysticalRefer;
import dev.dejvokep.boostedyaml.YamlDocument;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class LocaleManager {

    private final MysticalRefer plugin;
    private final YamlDocument langFile;

    public LocaleManager(MysticalRefer plugin) {
        this.plugin = plugin;
        this.langFile = plugin.createUpdatingConfig(new File(plugin.getDataFolder(), "lang.yml"));
    }

    public Message getMessage(String path) {
        List<String> messages;
        if (langFile.isString(path)) {
            messages = new ArrayList<>();
            messages.add(langFile.getString(path));
        }else{
            messages = langFile.getStringList(path);
        }

        return new Message(plugin.getAdventure(), messages.stream()
                .map(text -> MiniMessage.miniMessage().deserialize(text))
                .collect(Collectors.toList()));
    }

    public YamlDocument getLangFile() {
        return langFile;
    }

    public void reload() throws IOException {
        langFile.reload();
    }
}

