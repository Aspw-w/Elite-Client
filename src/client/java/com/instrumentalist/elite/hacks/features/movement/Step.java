package com.instrumentalist.elite.hacks.features.movement;

import com.instrumentalist.elite.events.features.UpdateEvent;
import com.instrumentalist.elite.hacks.Module;
import com.instrumentalist.elite.hacks.ModuleCategory;
import com.instrumentalist.elite.hacks.ModuleManager;
import com.instrumentalist.elite.hacks.features.combat.KillAura;
import com.instrumentalist.elite.hacks.features.player.Scaffold;
import com.instrumentalist.elite.utils.ChatUtil;
import com.instrumentalist.elite.utils.IMinecraft;
import com.instrumentalist.elite.utils.math.TimerUtil;
import com.instrumentalist.elite.utils.move.MovementUtil;
import com.instrumentalist.elite.utils.packet.PacketUtil;
import com.instrumentalist.elite.utils.rotation.RotationUtil;
import com.instrumentalist.elite.utils.value.BooleanValue;
import com.instrumentalist.elite.utils.value.FloatValue;
import com.instrumentalist.elite.utils.value.IntValue;
import com.instrumentalist.elite.utils.value.ListValue;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.BlockPos;
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
    private static final IntValue delay = new IntValue(
            "Delay",
            1,
            1,
            10
    );

    @Setting
    private static final BooleanValue customTimer = new BooleanValue("Custom Timer", true);

    @Setting
    private static final FloatValue timerSpeed = new FloatValue(
            "Timer Speed",
            0.6f,
            0.1f,
            10f,
            customTimer::get
    );

    private static boolean canStep = true;
    private static boolean sentBypass = false;
    private static boolean calledModifiedStep = false;
    private static int stepDelay = 0;

    public static float hookStepHeight(float original, LivingEntity entity) {
        if (entity instanceof ClientPlayerEntity && IMinecraft.mc.player != null) {
            if (ModuleManager.getModuleState(new Step()) && IMinecraft.mc.player.isOnGround() && canStep && (!disableWhenSpeed.get() || !ModuleManager.getModuleState(new Speed()))) {
                if (calledModifiedStep)
                    afterStepFunctions();

                if (mode.get().equalsIgnoreCase("hypixel")) {
                    if (!RotationUtil.INSTANCE.isRotating() && IMinecraft.mc.world.getBlockState(IMinecraft.mc.player.getBlockPos().up(3)).isAir() && IMinecraft.mc.world.getBlockState(new BlockPos(IMinecraft.mc.player.getBlockPos().up(3).getX() + 1, IMinecraft.mc.player.getBlockPos().up(3).getY(), IMinecraft.mc.player.getBlockPos().up(3).getZ())).isAir() && IMinecraft.mc.world.getBlockState(new BlockPos(IMinecraft.mc.player.getBlockPos().up(3).getX(), IMinecraft.mc.player.getBlockPos().up(3).getY(), IMinecraft.mc.player.getBlockPos().up(3).getZ() + 1)).isAir() && IMinecraft.mc.world.getBlockState(new BlockPos(IMinecraft.mc.player.getBlockPos().up(3).getX() - 1, IMinecraft.mc.player.getBlockPos().up(3).getY(), IMinecraft.mc.player.getBlockPos().up(3).getZ())).isAir() && IMinecraft.mc.world.getBlockState(new BlockPos(IMinecraft.mc.player.getBlockPos().up(3).getX(), IMinecraft.mc.player.getBlockPos().up(3).getY(), IMinecraft.mc.player.getBlockPos().up(3).getZ() - 1)).isAir())
                        return 1f;
                } else return height.get();
            }

            if (calledModifiedStep && !IMinecraft.mc.player.isOnGround())
                afterStepFunctions();
        }

        return original;
    }

    public static void steppingFunctions() {
        if (customTimer.get())
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
        if (customTimer.get())
            TimerUtil.reset();

        stepDelay = delay.get();
        canStep = false;
        sentBypass = false;
        calledModifiedStep = false;
    }

    @Override
    public void onDisable() {
        if (customTimer.get())
            TimerUtil.reset();

        canStep = true;
        stepDelay = 0;
        calledModifiedStep = false;
        sentBypass = false;
    }

    @Override
    public void onEnable() {
    }

    @Override
    public void onUpdate(UpdateEvent event) {
        if (IMinecraft.mc.player == null) return;

        if (!canStep) {
            if (stepDelay <= 0) {
                stepDelay = 0;
                canStep = true;
            } else stepDelay--;
        }
    }
}
