package com.instrumentalist.mixin.injector;

import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import com.instrumentalist.elite.Client;
import com.instrumentalist.elite.events.features.RenderHudEvent;
import com.instrumentalist.elite.hacks.ModuleManager;
import com.instrumentalist.elite.hacks.features.render.HudPoser;
import com.instrumentalist.elite.utils.IMinecraft;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.StatusEffectSpriteManager;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

@Mixin(InGameHud.class)
public abstract class InGameHudMixin {

    @Shadow public abstract TextRenderer getTextRenderer();

    @Shadow @Final private MinecraftClient client;

    @Shadow @Final private static Identifier EFFECT_BACKGROUND_AMBIENT_TEXTURE;

    @Shadow @Final private static Identifier EFFECT_BACKGROUND_TEXTURE;

    @Inject(method = "render", at = @At("HEAD"))
    public void renderHud(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        TextRenderer textRenderer = this.getTextRenderer();
        Matrix4f matrix4f = context.getMatrices().peek().getPositionMatrix();
        VertexConsumerProvider.Immediate vertexConsumers = IMinecraft.mc.getBufferBuilders().getEntityVertexConsumers();

        RenderHudEvent event = new RenderHudEvent(textRenderer, matrix4f, vertexConsumers);
        Objects.requireNonNull(Client.eventManager).call(event);

        vertexConsumers.draw();
    }

    @Inject(method = "renderStatusEffectOverlay", at = @At("HEAD"), cancellable = true)
    private void disableStatusEffectOverlay(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        if (ModuleManager.getModuleState(new HudPoser())) {
            switch (HudPoser.statusEffects.get().toLowerCase()) {
                case "down":
                    ci.cancel();
                    context.getMatrices().push();
                    context.getMatrices().translate(0.0F, IMinecraft.mc.getWindow().getScaledHeight() - 26.0F, 0.0F);
                    this.customRenderStatusEffectOverlay(context, tickCounter);
                    context.getMatrices().pop();
                    break;

                case "hide":
                    ci.cancel();
                    break;
            }
        }
    }

    @Unique
    private void customRenderStatusEffectOverlay(DrawContext context, RenderTickCounter tickCounter) {
        Collection<StatusEffectInstance> collection = this.client.player.getStatusEffects();
        if (!collection.isEmpty() && (this.client.currentScreen == null || !this.client.currentScreen.shouldHideStatusEffectHud())) {
            int i = 0;
            int j = 0;
            StatusEffectSpriteManager statusEffectSpriteManager = this.client.getStatusEffectSpriteManager();
            List<Runnable> list = Lists.newArrayListWithExpectedSize(collection.size());

            for(StatusEffectInstance statusEffectInstance : Ordering.natural().reverse().sortedCopy(collection)) {
                RegistryEntry<StatusEffect> registryEntry = statusEffectInstance.getEffectType();
                if (statusEffectInstance.shouldShowIcon()) {
                    int k = context.getScaledWindowWidth();
                    int l = 1;

                    if (((StatusEffect)registryEntry.value()).isBeneficial()) {
                        ++i;
                        k -= 25 * i;
                    } else {
                        ++j;
                        k -= 25 * j;
                        l -= 26;
                    }

                    float f = 1.0F;
                    if (statusEffectInstance.isAmbient()) {
                        context.drawGuiTexture(RenderLayer::getGuiTextured, EFFECT_BACKGROUND_AMBIENT_TEXTURE, k, l, 24, 24);
                    } else {
                        context.drawGuiTexture(RenderLayer::getGuiTextured, EFFECT_BACKGROUND_TEXTURE, k, l, 24, 24);
                        if (statusEffectInstance.isDurationBelow(200)) {
                            int m = statusEffectInstance.getDuration();
                            int n = 10 - m / 20;
                            f = MathHelper.clamp((float)m / 10.0F / 5.0F * 0.5F, 0.0F, 0.5F) + MathHelper.cos((float)m * (float)Math.PI / 5.0F) * MathHelper.clamp((float)n / 10.0F * 0.25F, 0.0F, 0.25F);
                            f = MathHelper.clamp(f, 0.0F, 1.0F);
                        }
                    }

                    Sprite sprite = statusEffectSpriteManager.getSprite(registryEntry);
                    final int finalK = k;
                    final int finalL = l;
                    final float finalF = f;
                    list.add(() -> {
                        int color = ColorHelper.getWhite(finalF);
                        context.drawSpriteStretched(RenderLayer::getGuiTextured, sprite, finalK + 3, finalL + 3, 18, 18, color);
                    });
                }
            }

            list.forEach(Runnable::run);
        }
    }
}