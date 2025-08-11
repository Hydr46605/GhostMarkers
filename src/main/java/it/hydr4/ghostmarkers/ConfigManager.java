package it.hydr4.ghostmarkers;

import com.google.common.io.Files;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

/**
 * Manages the plugin's configuration and markers.
 */
public class ConfigManager {

    private final GhostMarkers plugin;
    private final Map<String, Marker> markers = new ConcurrentHashMap<>();
    private static final String MARKERS_FILE_NAME = "markers.yml";

    public ConfigManager(GhostMarkers plugin) {
        this.plugin = plugin;
        plugin.saveDefaultConfig();
        loadMarkers();
        loadPremadeMarkers();
    }

    /**
     * Loads all markers from the config.yml file.
     */
    public void loadMarkers() {
        markers.clear();
        try {
            plugin.reloadConfig();
            FileConfiguration config = plugin.getConfig();
            ConfigurationSection markersSection = config.getConfigurationSection("markers");
            if (markersSection == null) {
                return;
            }

            for (String id : markersSection.getKeys(false)) {
                loadMarkerFromConfig(markersSection, id);
            }
            plugin.getLogger().info("Loaded " + markers.size() + " markers from config.yml");
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Error loading markers from config.yml", e);
        }
    }

    private void loadMarkerFromConfig(ConfigurationSection markersSection, String id) {
        ConfigurationSection markerSection = markersSection.getConfigurationSection(id);
        if (markerSection == null) {
            plugin.getLogger().warning("Marker section '" + id + "' is malformed. Skipping.");
            return;
        }

        String type = markerSection.getString("type", "RED_BANNER");
        int updateInterval = markerSection.getInt("update_interval", 20);
        List<String> visibleTo = markerSection.getStringList("visible_to");
        String worldName = markerSection.getString("world", "world");
        List<String> conditions = markerSection.getStringList("conditions");
        String displayName = markerSection.getString("display_name", null);

        String targetPlayer = markerSection.getString("follow");
        Location targetLocation = null;

        if (targetPlayer == null && markerSection.contains("follow_coords")) {
            targetLocation = parseLocation(markerSection.getConfigurationSection("follow_coords"), worldName, id);
        }

        Marker marker = new Marker(id, type, targetPlayer, targetLocation, updateInterval, visibleTo, worldName, conditions, displayName);
        markers.put(id, marker);
    }

