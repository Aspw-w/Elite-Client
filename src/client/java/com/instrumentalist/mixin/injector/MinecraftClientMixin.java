package com.instrumentalist.mixin.injector;

import com.instrumentalist.elite.Client;
import com.instrumentalist.elite.events.features.HandleInputEvent;
import com.instrumentalist.elite.events.features.TickEvent;
import com.instrumentalist.elite.events.features.WorldEvent;
import com.instrumentalist.elite.hacks.ModuleManager;
import com.instrumentalist.elite.hacks.features.render.LegacyCombat;
import com.instrumentalist.elite.hacks.features.world.XRay;
import com.instrumentalist.elite.utils.IMinecraft;
import com.instrumentalist.elite.utils.entity.PlayerUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.consume.UseAction;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {

    @Shadow @Nullable public ClientPlayerEntity player;

    @Shadow public int attackCooldown;

    @Shadow @Nullable public HitResult crosshairTarget;

    @Shadow @Nullable public ClientWorld world;

    @Shadow @Nullable public ClientPlayerInteractionManager interactionManager;

    @Shadow @Final public ParticleManager particleManager;

    @Inject(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;onResolutionChanged()V"))
    private void injectClient(CallbackInfo callback) {
        Client.inject();
    }

    @Inject(at = @At(value = "FIELD", target = "Lnet/minecraft/client/MinecraftClient;overlay:Lnet/minecraft/client/gui/screen/Overlay;", ordinal = 0), method = "tick()V", cancellable = true)
    private void handleInputEvent(CallbackInfo ci) {
        if (this.player == null)
            return;

        HandleInputEvent event = new HandleInputEvent();
        Objects.requireNonNull(Client.eventManager).call(event);
        if (event.isCancelled())
            ci.cancel();
    }

    @Inject(method = "handleBlockBreaking", at = @At("HEAD"), cancellable = true)
    private void handleBlockBreaking(boolean breaking, CallbackInfo ci) {
        if (this.attackCooldown <= 0) {
            if (breaking && this.crosshairTarget != null && this.crosshairTarget.getType() == net.minecraft.util.hit.HitResult.Type.BLOCK) {
                BlockHitResult blockHitResult = (BlockHitResult)this.crosshairTarget;
                BlockPos blockPos = blockHitResult.getBlockPos();
                if (!this.world.getBlockState(blockPos).isAir()) {
                    if (ModuleManager.getModuleState(new LegacyCombat()) && LegacyCombat.Companion.getSwingHandWhileDigging().get() && this.player.isUsingItem() && (IMinecraft.mc.player.getActiveItem().getUseAction() == UseAction.BLOCK || IMinecraft.mc.player.getActiveItem().getUseAction() == UseAction.EAT || IMinecraft.mc.player.getActiveItem().getUseAction() == UseAction.DRINK || IMinecraft.mc.player.getActiveItem().getUseAction() == UseAction.BOW)) {
                        ci.cancel();
                        PlayerUtil.INSTANCE.swingHandWithoutPacket(Hand.MAIN_HAND);
                    }
                }
            }
        }
    }

    @Inject(method = "setWorld", at = @At("HEAD"), cancellable = true)
    private void worldEvent(ClientWorld world, CallbackInfo ci) {
        WorldEvent event = new WorldEvent();
        Objects.requireNonNull(Client.eventManager).call(event);
        if (event.isCancelled())
            ci.cancel();
    }

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void tickEvent(CallbackInfo ci) {
        TickEvent event = new TickEvent();
        Objects.requireNonNull(Client.eventManager).call(event);
        if (event.isCancelled())
            ci.cancel();
    }

    @Inject(method = "isAmbientOcclusionEnabled", at = @At("HEAD"), cancellable = true)
    private static void xRayHook(CallbackInfoReturnable<Boolean> ci) {
        if (ModuleManager.getModuleState(new XRay())) {
            ci.setReturnValue(false);
            ci.cancel();
        }
    }
}
