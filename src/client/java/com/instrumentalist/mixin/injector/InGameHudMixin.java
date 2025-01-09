package com.instrumentalist.mixin.injector;

import com.instrumentalist.elite.Client;
import com.instrumentalist.elite.events.features.RenderHudEvent;
import com.instrumentalist.elite.utils.IMinecraft;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.render.VertexConsumerProvider;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

@Mixin(InGameHud.class)
public abstract class InGameHudMixin {

    @Shadow public abstract TextRenderer getTextRenderer();

    @Inject(method = "render", at = @At("HEAD"))
    public void renderHud(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        TextRenderer textRenderer = this.getTextRenderer();
        Matrix4f matrix4f = context.getMatrices().peek().getPositionMatrix();
        VertexConsumerProvider.Immediate vertexConsumers = IMinecraft.mc.getBufferBuilders().getEntityVertexConsumers();

        RenderHudEvent event = new RenderHudEvent(textRenderer, matrix4f, vertexConsumers);
        Objects.requireNonNull(Client.eventManager).call(event);

        vertexConsumers.draw();
    }
}