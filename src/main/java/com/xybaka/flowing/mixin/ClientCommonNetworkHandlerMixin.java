package com.xybaka.flowing.mixin;

import com.xybaka.flowing.modules.movement.InvMove;
import net.minecraft.client.network.ClientCommonNetworkHandler;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientCommonNetworkHandler.class)
public abstract class ClientCommonNetworkHandlerMixin {
    @Inject(method = "sendPacket", at = @At("HEAD"), cancellable = true)
    private void flowing$bufferInvMoveClickSlot(Packet<?> packet, CallbackInfo ci) {
        if (!(packet instanceof ClickSlotC2SPacket clickSlotPacket)) {
            return;
        }

        if (!InvMove.shouldBufferClickSlotPacket()) {
            return;
        }

        InvMove.bufferPacket(clickSlotPacket);
        ci.cancel();
    }
}
