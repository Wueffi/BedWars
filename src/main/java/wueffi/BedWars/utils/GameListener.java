package wueffi.BedWars.utils;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.scheduler.BukkitTask;
import wueffi.BedWars.generic.checkWins;
import wueffi.MiniGameCore.api.GameStartEvent;
import wueffi.MiniGameCore.api.GameOverEvent;
import wueffi.MiniGameCore.api.MiniGameCoreAPI;
import wueffi.MiniGameCore.managers.LobbyManager;
import wueffi.MiniGameCore.utils.Lobby;
import org.bukkit.plugin.Plugin;
import wueffi.MiniGameCore.utils.Team;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class GameListener implements Listener {

    private final Plugin plugin;
    private final Map<Lobby, BedBreakListener> bedBreakListeners = new HashMap<>();
    private final Map<Lobby, PlayerDeathEvent> deathListeners = new HashMap<>();
    private final Map<Lobby, ShopListener> shopListeners = new HashMap<>();
    private final Map<Lobby, SpecialItemsListener> sListeners = new HashMap<>();
    private final Map<Lobby, BlockListener> blockListeners = new HashMap<>();
    private final Map<Lobby, checkWins> winCheckers = new HashMap<>();
    private final Map<Lobby, BedChecker> bedCheckers = new HashMap<>();
    private final Map<Lobby, Generators> generators = new HashMap<>();
    private final Map<Lobby, BukkitTask> startupTasks = new HashMap<>();

    public GameListener(Plugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onGameStart(GameStartEvent event) {
        String name = event.getGameName();
        String lobbyId = event.getLobby().getLobbyId();

        LobbyManager lobbyManager = MiniGameCoreAPI.getLobbyManager();
        Lobby lobby = lobbyManager.getLobby(lobbyId);

        if (Objects.equals(name, "BedWars")) {
            BedBreakListener bbl = new BedBreakListener(plugin, lobby);
            bedBreakListeners.put(lobby, bbl);
            Bukkit.getPluginManager().registerEvents(bbl, plugin);

            BedChecker bc = new BedChecker(plugin, lobby, bbl);
            bedCheckers.put(lobby, bc);
            bc.startChecking();

            ShopListener shl = new ShopListener(plugin);
            shopListeners.put(lobby, shl);
            Bukkit.getPluginManager().registerEvents(shl, plugin);

            Generators g = new Generators(plugin, lobby, shl);
            generators.put(lobby, g);

            SpecialItemsListener sl = new SpecialItemsListener(plugin);
            sListeners.put(lobby, sl);
            Bukkit.getPluginManager().registerEvents(sl, plugin);

            checkWins wc = new checkWins(plugin, lobby, bc);
            winCheckers.put(lobby, wc);
            wc.startChecking();

            World world = Bukkit.getWorld(lobby.getWorldFolder().getName());
            if (world == null) {
                plugin.getLogger().warning("World was null for Lobby " + lobbyId + "(" + lobby.getWorldFolder().getName() + ")");
                return;
            }
            world.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false);
            world.setGameRule(GameRule.DO_FIRE_TICK, false);

            BlockListener bl = new BlockListener(world, lobby);
            blockListeners.put(lobby, bl);
            Bukkit.getPluginManager().registerEvents(bl, plugin);

            Villager redShop = ShopKeeper.spawnShopKeeper(new Location(world, -5.5, 66, 80), "Red", world, 270, false);
            Villager blueShop = ShopKeeper.spawnShopKeeper(new Location(world, -79, 66, -5.5), "Blue", world, 0, false);
            Villager yellowShop = ShopKeeper.spawnShopKeeper(new Location(world, 6.5, 66, -79), "Yellow", world, 90, false);
            Villager greenShop = ShopKeeper.spawnShopKeeper(new Location(world, 80, 66, 6.5), "Green", world, 180, false);

            shl.registerShopKeeper(redShop, "Red", false);
            shl.registerShopKeeper(blueShop, "Blue", false);
            shl.registerShopKeeper(yellowShop, "Yellow", false);
            shl.registerShopKeeper(greenShop, "Green", false);

            Villager redTeamShop = ShopKeeper.spawnShopKeeper(new Location(world, 6.5, 66, 80), "Red", world, 90, true);
            Villager blueTeamShop = ShopKeeper.spawnShopKeeper(new Location(world, -79, 66, 6.5), "Blue", world, 180, true);
            Villager yellowTeamShop = ShopKeeper.spawnShopKeeper(new Location(world, -5.5, 66, -79), "Yellow", world, 270, true);
            Villager greenTeamShop = ShopKeeper.spawnShopKeeper(new Location(world, 80, 66, -5.5), "Green", world, 0, true);

            shl.registerShopKeeper(redTeamShop, "Red", true);
            shl.registerShopKeeper(blueTeamShop, "Blue", true);
            shl.registerShopKeeper(yellowTeamShop, "Yellow", true);
            shl.registerShopKeeper(greenTeamShop, "Green", true);

            PlayerDeathEvent dl = new PlayerDeathEvent(plugin, lobby, bc, shl);
            deathListeners.put(lobby, dl);
            Bukkit.getPluginManager().registerEvents(dl, plugin);

            BukkitTask st = Bukkit.getScheduler().runTaskLater(plugin, () -> {
                for (Player player : lobby.getPlayers()) {
                    player.getEnderChest().clear();
                    String color = lobby.getTeamByPlayer(player).getColor();
                    Color leatherColor = getTeamLeatherColor(color);

                    ItemStack helmet = new ItemStack(Material.LEATHER_HELMET);
                    ItemStack chestplate = new ItemStack(Material.LEATHER_CHESTPLATE);
                    ItemStack leggings = new ItemStack(Material.LEATHER_LEGGINGS);
                    ItemStack boots = new ItemStack(Material.LEATHER_BOOTS);

                    LeatherArmorMeta helmetMeta = (LeatherArmorMeta) helmet.getItemMeta();
                    helmetMeta.setColor(leatherColor);
                    helmet.setItemMeta(helmetMeta);

                    LeatherArmorMeta chestMeta = (LeatherArmorMeta) chestplate.getItemMeta();
                    chestMeta.setColor(leatherColor);
                    chestplate.setItemMeta(chestMeta);

                    LeatherArmorMeta legMeta = (LeatherArmorMeta) leggings.getItemMeta();
                    legMeta.setColor(leatherColor);
                    leggings.setItemMeta(legMeta);

                    LeatherArmorMeta bootsMeta = (LeatherArmorMeta) boots.getItemMeta();
                    bootsMeta.setColor(leatherColor);
                    boots.setItemMeta(bootsMeta);

                    player.getInventory().setHelmet(helmet);
                    player.getInventory().setChestplate(chestplate);
                    player.getInventory().setLeggings(leggings);
                    player.getInventory().setBoots(boots);
                }
                for (Team team : lobby.getTeamList()) {
                    shopListeners.get(lobby).setUpTeamLevels(team);
                }
                generators.get(lobby).startGenerators();
            }, 201L);

            startupTasks.put(lobby, st);
        }
    }

    @EventHandler
    public void onGameEnd(GameOverEvent event) {
        Lobby lobby = event.getLobby();
        String name = lobby.getGameName();

        if (Objects.equals(name, "BedWars")) {
            BukkitTask st = startupTasks.remove(lobby);
            if (st != null) st.cancel();

            checkWins wc = winCheckers.remove(lobby);
            if (wc != null) wc.stopChecking();

            BedChecker bc = bedCheckers.remove(lobby);
            if (bc != null) bc.stopChecking();

            Generators gen = generators.remove(lobby);
            if (gen != null) gen.stopGenerators();

            BedBreakListener bbl = bedBreakListeners.remove(lobby);
            if (bbl != null) HandlerList.unregisterAll(bbl);

            PlayerDeathEvent dl = deathListeners.remove(lobby);
            if (dl != null) HandlerList.unregisterAll(dl);

            ShopListener sl = shopListeners.remove(lobby);
            if (sl != null) HandlerList.unregisterAll(sl);

            SpecialItemsListener sl2 = sListeners.remove(lobby);
            if (sl2 != null) HandlerList.unregisterAll(sl2);

            BlockListener bl = blockListeners.remove(lobby);
            if (bl != null) HandlerList.unregisterAll(bl);
        }
    }

    private Color getTeamLeatherColor(String teamColor) {
        switch (teamColor) {
            case "Red": return Color.fromRGB(255, 0, 0);
            case "Blue": return Color.fromRGB(0, 0, 255);
            case "Yellow": return Color.fromRGB(255, 255, 0);
            case "Green": return Color.fromRGB(0, 255, 0);
            default: return Color.WHITE;
        }
    }
}