package com.instrumentalist.mixin.injector;

import com.instrumentalist.elite.Client;
import com.instrumentalist.elite.events.features.RenderHudEvent;
import com.instrumentalist.elite.hacks.features.render.Scoreboard;
import com.instrumentalist.elite.utils.IMinecraft;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.gui.hud.PlayerListHud;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.scoreboard.ScoreboardDisplaySlot;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

@Mixin(InGameHud.class)
public abstract class InGameHudMixin {

    @Shadow
    public abstract TextRenderer getTextRenderer();

    @Shadow
    private PlayerListHud playerListHud;

    @Inject(method = "render", at = @At("HEAD"))
    public void renderHud(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        TextRenderer textRenderer = this.getTextRenderer();
        Matrix4f matrix4f = context.getMatrices().peek().getPositionMatrix();
        VertexConsumerProvider.Immediate vertexConsumers = IMinecraft.mc.getBufferBuilders().getEntityVertexConsumers();

        RenderHudEvent event = new RenderHudEvent(textRenderer, matrix4f, vertexConsumers);
        Objects.requireNonNull(Client.eventManager).call(event);

        vertexConsumers.draw();
    }

    @Inject(method = "renderScoreboardSidebar", at = @At("HEAD"), cancellable = true)
    private void onRenderScoreboardSidebar(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        if (Scoreboard.isEnabled()) {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.world == null) return;

            net.minecraft.scoreboard.Scoreboard scoreboard = client.world.getScoreboard();
            if (scoreboard == null) return;

            ScoreboardObjective objective = scoreboard.getObjectiveForSlot(ScoreboardDisplaySlot.SIDEBAR);
            if (objective == null) return;

            String title = objective.getDisplayName().getString();

            if (title.matches(".*\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}.*") || title.matches(".*\\bhypixel\\.net\\b.*")) {
                String modifiedTitle = title.replaceAll("\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}|\\bhypixel\\.net\\b", "eliteclient.club");

                Text newTitle = Text.literal(modifiedTitle).formatted(Formatting.GOLD);

                objective.setDisplayName(newTitle);

                System.out.println("Objective Updated: " + objective.getDisplayName().getString());
            }
        }
    }
}