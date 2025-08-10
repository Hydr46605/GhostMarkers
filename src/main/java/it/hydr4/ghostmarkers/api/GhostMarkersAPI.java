package it.hydr4.ghostmarkers.api;

import it.hydr4.ghostmarkers.GhostMarkers;
import it.hydr4.ghostmarkers.Marker;
import org.bukkit.Location;

import java.util.List;

public class GhostMarkersAPI {

    private static GhostMarkers plugin;

    public static void init(GhostMarkers plugin) {
        GhostMarkersAPI.plugin = plugin;
    }

    public static Marker getMarker(String id) {
        return plugin.getConfigManager().getMarker(id);
    }

    public static List<Marker> getAllMarkers() {
        return new java.util.ArrayList<>(plugin.getConfigManager().getMarkers().values());
    }

    public static void createMarker(String id, String type, String targetPlayer, Location targetLocation, int updateInterval, List<String> visibleTo, String world) {
        Marker marker = new Marker(id, type, targetPlayer, targetLocation, updateInterval, visibleTo, world, null);
        plugin.getConfigManager().addMarker(marker);
    }

    public static void removeMarker(String id) {
        plugin.getConfigManager().removeMarker(id);
    }
}
