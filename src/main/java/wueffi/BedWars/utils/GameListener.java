package wueffi.BedWars.utils;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import wueffi.BedWars.generic.checkWins;
import wueffi.MiniGameCore.api.GameStartEvent;
import wueffi.MiniGameCore.api.GameOverEvent;
import wueffi.MiniGameCore.api.MiniGameCoreAPI;
import wueffi.MiniGameCore.managers.LobbyManager;
import wueffi.MiniGameCore.utils.Lobby;
import org.bukkit.plugin.Plugin;
import wueffi.MiniGameCore.utils.Team;

import java.util.Objects;

public class GameListener implements Listener {

    private final Plugin plugin;
    private checkWins winChecker;
    private BedChecker bedChecker;

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
            BedBreakListener bedBreakListener = new BedBreakListener(plugin, lobby);
            Bukkit.getPluginManager().registerEvents(bedBreakListener, plugin);

            BedChecker bedChecker = new BedChecker(plugin, lobby, bedBreakListener);
            bedChecker.startChecking();

            PlayerDeathEvent deathListener = new PlayerDeathEvent(plugin, lobby, bedChecker);
            Bukkit.getPluginManager().registerEvents(deathListener, plugin);

            ShopListener shopListener = new ShopListener(plugin);
            Bukkit.getPluginManager().registerEvents(shopListener, plugin);

            Generators generators = new Generators(plugin, lobby, shopListener);

            SpecialItemsListener sListener = new SpecialItemsListener(plugin);
            Bukkit.getPluginManager().registerEvents(sListener, plugin);

            winChecker = new checkWins(plugin, lobby, bedChecker);
            winChecker.startChecking();

            World world = Bukkit.getWorld(lobby.getWorldFolder().getName());
            if (world == null) {
                plugin.getLogger().warning("World was null for Lobby " + lobbyId + "(" + lobby.getWorldFolder().getName() + ")");
                return;
            }
            world.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false);
            world.setGameRule(GameRule.DO_FIRE_TICK, false);

            BlockListener blockListener = new BlockListener(world, lobby);
            Bukkit.getPluginManager().registerEvents(blockListener, plugin);

            Villager redShop = ShopKeeper.spawnShopKeeper(new Location(world, -5.5, 66, 80), "Red", world, 270, false);
            Villager blueShop = ShopKeeper.spawnShopKeeper(new Location(world, -79, 66, -5.5), "Blue", world, 0, false);
            Villager yellowShop = ShopKeeper.spawnShopKeeper(new Location(world, 6.5, 66, -79), "Yellow", world, 90, false);
            Villager greenShop = ShopKeeper.spawnShopKeeper(new Location(world, 80, 66, 6.5), "Green", world, 180, false);

            shopListener.registerShopKeeper(redShop, "Red", false);
            shopListener.registerShopKeeper(blueShop, "Blue", false);
            shopListener.registerShopKeeper(yellowShop, "Yellow", false);
            shopListener.registerShopKeeper(greenShop, "Green", false);

            Villager redTeamShop = ShopKeeper.spawnShopKeeper(new Location(world, 6.5, 66, 80), "Red", world, 90, true);
            Villager blueTeamShop = ShopKeeper.spawnShopKeeper(new Location(world, -79, 66, 6.5), "Blue", world, 180, true);
            Villager yellowTeamShop = ShopKeeper.spawnShopKeeper(new Location(world, -5.5, 66, -79), "Yellow", world, 270, true);
            Villager greenTeamShop = ShopKeeper.spawnShopKeeper(new Location(world, 80, 66, -5.5), "Green", world, 0, true);

            shopListener.registerShopKeeper(redTeamShop, "Red", true);
            shopListener.registerShopKeeper(blueTeamShop, "Blue", true);
            shopListener.registerShopKeeper(yellowTeamShop, "Yellow", true);
            shopListener.registerShopKeeper(greenTeamShop, "Green", true);

            Bukkit.getScheduler().runTaskLater(plugin, () -> {
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
                    ShopListener.setUpTeamLevels(team);
                }
                generators.startGenerators();
            }, 201L);
        }
    }

    @EventHandler
    public void onGameEnd(GameOverEvent event) {
        Lobby lobby = event.getLobby();
        String name = lobby.getGameName();

        if (Objects.equals(name, "BedWars")) {
            winChecker.stopChecking();
            bedChecker.stopChecking();
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