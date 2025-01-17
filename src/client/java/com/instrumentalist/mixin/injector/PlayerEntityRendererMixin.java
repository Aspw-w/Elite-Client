package com.instrumentalist.mixin.injector;

import com.instrumentalist.elite.hacks.ModuleManager;
import com.instrumentalist.elite.hacks.features.player.SpinBot;
import com.instrumentalist.elite.hacks.features.render.FullBright;
import com.instrumentalist.elite.utils.ChatUtil;
import com.instrumentalist.elite.utils.IMinecraft;
import com.instrumentalist.elite.utils.math.Interpolation;
import com.instrumentalist.elite.utils.rotation.RotationUtil;
import com.instrumentalist.mixin.oringo.IEntityRenderState;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntityRenderer.class)
public abstract class PlayerEntityRendererMixin {

    @Unique
    private float prevYaw = 0.0F;

    @Unique
    private float prevPitch = 0.0F;

    @Unique
    private long lastFrameTime = System.nanoTime();

    @Inject(method = "updateRenderState(Lnet/minecraft/client/network/AbstractClientPlayerEntity;Lnet/minecraft/client/render/entity/state/PlayerEntityRenderState;F)V", at = @At("RETURN"))
    private void clientSideRotations(AbstractClientPlayerEntity player, PlayerEntityRenderState state, float f, CallbackInfo info) {
        if (player != IMinecraft.mc.player) return;
        if (RotationUtil.INSTANCE.getCurrentYaw() != null && RotationUtil.INSTANCE.getCurrentPitch() != null && !RotationUtil.INSTANCE.getCurrentYaw().isInfinite() && !RotationUtil.INSTANCE.getCurrentYaw().isNaN() && !RotationUtil.INSTANCE.getCurrentPitch().isInfinite() && !RotationUtil.INSTANCE.getCurrentPitch().isNaN()) {
            long currentTime = System.nanoTime();
            float deltaTime = (currentTime - lastFrameTime) / 1e9f;

            if (deltaTime > 0.1f) deltaTime = 0.1f;

            if (lastFrameTime == 0) {
                prevYaw = RotationUtil.INSTANCE.getCurrentYaw();
                prevPitch = RotationUtil.INSTANCE.getCurrentPitch();
                lastFrameTime = currentTime;
                return;
            }

            lastFrameTime = currentTime;

            float showYaw = RotationUtil.INSTANCE.getCurrentYaw();

            if (showYaw >= 360f)
                showYaw = 360f;
            else if (showYaw <= -360f)
                showYaw = -360f;

            float yaw = showYaw >= 0f && (showYaw >= 315f || showYaw <= 45f) || showYaw <= 0f && (showYaw <= -315f || showYaw >= -45f) ? showYaw : Interpolation.INSTANCE.lerpWithTime(this.prevYaw, RotationUtil.INSTANCE.getCurrentYaw(), 18f, deltaTime);
            float pitch = Interpolation.INSTANCE.lerpWithTime(this.prevPitch, RotationUtil.INSTANCE.getCurrentPitch(), 14f, deltaTime);

            state.bodyYaw = yaw;
            this.prevYaw = state.bodyYaw;

            state.pitch = pitch;
            this.prevPitch = state.pitch;
        }
    }
}