package it.hydr4.ghostmarkers;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;

public class ConditionManager {

    public boolean check(Player player, List<String> conditions) {
        if (conditions == null || conditions.isEmpty()) {
            return true;
        }

        for (String condition : conditions) {
            if (!checkCondition(player, condition)) {
                return false;
            }
        }
        return true;
    }

    private boolean checkCondition(Player player, String condition) {
        String[] parts = condition.split(" ");
        String key = parts[0].toLowerCase();

        switch (key) {
            case "sneaking":
                return player.isSneaking();
            case "on_fire":
                return player.getFireTicks() > 0;
            case "sprinting":
                return player.isSprinting();
            case "health_less_than":
                if (parts.length < 2) return false;
                try {
                    double health = Double.parseDouble(parts[1]);
                    return player.getHealth() < health;
                } catch (NumberFormatException e) {
                    return false;
                }
            case "holding":
                if (parts.length < 2) return false;
                return player.getInventory().getItemInMainHand().getType().name().equalsIgnoreCase(parts[1]);
            default:
                // It might be better to log this warning only once to avoid spam
                // but for now, this is fine.
                Bukkit.getLogger().warning("[GhostMarkers] Unknown condition: " + condition);
                return false;
        }
    }
}
