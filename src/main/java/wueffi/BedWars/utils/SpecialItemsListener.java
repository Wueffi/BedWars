package wueffi.BedWars.utils;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.TNT;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import wueffi.MiniGameCore.managers.LobbyManager;
import wueffi.MiniGameCore.utils.Lobby;
import wueffi.MiniGameCore.utils.Team;

import java.util.*;

public class SpecialItemsListener implements Listener {

    private final Plugin plugin;
    private final Map<UUID, Long> cooldowns = new HashMap<>();

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
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }
        if (Objects.requireNonNull(event.getItem()).getType() == Material.EGG) eggThrow(event);
        if (event.getItem() == null || event.getItem().getType() != Material.FIRE_CHARGE) {
            return;
        }

        UUID playerId = player.getUniqueId();
        long currentTime = System.currentTimeMillis();

        Long lastUse = cooldowns.putIfAbsent(playerId, currentTime);
        if (lastUse != null) {
            long timeSinceLastUse = currentTime - lastUse;
            if (timeSinceLastUse < 200) {
                return;
            }
            cooldowns.put(playerId, currentTime);
        }

        event.setCancelled(true);
        ItemStack item = event.getItem();
        if (item.getAmount() > 1) {
            item.setAmount(item.getAmount() - 1);
        } else {
            player.getInventory().setItemInMainHand(null);
        }

        Vector direction = player.getEyeLocation().getDirection().normalize();
        Location spawnLocation = player.getEyeLocation().add(direction.multiply(1.5));

        Fireball fireball = (Fireball) player.getWorld().spawnEntity(spawnLocation, EntityType.FIREBALL);
        fireball.setDirection(direction.multiply(15));
        fireball.setYield(4.0f);
        fireball.setShooter(player);
    }

    @EventHandler
    public void onFireballDamage(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Fireball fireball) {
            if (fireball.getShooter() instanceof Player) {
                event.setCancelled(true);
            }
        }
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

    public void eggThrow(PlayerInteractEvent event) {
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

        Vector direction = player.getEyeLocation().getDirection().normalize();
        Location spawnLocation = player.getEyeLocation().add(direction.multiply(1.5));

        Egg egg = (Egg) player.getWorld().spawnEntity(spawnLocation, EntityType.EGG);
        egg.setRotation(player.getLocation().getYaw(), player.getLocation().getPitch());
        egg.setVelocity(direction.multiply(1.5));


        new BukkitRunnable() {
            private Location lastLocation = null;
            private int placedBlocks = 0;

            @Override
            public void run() {
                if (egg.isDead() || !egg.isValid() || placedBlocks >= 30) {
                    egg.remove();
                    cancel();
                    return;
                }

                Location current = egg.getLocation();
                if (lastLocation == null) {
                    lastLocation = current.clone();
                    return;
                }

                Vector direction = current.toVector().subtract(lastLocation.toVector());
                double distance = direction.length();
                if (distance == 0) return;

                direction.normalize().multiply(0.25);
                Location stepLocation = lastLocation.clone();

                for (double traveled = 0; traveled < distance; traveled += 0.25) {
                    stepLocation.add(direction);
                    Block block = stepLocation.getBlock();
                    Block eggBlock = egg.getLocation().getBlock();

                    if (block.getType() == Material.AIR && !block.equals(eggBlock)) {
                        block.setType(woolType);
                        placedBlocks++;
                        if (placedBlocks >= 30) {
                            cancel();
                            return;
                        }
                    }
                }
                lastLocation = current.clone();
            }

        }.runTaskTimer(plugin, 0L, 1L);
    }
}