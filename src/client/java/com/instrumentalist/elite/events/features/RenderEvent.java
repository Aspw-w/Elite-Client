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
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        Objects.requireNonNull(listener).onRender(this);
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
    }
}