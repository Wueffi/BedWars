package wueffi.BedWars.utils;

import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import wueffi.MiniGameCore.managers.LobbyManager;
import wueffi.MiniGameCore.utils.Team;

import java.util.*;

public class ShopListener implements Listener {

    private final Plugin plugin;
    private final Map<UUID, Inventory> shopInventories = new HashMap<>();
    private static Map<UUID, ShopKeeper.DyeColor> villagerColors = new HashMap<>();
    private static List<UUID> teamShops = new ArrayList<>();

    static Map<Team, Integer> currentSharpnessLevel = new HashMap<>();
    private static Map<Team, Integer> currentProtectionLevel = new HashMap<>();
    private static Map<Team, Integer> currentForgeLevel = new HashMap<>();

    public ShopListener(Plugin plugin) {
        this.plugin = plugin;
    }

    public void registerShopKeeper(Villager villager, ShopKeeper.DyeColor color, boolean teamShop) {
        villagerColors.put(villager.getUniqueId(), color);
        if (teamShop) {
            teamShops.add(villager.getUniqueId());
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEntityEvent event) {
        if (!(event.getRightClicked() instanceof Villager villager)) return;

        if (!villagerColors.containsKey(villager.getUniqueId())) {
            return;
        }
        event.setCancelled(true);

        ShopKeeper.DyeColor color = villagerColors.get(villager.getUniqueId());
        Player player = event.getPlayer();

        Inventory shop;

        if (teamShops.contains(villager.getUniqueId())) {
            shop = Bukkit.createInventory(null, 36, color.chatColor() + "General Shop");
            populateShop(shop, color, player);
            shopInventories.put(player.getUniqueId(), shop);
        } else {
            shop = Bukkit.createInventory(null, 27, color.chatColor() + "Upgrade Shop");
            populateTeamShop(shop, player);
            shopInventories.put(player.getUniqueId(), shop);
        }
        player.openInventory(shop);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        Team team = LobbyManager.getLobbyByPlayer(player).getTeamByPlayer(player);

        if (!shopInventories.containsKey(player.getUniqueId())) return;
        if (!event.getInventory().equals(shopInventories.get(player.getUniqueId()))) return;

        event.setCancelled(true);
        if (event.getCurrentItem() == null) return;

        ItemStack clicked = event.getCurrentItem();
        if (clicked.getItemMeta() == null || !clicked.getItemMeta().hasLore()) return;

        String loreLine = Objects.requireNonNull(clicked.getItemMeta().getLore()).getFirst();
        Material costMaterial;
        int costAmount;

        if (loreLine.contains("Iron")) {
            costMaterial = Material.IRON_INGOT;
        } else if (loreLine.contains("Gold")) {
            costMaterial = Material.GOLD_INGOT;
        } else if (loreLine.contains("Emerald")) {
            costMaterial = Material.EMERALD;
        } else if (loreLine.contains("Diamond")) {
            costMaterial = Material.DIAMOND;
        } else {
            return;
        }

        String title = clicked.getItemMeta().getDisplayName();
        String[] parts = loreLine.split(" ");

        costAmount = Integer.parseInt(parts[1]);

        int playerHas = countMaterial(player.getInventory().getContents(), costMaterial);
        if (playerHas < costAmount) {
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
            return;
        }

        player.sendMessage(title);

        switch (title) {
            case "§bProtection 1" -> {
                player.closeInventory();
                updateProtection(team, 1);
            }
            case "§bProtection 2" -> {
                player.closeInventory();
                updateProtection(team, 2);
            }
            case "§bProtection 3" -> {
                player.closeInventory();
                updateProtection(team, 3);
            }
            case "§bProtection 4" -> {
                player.closeInventory();
                updateProtection(team, 4);
            }
            case "§bBase Generator Level 2" -> {
                player.closeInventory();
                currentForgeLevel.put(team, 2);
            }
            case "§bBase Generator Level 3" -> {
                player.closeInventory();
                currentForgeLevel.put(team, 3);
            }
            case "§bBase Generator Level 4" -> {
                player.closeInventory();
                currentForgeLevel.put(team, 4);
            }
            case "§bSharpness 1" -> {
                player.closeInventory();
                applySharpness(team);
            }

            case "§aAlready bought!" -> {
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
                return;
            }
            default -> {
                ItemStack item = clicked.clone();
                item.setLore(Collections.singletonList(""));

                if (item.equals(ItemStack.of(Material.CHAINMAIL_CHESTPLATE))) {
                    ItemStack helmet = player.getInventory().getHelmet();
                    if (currentProtectionLevel.get(team) > 0) {
                        helmet.addEnchantment(Enchantment.PROTECTION, currentProtectionLevel.get(team));
                    }
                    player.getInventory().setHelmet(helmet);

                    ItemStack chestplate = new ItemStack(Material.CHAINMAIL_CHESTPLATE);
                    if (currentProtectionLevel.get(team) > 0) {
                        chestplate.addEnchantment(Enchantment.PROTECTION, currentProtectionLevel.get(team));
                    }
                    player.getInventory().setHelmet(chestplate);
                } else if (item.equals(ItemStack.of(Material.IRON_CHESTPLATE))) {
                    ItemStack helmet = new ItemStack(Material.IRON_HELMET);
                    if (currentProtectionLevel.get(team) > 0) {
                        helmet.addEnchantment(Enchantment.PROTECTION, currentProtectionLevel.get(team));
                    }
                    player.getInventory().setHelmet(helmet);

                    ItemStack chestplate = new ItemStack(Material.IRON_CHESTPLATE);
                    if (currentProtectionLevel.get(team) > 0) {
                        chestplate.addEnchantment(Enchantment.PROTECTION, currentProtectionLevel.get(team));
                    }
                    player.getInventory().setHelmet(chestplate);
                } else if (item.equals(ItemStack.of(Material.DIAMOND_CHESTPLATE))) {
                    ItemStack helmet = new ItemStack(Material.DIAMOND_HELMET);
                    if (currentProtectionLevel.get(team) > 0) {
                        helmet.addEnchantment(Enchantment.PROTECTION, currentProtectionLevel.get(team));
                    }
                    player.getInventory().setHelmet(helmet);

                    ItemStack chestplate = new ItemStack(Material.DIAMOND_CHESTPLATE);
                    if (currentProtectionLevel.get(team) > 0) {
                        chestplate.addEnchantment(Enchantment.PROTECTION, currentProtectionLevel.get(team));
                    }
                    player.getInventory().setHelmet(chestplate);
                } else {
                    player.getInventory().addItem(item);
                }
            }
        }
        removeMaterial(player, costMaterial, costAmount);
        player.updateInventory();
        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_YES, 1, 1);
    }

