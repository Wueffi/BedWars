package wueffi.BedWars.utils;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import wueffi.MiniGameCore.managers.LobbyManager;
import wueffi.MiniGameCore.utils.Lobby;

public class BlockListener implements Listener {
    private final Lobby lobby;
    private final World world;

    private final Location[] generators;

    public BlockListener(World world, Lobby lobby) {
        this.lobby = lobby;
        this.world = world;

        generators = new Location[]{
                new Location(world, -6.5, 72, -10.5),
                new Location(world, 8.5, 72, 12.5),
                new Location(world, 29.5, 68, -30.5),
                new Location(world, 31.5, 68, 29.5),
                new Location(world, -28.5, 68, 31.5),
                new Location(world, -30.5, 68, -28.5),
                new Location(world, 0.5, 65.5, 83.5),
                new Location(world, -82.5, 65.5, 0.5),
                new Location(world, 0.5, 65.5, -82.5),
                new Location(world, 83.5, 65.5, 0.5)
        };
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();

        Lobby lobby1 = LobbyManager.getLobbyByPlayer(player);

        if (!lobby1.equals(lobby)) return;
        if (!player.getWorld().equals(world)) return;

        if (block.getY() > 85) {
            event.setCancelled(true);
            player.sendMessage("§7[§6BedWars§7]§c You can't build this high up!");
        } else if (block.getX() < -100 || block.getX() > 100 || block.getZ() > 100 || block.getZ() < -100) {
            event.setCancelled(true);
            player.sendMessage("§7[§6BedWars§7]§c You can't build this far out!");
        } else if (isBlockNearGenerator(block)) {
            event.setCancelled(true);
            player.sendMessage("§7[§6BedWars§7]§c You can't build at Generators!");
        }
    }

    public boolean isBlockNearGenerator(Block block) {
        boolean isNear = false;

        for (Location block2 : generators) {
            if (BedBreakListener.isBlockNearLocation(block.getLocation(), block2)) isNear = true;
        }

        return isNear;
    }
}
