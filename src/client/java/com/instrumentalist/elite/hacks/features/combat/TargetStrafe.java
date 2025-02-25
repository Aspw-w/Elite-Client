package com.instrumentalist.elite.hacks.features.combat;

import com.instrumentalist.elite.Client;
import com.instrumentalist.elite.events.features.*;
import com.instrumentalist.elite.hacks.Module;
import com.instrumentalist.elite.hacks.ModuleCategory;
import com.instrumentalist.elite.hacks.ModuleManager;
import com.instrumentalist.elite.hacks.features.movement.Fly;
import com.instrumentalist.elite.hacks.features.movement.Speed;
import com.instrumentalist.elite.utils.IMinecraft;
import com.instrumentalist.elite.utils.math.TargetUtil;
import com.instrumentalist.elite.utils.move.MovementUtil;
import com.instrumentalist.elite.utils.packet.PacketUtil;
import com.instrumentalist.elite.utils.rotation.RotationUtil;
import com.instrumentalist.elite.utils.value.BooleanValue;
import com.instrumentalist.elite.utils.value.FloatValue;
import com.instrumentalist.elite.utils.value.IntValue;
import com.instrumentalist.elite.utils.value.ListValue;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.util.Hand;
import org.lwjgl.glfw.GLFW;

import java.util.*;

public class TargetStrafe extends Module {

    public TargetStrafe() {
        super("Target Strafe", ModuleCategory.Combat, GLFW.GLFW_KEY_UNKNOWN, false, true);
    }

    @Setting
    public static FloatValue distance = new FloatValue(
            "Distance",
            1.5f,
            0f,
            8f
    );

    public static int direction = 1;

    public static boolean targetStrafeHook() {
        return ModuleManager.getModuleState(new TargetStrafe()) && shouldDoStrafe();
    }

    private static boolean shouldDoStrafe() {
        if (IMinecraft.mc.player == null || !ModuleManager.getModuleState(new KillAura()) || KillAura.closestEntity == null) {
            direction = 1;
            return false;
        }

        return (ModuleManager.getModuleState(new Fly()) || ModuleManager.getModuleState(new Speed())) && !InputUtil.isKeyPressed(IMinecraft.mc.getWindow().getHandle(), InputUtil.fromTranslationKey(IMinecraft.mc.options.leftKey.getBoundKeyTranslationKey()).getCode()) && !InputUtil.isKeyPressed(IMinecraft.mc.getWindow().getHandle(), InputUtil.fromTranslationKey(IMinecraft.mc.options.rightKey.getBoundKeyTranslationKey()).getCode()) && !InputUtil.isKeyPressed(IMinecraft.mc.getWindow().getHandle(), InputUtil.fromTranslationKey(IMinecraft.mc.options.backKey.getBoundKeyTranslationKey()).getCode());
    }

    @Override
    public void onDisable() {
        direction = 1;
    }

    @Override
    public void onEnable() {
    }

    @Override
    public void onWorld(WorldEvent event) {
        direction = 1;
    }

    @Override
    public void onMotion(MotionEvent event) {
        if (!shouldDoStrafe()) return;

        if (IMinecraft.mc.player.horizontalCollision)
            direction = -direction;
    }
}