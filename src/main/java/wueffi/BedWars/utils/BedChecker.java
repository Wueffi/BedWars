package wueffi.BedWars.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import wueffi.MiniGameCore.utils.Lobby;

import java.util.HashMap;
import java.util.Map;

public class BedChecker {

    private final Plugin plugin;
    private final World world;
    private final Map<String, Boolean> bedStatus;
    private final Map<String, BedLocation> bedLocations;
    private final Lobby lobby;
    private final BedBreakListener bedBreakListener;

    public BedChecker(Plugin plugin, Lobby lobby, BedBreakListener bedBreakListener) {
        this.plugin = plugin;
        this.world = Bukkit.getWorld(lobby.getWorldFolder().getName());
        this.bedStatus = new HashMap<>();
        this.bedLocations = new HashMap<>();
        this.lobby = lobby;
        this.bedBreakListener = bedBreakListener;

        bedLocations.put("Red", new BedLocation(0, 67, 66, "§4", "Red"));
        bedLocations.put("Blue", new BedLocation(-66, 67, 0, "§1", "Blue"));
        bedLocations.put("Yellow", new BedLocation(0, 67, -66, "§e", "Yellow"));
        bedLocations.put("Green", new BedLocation(66, 67, 0, "§2", "Green"));

        for (String team : bedLocations.keySet()) {
            bedStatus.put(team, true);
        }
    }

    public Map<String, Boolean> getBedStatus() {
        return bedStatus;
    }

    public void startChecking() {
        new BukkitRunnable() {
            @Override
            public void run() {
                checkBeds();
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    private void checkBeds() {
        for (Map.Entry<String, BedLocation> entry : bedLocations.entrySet()) {
            String team = entry.getKey();
            BedLocation bedLoc = entry.getValue();

            if (bedStatus.get(team)) {
                Location loc = new Location(world, bedLoc.x, bedLoc.y, bedLoc.z);
                Block block = loc.getBlock();

                if (!isBed(block)) {
                    bedStatus.put(team, false);

                    Player breaker = null;
                    if (bedBreakListener.getLastBrokenBedTeam() != null &&
                            bedBreakListener.getLastBrokenBedTeam().equals(team) &&
                            bedBreakListener.getLastBedBreaker() != null) {
                        breaker = bedBreakListener.getLastBedBreaker();
                    }

                    announceBedBroken(bedLoc.colorCode, bedLoc.teamName, breaker);
                }
            }
        }
    }

    private boolean isBed(Block block) {
        Material type = block.getType();
        return type.name().contains("BED");
    }

    private void announceBedBroken(String colorCode, String teamName, Player breaker) {
        String message = "";
        if (breaker == null) {
            message = "§7The " + colorCode + teamName + " Teams §7Bed has been broken!";
        } else {
            message = "§7The " + colorCode + teamName + " Teams §7Bed has been broken by " + lobby.getTeamByPlayer(breaker).getColorCode() + breaker.getName() + "§7!";
        }
        for (Player player : lobby.getPlayers()) {
            player.sendMessage(message);
        }
    }

    private static class BedLocation {
        int x, y, z;
        String colorCode;
        String teamName;

        BedLocation(int x, int y, int z, String colorCode, String teamName) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.colorCode = colorCode;
            this.teamName = teamName;
        }
    }
}