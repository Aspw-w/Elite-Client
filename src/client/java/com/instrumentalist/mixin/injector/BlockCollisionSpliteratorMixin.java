package com.instrumentalist.mixin.injector;

import com.instrumentalist.elite.Client;
import com.instrumentalist.elite.events.features.CollisionEvent;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockCollisionSpliterator;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Objects;

@Mixin(value = BlockCollisionSpliterator.class, priority = 800)
public abstract class BlockCollisionSpliteratorMixin {

    @Redirect(method = "computeNext", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/BlockView;getBlockState(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/block/BlockState;"))
    private BlockState computeNext(BlockView instance, BlockPos blockPos) {
        CollisionEvent event = new CollisionEvent(instance.getBlockState(blockPos), blockPos);
        Objects.requireNonNull(Client.eventManager).call(event);

        return event.blockState;
    }
}