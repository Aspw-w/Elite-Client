package com.instrumentalist.elite.events.features;

import com.instrumentalist.elite.events.EventArgument;
import com.instrumentalist.elite.events.EventListener;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import org.joml.Matrix4f;

import java.util.Objects;

public class RenderHudEvent extends EventArgument {
    public final TextRenderer textRenderer;
    public final Matrix4f matrix4f;
    public final VertexConsumerProvider vertexConsumers;

    public RenderHudEvent(TextRenderer textRenderer, Matrix4f matrix4f, VertexConsumerProvider vertexConsumers) {
        this.textRenderer = textRenderer;
        this.matrix4f = matrix4f;
        this.vertexConsumers = vertexConsumers;
    }

    @Override
    public void call(EventListener listener) {
        Objects.requireNonNull(listener).onRenderHud(this);
    }
}