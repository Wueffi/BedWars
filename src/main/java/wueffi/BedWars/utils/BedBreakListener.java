package wueffi.BedWars.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.plugin.Plugin;
import wueffi.MiniGameCore.managers.LobbyManager;
import wueffi.MiniGameCore.utils.Lobby;
import wueffi.MiniGameCore.utils.Team;

import java.util.HashMap;
import java.util.Map;

public class BedBreakListener implements Listener {

    private final Map<String, Location> bedLocations;
    private final Map<String, String> bedColorCodes;
    private Player lastBedBreaker;
    private String lastBrokenBedTeam;

    public BedBreakListener(Plugin plugin, Lobby lobby) {
        this.bedLocations = new HashMap<>();
        this.bedColorCodes = new HashMap<>();

        World world = Bukkit.getWorld(lobby.getWorldFolder().getName());

        bedLocations.put("Red", new Location(world, 0, 67, 66));
        bedLocations.put("Blue", new Location(world, -66, 67, 0));
        bedLocations.put("Yellow", new Location(world, 0, 67, -66));
        bedLocations.put("Green", new Location(world, 66, 67, 0));

        bedColorCodes.put("Red", "§4");
        bedColorCodes.put("Blue", "§1");
        bedColorCodes.put("Yellow", "§e");
        bedColorCodes.put("Green", "§2");
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        Player player = event.getPlayer();

        if (!isBed(block)) {
            return;
        }

        Lobby lobby = LobbyManager.getLobbyByPlayer(player);
        if (lobby == null) return;
        Team team1 = lobby.getTeamByPlayer(player);
        if (team1 == null) return;

        for (Map.Entry<String, Location> entry : bedLocations.entrySet()) {
            String team = entry.getKey();
            Location bedLoc = entry.getValue();

            if (isBlockNearLocation(block.getLocation(), bedLoc)) {
                lastBedBreaker = player;
                lastBrokenBedTeam = team;
                if (team1.getColor().equals(team)) {
                    player.sendMessage("§cYou can't break your own Bed!");
                    event.setCancelled(true);
                }
                break;
            }
        }
    }

    public static boolean isBlockNearLocation(Location blockLoc, Location bedLoc) {
        int dx = Math.abs(blockLoc.getBlockX() - bedLoc.getBlockX());
        int dy = Math.abs(blockLoc.getBlockY() - bedLoc.getBlockY());
        int dz = Math.abs(blockLoc.getBlockZ() - bedLoc.getBlockZ());

        return dx <= 1 && dy <= 1 && dz <= 1;
    }

    private boolean isBed(Block block) {
        Material type = block.getType();
        return type.name().contains("BED");
    }

    public Player getLastBedBreaker() {
        return lastBedBreaker;
    }

    public String getLastBrokenBedTeam() {
        return lastBrokenBedTeam;
    }

    public String getColorCode(String team) {
        return bedColorCodes.getOrDefault(team, "§7");
    }
}