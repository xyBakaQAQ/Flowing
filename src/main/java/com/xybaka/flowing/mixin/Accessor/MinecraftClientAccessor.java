package com.xybaka.flowing.mixin.Accessor;

import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(MinecraftClient.class)
public interface MinecraftClientAccessor {
    @Invoker("doAttack")
    boolean flowing$doAttack();

    @Invoker("doItemUse")
    void flowing$doItemUse();
}
