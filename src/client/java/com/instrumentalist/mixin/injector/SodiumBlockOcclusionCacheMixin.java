package com.instrumentalist.mixin.injector;

import com.instrumentalist.elite.hacks.ModuleManager;
import com.instrumentalist.elite.hacks.features.world.XRay;
import com.instrumentalist.elite.utils.IMinecraft;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;
import java.util.Set;

@Pseudo
@Mixin(targets = "net.caffeinemc.mods.sodium.client.render.chunk.compile.pipeline.BlockOcclusionCache", remap = false)
public class SodiumBlockOcclusionCacheMixin {

    @Inject(method = "shouldDrawSide", at = @At("RETURN"), cancellable = true)
    private void xRayHook(BlockState selfState, BlockView view, BlockPos pos, Direction facing, CallbackInfoReturnable<Boolean> ci) {
        if (ModuleManager.getModuleState(new XRay()) && !XRay.Companion.getBlocks().contains(selfState.getBlock())) {
            ci.setReturnValue(false);
            ci.cancel();
            return;
        }

        if (ModuleManager.getModuleState(new XRay())) {
            Set<Block> blocks = XRay.Companion.getBlocks();
            ci.setReturnValue(blocks.contains(selfState.getBlock()));
            ci.cancel();
        }
    }
}