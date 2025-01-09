package com.instrumentalist.elite.events.features;

import com.instrumentalist.elite.events.EventArgument;
import com.instrumentalist.elite.events.EventListener;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

import java.util.Objects;

public class CollisionEvent extends EventArgument {

    public BlockState blockState;
    public final BlockPos blockPos;

    public CollisionEvent(BlockState blockState, BlockPos blockPos) {
        this.blockState = blockState;
        this.blockPos = blockPos;
    }

    @Override
    public void call(EventListener listener) {
        Objects.requireNonNull(listener).onCollision(this);
    }
}