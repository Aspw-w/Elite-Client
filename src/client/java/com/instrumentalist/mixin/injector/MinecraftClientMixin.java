package com.instrumentalist.mixin.injector;

import com.instrumentalist.elite.Client;
import com.instrumentalist.elite.events.features.HandleInputEvent;
import com.instrumentalist.elite.events.features.TickEvent;
import com.instrumentalist.elite.events.features.WorldEvent;
import com.instrumentalist.elite.hacks.ModuleManager;
import com.instrumentalist.elite.hacks.features.world.XRay;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import org.jetbrains.annotations.Nullable;
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
