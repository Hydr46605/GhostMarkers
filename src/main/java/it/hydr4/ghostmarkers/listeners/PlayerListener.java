package it.hydr4.ghostmarkers.listeners;

import it.hydr4.ghostmarkers.GhostMarkers;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener {

    private final GhostMarkers plugin;

    public PlayerListener(GhostMarkers plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        plugin.getMarkerManager().startTaskForPlayer(event.getPlayer());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        plugin.getMarkerManager().stopTaskForPlayer(event.getPlayer());
    }
}
