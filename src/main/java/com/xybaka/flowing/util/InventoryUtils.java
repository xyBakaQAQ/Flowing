package com.xybaka.flowing.util;

import net.minecraft.block.BlockState;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.component.type.ToolComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ArrowItem;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BowItem;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.EggItem;
import net.minecraft.item.EnderPearlItem;
import net.minecraft.item.ExperienceBottleItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.LingeringPotionItem;
import net.minecraft.item.PotionItem;
import net.minecraft.item.ShieldItem;
import net.minecraft.item.SnowballItem;
import net.minecraft.item.SplashPotionItem;
import net.minecraft.item.SwordItem;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.ItemTags;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class InventoryUtils {
    private static final double SCORE_EPSILON = 0.0001D;

    private static final Set<Item> CLEANER_WHITELIST = new HashSet<>(Arrays.asList(
            Items.ENDER_PEARL, Items.ENDER_EYE, Items.TRIDENT, Items.MACE, Items.TNT,
            Items.BOW, Items.CROSSBOW, Items.ARROW,
            Items.GOLDEN_APPLE, Items.ENCHANTED_GOLDEN_APPLE, Items.APPLE,
            Items.MUSHROOM_STEW, Items.BREAD, Items.PORKCHOP, Items.COOKED_PORKCHOP,
            Items.GOLDEN_CARROT, Items.CARROT, Items.POTATO, Items.BAKED_POTATO,
            Items.COOKED_BEEF, Items.BEEF, Items.COOKED_CHICKEN, Items.CHICKEN,
            Items.COOKED_MUTTON, Items.MUTTON, Items.COOKED_RABBIT, Items.RABBIT,
            Items.RABBIT_STEW, Items.BEETROOT, Items.BEETROOT_SOUP, Items.MELON_SLICE,
            Items.PUMPKIN_PIE, Items.COOKIE, Items.SWEET_BERRIES, Items.COD,
            Items.COOKED_COD, Items.SALMON, Items.COOKED_SALMON, Items.HONEY_BOTTLE,
            Items.DRIED_KELP, Items.COMPASS, Items.RECOVERY_COMPASS,
            Items.WATER_BUCKET, Items.ELYTRA, Items.FLINT_AND_STEEL,
            Items.IRON_INGOT, Items.GOLD_INGOT, Items.DIAMOND,
            Items.EMERALD, Items.NETHERITE_INGOT
    ));
    private static final Set<Item> BLOCK_BLACKLIST = new HashSet<>(Arrays.asList(
            Items.TNT,
            Items.SAND, Items.RED_SAND,
            Items.GRAVEL,
            Items.CHEST, Items.TRAPPED_CHEST, Items.ENDER_CHEST, Items.BARREL,
            Items.HOPPER, Items.DISPENSER, Items.DROPPER,
            Items.CRAFTING_TABLE, Items.FURNACE, Items.BLAST_FURNACE, Items.SMOKER,
            Items.ENCHANTING_TABLE, Items.BREWING_STAND,
            Items.GRINDSTONE, Items.SMITHING_TABLE, Items.CARTOGRAPHY_TABLE,
            Items.FLETCHING_TABLE, Items.LOOM, Items.STONECUTTER,
            Items.ANVIL, Items.CHIPPED_ANVIL, Items.DAMAGED_ANVIL,
            Items.DRAGON_EGG,
            Items.SUSPICIOUS_SAND, Items.SUSPICIOUS_GRAVEL
    ));

    private InventoryUtils() {
    }

    public static String getItemCategory(ItemStack stack) {
        Item item = stack.getItem();

        if (stack.get(DataComponentTypes.EQUIPPABLE) != null) {
            return "armor_" + stack.get(DataComponentTypes.EQUIPPABLE).slot().getName();
        }

        if (stack.isIn(ItemTags.SWORDS)) {
            return "weapon_sword";
        }
        if (item instanceof BowItem) {
            return "weapon_bow";
        }
        if (item instanceof CrossbowItem) {
            return "weapon_crossbow";
        }

        if (stack.isIn(ItemTags.PICKAXES)) {
            return "tool_pickaxe";
        }
        if (stack.isIn(ItemTags.SHOVELS)) {
            return "tool_shovel";
        }
        if (stack.isIn(ItemTags.AXES)) {
            return "tool_axe";
        }
        if (stack.isIn(ItemTags.HOES)) {
            return "tool_hoe";
        }

        if (item == Items.GOLDEN_APPLE || item == Items.ENCHANTED_GOLDEN_APPLE) {
            return "golden_apple";
        }
        if (item instanceof EnderPearlItem) {
            return "pearl";
        }
        if (stack.contains(DataComponentTypes.FOOD)) {
            return "food";
        }
        if (item instanceof BlockItem) {
            return "block";
        }
        if (item instanceof ArrowItem) {
            return "arrow";
        }

        return "other";
    }

    public static String getHelperItemCategory(ItemStack stack) {
        Item item = stack.getItem();
        String category = getItemCategory(stack);
        if (item instanceof BlockItem && BLOCK_BLACKLIST.contains(item)) {
            category = "other";
        }
        if (!"other".equals(category) && !"block".equals(category)) {
            return category;
        }

        if ("block".equals(category)) {
            return "block";
        }

        if (item instanceof SnowballItem || item instanceof EggItem || item instanceof ExperienceBottleItem) {
            return "throwable";
        }
        if (item instanceof PotionItem || item instanceof SplashPotionItem || item instanceof LingeringPotionItem) {
            return "potion";
        }
        if (isCleanerWhitelisted(item)) {
            return "valuable";
        }

        return "other";
    }

    public static boolean isMiningToolCategory(String category) {
        return "tool_pickaxe".equals(category)
                || "tool_shovel".equals(category)
                || "tool_axe".equals(category)
                || "tool_hoe".equals(category);
    }

    public static boolean isArmorSlot(EquipmentSlot slot) {
        return slot == EquipmentSlot.HEAD
                || slot == EquipmentSlot.CHEST
                || slot == EquipmentSlot.LEGS
                || slot == EquipmentSlot.FEET;
    }

    public static int getArmorScreenSlot(EquipmentSlot slot) {
        return switch (slot) {
            case HEAD -> 5;
            case CHEST -> 6;
            case LEGS -> 7;
            case FEET -> 8;
            default -> -1;
        };
    }

    public static int toScreenSlot(int inventoryIndex) {
        return inventoryIndex < 9 ? inventoryIndex + 36 : inventoryIndex;
    }

    public static boolean isCleanerWhitelisted(Item item) {
        return CLEANER_WHITELIST.contains(item);
    }

    public static String preferenceToCategory(String preference) {
        return switch (preference) {
            case "Sword" -> "weapon_sword";
            case "Pickaxe" -> "tool_pickaxe";
            case "Axe" -> "tool_axe";
            case "Shovel" -> "tool_shovel";
            case "Bow" -> "weapon_bow";
            case "Crossbow" -> "weapon_crossbow";
            case "Block" -> "block";
            case "Arrow" -> "arrow";
            case "Throwable" -> "throwable";
            case "GoldenApple" -> "golden_apple";
            case "Pearl" -> "pearl";
            default -> null;
        };
    }

    public static boolean matchesOffHandPreference(ItemStack stack, String preference) {
        return switch (preference) {
            case "Shield" -> stack.getItem() instanceof ShieldItem;
            case "Totem" -> stack.getItem() == Items.TOTEM_OF_UNDYING;
            case "Arrow" -> stack.getItem() instanceof ArrowItem;
            case "Block" -> "block".equals(getHelperItemCategory(stack));
            case "GoldenApple" -> stack.getItem() == Items.GOLDEN_APPLE || stack.getItem() == Items.ENCHANTED_GOLDEN_APPLE;
            case "WindCharge" -> stack.getItem() == Items.WIND_CHARGE;
            default -> false;
        };
    }

    public static double getPlayerMaxScore(PlayerEntity player, String category) {
        double max = -1.0D;

        for (ItemStack stack : player.getInventory().main) {
            if (!stack.isEmpty() && category.equals(getHelperItemCategory(stack))) {
                max = Math.max(max, calculateScore(player, stack));
            }
        }

        for (ItemStack stack : player.getInventory().armor) {
            if (!stack.isEmpty() && category.equals(getHelperItemCategory(stack))) {
                max = Math.max(max, calculateScore(player, stack));
            }
        }

        ItemStack offhand = player.getOffHandStack();
        if (!offhand.isEmpty() && category.equals(getHelperItemCategory(offhand))) {
            max = Math.max(max, calculateScore(player, offhand));
        }

        return max;
    }

    public static int getBestPlayerInventorySlot(PlayerEntity player, String category) {
        int bestSlot = -1;
        double bestScore = -1.0D;

        for (int index = 0; index < player.getInventory().main.size(); index++) {
            ItemStack stack = player.getInventory().main.get(index);
            if (stack.isEmpty() || !category.equals(getHelperItemCategory(stack))) {
                continue;
            }

            double score = calculateScore(player, stack);
            if (bestSlot == -1 || score > bestScore + SCORE_EPSILON) {
                bestSlot = index;
                bestScore = score;
            }
        }

        return bestSlot;
    }

    public static void addBestScore(PlayerEntity player, Map<String, Double> scores, ItemStack stack) {
        if (stack.isEmpty()) {
            return;
        }

        String category = getHelperItemCategory(stack);
        if (category.equals("other")) {
            return;
        }

        double score = calculateScore(player, stack);
        scores.merge(category, score, Math::max);
    }

    public static int getBestToolSlot(PlayerEntity player, BlockState state, boolean includeSword) {
        int bestSlot = player.getInventory().selectedSlot;
        double bestScore = getToolScore(player, player.getInventory().getStack(bestSlot), state, includeSword);

        for (int slot = 0; slot < 9; slot++) {
            ItemStack stack = player.getInventory().getStack(slot);
            double score = getToolScore(player, stack, state, includeSword);
            if (score > bestScore) {
                bestScore = score;
                bestSlot = slot;
            }
        }

        return bestSlot;
    }

    public static double calculateScore(PlayerEntity player, ItemStack stack) {
        if (stack.isEmpty()) {
            return 0.0D;
        }

        if (stack.isIn(ItemTags.SWORDS) || stack.isIn(ItemTags.AXES)) {
            return getAttributeValue(stack, EntityAttributes.ATTACK_DAMAGE)
                    + getEnchantmentLevel(player, stack, Enchantments.SHARPNESS);
        }

        if (stack.contains(DataComponentTypes.TOOL)) {
            double baseSpeed = getMaxMiningSpeed(stack);
            int efficiency = getEnchantmentLevel(player, stack, Enchantments.EFFICIENCY);
            return baseSpeed + (efficiency > 0 ? (efficiency * efficiency) + 1.0D : 0.0D);
        }

        if (stack.contains(DataComponentTypes.EQUIPPABLE)) {
            return calculateArmorScore(player, stack);
        }

        if (stack.getItem() instanceof BowItem) {
            return getEnchantmentLevel(player, stack, Enchantments.POWER) * 2.0D
                    + getEnchantmentLevel(player, stack, Enchantments.PUNCH);
        }

        return 0.0D;
    }

    public static double calculateArmorScore(PlayerEntity player, ItemStack stack) {
        if (!stack.contains(DataComponentTypes.EQUIPPABLE)) {
            return 0.0D;
        }

        double armor = getAttributeValue(stack, EntityAttributes.ARMOR);
        double toughness = getAttributeValue(stack, EntityAttributes.ARMOR_TOUGHNESS);
        int protection = getEnchantmentLevel(player, stack, Enchantments.PROTECTION);

        double[] testDamages = {4.0D, 8.0D, 12.0D, 16.0D, 20.0D};
        double totalReduction = 0.0D;
        for (double damage : testDamages) {
            totalReduction += calculateDamageReduction(armor, toughness, damage);
        }

        double averageReduction = totalReduction / testDamages.length;
        double protectionReduction = protection * 0.04D;
        return Math.min(0.80D, averageReduction + protectionReduction) * 100.0D;
    }

    public static int getEnchantmentLevel(PlayerEntity player, ItemStack stack, RegistryKey<Enchantment> enchantmentKey) {
        Registry<Enchantment> registry = player.getRegistryManager().getOrThrow(RegistryKeys.ENCHANTMENT);
        Enchantment enchantment = registry.getValueOrThrow(enchantmentKey);
        return EnchantmentHelper.getLevel(registry.getEntry(enchantment), stack);
    }

    public static double getAttributeValue(ItemStack stack, RegistryEntry<EntityAttribute> attribute) {
        AttributeModifiersComponent component = stack.getOrDefault(
                DataComponentTypes.ATTRIBUTE_MODIFIERS,
                AttributeModifiersComponent.DEFAULT
        );

        double total = 0.0D;
        for (AttributeModifiersComponent.Entry entry : component.modifiers()) {
            if (entry.attribute().equals(attribute)) {
                total += entry.modifier().value();
            }
        }
        return total;
    }

    public static float getMaxMiningSpeed(ItemStack stack) {
        ToolComponent tool = stack.get(DataComponentTypes.TOOL);
        if (tool == null) {
            return 1.0F;
        }

        float maxSpeed = tool.defaultMiningSpeed();
        for (ToolComponent.Rule rule : tool.rules()) {
            if (rule.speed().isPresent()) {
                maxSpeed = Math.max(maxSpeed, rule.speed().get());
            }
        }
        return maxSpeed;
    }

    private static double getToolScore(PlayerEntity player, ItemStack stack, BlockState state, boolean includeSword) {
        if (stack.isEmpty()) {
            return 1.0D;
        }

        if (!includeSword && stack.getItem() instanceof SwordItem) {
            return 0.0D;
        }

        double speed = stack.getMiningSpeedMultiplier(state);
        if (speed <= 1.0D) {
            return speed;
        }

        int efficiencyLevel = getEnchantmentLevel(player, stack, Enchantments.EFFICIENCY);
        if (efficiencyLevel > 0) {
            speed += (efficiencyLevel * efficiencyLevel) + 1.0D;
        }

        return speed;
    }

    private static double calculateDamageReduction(double armor, double toughness, double damage) {
        double effectiveArmor = Math.max(0.2D * armor, armor - damage / 2.0D + 0.25D * toughness);
        effectiveArmor = Math.min(20.0D, effectiveArmor);
        return Math.min(0.80D, Math.max(0.0D, effectiveArmor / 25.0D));
    }
}
