package it.hydr4.ghostmarkers.commands;

import it.hydr4.ghostmarkers.GhostMarkers;
import it.hydr4.ghostmarkers.Marker;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import com.github.retrooper.packetevents.protocol.item.mapdecoration.MapDecorationType;
import com.github.retrooper.packetevents.protocol.item.mapdecoration.MapDecorationTypes;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class MarkerTabCompleter implements TabCompleter {

    private final GhostMarkers plugin;

    public MarkerTabCompleter(GhostMarkers plugin) {
        this.plugin = plugin;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        List<String> commands = new ArrayList<>(Arrays.asList("add", "remove", "list", "info", "edit", "reload"));

        if (args.length == 1) {
            StringUtil.copyPartialMatches(args[0], commands, completions);
            return completions;
        }

        String subCommand = args[0].toLowerCase();
        if (subCommand.equals("add")) {
            return handleAddCompletion(sender, args);
        } else if (subCommand.equals("remove") || subCommand.equals("info")) {
            return handleMarkerIdCompletion(args);
        } else if (subCommand.equals("edit")) {
            return handleEditCompletion(args);
        }

        return Collections.emptyList();
    }

    private List<String> handleAddCompletion(CommandSender sender, String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 3) { // Suggesting marker types
            StringUtil.copyPartialMatches(args[2], MapDecorationTypes.values().stream().map(Object::toString).collect(Collectors.toList()), completions);
        } else if (args.length == 4) { // Suggesting player names or "x,y,z"
            StringUtil.copyPartialMatches(args[3], plugin.getServer().getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList()), completions);
            if ("x,y,z".startsWith(args[3].toLowerCase())) {
                completions.add("x,y,z");
            }
        }
        return completions;
    }

    private List<String> handleMarkerIdCompletion(String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 2) {
            StringUtil.copyPartialMatches(args[1], plugin.getConfigManager().getMarkers().keySet(), completions);
        }
        return completions;
    }

    private List<String> handleEditCompletion(String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 2) { // Suggesting marker IDs
            StringUtil.copyPartialMatches(args[1], plugin.getConfigManager().getMarkers().keySet(), completions);
        } else if (args.length == 3) { // Suggesting properties to edit
            List<String> properties = Arrays.asList("type", "update_interval", "visible_to", "world", "follow", "follow_coords", "conditions");
            StringUtil.copyPartialMatches(args[2], properties, completions);
        }
        return completions;
    }
}
