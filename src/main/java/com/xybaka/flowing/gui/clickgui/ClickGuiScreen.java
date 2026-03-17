package com.xybaka.flowing.gui.clickgui;

import com.xybaka.flowing.gui.component.HudComponent;
import com.xybaka.flowing.modules.Category;
import com.xybaka.flowing.modules.Module;
import com.xybaka.flowing.modules.settings.BooleanSetting;
import com.xybaka.flowing.modules.settings.ColorSetting;
import com.xybaka.flowing.modules.settings.ModeSetting;
import com.xybaka.flowing.modules.settings.NumberSetting;
import com.xybaka.flowing.modules.settings.Setting;
import com.xybaka.flowing.util.ColorUtil;
import com.xybaka.flowing.util.MoveUtil;
import com.xybaka.flowing.util.WindowUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.Map;

public final class ClickGuiScreen extends Screen {
    private static final int PANEL_WIDTH = 410;
    private static final int PANEL_HEIGHT = 250;
    private static final int HEADER_HEIGHT = 22;
    private static final int CATEGORY_WIDTH = 96;
    private static final int MODULE_ROW_HEIGHT = 20;
    private static final int SETTING_ROW_HEIGHT = 14;
    private static final int SETTING_INDENT = 16;
    private static final int SETTING_LEVEL_WIDTH = 14;
    private static final int MODE_INDENT = 30;
    private static final int CONTENT_PADDING = 6;
    private static final int CONTENT_BOTTOM_PADDING = 12;
    private static final int SCROLL_STEP = 18;
    private static final int SCROLLBAR_WIDTH = 3;
    private static final int SCROLLBAR_MARGIN = 6;
    private static final int SCROLLBAR_TRACK_COLOR = ColorUtil.rgba(255, 255, 255, 26);
    private static final int SCROLLBAR_THUMB_COLOR = ColorUtil.rgba(168, 208, 255, 210);
    private static final int TEXT_COLOR = ColorUtil.rgb(255, 255, 255);
    private static final int MUTED_TEXT_COLOR = ColorUtil.rgb(211, 216, 224);
    private static final int ENABLED_TEXT_COLOR = ColorUtil.rgb(200, 255, 176);
    private static final int BOOLEAN_ON_COLOR = ColorUtil.rgb(115, 225, 140);
    private static final int BOOLEAN_OFF_COLOR = ColorUtil.rgb(255, 108, 108);
    private static final int PANEL_COLOR = ColorUtil.rgb(12, 17, 23);
    private static final int HEADER_COLOR = ColorUtil.rgb(28, 40, 55);
    private static final int CATEGORY_COLOR = ColorUtil.rgb(18, 25, 35);
    private static final int MODULE_COLOR = ColorUtil.rgb(29, 39, 51);
    private static final int ENABLED_MODULE_COLOR = ColorUtil.rgb(41, 74, 37);
    private static final int SETTING_COLOR = ColorUtil.rgb(20, 27, 37);
    private static final int SETTING_STRIPE_COLOR = ColorUtil.rgb(76, 116, 166);
    private static final int SETTING_SECTION_COLOR = ColorUtil.rgba(9, 13, 18, 110);
    private static final int MODE_OPTION_COLOR = ColorUtil.rgb(26, 38, 53);
    private static final int MODE_OPTION_STRIPE_COLOR = ColorUtil.rgb(118, 156, 209);
    private static final int SETTING_ACCENT_COLOR = ColorUtil.rgb(43, 64, 90);
    private static final int SETTING_FILL_COLOR = ColorUtil.rgb(131, 182, 255);
    private static final int SELECTED_CATEGORY_COLOR = ColorUtil.rgb(49, 70, 95);
    private static final int BORDER_COLOR = ColorUtil.rgb(81, 101, 125);

    private static final HudComponent POSITION = new HudComponent(
            "clickgui",
            WindowUtil.getCenteredX(PANEL_WIDTH),
            WindowUtil.getCenteredY(PANEL_HEIGHT)
    );

    private final ClickGuiManager manager = ClickGuiManager.getInstance();
    private final Map<KeyBinding, Integer> movementKeys;
    private int panelX;
    private int panelY;
    private boolean dragging;
    private int dragOffsetX;
    private int dragOffsetY;
    private int contentScroll;

