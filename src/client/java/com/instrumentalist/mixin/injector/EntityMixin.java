package com.instrumentalist.mixin.injector;

import com.instrumentalist.elite.hacks.ModuleManager;
import com.instrumentalist.elite.hacks.features.combat.TargetStrafe;
import com.instrumentalist.elite.hacks.features.movement.Step;
import com.instrumentalist.elite.hacks.features.render.Freecam;
import com.instrumentalist.elite.utils.move.MovementUtil;
import com.instrumentalist.elite.utils.rotation.RotationUtil;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityMixin {

    @Shadow
    public abstract float getYaw();

    @Shadow
    public abstract void setVelocity(Vec3d velocity);

    @Shadow
    public abstract Vec3d getVelocity();

    @Shadow
    public static Vec3d movementInputToVelocity(Vec3d movementInput, float speed, float yaw) {
        return null;
    }

    @Shadow public abstract boolean isSprinting();

    @Shadow public abstract void addVelocityInternal(Vec3d velocity);

    @Shadow public boolean velocityDirty;

    @Shadow public abstract void setVelocity(double x, double y, double z);

    @Inject(method = "changeLookDirection", at = @At("HEAD"))
    public void baseRotationHook(double xDelta, double yDelta, CallbackInfo ci) {
        if ((Object) this instanceof ClientPlayerEntity) {
            double pitchDelta = (yDelta * 0.15);
            double yawDelta = (xDelta * 0.15);

            RotationUtil.INSTANCE.setBasePitch(MathHelper.clamp(RotationUtil.INSTANCE.getBasePitch() + (float) pitchDelta, -90.0f, 90.0f));
            RotationUtil.INSTANCE.setBaseYaw(RotationUtil.INSTANCE.getBaseYaw() + (float) yawDelta);
        }
    }

    @Inject(method = "updateVelocity", at = @At("HEAD"), cancellable = true)
    public void updateVelocity(float speed, Vec3d movementInput, CallbackInfo ci) {
        ci.cancel();

        float strafe = this.getYaw();

        if ((Object) this instanceof ClientPlayerEntity) {
            if (TargetStrafe.targetStrafeHook())
                strafe = MovementUtil.getPlayerDirection();
        }

        Vec3d vec3d = movementInputToVelocity(movementInput, speed, strafe);
        this.setVelocity(this.getVelocity().add(vec3d));
    }

    @ModifyReturnValue(method = "isGlowing", at = @At("RETURN"))
    public final boolean freeCamHook1(boolean original) {
        if (ModuleManager.getModuleState(new Freecam()) && Freecam.Companion.getCanFly() && (Object) this instanceof ClientPlayerEntity)
            return true;

        return original;
    }

    @ModifyReturnValue(method = "isInvisible", at = @At("RETURN"))
    public boolean freeCamHook2(boolean original) {
        if (ModuleManager.getModuleState(new Freecam()) && Freecam.Companion.getCanFly() && (Object) this instanceof ClientPlayerEntity)
            return true;

        return original;
    }

    @Inject(method = "adjustMovementForCollisions(Lnet/minecraft/util/math/Vec3d;)Lnet/minecraft/util/math/Vec3d;", at = @At(value = "RETURN", ordinal = 0), cancellable = true)
    private void steppingHook(Vec3d movement, CallbackInfoReturnable<Vec3d> ci) {
        if (((Object) this instanceof ClientPlayerEntity) && ModuleManager.getModuleState(new Step()) && ci.getReturnValue().y > 0.6)
            Step.steppingFunctions();
    }
}