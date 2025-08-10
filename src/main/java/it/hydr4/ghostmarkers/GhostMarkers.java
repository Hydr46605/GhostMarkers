package it.hydr4.ghostmarkers;

import com.github.retrooper.packetevents.PacketEvents;
import it.hydr4.ghostmarkers.api.GhostMarkersAPI;
import it.hydr4.ghostmarkers.commands.MarkerCommand;
import it.hydr4.ghostmarkers.commands.MarkerTabCompleter;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public class GhostMarkers extends JavaPlugin {

    private ConfigManager configManager;
    private MarkerManager markerManager;
    private ConditionManager conditionManager;
    private MessageManager messageManager;

    @Override
    public void onEnable() {
        // Initialize PacketEvents
        PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this));
        PacketEvents.getAPI().load();

        // Initialize API
        GhostMarkersAPI.init(this);

        // Initialize managers
        messageManager = new MessageManager(this);
        configManager = new ConfigManager(this);
        conditionManager = new ConditionManager();
        markerManager = new MarkerManager(this);


        // Start the marker update task
        markerManager.start();

        // Register commands and tab completer
        Objects.requireNonNull(this.getCommand("marker")).setExecutor(new MarkerCommand(this));
        Objects.requireNonNull(this.getCommand("marker")).setTabCompleter(new MarkerTabCompleter(this));

        // Register listeners
        getServer().getPluginManager().registerEvents(new it.hydr4.ghostmarkers.listeners.PlayerListener(this), this);

        getLogger().info("GhostMarkers has been enabled!");
    }

    @Override
    public void onDisable() {
        // Terminate PacketEvents
        PacketEvents.getAPI().terminate();

        // Stop the marker update task to prevent memory leaks
        if (markerManager != null) {
            markerManager.stop();
        }
        // Save any changes made to markers
        if (configManager != null) {
            configManager.saveMarkers();
        }
        getLogger().info("GhostMarkers has been disabled!");
    }

    public void reload() {
        // Reload managers
        messageManager.loadMessages();
        configManager.loadMarkers();
        configManager.loadPremadeMarkers();

    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public MarkerManager getMarkerManager() {
        return markerManager;
    }

    public ConditionManager getConditionManager() {
        return conditionManager;
    }

    public MessageManager getMessageManager() {
        return messageManager;
    }
}