    public ClickGuiScreen() {
        super(Text.literal("Flowing ClickGUI"));
        POSITION.setSize(PANEL_WIDTH, PANEL_HEIGHT);
        this.panelX = POSITION.getRenderX();
        this.panelY = POSITION.getRenderY();
        this.movementKeys = MoveUtil.createMovementKeyMap(MinecraftClient.getInstance());
    }

    @Override
    protected void init() {
        POSITION.setSize(PANEL_WIDTH, PANEL_HEIGHT);
        panelX = POSITION.getRenderX();
        panelY = POSITION.getRenderY();
        clampContentScroll();
    }

    @Override
    public void tick() {
        MinecraftClient client = MinecraftClient.getInstance();
        MoveUtil.updateMovementKeys(client, movementKeys, false);
    }

    @Override
    protected void applyBlur() {
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void close() {
        dragging = false;
        manager.stopSliding();
        manager.closeModeList();
        MoveUtil.restoreMovementKeys(MinecraftClient.getInstance(), movementKeys);
        MinecraftClient.getInstance().setScreen(null);
    }

    @Override
    public void removed() {
        MoveUtil.restoreMovementKeys(MinecraftClient.getInstance(), movementKeys);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        manager.syncVisibleState();
        POSITION.setSize(PANEL_WIDTH, PANEL_HEIGHT);
        panelX = POSITION.getRenderX();
        panelY = POSITION.getRenderY();
        clampContentScroll();

        int panelRight = panelX + PANEL_WIDTH;
        int panelBottom = panelY + PANEL_HEIGHT;
        int contentX = panelX + CATEGORY_WIDTH + 12;

        context.fill(panelX, panelY, panelRight, panelBottom, PANEL_COLOR);
        context.drawBorder(panelX, panelY, PANEL_WIDTH, PANEL_HEIGHT, BORDER_COLOR);
        context.fill(panelX, panelY, panelRight, panelY + HEADER_HEIGHT, HEADER_COLOR);
        context.fill(panelX, panelY + HEADER_HEIGHT, panelX + CATEGORY_WIDTH, panelBottom, CATEGORY_COLOR);
        context.fill(panelX + CATEGORY_WIDTH, panelY + HEADER_HEIGHT, panelX + CATEGORY_WIDTH + 1, panelBottom, BORDER_COLOR);

        context.drawText(textRenderer, title, panelX + 10, panelY + 7, TEXT_COLOR, true);

        Text hint = manager.getBindingModule() == null
                ? Text.literal("LMB toggle | RMB settings | Wheel scroll | Drag header")
                : Text.literal("Binding " + manager.getBindingModule().getName() + " - press a key, ESC clears");
        context.drawText(textRenderer, hint, panelX + 126, panelY + 7, MUTED_TEXT_COLOR, false);

        int categoryY = getContentTop();
        for (Category category : manager.getCategories()) {
            int top = categoryY - 4;
            int bottom = categoryY + 12;
            if (category == manager.getSelectedCategory()) {
                context.fill(panelX + 5, top, panelX + CATEGORY_WIDTH - 5, bottom, SELECTED_CATEGORY_COLOR);
            }

            int color = category == manager.getSelectedCategory() ? TEXT_COLOR : MUTED_TEXT_COLOR;
            context.drawText(textRenderer, manager.getCategoryDisplayName(category), panelX + 10, categoryY, color, true);
            categoryY += 16;
        }

        context.enableScissor(contentX - 6, getContentTop() - 4, panelRight - 12, getContentBottom());
        int moduleY = getContentTop() - contentScroll;
        for (Module module : manager.getVisibleModules()) {
            int rowTop = moduleY - 4;
            int rowBottom = moduleY + MODULE_ROW_HEIGHT - 4;
            int rowColor = manager.isEnabled(module) ? ENABLED_MODULE_COLOR : MODULE_COLOR;
            context.fill(contentX - 6, rowTop, panelRight - 12, rowBottom, rowColor);
            context.drawBorder(contentX - 6, rowTop, panelRight - contentX - 6, MODULE_ROW_HEIGHT, BORDER_COLOR);

            int nameColor = manager.isEnabled(module) ? ENABLED_TEXT_COLOR : TEXT_COLOR;
            context.drawText(textRenderer, module.getName(), contentX, moduleY, nameColor, true);

            int expandX = panelRight - 24;
            String expandState = manager.hasSettings(module) ? (manager.isExpanded(module) ? "-" : "+") : " ";
            context.drawText(textRenderer, expandState, expandX, moduleY, MUTED_TEXT_COLOR, true);

            String bind = manager.isBinding(module) ? "[...]" : manager.getKeyName(module);
            if (!bind.isEmpty()) {
                int bindWidth = textRenderer.getWidth(bind);
                context.drawText(textRenderer, bind, expandX - 8 - bindWidth, moduleY, MUTED_TEXT_COLOR, true);
            }

            moduleY += MODULE_ROW_HEIGHT;
            if (manager.isExpanded(module)) {
                for (Setting setting : manager.getVisibleSettings(module)) {
                    moduleY += renderSettingBlock(context, setting, contentX, panelRight, moduleY);
                }
            }
        }
        context.disableScissor();
        renderScrollbar(context, panelRight);

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        manager.syncVisibleState();

        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT && isHoveringHeader(mouseX, mouseY)) {
            dragging = true;
            dragOffsetX = (int) mouseX - panelX;
            dragOffsetY = (int) mouseY - panelY;
            return true;
        }

        int categoryY = getContentTop();
        for (Category category : manager.getCategories()) {
            int top = categoryY - 4;
            int bottom = categoryY + 12;
            if (mouseX >= panelX + 5 && mouseX <= panelX + CATEGORY_WIDTH - 5 && mouseY >= top && mouseY <= bottom) {
                manager.selectCategory(category);
                contentScroll = 0;
                return true;
            }
            categoryY += 16;
        }

        int panelRight = panelX + PANEL_WIDTH;
        int contentX = panelX + CATEGORY_WIDTH + 12;
        int moduleY = getContentTop() - contentScroll;

        if (!isHoveringContent(mouseX, mouseY)) {
            manager.stopSliding();
            manager.closeModeList();
            return super.mouseClicked(mouseX, mouseY, button);
        }

        for (Module module : manager.getVisibleModules()) {
            int rowTop = moduleY - 4;
            int rowBottom = moduleY + MODULE_ROW_HEIGHT - 4;
            boolean hoveredRow = mouseX >= contentX - 6 && mouseX <= panelRight - 12 && mouseY >= rowTop && mouseY <= rowBottom;
            if (hoveredRow) {
                if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                    if (isLeftShiftDown()) {
                        manager.beginBinding(module);
                    } else {
                        manager.clearBindingTarget();
                        manager.stopSliding();
                        manager.closeModeList();
                        manager.toggle(module);
                    }
                    return true;
                }

                if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
                    manager.clearBindingTarget();
                    manager.stopSliding();
                    manager.closeModeList();
                    manager.toggleExpanded(module);
                    clampContentScroll();
                    return true;
                }
            }

