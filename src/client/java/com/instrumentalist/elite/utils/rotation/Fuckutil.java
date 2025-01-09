package com.instrumentalist.elite.utils.rotation;

import com.instrumentalist.elite.hacks.ModuleManager;
import com.instrumentalist.elite.utils.IMinecraft;
import net.minecraft.entity.Entity;

import java.util.Random;

public class Fuckutil {
    public static Float customYaw = null;
    public static Float customPitch = null;
    public static boolean isRotationActivated = false;

    public static float interpolateAngle(float current, float target, float maxSpeed, float interpolationStep) {
        float normalizedCurrent = normalizeAngle(current);
        float normalizedTarget = normalizeAngle(target);

        float delta = normalizedTarget - normalizedCurrent;

        if (delta > 180)
            delta -= 360;

        if (delta < -180)
            delta += 360;

        float step = Math.min(Math.abs(delta), maxSpeed);

        return normalizeAngle(normalizedCurrent + delta * interpolationStep * step / Math.abs(delta));
    }

    public static void setRotation(Float targetYaw, Float targetPitch, Float speed) {
        if (customYaw == null)
            customYaw = IMinecraft.mc.player.getYaw() + 0.001f;

        if (customPitch == null)
            customPitch = IMinecraft.mc.player.getPitch() + 0.001f;

        isRotationActivated = true;

        float rotYaw = smoothRotation(customYaw, targetYaw, speed * 2);
        float rotPitch = smoothRotation(customPitch, targetPitch, speed);

        applyHumanLikeRotation(rotYaw, rotPitch);
    }

    public static void aimAtEntity(
            Entity target,
            float speed,
            boolean random,
            float randomSpeed,
            float baseMaxOffset,
            float jitterAmount
    ) {
        if (IMinecraft.mc.player == null) return;

        new Thread(() -> {
            float adjustedMaxOffset = baseMaxOffset * (speed / 10);

            double targetCenterX = target.getX() + getRandomOffset(adjustedMaxOffset);
            double targetCenterY = target.getY() + target.getHeight() / 2 + getRandomOffset(adjustedMaxOffset);
            double targetCenterZ = target.getZ() + getRandomOffset(adjustedMaxOffset);

            double playerEyeY = IMinecraft.mc.player.getY() + IMinecraft.mc.player.getStandingEyeHeight();

            double xDiff = targetCenterX - IMinecraft.mc.player.getX();
            double yDiff = targetCenterY - playerEyeY;
            double zDiff = targetCenterZ - IMinecraft.mc.player.getZ();
            double distance = Math.sqrt(xDiff * xDiff + zDiff * zDiff);

            float targetYaw = (float) (Math.atan2(zDiff, xDiff) * (180.0 / Math.PI)) - 90.0f;
            float targetPitch = -(float) (Math.atan2(yDiff, distance) * (180.0 / Math.PI));

            if (customYaw == null)
                customYaw = IMinecraft.mc.player.getYaw();

            if (customPitch == null)
                customPitch = IMinecraft.mc.player.getPitch();

            isRotationActivated = true;

            float rotSpeed = random ? (speed + new Random().nextFloat() * randomSpeed) : speed;

            float[] rotation = humanizeRotation(
                    customYaw,
                    customPitch,
                    targetYaw,
                    targetPitch,
                    rotSpeed,
                    jitterAmount
            );

            customYaw = rotation[0];
            customPitch = rotation[1];

            applyHumanLikeRotation(customYaw, customPitch);
            applyTimingDelay(50, 100);

        }).start();
    }

    private static float normalizeAngle(float angle) {
        float normalized = angle % 360;
        if (normalized > 180) {
            normalized -= 360;
        }
        if (normalized < -180) {
            normalized += 360;
        }
        return normalized;
    }

    private static float getRandomOffset(float maxOffset) {
        return new Random().nextFloat() * maxOffset * 2 - maxOffset;
    }

    private static float smoothRotation(float current, float target, float speed) {
        float diff = ((target - current + 540) % 360) - 180;
        float maxStep = Math.min(Math.abs(diff), speed);
        return (current + maxStep * (diff / Math.abs(diff))) % 360;
    }

    private static float addJitter(float value, float jitterAmount) {
        return value + (new Random().nextFloat() * jitterAmount - jitterAmount / 2);
    }

    private static float[] humanizeRotation(
            float currentYaw,
            float currentPitch,
            float targetYaw,
            float targetPitch,
            float speed,
            float jitterAmount
    ) {
        float newYaw = smoothRotation(currentYaw, addJitter(targetYaw, jitterAmount), speed);
        float newPitch = smoothRotation(currentPitch, addJitter(targetPitch, jitterAmount), speed);
        return new float[]{newYaw, newPitch};
    }

    private static void applyHumanLikeRotation(float yaw, float pitch) {
        customYaw = yaw + getRandomOffset(0.1f);
        customPitch = pitch + getRandomOffset(0.1f);
    }

    private static void applyTimingDelay(long minDelay, long maxDelay) {
        try {
            long delay = minDelay + (long) (Math.random() * (maxDelay - minDelay));
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
