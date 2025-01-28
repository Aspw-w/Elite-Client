package com.instrumentalist.elite.events.features;

import com.instrumentalist.elite.events.EventArgument;
import com.instrumentalist.elite.events.EventListener;
import net.minecraft.client.util.math.MatrixStack;
import org.lwjgl.opengl.GL11;

import java.util.Objects;

public class RenderEvent extends EventArgument {
    public final MatrixStack matrix;
    public final float partialTicks;

    public RenderEvent(MatrixStack matrix, Float partialTicks) {
        this.matrix = matrix;
        this.partialTicks = partialTicks;
    }

    @Override
    public void call(EventListener listener) {
        Objects.requireNonNull(listener).onRender(this);
    }
}