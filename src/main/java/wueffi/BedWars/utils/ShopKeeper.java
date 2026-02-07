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

    public static Villager spawnShopKeeper(Location loc, DyeColor color, World world, float yaw) {
        loc.setYaw(yaw);
        Villager villager = (Villager) world.spawnEntity(loc, EntityType.VILLAGER);
        villager.setCustomName(color.chatColor() + "Team " + color.name().charAt(0) + color.name().substring(1).toLowerCase() + " Shop");
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

    enum DyeColor {
        RED(ChatColor.DARK_RED),
        BLUE(ChatColor.DARK_BLUE),
        YELLOW(ChatColor.YELLOW),
        GREEN(ChatColor.DARK_GREEN);

        private final ChatColor chatColor;

        DyeColor(ChatColor chatColor) {
            this.chatColor = chatColor;
        }

        public ChatColor chatColor() {
            return chatColor;
        }
    }
}