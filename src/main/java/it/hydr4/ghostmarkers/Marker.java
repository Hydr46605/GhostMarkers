package it.hydr4.ghostmarkers;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

public class Marker {

    private final String id;
    private String type;
    private String targetPlayer;
    private Location targetLocation;
    private int updateInterval;
    private List<String> visibleTo;
    private String world;
    private List<String> visibilityConditions;
    private String displayName;

    public Marker(String id, String type, String targetPlayer, Location targetLocation, int updateInterval, List<String> visibleTo, String world, List<String> visibilityConditions, String displayName) {
        this.id = id;
        this.type = type;
        this.targetPlayer = targetPlayer;
        this.targetLocation = targetLocation;
        this.updateInterval = updateInterval;
        this.visibleTo = visibleTo;
        this.world = world;
        this.visibilityConditions = visibilityConditions;
    }

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTargetPlayer() {
        return targetPlayer;
    }

    public void setTargetPlayer(String targetPlayer) {
        this.targetPlayer = targetPlayer;
        this.targetLocation = null;
    }

    public Location getTargetLocation() {
        if (targetPlayer != null) {
            Player player = Bukkit.getPlayer(targetPlayer);
            if (player != null) {
                return player.getLocation();
            }
        }
        return targetLocation;
    }

    public void setTargetLocation(Location targetLocation) {
        this.targetLocation = targetLocation;
        this.targetPlayer = null;
    }

    public int getUpdateInterval() {
        return updateInterval;
    }

    public void setUpdateInterval(int updateInterval) {
        this.updateInterval = updateInterval;
    }

    public List<String> getVisibleTo() {
        return visibleTo;
    }

    public void setVisibleTo(List<String> visibleTo) {
        this.visibleTo = visibleTo;
    }

    public boolean isVisibleToAll() {
        return visibleTo.contains("all");
    }

    public String getWorld() {
        return world;
    }

    public void setWorld(String world) {
        this.world = world;
    }

    public List<String> getVisibilityConditions() {
        return visibilityConditions;
    }

    public void setVisibilityConditions(List<String> visibilityConditions) {
        this.visibilityConditions = visibilityConditions;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
}
