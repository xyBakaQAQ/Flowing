package com.xybaka.flowing.mixin;

import com.xybaka.flowing.modules.ModuleManager;
import com.xybaka.flowing.modules.player.InventoryHelper;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.screen.slot.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HandledScreen.class)
public abstract class HandledScreenMixin {
    @Inject(method = "drawSlot", at = @At("TAIL"))
    private void flowing$highlightInventoryHelperSlot(DrawContext context, Slot slot, CallbackInfo ci) {
        InventoryHelper module = ModuleManager.getModule(InventoryHelper.class);
        if (module == null || !module.isEnabled()) {
            return;
        }

        int color = module.getSlotHighlightColor(slot);
        if (color == 0) {
            return;
        }

        context.fill(slot.x, slot.y, slot.x + 16, slot.y + 16, color);
    }
}
