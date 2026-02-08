package wueffi.BedWars.utils;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.TNT;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.*;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import wueffi.MiniGameCore.managers.LobbyManager;
import wueffi.MiniGameCore.utils.Lobby;
import wueffi.MiniGameCore.utils.Team;

import java.util.*;

public class SpecialItemsListener implements Listener {

    private final Plugin plugin;

    public SpecialItemsListener(Plugin plugin) {
        this.plugin = plugin;
    }

    private static final Set<Material> BREAKABLE_BLOCKS = new HashSet<>(Arrays.asList(
            Material.OAK_PLANKS,
            Material.RED_WOOL,
            Material.BLUE_WOOL,
            Material.YELLOW_WOOL,
            Material.GREEN_WOOL,
            Material.END_STONE
    ));

    private static final Map<DyeColor, Material> DYE_TO_WOOL = new HashMap<>() {{
        put(DyeColor.RED, Material.RED_WOOL);
        put(DyeColor.BLUE, Material.BLUE_WOOL);
        put(DyeColor.YELLOW, Material.YELLOW_WOOL);
        put(DyeColor.GREEN, Material.GREEN_WOOL);
    }};

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        if (event.getItem() == null || event.getItem().getType() != Material.FIRE_CHARGE) {
            return;
        }

        event.setCancelled(true);
        event.getItem().setAmount(event.getItem().getAmount() - 1);

        Vector direction = player.getEyeLocation().getDirection().normalize();
        Location spawnLocation = player.getEyeLocation().add(direction.multiply(1.5));

        Fireball fireball = (Fireball) player.getWorld().spawnEntity(spawnLocation, EntityType.FIREBALL);
        fireball.setDirection(direction.multiply(15));
        fireball.setYield(4.0f);
        fireball.setShooter(player);
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (event.getBlock().getType() != Material.TNT) {
            return;
        }

        Location loc = event.getBlock().getLocation();
        loc.getBlock().setType(Material.AIR);
        loc.getWorld().spawnEntity(loc.add(0.5, 0.5, 0.5), EntityType.TNT);
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        if ((event.getEntity() instanceof Fireball) || (event.getEntity() instanceof TNT) || (event.getEntity() instanceof TNTPrimed)) {
            event.blockList().removeIf(block -> !BREAKABLE_BLOCKS.contains(block.getType()));
        }
    }

    @EventHandler
    public void onEggThrow(PlayerEggThrowEvent event) {
        Player player = event.getPlayer();

        Lobby lobby = LobbyManager.getLobbyByPlayer(player);
        if (lobby == null) {
            return;
        }
        Team team = lobby.getTeamByPlayer(player);
        if (team == null) {
            return;
        }

        DyeColor dyeColor;
        try {
            dyeColor = DyeColor.valueOf(team.getColor().toUpperCase());
        } catch (IllegalArgumentException e) {
            dyeColor = DyeColor.WHITE;
        }

        Material woolType = DYE_TO_WOOL.getOrDefault(dyeColor, Material.WHITE_WOOL);
        Egg egg = event.getEgg();

        new BukkitRunnable() {
            private Location lastBlock = null;
            private int placedBlocks = 0;

            @Override
            public void run() {
                if (egg.isDead() || !egg.isValid() || placedBlocks >= 30) {
                    cancel();
                    return;
                }

                Location current = egg.getLocation();
                Location currentBlock = current.getBlock().getLocation();

                if (lastBlock == null) {
                    lastBlock = currentBlock;
                    return;
                }
                if (!currentBlock.equals(lastBlock)) {
                    Block placeBlock = lastBlock.getBlock();

                    if (placeBlock.getType() == Material.AIR) {
                        placeBlock.setType(woolType);
                        placedBlocks++;
                    }

                    lastBlock = currentBlock;
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }
}