package com.instrumentalist.elite.hacks.features.movement;

import com.instrumentalist.elite.hacks.Module;
import com.instrumentalist.elite.hacks.ModuleCategory;
import com.instrumentalist.elite.hacks.ModuleManager;
import com.instrumentalist.elite.utils.ChatUtil;
import com.instrumentalist.elite.utils.IMinecraft;
import com.instrumentalist.elite.utils.math.TimerUtil;
import com.instrumentalist.elite.utils.move.MovementUtil;
import com.instrumentalist.elite.utils.packet.PacketUtil;
import com.instrumentalist.elite.utils.value.BooleanValue;
import com.instrumentalist.elite.utils.value.FloatValue;
import com.instrumentalist.elite.utils.value.ListValue;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;
import xyz.breadloaf.imguimc.customwindow.ModuleRenderable;
import xyz.breadloaf.imguimc.screen.EmptyScreen;

import java.util.ArrayList;
import java.util.Arrays;

public class Step extends Module {

    public Step() {
        super("Step", ModuleCategory.Movement, GLFW.GLFW_KEY_UNKNOWN, false, true);
    }

    @Setting
    private static final ListValue mode = new ListValue(
            "Mode",
            Arrays.asList("Vanilla", "NCP", "Hypixel").toArray(new String[0]),
            "Vanilla"
    );

    @Setting
    private static final BooleanValue disableWhenSpeed = new BooleanValue("Disable When Speed", true);

    @Setting
    private static final FloatValue height = new FloatValue(
            "Height",
            2f,
            1f,
            10f,
            () -> !mode.get().equalsIgnoreCase("hypixel")
    );

    @Setting
    private static final BooleanValue customTimer = new BooleanValue("Custom Timer", true, () -> !mode.get().equalsIgnoreCase("hypixel"));

    @Setting
    private static final FloatValue timerSpeed = new FloatValue(
            "Timer Speed",
            0.6f,
            0.1f,
            10f,
            () -> customTimer.get() && !mode.get().equalsIgnoreCase("hypixel")
    );

    private static Vec3d oldVelocity = null;
    private static boolean wasSprinting = false;
    private static boolean sentBypass = false;
    private static boolean calledModifiedStep = false;

    public static float hookStepHeight(float original, LivingEntity entity) {
        if (ModuleManager.getModuleState(new Step()) && entity instanceof ClientPlayerEntity && IMinecraft.mc.player != null && (!disableWhenSpeed.get() || !ModuleManager.getModuleState(new Speed()))) {
            if (IMinecraft.mc.player.isOnGround() && IMinecraft.mc.player.horizontalCollision && MovementUtil.isMoving())
                steppingFunctions();
            else if (calledModifiedStep)
                afterStepFunctions();

            if (calledModifiedStep)
                return mode.get().equalsIgnoreCase("hypixel") ? 1f : height.get();
        }

        if (entity instanceof ClientPlayerEntity && !calledModifiedStep) {
            oldVelocity = IMinecraft.mc.player.getVelocity();
            wasSprinting = IMinecraft.mc.player.isSprinting();
        }

        return original;
    }

    private static void steppingFunctions() {
        if (wasSprinting)
            IMinecraft.mc.player.setSprinting(true);

        if (mode.get().equalsIgnoreCase("hypixel"))
            TimerUtil.timerSpeed = 0.25f;
        else if (customTimer.get())
            TimerUtil.timerSpeed = timerSpeed.get();

        if (!sentBypass) {
            switch (mode.get().toLowerCase()) {
                case "ncp":
                    PacketUtil.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(IMinecraft.mc.player.getPos().x, IMinecraft.mc.player.getPos().y + 0.41999998688698, IMinecraft.mc.player.getPos().z, false, IMinecraft.mc.player.horizontalCollision));
                    PacketUtil.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(IMinecraft.mc.player.getPos().x, IMinecraft.mc.player.getPos().y + 0.7531999805212, IMinecraft.mc.player.getPos().z, false, IMinecraft.mc.player.horizontalCollision));
                    break;

                case "hypixel":
                    ArrayList<Double> stepMotion = new ArrayList<>(Arrays.asList(.41999998688698, .7531999805212, 1.001335997911214, 1.16610926093821, 1.24918707874468, 1.093955074228084));
                    for (Double motion : stepMotion) {
                        PacketUtil.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(IMinecraft.mc.player.getPos().x, IMinecraft.mc.player.getPos().y + motion, IMinecraft.mc.player.getPos().z, false, IMinecraft.mc.player.horizontalCollision));
                    }
                    break;
            }
            sentBypass = true;
        }

        calledModifiedStep = true;
    }

    private static void afterStepFunctions() {
        if (oldVelocity != null) {
            if (!mode.get().equalsIgnoreCase("hypixel")) {
                IMinecraft.mc.player.getVelocity().x = oldVelocity.x;
                IMinecraft.mc.player.getVelocity().z = oldVelocity.z;
            }
            oldVelocity = null;
        }

        if (customTimer.get() || mode.get().equalsIgnoreCase("hypixel"))
            TimerUtil.reset();

        sentBypass = false;
        calledModifiedStep = false;
    }

    @Override
    public void onDisable() {
        wasSprinting = false;
        calledModifiedStep = false;
        oldVelocity = null;
        sentBypass = false;
    }

    @Override
    public void onEnable() {
    }
}
