package it.hydr4.ghostmarkers.commands;

import it.hydr4.ghostmarkers.GhostMarkers;
import it.hydr4.ghostmarkers.Marker;
import it.hydr4.ghostmarkers.MessageManager;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import com.github.retrooper.packetevents.protocol.item.mapdecoration.MapDecorationType;
import com.github.retrooper.packetevents.protocol.item.mapdecoration.MapDecorationTypes;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class MarkerCommand implements CommandExecutor {

    private final GhostMarkers plugin;
    private final MessageManager messages;

    public MarkerCommand(GhostMarkers plugin) {
        this.plugin = plugin;
        this.messages = plugin.getMessageManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(messages.getPrefixed("usage"));
            return true;
        }

        String subCommand = args[0].toLowerCase();
        switch (subCommand) {
            case "add":
                return handleAdd(sender, args);
            case "remove":
                return handleRemove(sender, args);
            case "list":
                return handleList(sender);
            case "info":
                return handleInfo(sender, args);
            case "edit":
                return handleEdit(sender, args);
            case "reload":
                return handleReload(sender);
            case "admin":
                return handleAdmin(sender, args);
            default:
                sender.sendMessage(messages.getPrefixed("invalid_subcommand"));
                return true;
        }
    }

    private boolean handleAdd(CommandSender sender, String[] args) {
        if (!sender.hasPermission("ghostmarkers.command.add")) {
            sender.sendMessage(messages.getPrefixed("no_permission"));
            return true;
        }
        if (args.length < 4) {
            sender.sendMessage(messages.getPrefixed("add_usage"));
            return true;
        }

        String id = args[1];
        if (plugin.getConfigManager().getMarker(id) != null) {
            sender.sendMessage(messages.getPrefixed("add_fail_exists", "%id%", id));
            return true;
        }

        String typeStr = args[2].toUpperCase();
        if (MapDecorationTypes.getByName(typeStr) == null) {
            sender.sendMessage(messages.getPrefixed("add_fail_invalid_type", "%type%", typeStr));
            String validTypes = MapDecorationTypes.values().stream().map(Object::toString).collect(Collectors.joining(", "));
            sender.sendMessage(messages.get("valid_types", "%types%", validTypes));
            return true;
        }

        String target = args[3];
        String targetPlayer = null;
        Location targetLocation = null;
        String worldName;

        if (args.length > 4) {
            worldName = args[4];
        } else if (sender instanceof Player) {
            worldName = ((Player) sender).getWorld().getName();
        } else {
            sender.sendMessage(messages.getPrefixed("add_fail_console_no_world"));
            return true;
        }

        if (plugin.getServer().getWorld(worldName) == null) {
            sender.sendMessage(messages.getPrefixed("add_fail_world_not_found", "%world%", worldName));
            return true;
        }

        if (target.contains(",")) {
            String[] coords = target.split(",");
            if (coords.length != 3) {
                sender.sendMessage(messages.getPrefixed("add_fail_invalid_coords"));
                return true;
            }
            try {
                double x = Double.parseDouble(coords[0]);
                double y = Double.parseDouble(coords[1]);
                double z = Double.parseDouble(coords[2]);
                targetLocation = new Location(plugin.getServer().getWorld(worldName), x, y, z);
            } catch (NumberFormatException e) {
                sender.sendMessage(messages.getPrefixed("add_fail_invalid_number"));
                return true;
            }
        } else {
            targetPlayer = target;
        }

        List<String> visibleTo = new ArrayList<>(Arrays.asList("all"));
        List<String> conditions = new ArrayList<>();

        if (args.length > 5) {
            for (int i = 5; i < args.length; i++) {
                String[] parts = args[i].split(":", 2);
                if (parts.length != 2) {
                    sender.sendMessage(messages.getPrefixed("add_fail_invalid_argument", "%arg%", args[i]));
                    return true;
                }
                String key = parts[0].toLowerCase();
                String value = parts[1];

                switch (key) {
                    case "visible":
                        visibleTo = Arrays.asList(value.split(","));
                        break;
                    case "conditions":
                        conditions = Arrays.asList(value.split(","));
                        break;
                    default:
                        sender.sendMessage(messages.getPrefixed("add_fail_unknown_argument", "%key%", key));
                        return true;
                }
            }
        }

        Marker marker = new Marker(id, typeStr, targetPlayer, targetLocation, 20, visibleTo, worldName, conditions, null);
        plugin.getConfigManager().addMarker(marker);
        sender.sendMessage(messages.getPrefixed("add_success", "%id%", id));
        return true;
    }

    private boolean handleRemove(CommandSender sender, String[] args) {
        if (!sender.hasPermission("ghostmarkers.command.remove")) {
            sender.sendMessage(messages.getPrefixed("no_permission"));
            return true;
        }
        if (args.length != 2) {
            sender.sendMessage(messages.getPrefixed("remove_usage"));
            return true;
        }
        String id = args[1];
        if (plugin.getConfigManager().getMarker(id) == null) {
            sender.sendMessage(messages.getPrefixed("remove_fail_not_found", "%id%", id));
            return true;
        }
        plugin.getConfigManager().removeMarker(id);
        sender.sendMessage(messages.getPrefixed("remove_success", "%id%", id));
        return true;
    }

    private boolean handleList(CommandSender sender) {
        if (!sender.hasPermission("ghostmarkers.command.list")) {
            sender.sendMessage(messages.getPrefixed("no_permission"));
            return true;
        }
        List<Marker> markers = new ArrayList<>(plugin.getConfigManager().getMarkers().values());
        if (markers.isEmpty()) {
            sender.sendMessage(messages.getPrefixed("list_empty"));
            return true;
        }

        sender.sendMessage(messages.get("list_header"));
        for (Marker marker : markers) {
            String targetStr;
            if (marker.getTargetPlayer() != null) {
                targetStr = messages.get("list_target_following", "%player%", marker.getTargetPlayer());
            } else if (marker.getTargetLocation() != null) {
                Location loc = marker.getTargetLocation();
                if (loc.getWorld() == null) {
                    targetStr = messages.get("list_target_location_unloaded",
                            "%x%", String.format("%.1f", loc.getX()),
                            "%y%", String.format("%.1f", loc.getY()),
                            "%z%", String.format("%.1f", loc.getZ()),
                            "%world%", marker.getWorld());
                } else {
                    targetStr = messages.get("list_target_location",
                            "%x%", String.format("%.1f", loc.getX()),
                            "%y%", String.format("%.1f", loc.getY()),
                            "%z%", String.format("%.1f", loc.getZ()),
                            "%world%", loc.getWorld().getName());
                }
            } else {
                targetStr = messages.get("list_target_none");
            }
            sender.sendMessage(messages.get("list_item", "%id%", marker.getId(), "%target%", targetStr));
        }
        return true;
    }

    private boolean handleInfo(CommandSender sender, String[] args) {
        if (!sender.hasPermission("ghostmarkers.command.info")) {
            sender.sendMessage(messages.getPrefixed("no_permission"));
            return true;
        }
        if (args.length != 2) {
            sender.sendMessage(messages.getPrefixed("info_usage"));
            return true;
        }
        String id = args[1];
        Marker marker = plugin.getConfigManager().getMarker(id);
        if (marker == null) {
            sender.sendMessage(messages.getPrefixed("info_fail_not_found", "%id%", id));
            return true;
        }

        sender.sendMessage(messages.get("info_header", "%id%", id));
        sender.sendMessage(messages.get("info_line", "%key%", "Type", "%value%", marker.getType()));
        sender.sendMessage(messages.get("info_line", "%key%", "World", "%value%", marker.getWorld()));
        sender.sendMessage(messages.get("info_line", "%key%", "Update Interval", "%value%", String.valueOf(marker.getUpdateInterval())));
        sender.sendMessage(messages.get("info_line", "%key%", "Visible To", "%value%", String.join(", ", marker.getVisibleTo())));
        sender.sendMessage(messages.get("info_line", "%key%", "Conditions", "%value%", String.join(", ", marker.getVisibilityConditions())));

        if (marker.getTargetPlayer() != null) {
            sender.sendMessage(messages.get("info_line", "%key%", "Following", "%value%", marker.getTargetPlayer()));
        } else if (marker.getTargetLocation() != null) {
            Location loc = marker.getTargetLocation();
            String locStr = String.format("%.1f, %.1f, %.1f", loc.getX(), loc.getY(), loc.getZ());
            sender.sendMessage(messages.get("info_line", "%key%", "Location", "%value%", locStr));
        }
        return true;
    }

    private boolean handleEdit(CommandSender sender, String[] args) {
        if (!sender.hasPermission("ghostmarkers.command.edit")) {
            sender.sendMessage(messages.getPrefixed("no_permission"));
            return true;
        }
        if (args.length < 4) {
            sender.sendMessage(messages.getPrefixed("edit_usage"));
            return true;
        }

        String id = args[1];
        Marker marker = plugin.getConfigManager().getMarker(id);
        if (marker == null) {
            sender.sendMessage(messages.getPrefixed("edit_fail_not_found", "%id%", id));
            return true;
        }

        String property = args[2].toLowerCase();
        String value = String.join(" ", Arrays.copyOfRange(args, 3, args.length));

        boolean success = plugin.getConfigManager().updateMarkerProperty(id, property, value);

        if (success) {
            sender.sendMessage(messages.getPrefixed("edit_success", "%property%", property, "%id%", id));
        } else {
            sender.sendMessage(messages.getPrefixed("edit_fail_invalid_property", "%property%", property));
            sender.sendMessage(messages.get("valid_properties", "%properties%", "type, update_interval, visible_to, world, follow, follow_coords, conditions"));
        }
        return true;
    }

    private boolean handleReload(CommandSender sender) {
        if (!sender.hasPermission("ghostmarkers.command.reload")) {
            sender.sendMessage(messages.getPrefixed("no_permission"));
            return true;
        }
        try {
            plugin.reload();
            sender.sendMessage(messages.getPrefixed("reload_success"));
        } catch (Exception e) {
            sender.sendMessage(messages.getPrefixed("reload_fail"));
            plugin.getLogger().log(java.util.logging.Level.SEVERE, "Failed to reload plugin", e);
        }
        return true;
    }

    private boolean handleAdmin(CommandSender sender, String[] args) {
        if (!sender.hasPermission("ghostmarkers.command.admin")) {
            sender.sendMessage(messages.getPrefixed("no_permission"));
            return true;
        }
        if (args.length < 2) {
            sender.sendMessage(messages.getPrefixed("admin_usage"));
            return true;
        }

        String adminCommand = args[1].toLowerCase();
        switch (adminCommand) {
            case "backup":
                return handleBackup(sender, args);
            case "restore":
                return handleRestore(sender, args);
            default:
                sender.sendMessage(messages.getPrefixed("admin_invalid_subcommand"));
                return true;
        }
    }

    private boolean handleBackup(CommandSender sender, String[] args) {
        if (args.length != 3) {
            sender.sendMessage(messages.getPrefixed("admin_backup_usage"));
            return true;
        }
        String backupName = args[2];
        if (plugin.getConfigManager().backupConfig(backupName)) {
            sender.sendMessage(messages.getPrefixed("admin_backup_success", "%name%", backupName));
        } else {
            sender.sendMessage(messages.getPrefixed("admin_backup_fail", "%name%", backupName));
        }
        return true;
    }

    private boolean handleRestore(CommandSender sender, String[] args) {
        if (args.length != 3) {
            sender.sendMessage(messages.getPrefixed("admin_restore_usage"));
            return true;
        }
        String backupName = args[2];
        if (plugin.getConfigManager().restoreConfig(backupName)) {
            sender.sendMessage(messages.getPrefixed("admin_restore_success", "%name%", backupName));
        } else {
            sender.sendMessage(messages.getPrefixed("admin_restore_fail_not_found", "%name%", backupName));
        }
        return true;
    }
}
