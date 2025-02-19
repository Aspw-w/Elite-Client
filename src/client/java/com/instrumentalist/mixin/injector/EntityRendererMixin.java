package com.instrumentalist.mixin.injector;

import com.instrumentalist.elite.hacks.ModuleManager;
import com.instrumentalist.elite.hacks.features.player.MurdererDetector;
import com.instrumentalist.elite.hacks.features.render.NameTags;
import com.instrumentalist.elite.utils.IMinecraft;
import com.instrumentalist.elite.utils.math.TargetUtil;
import com.instrumentalist.mixin.oringo.IEntityRenderState;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin<T extends Entity, S extends EntityRenderState> implements IEntityRenderState {

    @Shadow protected abstract void renderLabelIfPresent(S state, Text text, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light);

    @Shadow
    @Final
    protected EntityRenderDispatcher dispatcher;

    @Inject(at = @At("HEAD"), method = "render", cancellable = true)
    public void render(S state, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        EntityRenderState.LeashData leashData = state.leashData;
        if (leashData != null) {
            ci.cancel();
            return;
        }

        if (ModuleManager.getModuleState(new NameTags())) {
            ci.cancel();

            Entity entity = ((IEntityRenderState) state).client$getEntity();
            Text renderText = state.displayName != null ? state.displayName : entity.getName();
            if (NameTags.Companion.forceShouldRenderName(entity))
                customRenderLabelIfPresent(state, entity, renderText, matrices, vertexConsumers, light);
            else if (state.displayName != null)
                this.renderLabelIfPresent(state, state.displayName, matrices, vertexConsumers, light);
        }
    }

    @Unique
    protected void customRenderLabelIfPresent(S state, Entity entity, Text text, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        Vec3d attVec = state.nameLabelPos;
        if (attVec == null) return;

        int labelY = "deadmau5".equals(text.getString()) ? -10 : 0;

        matrices.push();
        matrices.translate(attVec.x, attVec.y + 0.5, attVec.z);
        matrices.multiply(dispatcher.getRotation());

        float scale = 0.025F;

        Vec3d entityPos = new Vec3d(state.x, state.y, state.z);
        double distance = IMinecraft.mc.player.getPos().distanceTo(entityPos);
        if (distance > 10)
            scale *= (float) (distance / 10f);

        matrices.scale(scale, -scale, scale);

        Matrix4f matrix = matrices.peek().getPositionMatrix();
        float bgOpacity = IMinecraft.mc.options.getTextBackgroundOpacity(0.25F);
        int bgColor = (int)(bgOpacity * 255F) << 24;
        TextRenderer tr = getTextRenderer();
        float labelX = -tr.getWidth(text) / 2f;

        String pingHpTag = null;
        String extendedTag = null;

        if (ModuleManager.getModuleState(new MurdererDetector()) && entity instanceof PlayerEntity && MurdererDetector.murderers.contains(entity))
            extendedTag = "§7[§cMurderer§7]";
        else if (TargetUtil.isBot((LivingEntity) entity))
            extendedTag = "§7[§cBot§7]";
        else if (TargetUtil.isTeammate((LivingEntity) entity))
            extendedTag = "§7[§eTeam§7]";

        if (entity instanceof PlayerEntity && IMinecraft.mc.getNetworkHandler() != null) {
            PlayerListEntry entry = IMinecraft.mc.getNetworkHandler().getPlayerListEntry(entity.getUuid());
            if (entry != null)
                pingHpTag = "§7[§eHP: " + (int) ((PlayerEntity) entity).getHealth() + "§7] [§ePing: " + entry.getLatency() + "ms§7]";
        }

        if (extendedTag != null) {
            float tagY = pingHpTag != null ? labelY - 20 : labelY - 10;
            tr.draw(extendedTag, -tr.getWidth(extendedTag) / 2f, tagY, 0x20FFFFFF, false, matrix, vertexConsumers, TextRenderer.TextLayerType.SEE_THROUGH, bgColor, light);
            tr.draw(extendedTag, -tr.getWidth(extendedTag) / 2f, tagY, 0xFFFFFFFF, false, matrix, vertexConsumers, TextRenderer.TextLayerType.SEE_THROUGH, 0, light);
        }

        if (pingHpTag != null) {
            tr.draw(pingHpTag, -tr.getWidth(pingHpTag) / 2f, labelY - 10, 0x20FFFFFF, false, matrix, vertexConsumers, TextRenderer.TextLayerType.SEE_THROUGH, bgColor, light);
            tr.draw(pingHpTag, -tr.getWidth(pingHpTag) / 2f, labelY - 10, 0xFFFFFFFF, false, matrix, vertexConsumers, TextRenderer.TextLayerType.SEE_THROUGH, 0, light);
        }

        tr.draw(text, labelX, labelY, 0x20FFFFFF, false, matrix, vertexConsumers, TextRenderer.TextLayerType.SEE_THROUGH, bgColor, light);
        tr.draw(text, labelX, labelY, 0xFFFFFFFF, false, matrix, vertexConsumers, TextRenderer.TextLayerType.SEE_THROUGH, 0, light);

        matrices.pop();
    }

    @Shadow
    public abstract TextRenderer getTextRenderer();
}