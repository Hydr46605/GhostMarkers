<div align="center">

# 👻 GhostMarkers 👻

![GhostMarkers Banner](https://i.imgur.com/6iesqTy.png)

</div>

**GhostMarkers** is a lightweight and powerful Spigot plugin that allows you to create virtual markers on maps. These markers can be fixed at specific coordinates or can dynamically follow players, making them perfect for a variety of uses, such as marking points of interest, tracking players, or creating custom waypoints.

## ✨ Features

*   **Dynamic and Static Markers:** Create markers that are either fixed at a specific location or dynamically follow a player.
*   **Custom Marker Types:** Choose from any of the available `MapIcon.Type` values to customize the appearance of your markers.
*   **Visibility Control:** Control who can see the markers. You can make them visible to everyone, or only to specific players.
*   **Conditional Visibility:** Show or hide markers based on player conditions (e.g., sneaking, on fire).
*   **Developer API:** An easy-to-use API to manage markers from your own plugins.
*   **Easy to Use:** A simple and intuitive command system makes it easy to manage your markers.
*   **Lightweight and Performant:** Designed to be as lightweight as possible to minimize any impact on your server's performance.

## Installation

1.  Download the latest version of GhostMarkers from the [releases page](https://github.com/your-repo/ghostmarkers/releases).
2.  Make sure you have [PacketEvents](https://github.com/retrooper/packetevents) installed on your server. GhostMarkers requires it to function.
3.  Place the downloaded `.jar` file into your server's `plugins` directory.
4.  Restart or reload your server.

## 📖 Commands

Here is a list of all the available commands for GhostMarkers:

| Command                                                      | Description                               |
| ------------------------------------------------------------ | ----------------------------------------- |
| `/marker add <id> <type> <player_name\|x,y,z> [world] [visible_to...] [conditions: ...]` | Adds a new marker.                        |
| `/marker remove <id>`                                        | Removes an existing marker.               |
| `/marker list`                                               | Lists all the currently configured markers. |
| `/marker info <id>`                                          | Displays detailed information about a specific marker. |
| `/marker edit <id> <property> <value>`                       | Edits a property of an existing marker. |
| `/marker reload`                                             | Reloads the plugin's configuration files. |
| `/marker admin <backup\|restore> [name]`                     | Manages configuration backups. |

**Aliases:** `/gm`, `/ghostmarker`

## 🔒 Permissions

| Permission           | Description                               | Default |
| -------------------- | ----------------------------------------- | ------- |
| `ghostmarkers.command` | Base permission for all GhostMarkers commands. | op |
| `ghostmarkers.command.add` | Allows adding new markers. | op |
| `ghostmarkers.command.remove` | Allows removing markers. | op |
| `ghostmarkers.command.list` | Allows listing all markers. | op |
| `ghostmarkers.command.info` | Allows viewing detailed info about a marker. | op |
| `ghostmarkers.command.edit` | Allows editing markers. | op |
| `ghostmarkers.command.reload` | Allows reloading the plugin configuration. | op |
| `ghostmarkers.command.admin` | Allows access to admin commands like backup and restore. | op |

## ⚙️ Configuration

GhostMarkers uses two main files for configuration:

*   `config.yml`: This file stores all the markers that you create using the in-game commands. The plugin manages this file, and it's backed up automatically.
*   `markers.yml`: You can use this file to define "premade" markers that will be loaded when the plugin starts. This is useful for server-defined points of interest that you don't want to be editable via commands.

Here is an example of what the configuration for a marker looks like in either file:

```yaml
markers:
  example-marker:
    id: 'example-marker'
    type: 'PLAYER'
    follow: 'Jules'
    update-interval: 20
    visible_to:
      - 'all'
    world: 'world'
    conditions:
      - 'sneaking'
```

*   `id`: The unique identifier for the marker.
*   `type`: The type of the marker icon. A list of valid types can be found on the [Spigot Javadocs](https://hub.spigotmc.org/javadocs/spigot/org/bukkit/map/MapIcon.Type.html).
*   `follow`: The name of the player the marker should follow.
*   `follow_coords`: The fixed location of the marker.
*   `update-interval`: The interval in ticks at which the marker's position is updated.
*   `visible_to`: A list of players who can see the marker. Set to `['all']` to make it visible to everyone.
*   `world`: The world in which the marker is located.
*   `conditions`: A list of conditions that must be met for the marker to be visible.

## Support and Issues

If you encounter any bugs or have a feature request, please open an issue on the GitHub repository.

## License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.
