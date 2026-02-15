package wueffi.BedWars.utils;

import org.bukkit.*;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Villager;
import org.bukkit.plugin.Plugin;
import wueffi.MiniGameCore.utils.Lobby;

import java.util.ArrayList;
import java.util.List;

public class ShopKeeper {

    private final Plugin plugin;
    private final Lobby lobby;
    private static final List<Villager> shopKeepers = new ArrayList<>();

    public ShopKeeper(Plugin plugin, Lobby lobby) {
        this.plugin = plugin;
        this.lobby = lobby;
    }

    public static Villager spawnShopKeeper(Location loc, String color, World world, float yaw, boolean teamShop) {
        loc.setYaw(yaw);
        Villager villager = (Villager) world.spawnEntity(loc, EntityType.VILLAGER);
        if (teamShop) villager.setCustomName(getChatColor(color) + color + "Team " + " Shop");
        else villager.setCustomName(getChatColor(color) + color + "Team Upgrades");
        villager.setCustomNameVisible(true);
        villager.setInvulnerable(true);
        villager.setAI(false);
        villager.setPersistent(false);
        villager.setProfession(Villager.Profession.ARMORER);
        villager.setVillagerType(Villager.Type.PLAINS);

        villager.setAdult();

        shopKeepers.add(villager);
        return villager;
    }

    public void removeAll() {
        for (Villager villager : shopKeepers) {
            villager.remove();
        }
        shopKeepers.clear();
    }

    public static String getChatColor(String color) {
        return switch (color) {
            case "Red" -> "§4";
            case "Blue" -> "§1";
            case "Yellow" -> "§e";
            case "Green" -> "§2";
            default -> "§f";
        };
    }
}