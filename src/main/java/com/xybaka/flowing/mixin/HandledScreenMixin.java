package com.xybaka.flowing.mixin;

import com.xybaka.flowing.modules.ModuleManager;
import com.xybaka.flowing.modules.movement.InvMove;
import com.xybaka.flowing.modules.player.InventoryHelper;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.screen.slot.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

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

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void flowing$blockInvMoveClicks(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        if (InvMove.shouldBlockHandledClicks()) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "mouseReleased", at = @At("HEAD"), cancellable = true)
    private void flowing$blockInvMoveReleases(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        if (InvMove.shouldBlockHandledClicks()) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "mouseDragged", at = @At("HEAD"), cancellable = true)
    private void flowing$blockInvMoveDrags(double mouseX, double mouseY, int button, double deltaX, double deltaY, CallbackInfoReturnable<Boolean> cir) {
        if (InvMove.shouldBlockHandledClicks()) {
            cir.setReturnValue(false);
        }
    }
}