    private void populateShop(Inventory shop, ShopKeeper.DyeColor color, Player player) {
        Material woolMaterial = getColoredWool(color);
        Team team = LobbyManager.getLobbyByPlayer(player).getTeamByPlayer(player);

        if (!currentSharpnessLevel.containsKey(team)) {
            setUpTeamLevels(team);
        }

        ItemStack wool = new ItemStack(woolMaterial, 16);
        wool.setItemMeta(createItemMeta(wool, color.chatColor() + "Wool", "Cost: 4 Iron"));
        shop.setItem(0, wool);

        ItemStack endstone = new ItemStack(Material.END_STONE, 12);
        endstone.setItemMeta(createItemMeta(endstone, "§7Endstone", "Cost: 24 Iron"));
        shop.setItem(1, endstone);

        ItemStack planks = new ItemStack(Material.OAK_PLANKS, 16);
        planks.setItemMeta(createItemMeta(planks, "§6Oak Planks", "Cost: 4 Gold"));
        shop.setItem(2, planks);

        ItemStack obsidian = new ItemStack(Material.OBSIDIAN, 4);
        obsidian.setItemMeta(createItemMeta(obsidian, "§2Obsidian", "Cost: 4 Emerald"));
        shop.setItem(3, obsidian);
        //---------
        ItemStack chainArmor = new ItemStack(Material.CHAINMAIL_CHESTPLATE, 1);
        chainArmor.setItemMeta(createItemMeta(chainArmor, "§7Chainmail Armor", "Cost: 40 Iron"));
        if (currentProtectionLevel.get(team) > 0) {
            chainArmor.addEnchantment(Enchantment.PROTECTION, currentProtectionLevel.get(team));
        }
        shop.setItem(5, chainArmor);

        ItemStack ironArmor = new ItemStack(Material.IRON_CHESTPLATE, 1);
        ironArmor.setItemMeta(createItemMeta(ironArmor, "§6Iron Armor", "Cost: 12 Gold"));
        if (currentProtectionLevel.get(team) > 0) {
            ironArmor.addEnchantment(Enchantment.PROTECTION, currentProtectionLevel.get(team));
        }
        shop.setItem(6, ironArmor);

        ItemStack diaArmor = new ItemStack(Material.DIAMOND_CHESTPLATE, 1);
        diaArmor.setItemMeta(createItemMeta(diaArmor, "§2Diamond Armor", "Cost: 6 Emerald"));
        if (currentProtectionLevel.get(team) > 0) {
            diaArmor.addEnchantment(Enchantment.PROTECTION, currentProtectionLevel.get(team));
        }
        shop.setItem(7, diaArmor);

        ItemStack diaAxe = new ItemStack(Material.DIAMOND_AXE, 1);
        diaAxe.setItemMeta(createItemMeta(diaAxe, "§6Diamond Axe (EFF II)", "Cost: 8 Gold"));
        ItemMeta axeMeta = diaAxe.getItemMeta();
        axeMeta.addEnchant(Enchantment.EFFICIENCY, 2, false);
        diaAxe.setItemMeta(axeMeta);
        shop.setItem(8, diaAxe);
        //---------
        ItemStack stoneSword = new ItemStack(Material.STONE_SWORD, 1);
        stoneSword.setItemMeta(createItemMeta(stoneSword, "§7Stone Sword", "Cost: 10 Iron"));
        if (currentSharpnessLevel.get(team) > 0) {
            stoneSword.addEnchantment(Enchantment.SHARPNESS, currentSharpnessLevel.get(team));
        }
        shop.setItem(9, stoneSword);

        ItemStack ironSword = new ItemStack(Material.IRON_SWORD, 1);
        ironSword.setItemMeta(createItemMeta(ironSword, "§6Iron Sword", "Cost: 7 Gold"));
        if (currentSharpnessLevel.get(team) > 0) {
            ironSword.addEnchantment(Enchantment.SHARPNESS, currentSharpnessLevel.get(team));
        }
        shop.setItem(10, ironSword);

        ItemStack diaSword = new ItemStack(Material.DIAMOND_SWORD, 1);
        diaSword.setItemMeta(createItemMeta(diaSword, "§7Diamond Sword", "Cost: 4 Emerald"));
        if (currentSharpnessLevel.get(team) > 0) {
            diaSword.addEnchantment(Enchantment.SHARPNESS, currentSharpnessLevel.get(team));
        }
        shop.setItem(11, diaSword);

        ItemStack knockbackStick = new ItemStack(Material.STICK, 1);
        knockbackStick.setItemMeta(createItemMeta(knockbackStick, "§6Knockback Stick", "Cost: 10 Gold"));
        ItemMeta stickMeta = knockbackStick.getItemMeta();
        stickMeta.addEnchant(Enchantment.KNOCKBACK, 1, true);
        knockbackStick.setItemMeta(stickMeta);
        shop.setItem(12, knockbackStick);
        //---------
        ItemStack arrow = new ItemStack(Material.ARROW, 8);
        arrow.setItemMeta(createItemMeta(arrow, "§6Arrows", "Cost: 2 Gold"));
        shop.setItem(14, arrow);

        ItemStack bow = new ItemStack(Material.BOW, 1);
        bow.setItemMeta(createItemMeta(bow, "§6Bow", "Cost: 12 Gold"));
        shop.setItem(15, bow);

        ItemStack bowPowerI = new ItemStack(Material.BOW, 1);
        bowPowerI.setItemMeta(createItemMeta(bowPowerI, "§6Bow (Power I)", "Cost: 24 Gold"));
        ItemMeta bowPowerIMeta = bowPowerI.getItemMeta();
        bowPowerIMeta.addEnchant(Enchantment.POWER, 1, false);
        bowPowerI.setItemMeta(bowPowerIMeta);
        shop.setItem(16, bowPowerI);

        ItemStack bowPowerII = new ItemStack(Material.BOW, 1);
        bowPowerII.setItemMeta(createItemMeta(bowPowerII, "§2Bow (Power I + PUNCH I)", "Cost: 6 Emerald"));
        ItemMeta bowPowerIIMeta = bowPowerII.getItemMeta();
        bowPowerIIMeta.addEnchant(Enchantment.POWER, 1, false);
        bowPowerIIMeta.addEnchant(Enchantment.PUNCH, 1, false);
        bowPowerII.setItemMeta(bowPowerIIMeta);
        shop.setItem(17, bowPowerII);
        //---------
        ItemStack woodPick = new ItemStack(Material.WOODEN_PICKAXE, 1);
        woodPick.setItemMeta(createItemMeta(woodPick, "§7Wooden Pickaxe", "Cost: 10 Iron"));
        shop.setItem(18, woodPick);

        ItemStack stonePick = new ItemStack(Material.STONE_PICKAXE, 1);
        stonePick.setItemMeta(createItemMeta(stonePick, "§7Stone Pickaxe (EFF I)", "Cost: 20 Iron"));
        ItemMeta stoneMeta = stonePick.getItemMeta();
        stoneMeta.addEnchant(Enchantment.EFFICIENCY, 1, false);
        stonePick.setItemMeta(stoneMeta);
        shop.setItem(19, stonePick);

        ItemStack ironPick = new ItemStack(Material.IRON_PICKAXE, 1);
        ironPick.setItemMeta(createItemMeta(ironPick, "§6Iron Pickaxe (EFF II)", "Cost: 8 Gold"));
        ItemMeta ironMeta = ironPick.getItemMeta();
        ironMeta.addEnchant(Enchantment.EFFICIENCY, 2, false);
        ironPick.setItemMeta(ironMeta);
        shop.setItem(20, ironPick);

        ItemStack diaPick = new ItemStack(Material.DIAMOND_PICKAXE, 1);
        diaPick.setItemMeta(createItemMeta(diaPick, "§6Diamond Pickaxe (EFF III)", "Cost: 12 Gold"));
        ItemMeta diaMeta = diaPick.getItemMeta();
        diaMeta.addEnchant(Enchantment.EFFICIENCY, 3, false);
        diaPick.setItemMeta(diaMeta);
        shop.setItem(21, diaPick);

        ItemStack shears = new ItemStack(Material.SHEARS, 1);
        shears.setItemMeta(createItemMeta(shears, "§7Shears", "Cost: 30 Iron"));
        shop.setItem(23, shears);
        //---------
        ItemStack speedPotion = new ItemStack(Material.POTION, 1);
        PotionMeta potionMeta = (PotionMeta) speedPotion.getItemMeta();
        potionMeta.setDisplayName("§2Speed Potion II");
        potionMeta.setLore(Collections.singletonList("Cost: 1 Emerald"));
        potionMeta.addCustomEffect(new PotionEffect(PotionEffectType.SPEED, 900, 2), true);
        potionMeta.setBasePotionType(PotionType.STRONG_SWIFTNESS);
        speedPotion.setItemMeta(potionMeta);
        shop.setItem(24, speedPotion);

        ItemStack jumpPotion = new ItemStack(Material.POTION, 1);
        PotionMeta potionMeta2 = (PotionMeta) jumpPotion.getItemMeta();
        potionMeta2.setDisplayName("§2Jump Potion V");
        potionMeta2.setLore(Collections.singletonList("Cost: 1 Emerald"));
        potionMeta2.addCustomEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, 900, 4), true);
        potionMeta2.setBasePotionType(PotionType.STRONG_LEAPING);
        jumpPotion.setItemMeta(potionMeta2);
        shop.setItem(25, jumpPotion);

        ItemStack invisibilityPotion = new ItemStack(Material.POTION, 1);
        PotionMeta potionMeta3 = (PotionMeta) invisibilityPotion.getItemMeta();
        potionMeta3.setDisplayName("§2Inivisibility Potion");
        potionMeta3.setLore(Collections.singletonList("Cost: 1 Emerald"));
        potionMeta3.addCustomEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 600, 1), true);
        potionMeta3.setBasePotionType(PotionType.INVISIBILITY);
        invisibilityPotion.setItemMeta(potionMeta3);
        shop.setItem(26, invisibilityPotion);
        //---------
        ItemStack enderPearl = new ItemStack(Material.ENDER_PEARL, 1);
        enderPearl.setItemMeta(createItemMeta(enderPearl, "§2Ender Pearl", "Cost: 4 Emerald"));
        shop.setItem(27, enderPearl);

        ItemStack goldApple = new ItemStack(Material.GOLDEN_APPLE, 1);
        goldApple.setItemMeta(createItemMeta(goldApple, "§6Golden Apple", "Cost: 3 Gold"));
        shop.setItem(28, goldApple);

        ItemStack waterBucket = new ItemStack(Material.WATER_BUCKET, 1);
        waterBucket.setItemMeta(createItemMeta(waterBucket, "§2Water Bucket", "Cost: 1 Emerald"));
        shop.setItem(29, waterBucket);

        ItemStack TNT = new ItemStack(Material.TNT, 1);
        TNT.setItemMeta(createItemMeta(TNT, "§6TNT", "Cost: 5 Gold"));
        shop.setItem(30, TNT);

        ItemStack ironGolem = new ItemStack(Material.IRON_GOLEM_SPAWN_EGG, 1);
        ironGolem.setItemMeta(createItemMeta(ironGolem, "§7Iron Golem Spawn Egg", "Cost: 150 Iron"));
        shop.setItem(32, ironGolem);

        ItemStack fireBall = new ItemStack(Material.FIRE_CHARGE, 1);
        fireBall.setItemMeta(createItemMeta(fireBall, "§7Fire Ball", "Cost: 40 Iron"));
        shop.setItem(33, fireBall);

        ItemStack snowBall = new ItemStack(Material.SNOWBALL, 16);
        snowBall.setItemMeta(createItemMeta(snowBall, "§7Snow Ball", "Cost: 24 Iron"));
        shop.setItem(34, snowBall);

        ItemStack bridgeEgg = new ItemStack(Material.EGG, 1);
        bridgeEgg.setItemMeta(createItemMeta(bridgeEgg, "§2Bridge Egg", "Cost: 2 Emerald"));
        shop.setItem(35, bridgeEgg);
    }

    private void populateTeamShop(Inventory shop, Player player) {
        Team team = LobbyManager.getLobbyByPlayer(player).getTeamByPlayer(player);

        if (!currentSharpnessLevel.containsKey(team)) {
            setUpTeamLevels(team);
        }

        ItemStack sharpness = new ItemStack(Material.IRON_SWORD, 1);
        if (currentSharpnessLevel.get(team) > 0) {
            sharpness.setItemMeta(createItemMeta(sharpness, "§aAlready bought!", ""));
        } else {
            sharpness.setItemMeta(createItemMeta(sharpness, "§bSharpness 1", "Cost: 4 Diamonds"));
            sharpness.addEnchantment(Enchantment.SHARPNESS, 1);
        }
        shop.setItem(10, sharpness);

        ItemStack protection = new ItemStack(Material.IRON_CHESTPLATE, 1);
        if (currentProtectionLevel.get(team) >= 4) {
            protection.setItemMeta(createItemMeta(protection, "§aAlready bought!", ""));
        } else {
            protection.addEnchantment(Enchantment.PROTECTION, (currentProtectionLevel.get(team) + 1));
            protection.setItemMeta(createItemMeta(protection, "§bProtection " + (currentProtectionLevel.get(team) + 1), "Cost: " + (int) Math.pow(2, (currentProtectionLevel.get(team) + 1)) + " Diamonds"));
        }
        shop.setItem(13, protection);

        ItemStack forge = new ItemStack(Material.IRON_INGOT, 1);
        if (currentForgeLevel.get(team) >= 4) {
            forge.setItemMeta(createItemMeta(forge, "§aAlready bought!", ""));
        } else {
            forge.setItemMeta(createItemMeta(forge, "§bBase Generator Level " + (currentForgeLevel.get(team) + 1), "Cost: " + (8 + 4 * currentForgeLevel.get(team)) + " Diamonds"));
        }
        shop.setItem(16, forge);
    }

    private void updateProtection(Team team, int level) {
        currentProtectionLevel.remove(team);
        currentProtectionLevel.put(team, level);
        for (Player player1 : team.getPlayers()) {
            ItemStack helmet = player1.getInventory().getHelmet();
            assert helmet != null;
            helmet.addEnchantment(Enchantment.PROTECTION, level);

            ItemStack chestplate = player1.getInventory().getChestplate();
            assert chestplate != null;
            chestplate.addEnchantment(Enchantment.PROTECTION, level);
        }
    }

    private void applySharpness(Team team) {
        currentSharpnessLevel.remove(team);
        currentSharpnessLevel.put(team, 1);
        for (Player player : team.getPlayers()) {
            for (ItemStack item : player.getInventory().getContents()) {
                if (item == null) {
                    continue;
                }

                Material type = item.getType();

                if (type == Material.WOODEN_SWORD || type == Material.STONE_SWORD || type == Material.IRON_SWORD || type == Material.DIAMOND_SWORD) {
                    item.addEnchantment(Enchantment.SHARPNESS, 1);
                }
            }
        }
    }


    public int getForgeLevel(Team team) {
        return currentForgeLevel.get(team);
    }

    public static void setUpTeamLevels(Team team) {
        currentForgeLevel.put(team, 1);
        currentProtectionLevel.put(team, 0);
        currentSharpnessLevel.put(team, 0);
    }

    private ItemMeta createItemMeta(ItemStack item, String name, String cost) {
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(Arrays.asList(cost));
        item.setItemMeta(meta);
        return meta;
    }

    private Material getColoredWool(ShopKeeper.DyeColor color) {
        switch (color) {
            case RED: return Material.RED_WOOL;
            case BLUE: return Material.BLUE_WOOL;
            case YELLOW: return Material.YELLOW_WOOL;
            case GREEN: return Material.GREEN_WOOL;
            default: return Material.WHITE_WOOL;
        }
    }

    private int countMaterial(ItemStack[] contents, Material material) {
        int count = 0;
        for (ItemStack item : contents) {
            if (item != null && item.getType() == material) {
                count += item.getAmount();
            }
        }
        return count;
    }

    private void removeMaterial(Player player, Material material, int amount) {
        int remaining = amount;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == material) {
                if (item.getAmount() <= remaining) {
                    remaining -= item.getAmount();
                    item.setAmount(0);
                } else {
                    item.setAmount(item.getAmount() - remaining);
                    remaining = 0;
                }
                if (remaining == 0) break;
            }
        }
        player.updateInventory();
    }

    public void playerLeft(Player player) {
        shopInventories.remove(player.getUniqueId());
    }
}