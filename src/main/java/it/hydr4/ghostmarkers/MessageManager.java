package it.hydr4.ghostmarkers;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class MessageManager {

    private final GhostMarkers plugin;
    private FileConfiguration messagesConfig;
    private final Map<String, String> messages = new HashMap<>();
    private String prefix;

    public MessageManager(GhostMarkers plugin) {
        this.plugin = plugin;
        loadMessages();
    }

    public void loadMessages() {
        File messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        if (!messagesFile.exists()) {
            plugin.saveResource("messages.yml", false);
        }

        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
        messages.clear();

        // Load messages from the file, falling back to defaults if necessary
        InputStream defaultConfigStream = plugin.getResource("messages.yml");
        if (defaultConfigStream != null) {
            YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defaultConfigStream, StandardCharsets.UTF_8));
            messagesConfig.setDefaults(defaultConfig);
        }

        for (String key : messagesConfig.getKeys(true)) {
            if (!messagesConfig.isConfigurationSection(key)) {
                messages.put(key, messagesConfig.getString(key));
            }
        }

        prefix = messages.getOrDefault("prefix", "&8[&6GhostMarkers&8] &r");

        plugin.getLogger().info("Loaded " + messages.size() + " messages.");
    }

    public String get(String key, String... replacements) {
        String message = messages.getOrDefault(key, "Missing message: " + key);

        for (int i = 0; i < replacements.length; i += 2) {
            if (i + 1 < replacements.length) {
                message = message.replace(replacements[i], replacements[i + 1]);
            }
        }

        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public String getPrefixed(String key, String... replacements) {
        String message = get(key, replacements);
        return ChatColor.translateAlternateColorCodes('&', prefix) + message;
    }
}
