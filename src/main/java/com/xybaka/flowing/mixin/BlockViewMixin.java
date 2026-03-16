package com.xybaka.flowing.mixin;

import com.xybaka.flowing.modules.ModuleManager;
import com.xybaka.flowing.modules.player.GhostHand;
import net.minecraft.block.BlockState;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.RaycastContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockView.class)
public interface BlockViewMixin {
    @Inject(method = "raycast(Lnet/minecraft/world/RaycastContext;)Lnet/minecraft/util/hit/BlockHitResult;", at = @At("HEAD"), cancellable = true)
    private void flowing$ghostHandRaycast(RaycastContext context, CallbackInfoReturnable<BlockHitResult> cir) {
        GhostHand ghostHand = ModuleManager.getModule(GhostHand.class);
        if (ghostHand == null || !ghostHand.isEnabled()) {
            return;
        }

        BlockView self = (BlockView) (Object) this;
        BlockHitResult ghostResult = BlockView.raycast(context.getStart(), context.getEnd(), context, (ctx, pos) -> {
            BlockState blockState = self.getBlockState(pos);
            if (!ghostHand.getTargetedBlocks().contains(blockState.getBlock())) {
                return null;
            }

            VoxelShape voxelShape = ctx.getBlockShape(blockState, self, pos);
            return self.raycastBlock(ctx.getStart(), ctx.getEnd(), pos, voxelShape, blockState);
        }, ctx -> null);

        if (ghostResult != null) {
            cir.setReturnValue(ghostResult);
        }
    }
}
