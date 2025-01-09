package com.instrumentalist.mixin.injector;

import com.instrumentalist.elite.Client;
import com.instrumentalist.elite.events.features.UpdateEvent;
import com.instrumentalist.elite.hacks.features.movement.Sprint;
import com.instrumentalist.elite.utils.IMinecraft;
import com.instrumentalist.elite.utils.move.MovementUtil;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin {

    @Inject(method = "tickMovement", at = @At("HEAD"), cancellable = true)
    private void updateEvent(CallbackInfo ci) {
        if (!((Object) this instanceof ClientPlayerEntity)) return;

        if (IMinecraft.mc.player != null) {
            if (IMinecraft.mc.player.isOnGround())
                MovementUtil.fallTicks = 0;
            else MovementUtil.fallTicks++;
        }

        UpdateEvent event = new UpdateEvent();
        Objects.requireNonNull(Client.eventManager).call(event);
        if (event.isCancelled())
            ci.cancel();
    }

    @WrapWithCondition(method = "attack", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;setVelocity(Lnet/minecraft/util/math/Vec3d;)V", ordinal = 0))
    private boolean keepSprintHook(PlayerEntity instance, Vec3d vec3d) {
        return Sprint.Companion.keepSprintHook(instance);
    }

    @WrapWithCondition(method = "attack", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;setSprinting(Z)V", ordinal = 0))
    private boolean keepSprintHook(PlayerEntity instance, boolean b) {
        return Sprint.Companion.keepSprintHook(instance);
    }
}