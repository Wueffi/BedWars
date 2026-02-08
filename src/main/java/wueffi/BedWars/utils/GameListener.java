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
import wueffi.MiniGameCore.api.MiniGameCoreAPI;
import wueffi.MiniGameCore.managers.LobbyManager;
import wueffi.MiniGameCore.utils.Lobby;
import org.bukkit.plugin.Plugin;
import wueffi.MiniGameCore.utils.Team;

import java.util.Objects;

public class GameListener implements Listener {

    private final Plugin plugin;

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

            checkWins winChecker = new checkWins(plugin, lobby, bedChecker);
            winChecker.startChecking();

            World world = Bukkit.getWorld(lobby.getWorldFolder().getName());

            Villager redShop = ShopKeeper.spawnShopKeeper(new Location(world, -5.5, 66, 80), ShopKeeper.DyeColor.RED, world, 270, false);
            Villager blueShop = ShopKeeper.spawnShopKeeper(new Location(world, -79, 66, -5.5), ShopKeeper.DyeColor.BLUE, world, 0, false);
            Villager yellowShop = ShopKeeper.spawnShopKeeper(new Location(world, 6.5, 66, -79), ShopKeeper.DyeColor.YELLOW, world, 90, false);
            Villager greenShop = ShopKeeper.spawnShopKeeper(new Location(world, 80, 66, 6.5), ShopKeeper.DyeColor.GREEN, world, 180, false);

            shopListener.registerShopKeeper(redShop, ShopKeeper.DyeColor.RED, false);
            shopListener.registerShopKeeper(blueShop, ShopKeeper.DyeColor.BLUE, false);
            shopListener.registerShopKeeper(yellowShop, ShopKeeper.DyeColor.YELLOW, false);
            shopListener.registerShopKeeper(greenShop, ShopKeeper.DyeColor.GREEN, false);

            Villager redTeamShop = ShopKeeper.spawnShopKeeper(new Location(world, 6.5, 66, 80), ShopKeeper.DyeColor.RED, world, 90, true);
            Villager blueTeamShop = ShopKeeper.spawnShopKeeper(new Location(world, -79, 66, 6.5), ShopKeeper.DyeColor.BLUE, world, 180, true);
            Villager yellowTeamShop = ShopKeeper.spawnShopKeeper(new Location(world, -5.5, 66, -79), ShopKeeper.DyeColor.YELLOW, world, 270, true);
            Villager greenTeamShop = ShopKeeper.spawnShopKeeper(new Location(world, 80, 66, -5.5), ShopKeeper.DyeColor.GREEN, world, 0, true);

            shopListener.registerShopKeeper(redTeamShop, ShopKeeper.DyeColor.RED, true);
            shopListener.registerShopKeeper(blueTeamShop, ShopKeeper.DyeColor.BLUE, true);
            shopListener.registerShopKeeper(yellowTeamShop, ShopKeeper.DyeColor.YELLOW, true);
            shopListener.registerShopKeeper(greenTeamShop, ShopKeeper.DyeColor.GREEN, true);

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