package com.instrumentalist.elite.utils.move;

import com.instrumentalist.elite.hacks.features.combat.KillAura;
import com.instrumentalist.elite.hacks.features.combat.TargetStrafe;
import com.instrumentalist.elite.utils.IMinecraft;
import com.instrumentalist.elite.utils.entity.EntityExtensionKt;
import com.instrumentalist.elite.utils.rotation.RotationUtil;
import net.minecraft.client.input.Input;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;

public class MovementUtil {

    public static int fallTicks = 0;

    public static boolean hasMotion() {
        return IMinecraft.mc.player != null && (IMinecraft.mc.player.getVelocity().x != 0.0 || IMinecraft.mc.player.getVelocity().y != 0.0 || IMinecraft.mc.player.getVelocity().z != 0.0);
    }

    public static boolean isMoving() {
        return IMinecraft.mc.player != null && (IMinecraft.mc.player.input.movementForward != 0.0 || IMinecraft.mc.player.input.movementSideways != 0.0);
    }

    public static double getBaseMoveSpeed(double customSpeed) {
        double baseSpeed = customSpeed;

        if (IMinecraft.mc.player != null && IMinecraft.mc.player.hasStatusEffect(StatusEffects.SPEED)) {
            StatusEffectInstance effect = IMinecraft.mc.player.getStatusEffect(StatusEffects.SPEED);

            if (effect != null) {
                int amplifier = effect.getAmplifier();
                baseSpeed *= 1.0 + 0.2 * (amplifier + 1);
            }
        }

        return baseSpeed;
    }

    public static int getSpeedEffect() {
        if (IMinecraft.mc.player != null && IMinecraft.mc.player.hasStatusEffect(StatusEffects.SPEED)) {
            StatusEffectInstance effect = IMinecraft.mc.player.getStatusEffect(StatusEffects.SPEED);

            if (effect != null)
                return effect.getAmplifier() + 1;
        }

        return 0;
    }

    public static void stopMoving() {
        if (IMinecraft.mc.player == null) return;

        IMinecraft.mc.player.setVelocity(0.0, IMinecraft.mc.player.getVelocity().y, 0.0);
        IMinecraft.mc.player.input.movementForward = 0f;
        IMinecraft.mc.player.input.movementSideways = 0f;
    }

    public static boolean isDiagonal(float threshold) {
        float yaw = getPlayerDirection();
        yaw = Math.abs(((yaw + 360) % 360));
        boolean isNorth = Math.abs(yaw) < threshold || Math.abs(yaw - 360) < threshold;
        boolean isSouth = Math.abs(yaw - 180) < threshold;
        boolean isEast = Math.abs(yaw - 90) < threshold;
        boolean isWest = Math.abs(yaw - 270) < threshold;
        return (!isNorth && !isSouth && !isEast && !isWest);
    }

    public static float getPlayerDirection() {
        if (IMinecraft.mc.player == null) return 0f;

        float yaw = IMinecraft.mc.player.getYaw();

        if (TargetStrafe.targetStrafeHook())
            yaw = RotationUtil.INSTANCE.getRotationsEntity((LivingEntity) KillAura.closestEntity).getFirst();

        float strafe = 45f;
        Input input = IMinecraft.mc.player.input;

        if (input.movementForward < 0) {
            strafe = -45f;
            yaw += 180f;
        }
        if (input.playerInput.left()) {
            yaw -= strafe;
            if (input.movementForward == 0f)
                yaw -= 45f;
        } else if (input.playerInput.right()) {
            yaw += strafe;
            if (input.movementForward == 0f)
                yaw += 45f;
        }

        yaw = (yaw % 360 + 360) % 360;

        return yaw;
    }

    public static void setVelocityY(Double y) {
        if (IMinecraft.mc.player == null) return;

        IMinecraft.mc.player.setVelocity(IMinecraft.mc.player.getVelocity().x, y, IMinecraft.mc.player.getVelocity().z);
    }

    public static void smoothStrafe(Float speed) {
        if (IMinecraft.mc.player == null) return;

        if (isMoving()) {
            double yaw = Math.toRadians(getPlayerDirection());
            IMinecraft.mc.player.setVelocity(IMinecraft.mc.player.getVelocity().x - Math.sin(yaw) * (speed / 4), IMinecraft.mc.player.getVelocity().y, IMinecraft.mc.player.getVelocity().z + Math.cos(yaw) * (speed / 4));
        }
    }

    public static void strafe(Float speed) {
        if (IMinecraft.mc.player == null) return;

        if (isMoving()) {
            if (TargetStrafe.targetStrafeHook()) {
                float yaw = RotationUtil.INSTANCE.getRotationsEntity((LivingEntity) KillAura.closestEntity).getFirst();
                double forward = EntityExtensionKt.distanceToWithoutY(IMinecraft.mc.player, KillAura.closestEntity) >= TargetStrafe.distance.get() + 2f ? 2.0 : EntityExtensionKt.distanceToWithoutY(IMinecraft.mc.player, KillAura.closestEntity) <= TargetStrafe.distance.get() ? 0.0 : 1.0;
                double direction = TargetStrafe.direction;

                if (forward == 2.0) {
                    double deltaX = KillAura.closestEntity.getX() - IMinecraft.mc.player.getX();
                    double deltaZ = KillAura.closestEntity.getZ() - IMinecraft.mc.player.getZ();
                    double distance = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);

                    if (distance > 0.0) {
                        deltaX /= distance;
                        deltaZ /= distance;
                    }

                    IMinecraft.mc.player.setVelocity(deltaX * speed, IMinecraft.mc.player.getVelocity().y, deltaZ * speed);
                } else {
                    if (forward != 0.0) {
                        if (direction > 0.0) yaw -= 45;
                        else if (direction < 0.0) yaw += 45;
                        direction = 0.0;
                    }

                    if (direction > 0.0)
                        direction = 1.0;
                    else if (direction < 0.0)
                        direction = -1.0;

                    double mx = Math.cos(Math.toRadians((yaw + 90f)));
                    double mz = Math.sin(Math.toRadians((yaw + 90f)));

                    double combinedX = forward * speed * mx + direction * speed * mz;
                    double combinedZ = forward * speed * mz - direction * speed * mx;

                    double magnitude = Math.sqrt(combinedX * combinedX + combinedZ * combinedZ);

                    if (magnitude > 0) {
                        combinedX /= magnitude;
                        combinedZ /= magnitude;
                    }

                    IMinecraft.mc.player.setVelocity(combinedX * speed, IMinecraft.mc.player.getVelocity().y, combinedZ * speed);
                }
            } else {
                double yaw = Math.toRadians(getPlayerDirection());
                double xSpeed = -Math.sin(yaw);
                double zSpeed = Math.cos(yaw);

                double magnitude = Math.sqrt(xSpeed * xSpeed + zSpeed * zSpeed);

                if (magnitude > 0) {
                    xSpeed /= magnitude;
                    zSpeed /= magnitude;
                }

                IMinecraft.mc.player.setVelocity(xSpeed * speed, IMinecraft.mc.player.getVelocity().y, zSpeed * speed);
            }
        } else stopMoving();
    }
}
