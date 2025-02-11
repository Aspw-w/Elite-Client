package com.instrumentalist.mixin.injector;

import com.instrumentalist.elite.hacks.features.render.LegacyCombat;
import com.instrumentalist.mixin.oringo.IEntityRenderState;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.CrossbowPosing;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.state.BipedEntityRenderState;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BipedEntityModel.class)
public abstract class BipedEntityModelMixin<T extends BipedEntityRenderState> extends EntityModel<T> {

    @Shadow @Final public ModelPart head;

    @Shadow @Final public ModelPart rightArm;
    @Shadow @Final public ModelPart leftArm;
    @Shadow @Final public ModelPart rightLeg;
    @Shadow @Final public ModelPart leftLeg;

    @Shadow protected abstract void positionRightArm(T state, BipedEntityModel.ArmPose armPose);

    @Shadow protected abstract void positionLeftArm(T state, BipedEntityModel.ArmPose armPose);

    @Shadow protected abstract void animateArms(T state, float animationProgress);

    @Shadow protected abstract float method_2807(float f);

    @Shadow @Final public ModelPart body;

    protected BipedEntityModelMixin(ModelPart root) {
        super(root);
    }

    @Inject(method = "setAngles(Lnet/minecraft/client/render/entity/state/BipedEntityRenderState;)V", at = @At(value = "FIELD", target = "Lnet/minecraft/client/model/ModelPart;roll:F", ordinal = 1, shift = At.Shift.AFTER), cancellable = true)
    private void setAngles(T bipedEntityRenderState, CallbackInfo ci) {
        if (((IEntityRenderState) bipedEntityRenderState).client$getEntity() instanceof ClientPlayerEntity) {
            ci.cancel();

            BipedEntityModel.ArmPose armPose = bipedEntityRenderState.leftArmPose;
            BipedEntityModel.ArmPose armPose2 = bipedEntityRenderState.rightArmPose;
            float f = bipedEntityRenderState.leaningPitch;
            boolean bl = bipedEntityRenderState.isGliding;

            this.head.pitch = bipedEntityRenderState.pitch * 0.017453292F;
            this.head.yaw = bipedEntityRenderState.yawDegrees * 0.017453292F;

            if (bl) {
                this.head.pitch = -0.7853982F;
            } else if (f > 0.0F) {
                this.head.pitch = MathHelper.lerpAngleRadians(f, this.head.pitch, -0.7853982F);
            }

            float g = bipedEntityRenderState.limbFrequency;
            float h = bipedEntityRenderState.limbAmplitudeMultiplier;

            if (LegacyCombat.Companion.shouldBlock()) {
                this.rightArm.pitch = this.rightArm.pitch * 0.5F - 0.9424779F;
                this.rightArm.yaw = -0.5F;
                this.rightArm.roll = 0.1F;
            } else this.rightArm.pitch = MathHelper.cos(g * 0.6662F + 3.1415927F) * 2.0F * h * 0.5F / bipedEntityRenderState.limbAmplitudeInverse;

            this.leftArm.pitch = MathHelper.cos(g * 0.6662F) * 2.0F * h * 0.5F / bipedEntityRenderState.limbAmplitudeInverse;
            this.rightLeg.pitch = MathHelper.cos(g * 0.6662F) * 1.4F * h / bipedEntityRenderState.limbAmplitudeInverse;
            this.leftLeg.pitch = MathHelper.cos(g * 0.6662F + 3.1415927F) * 1.4F * h / bipedEntityRenderState.limbAmplitudeInverse;
            this.rightLeg.yaw = 0.005F;
            this.leftLeg.yaw = -0.005F;
            this.rightLeg.roll = 0.005F;
            this.leftLeg.roll = -0.005F;
            ModelPart var10000;
            if (bipedEntityRenderState.hasVehicle) {
                var10000 = this.rightArm;
                var10000.pitch += -0.62831855F;
                var10000 = this.leftArm;
                var10000.pitch += -0.62831855F;
                this.rightLeg.pitch = -1.4137167F;
                this.rightLeg.yaw = 0.31415927F;
                this.rightLeg.roll = 0.07853982F;
                this.leftLeg.pitch = -1.4137167F;
                this.leftLeg.yaw = -0.31415927F;
                this.leftLeg.roll = -0.07853982F;
            }

            if (!LegacyCombat.Companion.shouldBlock()) {
                boolean bl2 = bipedEntityRenderState.mainArm == Arm.RIGHT;
                boolean bl3;
                if (bipedEntityRenderState.isUsingItem) {
                    bl3 = bipedEntityRenderState.activeHand == Hand.MAIN_HAND;
                    if (bl3 == bl2) {
                        this.positionRightArm(bipedEntityRenderState, armPose2);
                    } else {
                        this.positionLeftArm(bipedEntityRenderState, armPose);
                    }
                } else {
                    bl3 = bl2 ? armPose.isTwoHanded() : armPose2.isTwoHanded();
                    if (bl2 != bl3) {
                        this.positionLeftArm(bipedEntityRenderState, armPose);
                        this.positionRightArm(bipedEntityRenderState, armPose2);
                    } else {
                        this.positionRightArm(bipedEntityRenderState, armPose2);
                        this.positionLeftArm(bipedEntityRenderState, armPose);
                    }
                }
            }

            this.animateArms(bipedEntityRenderState, bipedEntityRenderState.age);
            if (bipedEntityRenderState.isInSneakingPose) {
                this.body.pitch = 0.5F;
                var10000 = this.rightArm;
                var10000.pitch += 0.4F;
                var10000 = this.leftArm;
                var10000.pitch += 0.4F;
                var10000 = this.rightLeg;
                var10000.pivotZ += 4.0F;
                var10000 = this.leftLeg;
                var10000.pivotZ += 4.0F;
                var10000 = this.head;
                var10000.pivotY += 4.2F;
                var10000 = this.body;
                var10000.pivotY += 3.2F;
                var10000 = this.leftArm;
                var10000.pivotY += 3.2F;
                var10000 = this.rightArm;
                var10000.pivotY += 3.2F;
            }

            if (armPose2 != BipedEntityModel.ArmPose.SPYGLASS) {
                CrossbowPosing.swingArm(this.rightArm, bipedEntityRenderState.age, 1.0F);
            }

            if (armPose != BipedEntityModel.ArmPose.SPYGLASS) {
                CrossbowPosing.swingArm(this.leftArm, bipedEntityRenderState.age, -1.0F);
            }

            if (f > 0.0F) {
                float i = g % 26.0F;
                Arm arm = bipedEntityRenderState.preferredArm;
                float j = arm == Arm.RIGHT && bipedEntityRenderState.handSwingProgress > 0.0F ? 0.0F : f;
                float k = arm == Arm.LEFT && bipedEntityRenderState.handSwingProgress > 0.0F ? 0.0F : f;
                float l;
                if (!bipedEntityRenderState.isUsingItem) {
                    if (i < 14.0F) {
                        this.leftArm.pitch = MathHelper.lerpAngleRadians(k, this.leftArm.pitch, 0.0F);
                        this.rightArm.pitch = MathHelper.lerp(j, this.rightArm.pitch, 0.0F);
                        this.leftArm.yaw = MathHelper.lerpAngleRadians(k, this.leftArm.yaw, 3.1415927F);
                        this.rightArm.yaw = MathHelper.lerp(j, this.rightArm.yaw, 3.1415927F);
                        this.leftArm.roll = MathHelper.lerpAngleRadians(k, this.leftArm.roll, 3.1415927F + 1.8707964F * this.method_2807(i) / this.method_2807(14.0F));
                        this.rightArm.roll = MathHelper.lerp(j, this.rightArm.roll, 3.1415927F - 1.8707964F * this.method_2807(i) / this.method_2807(14.0F));
                    } else if (i >= 14.0F && i < 22.0F) {
                        l = (i - 14.0F) / 8.0F;
                        this.leftArm.pitch = MathHelper.lerpAngleRadians(k, this.leftArm.pitch, 1.5707964F * l);
                        this.rightArm.pitch = MathHelper.lerp(j, this.rightArm.pitch, 1.5707964F * l);
                        this.leftArm.yaw = MathHelper.lerpAngleRadians(k, this.leftArm.yaw, 3.1415927F);
                        this.rightArm.yaw = MathHelper.lerp(j, this.rightArm.yaw, 3.1415927F);
                        this.leftArm.roll = MathHelper.lerpAngleRadians(k, this.leftArm.roll, 5.012389F - 1.8707964F * l);
                        this.rightArm.roll = MathHelper.lerp(j, this.rightArm.roll, 1.2707963F + 1.8707964F * l);
                    } else if (i >= 22.0F && i < 26.0F) {
                        l = (i - 22.0F) / 4.0F;
                        this.leftArm.pitch = MathHelper.lerpAngleRadians(k, this.leftArm.pitch, 1.5707964F - 1.5707964F * l);
                        this.rightArm.pitch = MathHelper.lerp(j, this.rightArm.pitch, 1.5707964F - 1.5707964F * l);
                        this.leftArm.yaw = MathHelper.lerpAngleRadians(k, this.leftArm.yaw, 3.1415927F);
                        this.rightArm.yaw = MathHelper.lerp(j, this.rightArm.yaw, 3.1415927F);
                        this.leftArm.roll = MathHelper.lerpAngleRadians(k, this.leftArm.roll, 3.1415927F);
                        this.rightArm.roll = MathHelper.lerp(j, this.rightArm.roll, 3.1415927F);
                    }
                }

                l = 0.3F;
                float m = 0.33333334F;
                this.leftLeg.pitch = MathHelper.lerp(f, this.leftLeg.pitch, 0.3F * MathHelper.cos(g * 0.33333334F + 3.1415927F));
                this.rightLeg.pitch = MathHelper.lerp(f, this.rightLeg.pitch, 0.3F * MathHelper.cos(g * 0.33333334F));
            }
        }
    }
}