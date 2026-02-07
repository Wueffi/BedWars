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
import wueffi.MiniGameCore.utils.Lobby;

import java.util.HashMap;
import java.util.Map;

public class BedBreakListener implements Listener {

    private final Plugin plugin;
    private final Lobby lobby;
    private final Map<String, Location> bedLocations;
    private final Map<String, String> bedColorCodes;
    private Player lastBedBreaker;
    private String lastBrokenBedTeam;

    public BedBreakListener(Plugin plugin, Lobby lobby) {
        this.plugin = plugin;
        this.lobby = lobby;
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

        for (Map.Entry<String, Location> entry : bedLocations.entrySet()) {
            String team = entry.getKey();
            Location bedLoc = entry.getValue();

            if (block.getLocation().getBlockX() == bedLoc.getBlockX() &&
                    block.getLocation().getBlockY() == bedLoc.getBlockY() &&
                    block.getLocation().getBlockZ() == bedLoc.getBlockZ()) {

                lastBedBreaker = player;
                lastBrokenBedTeam = team;
                break;
            }
        }
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