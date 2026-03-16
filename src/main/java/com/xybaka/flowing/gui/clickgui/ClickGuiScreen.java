package com.xybaka.flowing.gui.clickgui;

import com.xybaka.flowing.modules.Category;
import com.xybaka.flowing.modules.Module;
import com.xybaka.flowing.modules.settings.ModeSetting;
import com.xybaka.flowing.modules.settings.NumberSetting;
import com.xybaka.flowing.modules.settings.Setting;
import com.xybaka.flowing.util.ColorUtil;
import com.xybaka.flowing.util.WindowUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public final class ClickGuiScreen extends Screen {
    private static final int PANEL_WIDTH = 410;
    private static final int PANEL_HEIGHT = 250;
    private static final int HEADER_HEIGHT = 22;
    private static final int CATEGORY_WIDTH = 96;
    private static final int MODULE_ROW_HEIGHT = 20;
    private static final int SETTING_ROW_HEIGHT = 14;
    private static final int TEXT_COLOR = ColorUtil.rgb(255, 255, 255);
    private static final int MUTED_TEXT_COLOR = ColorUtil.rgb(211, 216, 224);
    private static final int ENABLED_TEXT_COLOR = ColorUtil.rgb(200, 255, 176);
    private static final int PANEL_COLOR = ColorUtil.rgb(12, 17, 23);
    private static final int HEADER_COLOR = ColorUtil.rgb(28, 40, 55);
    private static final int CATEGORY_COLOR = ColorUtil.rgb(18, 25, 35);
    private static final int MODULE_COLOR = ColorUtil.rgb(29, 39, 51);
    private static final int ENABLED_MODULE_COLOR = ColorUtil.rgb(41, 74, 37);
    private static final int SETTING_COLOR = ColorUtil.rgb(22, 30, 40);
    private static final int MODE_OPTION_COLOR = ColorUtil.rgb(34, 48, 65);
    private static final int SETTING_ACCENT_COLOR = ColorUtil.rgb(43, 64, 90);
    private static final int SETTING_FILL_COLOR = ColorUtil.rgb(131, 182, 255);
    private static final int SELECTED_CATEGORY_COLOR = ColorUtil.rgb(49, 70, 95);
    private static final int BORDER_COLOR = ColorUtil.rgb(81, 101, 125);

    private final ClickGuiManager manager = ClickGuiManager.getInstance();
    private int panelX;
    private int panelY;
    private boolean dragging;
    private int dragOffsetX;
    private int dragOffsetY;

    public ClickGuiScreen() {
        super(Text.literal("Flowing ClickGUI"));
        this.panelX = WindowUtil.getCenteredX(PANEL_WIDTH);
        this.panelY = WindowUtil.getCenteredY(PANEL_HEIGHT);
    }

    @Override
    protected void init() {
        panelX = WindowUtil.getCenteredX(PANEL_WIDTH);
        panelY = WindowUtil.getCenteredY(PANEL_HEIGHT);
    }

    @Override
    protected void applyBlur() {
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        manager.syncVisibleState();

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
                ? Text.literal("LMB toggle | RMB settings | Drag header")
                : Text.literal("Binding " + manager.getBindingModule().getName() + " - press a key, ESC clears");
        context.drawText(textRenderer, hint, panelX + 126, panelY + 7, MUTED_TEXT_COLOR, false);

        int categoryY = panelY + HEADER_HEIGHT + 10;
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

        int moduleY = panelY + HEADER_HEIGHT + 10;
        for (Module module : manager.getVisibleModules()) {
            int rowTop = moduleY - 4;
            int rowBottom = moduleY + MODULE_ROW_HEIGHT - 4;
            int rowColor = manager.isEnabled(module) ? ENABLED_MODULE_COLOR : MODULE_COLOR;
            context.fill(contentX - 6, rowTop, panelRight - 12, rowBottom, rowColor);
            context.drawBorder(contentX - 6, rowTop, panelRight - contentX - 6, MODULE_ROW_HEIGHT, BORDER_COLOR);

            int nameColor = manager.isEnabled(module) ? ENABLED_TEXT_COLOR : TEXT_COLOR;
            context.drawText(textRenderer, module.getName(), contentX, moduleY, nameColor, true);

            if (manager.hasSettings(module)) {
                String expandState = manager.isExpanded(module) ? "-" : "+";
                context.drawText(textRenderer, expandState, panelRight - 54, moduleY, MUTED_TEXT_COLOR, true);
            }

            String bind = manager.isBinding(module) ? "..." : manager.getKeyName(module);
            int bindWidth = textRenderer.getWidth(bind);
            context.drawText(textRenderer, bind, panelRight - 18 - bindWidth, moduleY, MUTED_TEXT_COLOR, true);

            moduleY += MODULE_ROW_HEIGHT;
            if (manager.isExpanded(module)) {
                for (Setting setting : manager.getVisibleSettings(module)) {
                    moduleY += renderSettingBlock(context, setting, contentX, panelRight, moduleY);
                    if (moduleY > panelBottom - 18) {
                        break;
                    }
                }
            }

            if (moduleY > panelBottom - 18) {
                break;
            }
        }

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

        int categoryY = panelY + HEADER_HEIGHT + 10;
        for (Category category : manager.getCategories()) {
            int top = categoryY - 4;
            int bottom = categoryY + 12;
            if (mouseX >= panelX + 5 && mouseX <= panelX + CATEGORY_WIDTH - 5 && mouseY >= top && mouseY <= bottom) {
                manager.selectCategory(category);
                return true;
            }
            categoryY += 16;
        }

        int panelRight = panelX + PANEL_WIDTH;
        int contentX = panelX + CATEGORY_WIDTH + 12;
        int moduleY = panelY + HEADER_HEIGHT + 10;

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
            panelX = clampPanelX((int) mouseX - dragOffsetX);
            panelY = clampPanelY((int) mouseY - dragOffsetY);
            return true;
        }

        NumberSetting slidingSetting = manager.getSlidingSetting();
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT && slidingSetting != null) {
            int contentX = panelX + CATEGORY_WIDTH + 12;
            int panelRight = panelX + PANEL_WIDTH;
            updateNumberSettingFromMouse(slidingSetting, mouseX, contentX, panelRight);
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

    @Override
    public void close() {
        dragging = false;
        manager.stopSliding();
        manager.closeModeList();
        MinecraftClient.getInstance().setScreen(null);
    }

    private int renderSettingBlock(DrawContext context, Setting setting, int contentX, int panelRight, int y) {
        int blockHeight = getSettingBlockHeight(setting);
        int settingTop = y - 3;
        int settingBottom = y + SETTING_ROW_HEIGHT - 3;
        context.fill(contentX + 4, settingTop, panelRight - 16, settingBottom, SETTING_COLOR);
        context.drawBorder(contentX + 4, settingTop, panelRight - contentX - 20, SETTING_ROW_HEIGHT, BORDER_COLOR);

        if (setting instanceof NumberSetting numberSetting) {
            int sliderLeft = contentX + 6;
            int sliderRight = panelRight - 18;
            int sliderWidth = sliderRight - sliderLeft;
            int fillWidth = (int) Math.round(sliderWidth * manager.getNumberPercent(numberSetting));
            context.fill(sliderLeft, settingTop + 1, sliderRight, settingBottom - 1, SETTING_ACCENT_COLOR);
            context.fill(sliderLeft, settingTop + 1, sliderLeft + fillWidth, settingBottom - 1, SETTING_FILL_COLOR);
        }

        context.drawText(textRenderer, Text.literal(manager.getSettingLabel(setting)), contentX + 10, y, TEXT_COLOR, false);

        if (setting instanceof ModeSetting modeSetting && manager.isModeListOpen(modeSetting)) {
            int optionY = y + SETTING_ROW_HEIGHT;
            for (String mode : modeSetting.getModes()) {
                int optionTop = optionY - 3;
                int optionBottom = optionY + SETTING_ROW_HEIGHT - 3;
                int optionColor = mode.equals(modeSetting.getValue()) ? SELECTED_CATEGORY_COLOR : MODE_OPTION_COLOR;
                context.fill(contentX + 16, optionTop, panelRight - 28, optionBottom, optionColor);
                context.drawBorder(contentX + 16, optionTop, panelRight - contentX - 44, SETTING_ROW_HEIGHT, BORDER_COLOR);
                context.drawText(textRenderer, Text.literal(mode), contentX + 22, optionY, TEXT_COLOR, false);
                optionY += SETTING_ROW_HEIGHT;
            }
        }

        return blockHeight;
    }

    private int handleSettingClick(double mouseX, double mouseY, int button, Setting setting, int contentX, int panelRight, int y) {
        int settingTop = y - 3;
        int settingBottom = y + SETTING_ROW_HEIGHT - 3;
        boolean hoveredSetting = mouseX >= contentX + 4 && mouseX <= panelRight - 16 && mouseY >= settingTop && mouseY <= settingBottom;
        if (hoveredSetting) {
            manager.clearBindingTarget();
            if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT && manager.handleSettingLeftClick(setting)) {
                if (setting instanceof NumberSetting numberSetting) {
                    updateNumberSettingFromMouse(numberSetting, mouseX, contentX, panelRight);
                }
                return getSettingBlockHeight(setting);
            }

            if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT && manager.handleSettingRightClick(setting)) {
                return getSettingBlockHeight(setting);
            }
        }

        if (setting instanceof ModeSetting modeSetting && manager.isModeListOpen(modeSetting)) {
            int optionY = y + SETTING_ROW_HEIGHT;
            for (String mode : modeSetting.getModes()) {
                int optionTop = optionY - 3;
                int optionBottom = optionY + SETTING_ROW_HEIGHT - 3;
                boolean hoveredOption = mouseX >= contentX + 16 && mouseX <= panelRight - 28 && mouseY >= optionTop && mouseY <= optionBottom;
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

    private void updateNumberSettingFromMouse(NumberSetting setting, double mouseX, int contentX, int panelRight) {
        double sliderLeft = contentX + 6;
        double sliderRight = panelRight - 18;
        double percent = (mouseX - sliderLeft) / (sliderRight - sliderLeft);
        manager.updateSliding(setting, percent);
    }

    private boolean isLeftShiftDown() {
        MinecraftClient client = MinecraftClient.getInstance();
        return InputUtil.isKeyPressed(client.getWindow().getHandle(), GLFW.GLFW_KEY_LEFT_SHIFT);
    }

    private boolean isHoveringHeader(double mouseX, double mouseY) {
        return mouseX >= panelX && mouseX <= panelX + PANEL_WIDTH && mouseY >= panelY && mouseY <= panelY + HEADER_HEIGHT;
    }

    private int clampPanelX(int x) {
        return Math.max(0, Math.min(x, width - PANEL_WIDTH));
    }

    private int clampPanelY(int y) {
        return Math.max(0, Math.min(y, height - PANEL_HEIGHT));
    }
}