    private Location parseLocation(ConfigurationSection coordsSection, String worldName, String markerId) {
        if (coordsSection == null) {
            return null;
        }
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            plugin.getLogger().warning("World '" + worldName + "' not found for marker '" + markerId + "'. Skipping.");
            return null;
        }
        double x = coordsSection.getDouble("x");
        double y = coordsSection.getDouble("y");
        double z = coordsSection.getDouble("z");
        return new Location(world, x, y, z);
    }

    /**
     * Saves all markers to the config.yml file.
     */
    public void saveMarkers() {
        try {
            backupConfig("auto-" + new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(new Date()));
            FileConfiguration config = plugin.getConfig();
            config.set("markers", null); // Clear existing markers
            ConfigurationSection markersSection = config.createSection("markers");

            for (Marker marker : markers.values()) {
                ConfigurationSection markerSection = markersSection.createSection(marker.getId());
                markerSection.set("type", marker.getType());
                markerSection.set("update_interval", marker.getUpdateInterval());
                markerSection.set("visible_to", marker.getVisibleTo());
                markerSection.set("world", marker.getWorld());
                markerSection.set("conditions", marker.getVisibilityConditions());
                markerSection.set("display_name", marker.getDisplayName());

                if (marker.getTargetPlayer() != null) {
                    markerSection.set("follow", marker.getTargetPlayer());
                } else if (marker.getTargetLocation() != null) {
                    ConfigurationSection coordsSection = markerSection.createSection("follow_coords");
                    Location loc = marker.getTargetLocation();
                    coordsSection.set("x", loc.getX());
                    coordsSection.set("y", loc.getY());
                    coordsSection.set("z", loc.getZ());
                }
            }
            plugin.saveConfig();
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Error saving markers to config.yml", e);
        }
    }

    /**
     * Creates a backup of the config.yml file.
     * @param backupName The name of the backup file.
     * @return True if the backup was successful, false otherwise.
     */
    public boolean backupConfig(String backupName) {
        try {
            File configFile = new File(plugin.getDataFolder(), "config.yml");
            File backupFile = new File(plugin.getDataFolder(), "backups/" + backupName + ".yml");
            backupFile.getParentFile().mkdirs();
            Files.copy(configFile, backupFile);
            return true;
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to create config backup: " + backupName, e);
            return false;
        }
    }

    /**
     * Restores the config.yml file from a backup.
     * @param backupName The name of the backup file to restore.
     * @return True if the restore was successful, false otherwise.
     */
    public boolean restoreConfig(String backupName) {
        try {
            File configFile = new File(plugin.getDataFolder(), "config.yml");
            File backupFile = new File(plugin.getDataFolder(), "backups/" + backupName + ".yml");
            if (!backupFile.exists()) {
                return false;
            }
            Files.copy(backupFile, configFile);
            loadMarkers();
            return true;
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to restore config backup: " + backupName, e);
            return false;
        }
    }

    /**
     * @return A map of all markers.
     */
    public Map<String, Marker> getMarkers() {
        return markers;
    }

    /**
     * @param id The ID of the marker to get.
     * @return The marker with the given ID, or null if not found.
     */
    public Marker getMarker(String id) {
        return markers.get(id);
    }

    /**
     * Adds a new marker.
     * @param marker The marker to add.
     */
    public void addMarker(Marker marker) {
        markers.put(marker.getId(), marker);
        saveMarkers();
    }

    /**
     * Removes a marker.
     * @param id The ID of the marker to remove.
     */
    public void removeMarker(String id) {
        markers.remove(id);
        saveMarkers();
    }

    /**
     * Updates a property of a marker.
     * @param id The ID of the marker to update.
     * @param property The property to update.
     * @param value The new value of the property.
     * @return True if the update was successful, false otherwise.
     */
    public boolean updateMarkerProperty(String id, String property, String value) {
        Marker marker = getMarker(id);
        if (marker == null) {
            return false;
        }

        switch (property.toLowerCase()) {
            case "type":
                marker.setType(value.toUpperCase());
                break;
            case "display_name":
                marker.setDisplayName(value);
                break;
            case "update_interval":
                try {
                    marker.setUpdateInterval(Integer.parseInt(value));
                } catch (NumberFormatException e) {
                    return false;
                }
                break;
            case "visible_to":
                marker.getVisibleTo().clear();
                marker.getVisibleTo().addAll(Arrays.asList(value.split(",")));
                break;
            case "world":
                marker.setWorld(value);
                break;
            case "follow":
                marker.setTargetPlayer(value);
                marker.setTargetLocation(null);
                break;
            case "follow_coords":
                String[] coords = value.split(",");
                if (coords.length != 3) return false;
                try {
                    double x = Double.parseDouble(coords[0]);
                    double y = Double.parseDouble(coords[1]);
                    double z = Double.parseDouble(coords[2]);
                    World world = Bukkit.getWorld(marker.getWorld());
                    if (world == null) return false;
                    marker.setTargetLocation(new Location(world, x, y, z));
                    marker.setTargetPlayer(null);
                } catch (NumberFormatException e) {
                    return false;
                }
                break;
            case "conditions":
                marker.getVisibilityConditions().clear();
                marker.getVisibilityConditions().addAll(Arrays.asList(value.split(",")));
                break;
            default:
                return false;
        }
        saveMarkers();
        return true;
    }

    /**
     * Loads premade markers from markers.yml.
     */
    public void loadPremadeMarkers() {
        File premadeMarkersFile = new File(plugin.getDataFolder(), MARKERS_FILE_NAME);
        if (!premadeMarkersFile.exists()) {
            plugin.saveResource(MARKERS_FILE_NAME, false);
        }

        FileConfiguration premadeConfig = YamlConfiguration.loadConfiguration(premadeMarkersFile);

        for (String id : premadeConfig.getKeys(false)) {
            if (markers.containsKey(id)) {
                continue; // Skip if already defined in config.yml
            }
            ConfigurationSection markerSection = premadeConfig.getConfigurationSection(id);
            if (markerSection == null) {
                continue;
            }
            loadMarkerFromConfig(premadeConfig, id);
            plugin.getLogger().info("Loaded premade marker: " + id);
        }
    }
}
