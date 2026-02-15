package wueffi.BedWars.utils;

import org.bukkit.*;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import wueffi.MiniGameCore.utils.Lobby;

import java.util.ArrayList;
import java.util.List;

public class Generators {

    private final Plugin plugin;
    private final World world;
    private final HoloGram holoGram;
    private final Lobby lobby;
    private final ShopListener shopListener;
    private final List<ArmorStand> emeraldHolos = new ArrayList<>();
    private final List<ArmorStand> diamondHolos = new ArrayList<>();
    private final List<ArmorStand> emeraldHolos2 = new ArrayList<>();
    private final List<ArmorStand> diamondHolos2 = new ArrayList<>();

    private final Location[] emeraldGenerators;
    private final Location[] diamondGenerators;
    private final Location[] teamGenerators;

    private final List<BukkitTask> tasks = new ArrayList<>();

    private int emeraldLevel = 1;
    private int diamondLevel = 1;

    public Generators(Plugin plugin, Lobby lobby, ShopListener shopListener) {
        this.plugin = plugin;
        this.world = Bukkit.getWorld(lobby.getWorldFolder().getName());
        this.holoGram = new HoloGram(plugin);
        this.lobby = lobby;
        this.shopListener = shopListener;

        emeraldGenerators = new Location[]{
                new Location(world, -6.5, 72, -10.5),
                new Location(world, 8.5, 72, 12.5)
        };

        diamondGenerators = new Location[]{
                new Location(world, 29.5, 68, -30.5),
                new Location(world, 31.5, 68, 29.5),
                new Location(world, -28.5, 68, 31.5),
                new Location(world, -30.5, 68, -28.5)
        };

        teamGenerators = new Location[] {
                new Location(world, 0.5, 65.5, 83.5),
                new Location(world, -82.5, 65.5, 0.5),
                new Location(world, 0.5, 65.5, -82.5),
                new Location(world, 83.5, 65.5, 0.5)
        };
    }

    public void startGenerators() {
        for (Location loc : emeraldGenerators) {
            ArmorStand holo = holoGram.createHologram(loc.clone().add(0, 1, 0), "§aEmerald §7in §a30§7s");
            emeraldHolos.add(holo);
            ArmorStand holo2 = holoGram.createHologram(loc.clone().add(0, 1.5, 0), "§aEmerald II §7in §a120§7s");
            emeraldHolos2.add(holo2);
        }

        for (Location loc : diamondGenerators) {
            ArmorStand holo = holoGram.createHologram(loc.clone().add(0, 1, 0), "§bDiamond §7in §b30§7s");
            diamondHolos.add(holo);
            ArmorStand holo2 = holoGram.createHologram(loc.clone().add(0, 1.5, 0), "§bDiamond II §7in §b90§7s");
            diamondHolos2.add(holo2);
        }

        tasks.add(new BukkitRunnable() {
            int countdown = 30;

            @Override
            public void run() {
                if (countdown > 0) {
                    for (ArmorStand holo : diamondHolos) {
                        holoGram.updateText(holo, "§bDiamond §7in §b" + countdown + "§7s");
                    }
                    countdown--;
                } else {
                    if (diamondLevel == 2) {
                        spawnDiamonds();
                    }
                    spawnDiamonds();

                    countdown = 29;
                }
            }
        }.runTaskTimer(plugin, 0L, 20L));

        tasks.add(new BukkitRunnable() {
            int countdown = 65;

            @Override
            public void run() {
                if (countdown > 0) {
                    for (ArmorStand holo : emeraldHolos) {
                        holoGram.updateText(holo, "§aEmerald §7in §a" + countdown + "§7s");
                    }
                    countdown--;
                } else {
                    if (emeraldLevel == 2) {
                        spawnEmeralds();
                    }
                    spawnEmeralds();

                    countdown = 64;
                }
            }
        }.runTaskTimer(plugin, 0L, 20L));

        tasks.add(new BukkitRunnable() {
            @Override
            public void run() {
                spawnIron();
            }
        }.runTaskTimer(plugin, 0L, 20L));

        tasks.add(new BukkitRunnable() {
            int countdown = 8;

            @Override
            public void run() {
                if (countdown > 0) {
                    countdown--;
                } else {
                    spawnGold();
                    countdown = 7;
                }
            }
        }.runTaskTimer(plugin, 0L, 20L));

        tasks.add(new BukkitRunnable() {
            int countdown = 180;

            @Override
            public void run() {
                if (countdown > 0) {
                    for (ArmorStand holo : emeraldHolos2) {
                        holoGram.updateText(holo, "§aEmerald II §7in §a" + countdown + "§7s");
                    }
                    countdown--;
                } else {
                    emeraldLevel = 2;
                    for (Player player : lobby.getPlayers()) {
                        player.sendMessage("§aEmerald Generators §7are now Level §a2§7!");
                    }
                    for (ArmorStand holo : emeraldHolos2) {
                        holoGram.updateText(holo, "§aEmerald II");
                    }
                    this.cancel();
                }
            }
        }.runTaskTimer(plugin, 0L, 20L));

        tasks.add(new BukkitRunnable() {
            int countdown = 120;

            @Override
            public void run() {
                if (countdown > 0) {
                    for (ArmorStand holo : diamondHolos2) {
                        holoGram.updateText(holo, "§bDiamond II §7in §b" + countdown + "§7s");
                    }
                    countdown--;
                } else {
                    diamondLevel = 2;
                    for (Player player : lobby.getPlayers()) {
                        player.sendMessage("§bDiamond Generators §7are now Level §b2§7!");
                    }
                    for (ArmorStand holo : diamondHolos2) {
                        holoGram.updateText(holo, "§bDiamond II");
                    }
                    this.cancel();
                }
            }
        }.runTaskTimer(plugin, 0L, 20L));
    }

