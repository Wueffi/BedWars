package wueffi.BedWars.utils;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;

public class HoloGram {

    private final Plugin plugin;
    private final List<ArmorStand> armorStands = new ArrayList<>();

    public HoloGram(Plugin plugin) {
        this.plugin = plugin;
    }

    public ArmorStand createHologram(World world, double x, double y, double z, String text) {
        Location loc = new Location(world, x, y, z);
        return createHologram(loc, text);
    }

    public ArmorStand createHologram(Location loc, String text) {
        ArmorStand armorStand = (ArmorStand) loc.getWorld().spawnEntity(loc, EntityType.ARMOR_STAND);
        armorStand.setCustomName(text);
        armorStand.setCustomNameVisible(true);
        armorStand.setVisible(false);
        armorStand.setGravity(false);
        armorStand.setInvulnerable(true);
        armorStand.setMarker(true);
        armorStand.setPersistent(false);

        armorStands.add(armorStand);
        return armorStand;
    }

    public void updateText(ArmorStand armorStand, String newText) {
        armorStand.setCustomName(newText);
    }

    public void removeHologram(ArmorStand armorStand) {
        armorStand.remove();
        armorStands.remove(armorStand);
    }

    public void removeAll() {
        for (ArmorStand armorStand : armorStands) {
            armorStand.remove();
        }
        armorStands.clear();
    }
}