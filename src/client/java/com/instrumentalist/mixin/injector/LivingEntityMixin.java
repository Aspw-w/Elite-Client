package com.instrumentalist.mixin.injector;

import com.instrumentalist.elite.hacks.ModuleManager;
import com.instrumentalist.elite.hacks.features.combat.TargetStrafe;
import com.instrumentalist.elite.hacks.features.movement.Sprint;
import com.instrumentalist.elite.hacks.features.movement.Step;
import com.instrumentalist.elite.hacks.features.render.ItemView;
import com.instrumentalist.elite.utils.IMinecraft;
import com.instrumentalist.elite.utils.move.MovementUtil;
import com.instrumentalist.elite.utils.simulator.PredictUtil;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends EntityMixin {

    @Shadow public abstract float getJumpVelocity();

    @Inject(method = "jump", at = @At("HEAD"), cancellable = true)
    public void jumpEvent(CallbackInfo ci) {
        ci.cancel();

        float f = this.getJumpVelocity();
        if (!(f <= 1.0E-5F)) {
            Vec3d vec3d = this.getVelocity();
            this.setVelocity(vec3d.x, Math.max((double) f, vec3d.y), vec3d.z);
            if (this.isSprinting()) {
                float g = this.getYaw() * 0.017453292F;

                if ((Object) this instanceof ClientPlayerEntity && (ModuleManager.getModuleState(new Sprint()) && Sprint.Companion.getMultiDirection().get() && IMinecraft.mc.player.isSprinting() || TargetStrafe.targetStrafeHook()))
                    g = MovementUtil.getPlayerDirection() * 0.017453292F;

                this.addVelocityInternal(new Vec3d((double) (-MathHelper.sin(g)) * 0.2, 0.0, (double) MathHelper.cos(g) * 0.2));
            }

            this.velocityDirty = true;
        }
    }

    @Inject(method = "pushAwayFrom", at = @At("HEAD"), cancellable = true)
    private void predictPushFix(CallbackInfo ci) {
        if (((LivingEntity) (Object) this instanceof ClientPlayerEntity) && PredictUtil.predicting)
            ci.cancel();
    }

    @ModifyReturnValue(method = "getHandSwingDuration", at = @At("RETURN"))
    private int swingSpeedModifier(int original) {
        return ItemView.Companion.hookSwingSpeed(original, (LivingEntity) (Object) this);
    }

    @ModifyReturnValue(method = "getStepHeight", at = @At("RETURN"))
    private float stepHook(float original) {
        return Step.hookStepHeight(original, (LivingEntity) (Object) this);
    }
}