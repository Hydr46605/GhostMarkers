package it.hydr4.ghostmarkers;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.item.mapdecoration.MapDecorationType;
import com.github.retrooper.packetevents.protocol.item.mapdecoration.MapDecorationTypes;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerMapData;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapView;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Manages the markers and sends the map packets to the players.
 */
public class MarkerManager {

    private final GhostMarkers plugin;
    private final Map<UUID, BukkitTask> playerUpdateTasks = new ConcurrentHashMap<>();

    public MarkerManager(GhostMarkers plugin) {
        this.plugin = plugin;
    }

    /**
     * Starts the marker update tasks for all online players.
     */
    public void start() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            startTaskForPlayer(player);
        }
    }

    /**
     * Stops all marker update tasks.
     */
    public void stop() {
        for (BukkitTask task : playerUpdateTasks.values()) {
            task.cancel();
        }
        playerUpdateTasks.clear();
    }

    /**
     * Starts a marker update task for a specific player.
     * @param player The player to start the task for.
     */
    public void startTaskForPlayer(Player player) {
        stopTaskForPlayer(player);

        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline()) {
                    this.cancel();
                    return;
                }
                updateMarkersForPlayer(player);
            }
        }.runTaskTimerAsynchronously(plugin, 0L, 5L);

        playerUpdateTasks.put(player.getUniqueId(), task);
    }

    /**
     * Stops the marker update task for a specific player.
     * @param player The player to stop the task for.
     */
    public void stopTaskForPlayer(Player player) {
        BukkitTask existingTask = playerUpdateTasks.remove(player.getUniqueId());
        if (existingTask != null) {
            existingTask.cancel();
        }
    }

    /**
     * Checks if a player is holding a map and triggers a map update.
     * @param player The player to update the markers for.
     */
    private void updateMarkersForPlayer(Player player) {
        new BukkitRunnable() {
            @Override
            public void run() {
                ItemStack item = player.getInventory().getItemInMainHand();
                if (item.getType() != Material.FILLED_MAP) {
                    item = player.getInventory().getItemInOffHand();
                }

                if (item.getType() != Material.FILLED_MAP) {
                    return;
                }

                if (item.getItemMeta() == null) {
                    return;
                }

                MapMeta mapMeta = (MapMeta) item.getItemMeta();
                if (!mapMeta.hasMapView()) {
                    return;
                }

                MapView mapView = mapMeta.getMapView();
                if (mapView == null) {
                    return;
                }

                sendMapUpdate(player, mapView);
            }
        }.runTask(plugin);
    }

    /**
     * Sends a map update packet to a player with the visible markers.
     * @param player The player to send the packet to.
     * @param mapView The map view to update.
     */
    private void sendMapUpdate(Player player, MapView mapView) {
        List<WrapperPlayServerMapData.MapDecoration> decorations = new ArrayList<>();

        List<Marker> visibleMarkers = plugin.getConfigManager().getMarkers().values().stream()
                .filter(marker -> marker.getWorld().equalsIgnoreCase(player.getWorld().getName()))
                .filter(marker -> marker.isVisibleToAll() || marker.getVisibleTo().contains(player.getName()))
                .filter(marker -> plugin.getConditionManager().check(player, marker.getVisibilityConditions()))
                .collect(Collectors.toList());

        if (visibleMarkers.isEmpty()) {
            return;
        }

        for (Marker marker : visibleMarkers) {
            Location location = marker.getTargetLocation();
            if (location == null) {
                plugin.getLogger().warning("Marker '" + marker.getId() + "' has a null location, skipping.");
                continue;
            }

            double mapX = (location.getX() - mapView.getCenterX()) / (double) (1 << mapView.getScale().getValue());
            double mapZ = (location.getZ() - mapView.getCenterZ()) / (double) (1 << mapView.getScale().getValue());

            if (mapX < -128 || mapX > 127 || mapZ < -128 || mapZ > 127) {
                // Marker is off the map, so we don't display it.
                continue;
            }

            byte x = (byte) mapX;
            byte z = (byte) mapZ;
            byte rotation = (byte) Math.floorMod((int) Math.round(location.getYaw() / 360.0 * 16.0), 16);

            MapDecorationType iconType = MapDecorationTypes.getByName(marker.getType().toUpperCase());
            if (iconType == null) {
                plugin.getLogger().warning("Invalid marker type in config: '" + marker.getType() + "' for marker '" + marker.getId() + "'. Defaulting to RED_X.");
                iconType = MapDecorationTypes.RED_X;
            }

            WrapperPlayServerMapData.MapDecoration decoration = new WrapperPlayServerMapData.MapDecoration(iconType, x, z, rotation, null);
            decorations.add(decoration);
        }

        WrapperPlayServerMapData mapPacket = new WrapperPlayServerMapData(
                mapView.getId(),
                mapView.getScale().getValue(),
                true,
                false,
                decorations,
                0, 0, 0, 0, new byte[0]
        );

        PacketEvents.getAPI().getPlayerManager().sendPacket(player, mapPacket);
    }
}
