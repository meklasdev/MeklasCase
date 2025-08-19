# meklasCase

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Spigot Version](https://img.shields.io/badge/Spigot-1.20.6%2B-brightgreen)](https://www.spigotmc.org/)
[![Paper Version](https://img.shields.io/badge/Paper-1.20.6%2B-brightgreen)](https://papermc.io/)

meklasCase is a powerful and flexible crate plugin designed for Paper/Spigot servers running versions 1.20.6 and above. It allows server administrators to create and manage engaging crate systems with keys, stunning spin animations, daily loot rotations, and seamless fHolo integration.

## Features

*   **Crate Types**: Supports classic crate types such as `LOOTBOX` and `LUCKBLOCK`, each offering unique reward mechanics.
*   **Daily Loot Rotations**: Automatically changes reward lists or drop chances every 24 hours, keeping the crate system fresh and exciting.
*   **Day Boost System**: Select specific items to have increased drop rates for a set period, displayed prominently in the hologram.
*   **fHolo Integration**: Automatically displays crate information above the crate block, including name, type, current day's boost, time until the next rotation, and the last top drop.
*   **Spin Animation**: Engaging spin animation within a 27-slot inventory, or quick opening via Shift + Right-Click.
*   **Win Broadcast System**: Announces crate wins and top drops to the entire server.
*   **YAML Configuration**: All aspects of the plugin are configured via easy-to-understand YAML files, eliminating the need for a database.

## Installation

1.  **Download:** Download the latest `meklasCase.jar` from [GitHub Releases](YOUR_GITHUB_REPOSITORY_LINK_HERE/releases).
2.  **Installation:** Place the `.jar` file into your server's `plugins/` folder.
3.  **Restart:** Restart your Minecraft server to load the plugin.
4.  **Configuration:** Configure the plugin by editing the files in the `plugins/meklasCase/` folder.
5.  **fHolo (Optional):** If you want to use holograms, install the [fHolo](https://www.spigotmc.org/resources/fhologram.578/) plugin.

## Configuration Files

The `meklasCase` plugin uses several YAML files to manage its configuration. Here's a breakdown of each file:

### `config.yml`

This file contains the general plugin settings.

yaml
cases:
  example:
    lastRotationAt: 2025-08-17T04:00:00Z # Last time the crate was rotated (UTC)
    activeProfile: day1                # Currently active rotation profile
    lastTopDrop: "DIAMOND x2"            # Last top drop from the crate
| Command                      | Description                                           | Permission                |
| ---------------------------- | ----------------------------------------------------- | ------------------------- |
| `/meklascase create <name>`   | Creates a new crate configuration file.              | `meklascase.admin`        |
| `/meklascase delete <name>`   | Deletes a crate configuration file.                   | `meklascase.admin`        |
| `/meklascase setlocation <name>`| Sets the targeted block as a crate location.        | `meklascase.admin`        |
| `/meklascase removelocation <name>`| Removes a crate location.                          | `meklascase.admin`        |
| `/meklascase give <player> <case> <amount>`| Gives keys to a player.                               | `meklascase.admin`        |
| `/meklascase giveall <case> <amount>`| Gives keys to all online players.                       | `meklascase.admin`        |
| `/meklascase reload`         | Reloads the plugin configuration.                     | `meklascase.admin`        |
| `/meklascase enable <name>`   | Enables a crate.                                      | `meklascase.admin`        |
| `/meklascase disable <name>`  | Disables a crate.                                     | `meklascase.admin`        |
| `/meklascase rotate now`      | Forces a rotation of all crates.                      | `meklascase.rotate.admin` |
| `/meklascase boost set <case> <profile> <item> <multiplier>`| Sets the daily boost for a crate (legacy).       | `meklascase.boost.admin`  |
| `/meklascase info <case>`     | Shows the active profile and time until the next rotation. | `meklascase.admin`        |

## Permissions

| Permission               | Description                               |
| ------------------------ | ----------------------------------------- |
| `meklascase.admin`       | Grants access to all administrative commands. |
| `meklascase.rotate.admin`| Allows forcing crate rotations.           |
| `meklascase.boost.admin` | Allows managing daily boosts (legacy).      |

## Rotation Logic

The plugin supports two rotation modes, configured via `config.yml`:

*   **Fixed Time**: Resets at a specific server time (e.g., `04:00`) determined by the `fixedTime` setting. Enabled if `resetAtFixedTime` is set to `true`.
*   **Rolling Window**: Resets every `windowHours` (e.g. 24) hours from the last rotation. Enabled if `resetAtFixedTime` is set to `false`.

After each rotation, the next profile in the `cases/<case>.yml` file is activated. If the end of the profiles is reached, it loops back to the first profile.

Each profile can either:

*   **Boost Chances**: Increase the drop rate of specific items by adjusting their `multiplier`.
*   **Override Loot Table**: Completely replace the existing item list with a new set of items.

## Hologram Information (fHolo)

The plugin utilizes fHolo to display information above each crate.

Default hologram lines:

1.  Crate Name
2.  Crate Type
3.  Day's Boost: e.g., "Today's boost: DIAMOND x3"
4.  Time Remaining: e.g., "Remaining time: 12:34:56"
5.  Last TOP DROP

Available Placeholders:

*   `{case}`: Crate name
*   `{type}`: Crate type
*   `{boost_item}`: Boosted item name
*   `{boost_mult}`: Boost multiplier
*   `{time_left}`: Time until the next rotation
*   `{top_item}`: Last top drop item name
*   `{top_amount}`: Last top drop item amount

> **Note**: You can customize the hologram lines and placeholders in your fHolo configuration.

## Edge Cases

*   **Missing Key**: If a player tries to open a crate without a key, a message is displayed, and a sound is played.
*   **Full Inventory**: If a player's inventory is full, the items drop at their feet.
*   **Missing fHolo**: The plugin will function without fHolo, but no holograms will be displayed.
*   **System Time Changes**: The plugin uses UTC time for rotations to prevent issues caused by server time changes.

## Contributing

Contributions are welcome! If you have any ideas, suggestions, or bug reports, please open an issue or submit a pull request on the [GitHub repository](YOUR_GITHUB_REPOSITORY_LINK_HERE).

1.  Fork the repository.
2.  Create a new branch for your feature or bug fix.
3.  Make your changes and commit them with descriptive messages.
4.  Submit a pull request.

## License

This project is licensed under the MIT License - see the [LICENSE](YOUR_GITHUB_REPOSITORY_LINK_HERE/LICENSE) file for details.
