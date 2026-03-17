package com.xybaka.flowing.modules.player;

import com.xybaka.flowing.event.features.TickEvent;
import com.xybaka.flowing.modules.Category;
import com.xybaka.flowing.modules.movement.InvMove;
import com.xybaka.flowing.modules.Module;
import com.xybaka.flowing.modules.settings.BooleanSetting;
import com.xybaka.flowing.modules.settings.ModeSetting;
import com.xybaka.flowing.modules.settings.NumberSetting;
import com.xybaka.flowing.util.ColorUtil;
import com.xybaka.flowing.util.InventoryUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public final class InventoryHelper extends Module {
    private static final int USEFUL_COLOR = ColorUtil.withAlpha(ColorUtil.green, 0x50);
    private static final int GARBAGE_COLOR = ColorUtil.withAlpha(ColorUtil.red, 0x50);
    private static final double SCORE_EPSILON = 0.0001D;

    private final ModeSetting mode = mode("Mode", "Visual", "Visual", "Manual", "Auto");
    private final BooleanSetting stealer = bool("Stealer", true)
            .visibleWhen(() -> !mode.is("Visual"));
    private final NumberSetting minDelay = number("Min Delay", 150.0D, 0.0D, 1000.0D, 10.0D)
            .visibleWhen(() -> !mode.is("Visual"));
    private final NumberSetting maxDelay = number("Max Delay", 250.0D, 0.0D, 1000.0D, 10.0D)
            .visibleWhen(() -> !mode.is("Visual"));
    private final BooleanSetting autoClose = bool("Auto Close", true)
            .visibleWhen(() -> !mode.is("Visual"));
    private final BooleanSetting pauseWhileMoving = bool("Pause Moving", true)
            .visibleWhen(() -> !mode.is("Visual"));
    private final BooleanSetting checkTools = bool("Check Tools", true);

    private final ModeSetting slotItem1 = mode("Slot Item 1", "Sword", "None", "Sword", "Pickaxe", "Axe", "Shovel", "Bow", "Crossbow", "Block", "GoldenApple", "Pearl", "Arrow", "Throwable")
            .visibleWhen(() -> !mode.is("Visual"));
    private final ModeSetting slotItem2 = mode("Slot Item 2", "GoldenApple", "None", "Sword", "Pickaxe", "Axe", "Shovel", "Bow", "Crossbow", "Block", "GoldenApple", "Pearl", "Arrow", "Throwable")
            .visibleWhen(() -> !mode.is("Visual"));
    private final ModeSetting slotItem3 = mode("Slot Item 3", "Pickaxe", "None", "Sword", "Pickaxe", "Axe", "Shovel", "Bow", "Crossbow", "Block", "GoldenApple", "Pearl", "Arrow", "Throwable")
            .visibleWhen(() -> !mode.is("Visual"));
    private final ModeSetting slotItem4 = mode("Slot Item 4", "Axe", "None", "Sword", "Pickaxe", "Axe", "Shovel", "Bow", "Crossbow", "Block", "GoldenApple", "Pearl", "Arrow", "Throwable")
            .visibleWhen(() -> !mode.is("Visual"));
    private final ModeSetting slotItem5 = mode("Slot Item 5", "Bow", "None", "Sword", "Pickaxe", "Axe", "Shovel", "Bow", "Crossbow", "Block", "GoldenApple", "Pearl", "Arrow", "Throwable")
            .visibleWhen(() -> !mode.is("Visual"));
    private final ModeSetting slotItem6 = mode("Slot Item 6", "Pearl", "None", "Sword", "Pickaxe", "Axe", "Shovel", "Bow", "Crossbow", "Block", "GoldenApple", "Pearl", "Arrow", "Throwable")
            .visibleWhen(() -> !mode.is("Visual"));
    private final ModeSetting slotItem7 = mode("Slot Item 7", "None", "None", "Sword", "Pickaxe", "Axe", "Shovel", "Bow", "Crossbow", "Block", "GoldenApple", "Pearl", "Arrow", "Throwable")
            .visibleWhen(() -> !mode.is("Visual"));
    private final ModeSetting slotItem8 = mode("Slot Item 8", "None", "None", "Sword", "Pickaxe", "Axe", "Shovel", "Bow", "Crossbow", "Block", "GoldenApple", "Pearl", "Arrow", "Throwable")
            .visibleWhen(() -> !mode.is("Visual"));
    private final ModeSetting slotItem9 = mode("Slot Item 9", "Block", "None", "Sword", "Pickaxe", "Axe", "Shovel", "Bow", "Crossbow", "Block", "GoldenApple", "Pearl", "Arrow", "Throwable")
            .visibleWhen(() -> !mode.is("Visual"));
    private final ModeSetting offHand = mode("OffHand", "Block", "None", "Shield", "Totem", "Arrow", "Block", "GoldenApple", "WindCharge")
            .visibleWhen(() -> !mode.is("Visual"));

    private final Set<Integer> usefulSlots = new HashSet<>();
    private final Set<Integer> garbageSlots = new HashSet<>();
    private final Deque<InventoryAction> pendingActions = new ArrayDeque<>();

    private int queuedSyncId = -1;
    private long nextActionAt = 0L;

    public InventoryHelper() {
        super("InventoryHelper", Category.PLAYER, GLFW.GLFW_KEY_J, false);
    }

    @Override
    protected void onDisable() {
        clearState();
    }

    @Override
    public void onTick(TickEvent event) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.interactionManager == null) {
            clearState();
            return;
        }

        if (!(mc.currentScreen instanceof HandledScreen<?> screen)) {
            clearState();
            return;
        }

        if (pauseWhileMoving.getValue() && InvMove.shouldPauseInventoryActions()) {
            return;
        }

        if (mode.is("Auto")) {
            usefulSlots.clear();
            garbageSlots.clear();
        } else {
            updateHelpers(mc.player, screen.getScreenHandler().slots);
        }

        if (mode.is("Visual")) {
            pendingActions.clear();
            queuedSyncId = -1;
            nextActionAt = 0L;
            return;
        }

        if (screen.getScreenHandler().syncId != queuedSyncId) {
            pendingActions.clear();
            queuedSyncId = -1;
            nextActionAt = 0L;
        }

        if (pendingActions.isEmpty()) {
            if (stealer.getValue() && isChestScreen(screen)) {
                queueStealerActions(mc, screen);
            } else if (screen instanceof InventoryScreen inventoryScreen) {
                queueInventoryActions(mc, inventoryScreen);
            }
        }

        processPendingActions(mc, screen);
    }

    public int getSlotHighlightColor(Slot slot) {
        if (!isEnabled()) {
            return 0;
        }

        if (usefulSlots.contains(slot.id)) {
            return USEFUL_COLOR;
        }

        if (garbageSlots.contains(slot.id)) {
            return GARBAGE_COLOR;
        }

        return 0;
    }

    private void processPendingActions(MinecraftClient client, HandledScreen<?> screen) {
        if (pendingActions.isEmpty()) {
            return;
        }

        if (queuedSyncId != screen.getScreenHandler().syncId) {
            pendingActions.clear();
            queuedSyncId = -1;
            return;
        }

        long now = System.currentTimeMillis();
        if (now < nextActionAt) {
            return;
        }

        InventoryAction action = pendingActions.pollFirst();
        if (action == null) {
            return;
        }

        action.execute(client, screen);
        nextActionAt = now + randomDelay();

        if (pendingActions.isEmpty()) {
            queuedSyncId = -1;
        }
    }

    private void queueStealerActions(MinecraftClient client, HandledScreen<?> screen) {
        PlayerEntity player = client.player;
        if (player == null) {
            return;
        }

        Map<String, Double> bestScores = initBestScores(player);
        List<InventoryAction> actions = new ArrayList<>();

        for (Slot slot : screen.getScreenHandler().slots) {
            if (slot.inventory == player.getInventory()) {
                continue;
            }

            ItemStack stack = slot.getStack();
            if (stack.isEmpty()) {
                continue;
            }

            String category = getItemCategory(stack);
            if (!checkTools.getValue() && InventoryUtils.isMiningToolCategory(category)) {
                continue;
            }
            if (category.equals("food")) {
                continue;
            }

            boolean shouldTake;
            if (category.equals("other") || category.equals("block") || category.equals("valuable") || category.equals("potion")) {
                shouldTake = true;
            } else {
                double currentScore = calculateScore(player, stack);
                double playerBest = bestScores.getOrDefault(category, -1.0D);
                shouldTake = currentScore > playerBest + SCORE_EPSILON;
                if (shouldTake) {
                    bestScores.put(category, currentScore);
                }
            }

            if (shouldTake) {
                actions.add(InventoryAction.click(screen.getScreenHandler().syncId, slot.id, 0, SlotActionType.QUICK_MOVE));
            }
        }

        if (autoClose.getValue()) {
            actions.add(InventoryAction.closeScreen());
        }

        if (actions.isEmpty()) {
            return;
        }

        pendingActions.addAll(actions);
        queuedSyncId = screen.getScreenHandler().syncId;
        nextActionAt = 0L;
    }

    private void queueInventoryActions(MinecraftClient client, InventoryScreen screen) {
        PlayerEntity player = client.player;
        if (player == null) {
            return;
        }

        List<InventoryAction> actions = new ArrayList<>();
        actions.addAll(buildAutoArmorActions(player, screen));
        actions.addAll(buildCleanupActions(player));

        if (actions.isEmpty()) {
            return;
        }

        pendingActions.addAll(actions);
        queuedSyncId = screen.getScreenHandler().syncId;
        nextActionAt = 0L;
    }

    private List<InventoryAction> buildAutoArmorActions(PlayerEntity player, InventoryScreen screen) {
        Map<EquipmentSlot, Slot> bestArmorSlots = new LinkedHashMap<>();

        for (Slot slot : screen.getScreenHandler().slots) {
            ItemStack stack = slot.getStack();
            if (stack.isEmpty() || stack.get(DataComponentTypes.EQUIPPABLE) == null) {
                continue;
            }

            EquipmentSlot equipmentSlot = stack.get(DataComponentTypes.EQUIPPABLE).slot();
            if (!InventoryUtils.isArmorSlot(equipmentSlot)) {
                continue;
            }

            Slot currentBest = bestArmorSlots.get(equipmentSlot);
            if (currentBest == null || isBetterArmor(player, stack, currentBest.getStack())) {
                bestArmorSlots.put(equipmentSlot, slot);
            }
        }

        List<InventoryAction> actions = new ArrayList<>();
        int syncId = screen.getScreenHandler().syncId;
        for (Map.Entry<EquipmentSlot, Slot> entry : bestArmorSlots.entrySet()) {
            EquipmentSlot equipmentSlot = entry.getKey();
            Slot sourceSlot = entry.getValue();

            ItemStack currentArmor = player.getEquippedStack(equipmentSlot);
            ItemStack candidate = sourceSlot.getStack();
            if (!currentArmor.isEmpty() && !isBetterArmor(player, candidate, currentArmor)) {
                continue;
            }

            if (!currentArmor.isEmpty()) {
                int armorSlot = InventoryUtils.getArmorScreenSlot(equipmentSlot);
                if (armorSlot != -1) {
                    actions.add(InventoryAction.click(syncId, armorSlot, 1, SlotActionType.THROW));
                }
            }

            actions.add(InventoryAction.click(syncId, sourceSlot.id, 0, SlotActionType.QUICK_MOVE));
        }

        return actions;
    }

    private List<InventoryAction> buildCleanupActions(PlayerEntity player) {
        List<InventoryAction> actions = new ArrayList<>();
        Map<String, Integer> bestSlotPerCategory = new HashMap<>();
        Map<String, Double> bestScorePerCategory = new HashMap<>();
        Set<Integer> toDispose = new HashSet<>();
        Set<Integer> assigned = new HashSet<>();

        for (int index = 0; index < 36; index++) {
            ItemStack stack = player.getInventory().getStack(index);
            if (stack.isEmpty()) {
                continue;
            }

            String category = getItemCategory(stack);
            double score = calculateScore(player, stack);

            if (category.startsWith("tool_") || category.startsWith("weapon_") || category.startsWith("armor_")) {
                if (!checkTools.getValue() && InventoryUtils.isMiningToolCategory(category)) {
                    continue;
                }

                Double bestScore = bestScorePerCategory.get(category);
                if (bestScore == null || score > bestScore + SCORE_EPSILON) {
                    Integer previous = bestSlotPerCategory.put(category, index);
                    bestScorePerCategory.put(category, score);
                    if (previous != null) {
                        toDispose.add(previous);
                    }
                } else {
                    toDispose.add(index);
                }
            } else if (category.equals("other") && !InventoryUtils.isCleanerWhitelisted(stack.getItem())) {
                toDispose.add(index);
            }
        }

        ModeSetting[] slotSettings = {
                slotItem1, slotItem2, slotItem3, slotItem4, slotItem5,
                slotItem6, slotItem7, slotItem8, slotItem9
        };

        for (int hotbar = 0; hotbar < 9; hotbar++) {
            String preference = slotSettings[hotbar].getValue();
            if ("None".equals(preference)) {
                continue;
            }

            String targetCategory = InventoryUtils.preferenceToCategory(preference);
            if (targetCategory == null) {
                continue;
            }

            ItemStack currentHotbarStack = player.getInventory().getStack(hotbar);
            int bestIndex = -1;
            double bestScore = -1.0D;
            for (int index = 0; index < 36; index++) {
                if (assigned.contains(index)) {
                    continue;
                }

                ItemStack stack = player.getInventory().getStack(index);
                if (stack.isEmpty() || !targetCategory.equals(getItemCategory(stack))) {
                    continue;
                }

                double score = calculateScore(player, stack);
                if (score > bestScore + SCORE_EPSILON) {
                    bestScore = score;
                    bestIndex = index;
                }
            }

            if (!currentHotbarStack.isEmpty() && targetCategory.equals(getItemCategory(currentHotbarStack))) {
                double currentScore = calculateScore(player, currentHotbarStack);
                if (bestIndex == -1 || currentScore >= bestScore - SCORE_EPSILON) {
                    assigned.add(hotbar);
                    continue;
                }
            }

            if (bestIndex == -1 || bestIndex == hotbar) {
                if (bestIndex == hotbar) {
                    assigned.add(hotbar);
                }
                continue;
            }

            assigned.add(bestIndex);
            actions.add(InventoryAction.click(0, InventoryUtils.toScreenSlot(bestIndex), hotbar, SlotActionType.SWAP));
        }

        String offHandPreference = offHand.getValue();
        if (!"None".equals(offHandPreference) && !InventoryUtils.matchesOffHandPreference(player.getOffHandStack(), offHandPreference)) {
            for (int index = 0; index < 36; index++) {
                if (assigned.contains(index)) {
                    continue;
                }

                ItemStack stack = player.getInventory().getStack(index);
                if (stack.isEmpty() || !InventoryUtils.matchesOffHandPreference(stack, offHandPreference)) {
                    continue;
                }

                assigned.add(index);
                actions.add(InventoryAction.click(0, InventoryUtils.toScreenSlot(index), 40, SlotActionType.SWAP));
                break;
            }
        }

        for (int index : toDispose) {
            if (assigned.contains(index)) {
                continue;
            }

            actions.add(InventoryAction.click(0, InventoryUtils.toScreenSlot(index), 1, SlotActionType.THROW));
        }

        return actions;
    }

    private void updateHelpers(PlayerEntity player, List<Slot> slots) {
        usefulSlots.clear();
        garbageSlots.clear();

        if (player.isCreative() || player.isSpectator()) {
            return;
        }

        for (Slot slot : slots) {
            ItemStack stack = slot.getStack();
            if (stack.isEmpty()) {
                continue;
            }

            String category = getItemCategory(stack);
            if (!checkTools.getValue() && InventoryUtils.isMiningToolCategory(category)) {
                continue;
            }

            ItemLevel level = getItemLevel(player, stack, slot);
            if (level == ItemLevel.BETTER) {
                usefulSlots.add(slot.id);
            } else if (level == ItemLevel.GARBAGE) {
                garbageSlots.add(slot.id);
            }
        }
    }

    private ItemLevel getItemLevel(PlayerEntity player, ItemStack stack, Slot slot) {
        String category = getItemCategory(stack);

        if (category.equals("other") && !InventoryUtils.isCleanerWhitelisted(stack.getItem())) {
            return ItemLevel.GARBAGE;
        }

        if (!category.startsWith("tool_") && !category.startsWith("weapon_") && !category.startsWith("armor_")) {
            return ItemLevel.NORMAL;
        }

        double currentScore = calculateScore(player, stack);
        double playerMax = InventoryUtils.getPlayerMaxScore(player, category);

        if (slot.inventory == player.getInventory()) {
            int bestSlot = InventoryUtils.getBestPlayerInventorySlot(player, category);
            if (slot.getIndex() == bestSlot && currentScore > 0.0D && Math.abs(currentScore - playerMax) < SCORE_EPSILON) {
                return ItemLevel.BETTER;
            }
            return ItemLevel.GARBAGE;
        }

        return currentScore > playerMax + SCORE_EPSILON ? ItemLevel.BETTER : ItemLevel.GARBAGE;
    }

    private Map<String, Double> initBestScores(PlayerEntity player) {
        Map<String, Double> scores = new HashMap<>();

        for (ItemStack stack : player.getInventory().main) {
            InventoryUtils.addBestScore(player, scores, stack);
        }
        for (ItemStack stack : player.getInventory().armor) {
            InventoryUtils.addBestScore(player, scores, stack);
        }
        InventoryUtils.addBestScore(player, scores, player.getOffHandStack());

        return scores;
    }

    private double calculateScore(PlayerEntity player, ItemStack stack) {
        return InventoryUtils.calculateScore(player, stack);
    }

    private String getItemCategory(ItemStack stack) {
        return InventoryUtils.getHelperItemCategory(stack);
    }

    private boolean isChestScreen(HandledScreen<?> screen) {
        if (screen instanceof InventoryScreen || screen instanceof CreativeInventoryScreen) {
            return false;
        }

        ScreenHandlerType<?> type = screen.getScreenHandler().getType();
        if (type != ScreenHandlerType.GENERIC_9X1
                && type != ScreenHandlerType.GENERIC_9X2
                && type != ScreenHandlerType.GENERIC_9X3
                && type != ScreenHandlerType.GENERIC_9X4
                && type != ScreenHandlerType.GENERIC_9X5
                && type != ScreenHandlerType.GENERIC_9X6) {
            return false;
        }

        String title = screen.getTitle().getString().toLowerCase(Locale.ROOT);
        return title.contains("chest")
                || title.contains("\u7bb1")
                || title.contains("\u50a8\u7269")
                || title.contains("\u6536\u7eb3");
    }

    private boolean isBetterArmor(PlayerEntity player, ItemStack candidate, ItemStack current) {
        return InventoryUtils.calculateArmorScore(player, candidate)
                > InventoryUtils.calculateArmorScore(player, current) + SCORE_EPSILON;
    }

    private long randomDelay() {
        double min = Math.min(minDelay.getValue(), maxDelay.getValue());
        double max = Math.max(minDelay.getValue(), maxDelay.getValue());
        if (max <= min) {
            return Math.round(min);
        }

        return Math.round(ThreadLocalRandom.current().nextDouble(min, max));
    }

    private void clearState() {
        usefulSlots.clear();
        garbageSlots.clear();
        pendingActions.clear();
        queuedSyncId = -1;
        nextActionAt = 0L;
    }

    private enum ItemLevel {
        BETTER,
        NORMAL,
        GARBAGE
    }

    private record InventoryAction(ActionType type, int syncId, int slotId, int button, SlotActionType slotActionType) {
        private static InventoryAction click(int syncId, int slotId, int button, SlotActionType actionType) {
            return new InventoryAction(ActionType.CLICK, syncId, slotId, button, actionType);
        }

        private static InventoryAction closeScreen() {
            return new InventoryAction(ActionType.CLOSE_SCREEN, -1, -1, 0, null);
        }

        private void execute(MinecraftClient client, HandledScreen<?> screen) {
            if (type == ActionType.CLOSE_SCREEN) {
                if (client.player != null) {
                    client.player.closeHandledScreen();
                }
                return;
            }

            if (client.interactionManager == null || client.player == null) {
                return;
            }

            int actualSyncId = syncId >= 0 ? syncId : screen.getScreenHandler().syncId;
            client.interactionManager.clickSlot(actualSyncId, slotId, button, slotActionType, client.player);
        }
    }

    private enum ActionType {
        CLICK,
        CLOSE_SCREEN
    }
}











