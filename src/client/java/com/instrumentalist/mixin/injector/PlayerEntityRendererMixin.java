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
    private Float prevYaw = null;

    @Unique
    private Float prevPitch = null;

    @Unique
    private long lastFrameTime = System.nanoTime();

    @Inject(method = "updateRenderState(Lnet/minecraft/client/network/AbstractClientPlayerEntity;Lnet/minecraft/client/render/entity/state/PlayerEntityRenderState;F)V", at = @At("RETURN"))
    private void clientSideRotations(AbstractClientPlayerEntity player, PlayerEntityRenderState state, float f, CallbackInfo info) {
        if (player != IMinecraft.mc.player) return;
        if (ModuleManager.interpolatedYaw != null && ModuleManager.interpolatedPitch != null && !ModuleManager.interpolatedYaw.isInfinite() && !ModuleManager.interpolatedYaw.isNaN() && !ModuleManager.interpolatedPitch.isInfinite() && !ModuleManager.interpolatedPitch.isNaN()) {
            long currentTime = System.nanoTime();
            float deltaTime = (currentTime - lastFrameTime) / 1e9f;

            if (deltaTime > 0.1f) deltaTime = 0.1f;

            if (lastFrameTime == 0 || prevYaw == null || prevPitch == null) {
                prevYaw = ModuleManager.interpolatedYaw;
                prevPitch = ModuleManager.interpolatedPitch;
                lastFrameTime = currentTime;
                return;
            }

            lastFrameTime = currentTime;

            float showYaw = ModuleManager.interpolatedYaw;

            if (showYaw >= 360f)
                showYaw = 360f;
            else if (showYaw <= -360f)
                showYaw = -360f;

            float yaw = Interpolation.INSTANCE.valueLimitedLerpWithTime(this.prevYaw, showYaw, 18f, deltaTime, 320);
            float pitch = Interpolation.INSTANCE.lerpWithTime(this.prevPitch, ModuleManager.interpolatedPitch, 14f, deltaTime);

            state.bodyYaw = yaw;
            this.prevYaw = state.bodyYaw;

            state.pitch = pitch;
            this.prevPitch = state.pitch;
        } else if (lastFrameTime != 0) {
            prevYaw = null;
            prevPitch = null;
            lastFrameTime = 0;
        }
    }
}