    public void stopGenerators() {
        for (BukkitTask task : tasks) {
            task.cancel();
        }
    }

    private int countNearbyItems(Location loc, Material material, double radius) {
        int count = 0;
        for (org.bukkit.entity.Entity entity : loc.getWorld().getNearbyEntities(loc, radius, radius, radius)) {
            if (entity instanceof Item) {
                Item item = (Item) entity;
                if (item.getItemStack().getType() == material) {
                    count += item.getItemStack().getAmount();
                }
            }
        }
        return count;
    }

    private void spawnEmeralds() {
        for (Location loc : emeraldGenerators) {
            Item item = loc.getWorld().dropItem(loc, new ItemStack(Material.EMERALD, 1));
            item.setPickupDelay(10);
        }
    }

    private void spawnDiamonds() {
        for (Location loc : diamondGenerators) {
            Item item = loc.getWorld().dropItem(loc, new ItemStack(Material.DIAMOND, 1));
            item.setPickupDelay(10);
        }
    }

    private void spawnIron() {
        for (int i = 0; i < 4; i++) {
            Location loc = teamGenerators[i];

            int ironCount = countNearbyItems(loc, Material.IRON_INGOT, 2.5);
            if (ironCount >= 48) {
                continue;
            }

            Item item = loc.getWorld().dropItem(loc, new ItemStack(Material.IRON_INGOT, shopListener.getForgeLevel(lobby.getTeam(i))));
            item.setPickupDelay(0);
        }
    }

    private void spawnGold() {
        for (int i = 0; i < 4; i++) {
            Location loc = teamGenerators[i];

            int goldCount = countNearbyItems(loc, Material.GOLD_INGOT, 2.5);
            if (goldCount >= 12) {
                continue;
            }

            Item item = loc.getWorld().dropItem(loc, new ItemStack(Material.GOLD_INGOT, shopListener.getForgeLevel(lobby.getTeam(i))));
            item.setPickupDelay(0);

            int emeraldCount = countNearbyItems(loc, Material.EMERALD, 2.5);
            if (emeraldCount >= 12) {
                continue;
            }

            if (shopListener.getForgeLevel(lobby.getTeam(i)) >= 4) {
                item = loc.getWorld().dropItem(loc, new ItemStack(Material.EMERALD, 1));
                item.setPickupDelay(0);
            }
        }
    }
}