            moduleY += MODULE_ROW_HEIGHT;
            if (manager.isExpanded(module)) {
                for (Setting setting : manager.getVisibleSettings(module)) {
                    int consumedHeight = handleSettingClick(mouseX, mouseY, button, setting, contentX, panelRight, moduleY);
                    if (consumedHeight >= 0) {
                        return true;
                    }
                    moduleY += getSettingBlockHeight(setting);
                }
            }
        }

        manager.stopSliding();
        manager.closeModeList();
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        manager.syncVisibleState();

        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT && dragging) {
            int targetX = clampPanelX((int) mouseX - dragOffsetX);
            int targetY = clampPanelY((int) mouseY - dragOffsetY);
            int bottomOffset = height - PANEL_HEIGHT - targetY;
            POSITION.setPosition(targetX, bottomOffset);
            panelX = POSITION.getRenderX();
            panelY = POSITION.getRenderY();
            return true;
        }

        NumberSetting slidingNumberSetting = manager.getSlidingNumberSetting();
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT && slidingNumberSetting != null) {
            int contentX = panelX + CATEGORY_WIDTH + 12;
            int panelRight = panelX + PANEL_WIDTH;
            updateNumberSettingFromMouse(slidingNumberSetting, mouseX, contentX, panelRight);
            return true;
        }

        ColorSetting slidingColorSetting = manager.getSlidingColorSetting();
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT && slidingColorSetting != null) {
            int contentX = panelX + CATEGORY_WIDTH + 12;
            int panelRight = panelX + PANEL_WIDTH;
            updateColorSettingFromMouse(slidingColorSetting, mouseX, contentX, panelRight);
            return true;
        }

        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            dragging = false;
            manager.stopSliding();
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (!isHoveringContent(mouseX, mouseY)) {
            return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
        }

        if (verticalAmount == 0.0D) {
            return true;
        }

        contentScroll -= (int) Math.round(verticalAmount * SCROLL_STEP);
        clampContentScroll();
        manager.stopSliding();
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (manager.getBindingModule() != null) {
            manager.applyBinding(keyCode);
            return true;
        }

        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            close();
            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private int renderSettingBlock(DrawContext context, Setting setting, int contentX, int panelRight, int y) {
        int indent = SETTING_INDENT + setting.getIndentLevel() * SETTING_LEVEL_WIDTH;
        int blockHeight = getSettingBlockHeight(setting);
        int blockTop = y - 4;
        int blockBottom = y + blockHeight - 4;
        int settingLeft = contentX + indent;
        int sectionLeft = contentX + Math.max(4, indent - 12);
        int settingTop = y - 3;
        int settingBottom = y + SETTING_ROW_HEIGHT - 3;
        int settingWidth = panelRight - settingLeft - 16;

        context.fill(sectionLeft, blockTop, panelRight - 16, blockBottom, SETTING_SECTION_COLOR);
        context.fill(sectionLeft + 4, blockTop + 1, sectionLeft + 6, blockBottom - 1, SETTING_STRIPE_COLOR);
        context.fill(settingLeft, settingTop, panelRight - 16, settingBottom, SETTING_COLOR);
        context.drawBorder(settingLeft, settingTop, settingWidth, SETTING_ROW_HEIGHT, BORDER_COLOR);

        if (setting instanceof NumberSetting numberSetting) {
            renderSlider(context, settingTop, settingBottom, settingLeft, panelRight - 18, manager.getNumberPercent(numberSetting), SETTING_FILL_COLOR);
        } else if (setting instanceof ColorSetting colorSetting) {
            renderSlider(context, settingTop, settingBottom, settingLeft, panelRight - 18, manager.getColorPercent(colorSetting), colorSetting.getPreviewColor());
        }

        drawSettingLabel(context, setting, settingLeft + 6, y);

        if (setting instanceof ColorSetting colorSetting) {
            int swatchSize = SETTING_ROW_HEIGHT - 4;
            int swatchRight = panelRight - 20;
            int swatchLeft = swatchRight - swatchSize;
            context.fill(swatchLeft, settingTop + 2, swatchRight, settingBottom - 2, colorSetting.getColor());
            context.drawBorder(swatchLeft, settingTop + 2, swatchSize, swatchSize, BORDER_COLOR);
        }

        if (setting instanceof ModeSetting modeSetting && manager.isModeListOpen(modeSetting)) {
            int optionY = y + SETTING_ROW_HEIGHT;
            for (String mode : modeSetting.getModes()) {
                int optionLeft = contentX + MODE_INDENT + setting.getIndentLevel() * SETTING_LEVEL_WIDTH;
                int optionTop = optionY - 3;
                int optionBottom = optionY + SETTING_ROW_HEIGHT - 3;
                int optionWidth = panelRight - optionLeft - 28;
                int optionColor = mode.equals(modeSetting.getValue()) ? SELECTED_CATEGORY_COLOR : MODE_OPTION_COLOR;
                context.fill(optionLeft, optionTop, panelRight - 28, optionBottom, optionColor);
                context.fill(optionLeft + 2, optionTop + 1, optionLeft + 4, optionBottom - 1, MODE_OPTION_STRIPE_COLOR);
                context.drawBorder(optionLeft, optionTop, optionWidth, SETTING_ROW_HEIGHT, BORDER_COLOR);
                context.drawText(textRenderer, Text.literal("* " + mode), optionLeft + 8, optionY, TEXT_COLOR, false);
                optionY += SETTING_ROW_HEIGHT;
            }
        }

        return blockHeight;
    }

    private void renderSlider(DrawContext context, int settingTop, int settingBottom, int settingLeft, int sliderRight, double percent, int fillColor) {
        int sliderLeft = settingLeft + 2;
        int sliderWidth = sliderRight - sliderLeft;
        int fillWidth = (int) Math.round(sliderWidth * percent);
        context.fill(sliderLeft, settingTop + 1, sliderRight, settingBottom - 1, SETTING_ACCENT_COLOR);
        context.fill(sliderLeft, settingTop + 1, sliderLeft + fillWidth, settingBottom - 1, fillColor);
    }

    private void drawSettingLabel(DrawContext context, Setting setting, int x, int y) {
        String prefix = setting.getIndentLevel() > 0 ? "-> " : "- ";
        if (setting instanceof BooleanSetting booleanSetting) {
            String base = prefix + setting.getName() + ": ";
            String state = booleanSetting.getValue() ? "ON" : "OFF";
            int stateColor = booleanSetting.getValue() ? BOOLEAN_ON_COLOR : BOOLEAN_OFF_COLOR;
            context.drawText(textRenderer, Text.literal(base), x, y, TEXT_COLOR, false);
            context.drawText(textRenderer, Text.literal(state), x + textRenderer.getWidth(base), y, stateColor, false);
            return;
        }

        context.drawText(textRenderer, Text.literal(prefix + manager.getSettingLabel(setting)), x, y, TEXT_COLOR, false);
    }

    private int handleSettingClick(double mouseX, double mouseY, int button, Setting setting, int contentX, int panelRight, int y) {
        int indent = SETTING_INDENT + setting.getIndentLevel() * SETTING_LEVEL_WIDTH;
        int settingLeft = contentX + indent;
        int settingTop = y - 3;
        int settingBottom = y + SETTING_ROW_HEIGHT - 3;
        boolean hoveredSetting = mouseX >= settingLeft && mouseX <= panelRight - 16 && mouseY >= settingTop && mouseY <= settingBottom;
        if (hoveredSetting) {
            manager.clearBindingTarget();
            if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT && manager.handleSettingLeftClick(setting)) {
                clampContentScroll();
                if (setting instanceof NumberSetting numberSetting) {
                    updateNumberSettingFromMouse(numberSetting, mouseX, contentX, panelRight, setting);
                } else if (setting instanceof ColorSetting colorSetting) {
                    updateColorSettingFromMouse(colorSetting, mouseX, contentX, panelRight, setting);
                }
                return getSettingBlockHeight(setting);
            }

            if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT && manager.handleSettingRightClick(setting)) {
                clampContentScroll();
                return getSettingBlockHeight(setting);
            }
        }

        if (setting instanceof ModeSetting modeSetting && manager.isModeListOpen(modeSetting)) {
            int optionY = y + SETTING_ROW_HEIGHT;
            for (String mode : modeSetting.getModes()) {
                int optionLeft = contentX + MODE_INDENT + setting.getIndentLevel() * SETTING_LEVEL_WIDTH;
                int optionTop = optionY - 3;
                int optionBottom = optionY + SETTING_ROW_HEIGHT - 3;
                boolean hoveredOption = mouseX >= optionLeft && mouseX <= panelRight - 28 && mouseY >= optionTop && mouseY <= optionBottom;
                if (hoveredOption && button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                    manager.chooseMode(modeSetting, mode);
                    return getSettingBlockHeight(setting);
                }
                optionY += SETTING_ROW_HEIGHT;
            }
        }

        return -1;
    }

    private int getSettingBlockHeight(Setting setting) {
        if (setting instanceof ModeSetting modeSetting && manager.isModeListOpen(modeSetting)) {
            return SETTING_ROW_HEIGHT * (1 + modeSetting.getModes().size());
        }
        return SETTING_ROW_HEIGHT;
    }

    private void updateNumberSettingFromMouse(NumberSetting setting, double mouseX, int contentX, int panelRight, Setting baseSetting) {
        double sliderLeft = contentX + SETTING_INDENT + baseSetting.getIndentLevel() * SETTING_LEVEL_WIDTH + 2;
        double sliderRight = panelRight - 18;
        double percent = (mouseX - sliderLeft) / (sliderRight - sliderLeft);
        manager.updateSliding(setting, percent);
    }

    private void updateNumberSettingFromMouse(NumberSetting setting, double mouseX, int contentX, int panelRight) {
        updateNumberSettingFromMouse(setting, mouseX, contentX, panelRight, setting);
    }

    private void updateColorSettingFromMouse(ColorSetting setting, double mouseX, int contentX, int panelRight, Setting baseSetting) {
        double sliderLeft = contentX + SETTING_INDENT + baseSetting.getIndentLevel() * SETTING_LEVEL_WIDTH + 2;
        double sliderRight = panelRight - 18;
        double percent = (mouseX - sliderLeft) / (sliderRight - sliderLeft);
        manager.updateSliding(setting, percent);
    }

    private void updateColorSettingFromMouse(ColorSetting setting, double mouseX, int contentX, int panelRight) {
        updateColorSettingFromMouse(setting, mouseX, contentX, panelRight, setting);
    }

    private boolean isLeftShiftDown() {
        MinecraftClient client = MinecraftClient.getInstance();
        return InputUtil.isKeyPressed(client.getWindow().getHandle(), GLFW.GLFW_KEY_LEFT_SHIFT);
    }

    private boolean isHoveringHeader(double mouseX, double mouseY) {
        return mouseX >= panelX && mouseX <= panelX + PANEL_WIDTH && mouseY >= panelY && mouseY <= panelY + HEADER_HEIGHT;
    }

    private boolean isHoveringContent(double mouseX, double mouseY) {
        return mouseX >= panelX + CATEGORY_WIDTH
                && mouseX <= panelX + PANEL_WIDTH
                && mouseY >= getContentTop() - 4
                && mouseY <= getContentBottom();
    }

    private int getContentTop() {
        return panelY + HEADER_HEIGHT + CONTENT_PADDING;
    }

    private int getContentBottom() {
        return panelY + PANEL_HEIGHT - CONTENT_BOTTOM_PADDING;
    }

    private int getVisibleContentHeight() {
        return getContentBottom() - getContentTop();
    }

    private void clampContentScroll() {
        contentScroll = Math.max(0, Math.min(contentScroll, getMaxContentScroll()));
    }

    private int getMaxContentScroll() {
        return Math.max(0, getTotalContentHeight() - getVisibleContentHeight());
    }

    private void renderScrollbar(DrawContext context, int panelRight) {
        int maxScroll = getMaxContentScroll();
        if (maxScroll <= 0) {
            return;
        }

        int trackLeft = panelRight - SCROLLBAR_MARGIN - SCROLLBAR_WIDTH;
        int trackTop = getContentTop() - 2;
        int trackBottom = getContentBottom() - 2;
        int trackHeight = trackBottom - trackTop;
        if (trackHeight <= 0) {
            return;
        }

        int thumbHeight = Math.max(24, (int) Math.round((double) getVisibleContentHeight() / getTotalContentHeight() * trackHeight));
        int thumbTravel = Math.max(0, trackHeight - thumbHeight);
        int thumbTop = trackTop + (int) Math.round((double) contentScroll / maxScroll * thumbTravel);

        context.fill(trackLeft, trackTop, trackLeft + SCROLLBAR_WIDTH, trackBottom, SCROLLBAR_TRACK_COLOR);
        context.fill(trackLeft, thumbTop, trackLeft + SCROLLBAR_WIDTH, thumbTop + thumbHeight, SCROLLBAR_THUMB_COLOR);
    }

    private int getTotalContentHeight() {
        int height = 0;
        for (Module module : manager.getVisibleModules()) {
            height += MODULE_ROW_HEIGHT;
            if (manager.isExpanded(module)) {
                for (Setting setting : manager.getVisibleSettings(module)) {
                    height += getSettingBlockHeight(setting);
                }
            }
        }
        return height;
    }

    private int clampPanelX(int x) {
        return Math.max(0, Math.min(x, width - PANEL_WIDTH));
    }

    private int clampPanelY(int y) {
        return Math.max(0, Math.min(y, height - PANEL_HEIGHT));
    }
}

