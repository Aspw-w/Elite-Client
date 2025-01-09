package com.instrumentalist.mixin.injector;

import com.instrumentalist.elite.hacks.features.world.XRay;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Block.class)
public abstract class BlockMixin {

    @ModifyReturnValue(method = "shouldDrawSide", at = @At("RETURN"))
    private static boolean xRayHook(boolean original, BlockState state, BlockState otherState, Direction side) {
        return XRay.Companion.hookTransparentOre(state, original);
    }
}