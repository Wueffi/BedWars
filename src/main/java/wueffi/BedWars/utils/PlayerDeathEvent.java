package wueffi.BedWars.utils;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import wueffi.MiniGameCore.api.MiniGameCoreAPI;
import wueffi.MiniGameCore.utils.Lobby;
import wueffi.MiniGameCore.utils.Team;

import java.util.Map;

public class PlayerDeathEvent implements Listener {

    private final Plugin plugin;
    private final Lobby lobby;
    private final Map<String, Boolean> bedStatus;

    public PlayerDeathEvent(Plugin plugin, Lobby lobby, Map<String, Boolean> bedStatus) {
        this.plugin = plugin;
        this.lobby = lobby;
        this.bedStatus = bedStatus;
    }

    @EventHandler
    public void onPlayerDeath(org.bukkit.event.entity.PlayerDeathEvent event) {
        Player player = event.getEntity();
        Team team = lobby.getTeamByPlayer(player);

        if (team == null) {
            return;
        }
        String teamColor = team.getColor();

        if (bedStatus.getOrDefault(teamColor, false)) {
            new BukkitRunnable() {
                int countdown = 5;

                @Override
                public void run() {
                    if (countdown > 0) {
                        player.sendTitle("§c§lYOU DIED!", "§6Respawning in §a" + countdown + "§6...", 0, 25, 0);
                        countdown--;
                    } else {
                        Location respawnLoc = getTeamSpawnLocation(teamColor);
                        plugin.getLogger().info(respawnLoc.toString());
                        player.teleport(respawnLoc);
                        player.setHealth(20);
                        player.setSaturation(20);
                        player.sendTitle("§a§lRESPAWNED!", "", 0, 20, 10);
                        player.setGameMode(GameMode.SURVIVAL);
                        this.cancel();
                    }
                }
            }.runTaskTimer(plugin, 0L, 20L);
        } else {
            player.sendTitle("§c§lYOU DIED!", "§7Your bed is broken!", 0, 60, 20);
            MiniGameCoreAPI.playerDeath(player.getUniqueId());
        }
    }

    private Location getTeamSpawnLocation(String teamColor) {
        int x = 0, y = 66, z = 0;

        switch (teamColor) {
            case "Red":
                x = 0;
                z = 77;
                break;
            case "Blue":
                x = -77;
                z = 0;
                break;
            case "Yellow":
                x = 0;
                z = -77;
                break;
            case "Green":
                x = 77;
                z = 0;
                break;
        }

        return new Location(Bukkit.getWorld(lobby.getWorldFolder().getName()), x, y, z);
    }
}