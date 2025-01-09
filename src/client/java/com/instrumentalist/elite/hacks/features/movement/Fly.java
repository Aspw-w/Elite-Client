package com.instrumentalist.elite.hacks.features.movement;

import com.instrumentalist.elite.events.features.MotionEvent;
import com.instrumentalist.elite.events.features.TickEvent;
import com.instrumentalist.elite.events.features.UpdateEvent;
import com.instrumentalist.elite.hacks.Module;
import com.instrumentalist.elite.hacks.ModuleCategory;
import com.instrumentalist.elite.hacks.features.movement.flymode.FlyModeManager;
import com.instrumentalist.elite.hacks.features.movement.flymode.features.*;
import com.instrumentalist.elite.utils.ChatUtil;
import com.instrumentalist.elite.utils.IMinecraft;
import com.instrumentalist.elite.utils.math.TimerUtil;
import com.instrumentalist.elite.utils.move.MovementUtil;
import com.instrumentalist.elite.utils.packet.BlinkUtil;
import com.instrumentalist.elite.utils.rotation.RotationUtil;
import com.instrumentalist.elite.utils.value.BooleanValue;
import com.instrumentalist.elite.utils.value.FloatValue;
import com.instrumentalist.elite.utils.value.ListValue;
import org.lwjgl.glfw.GLFW;

import java.util.Arrays;

public class Fly extends Module {

    public Fly() {
        super("Fly", ModuleCategory.Movement, GLFW.GLFW_KEY_UNKNOWN, false, true);
    }

    @Setting
    private static final ListValue flyMode = new ListValue(
            "Fly Mode",
            Arrays.asList("Vanilla", "Cubecraft", "Miniblox", "Verus 1.8", "Verus JetPack", "Float").toArray(new String[0]),
            "Vanilla"
    );

    private static final FlyModeManager flyModeManager = new FlyModeManager(flyMode);

    // Vanilla
    @Setting
    public static final FloatValue vanillaHSpeed = new FloatValue("Horizontal Speed", 2f, 0.1f, 4f, () -> flyMode.get().equalsIgnoreCase("vanilla"));

    @Setting
    public static final FloatValue vanillaVSpeed = new FloatValue("Vertical Speed", 1.2f, 0.1f, 4f, () -> flyMode.get().equalsIgnoreCase("vanilla"));

    // Verus JetPack
    @Setting
    public static final BooleanValue verusJetPackJumpKeyOnly = new BooleanValue("Jump Key Only", false, () -> flyMode.get().equalsIgnoreCase("verus jetpack"));

    public static void onEnableFunctions() {
        if (flyModeManager.currentMode instanceof Verus1_8Fly) {
            if (IMinecraft.mc.player != null) {
                Verus1_8Fly.oldSelectedSlotStack = IMinecraft.mc.player.getInventory().getStack(8);
                Verus1_8Fly.verusRotating = true;
            }
        } else if (flyModeManager.currentMode instanceof MinibloxFly) {
            if (IMinecraft.mc.player != null)
                BlinkUtil.INSTANCE.doBlink();
        }
    }

    public static void onDisableFunctions() {
        if (flyModeManager.currentMode instanceof VanillaFly) {
            if (IMinecraft.mc.player != null) {
                MovementUtil.stopMoving();
                MovementUtil.setVelocityY(0.0);
            }
        } else if (flyModeManager.currentMode instanceof MinibloxFly) {
            if (IMinecraft.mc.player != null) {
                BlinkUtil.INSTANCE.sync(false, false);
                BlinkUtil.INSTANCE.stopBlink();
                MovementUtil.stopMoving();
                MovementUtil.setVelocityY(0.0);
            }
        } else if (flyModeManager.currentMode instanceof Verus1_8Fly) {
            if (IMinecraft.mc.player != null)
                IMinecraft.mc.player.getInventory().setStack(8, Verus1_8Fly.oldSelectedSlotStack);
            Verus1_8Fly.verusRotating = false;
            RotationUtil.INSTANCE.reset();
        } else if (flyModeManager.currentMode instanceof CubecraftFly) {
            CubecraftFly.blinkTimer.reset();
            BlinkUtil.INSTANCE.sync(true, true);
            BlinkUtil.INSTANCE.stopBlink();
        }
    }

    @Override
    public String tag() {
        return flyMode.get();
    }

    @Override
    public void onDisable() {
        onDisableFunctions();
    }

    @Override
    public void onEnable() {
        onEnableFunctions();
    }

    @Override
    public void onUpdate(UpdateEvent event) {
        flyModeManager.onUpdate(event);
    }

    @Override
    public void onMotion(MotionEvent event) {
        flyModeManager.onMotion(event);
    }

    @Override
    public void onTick(TickEvent event) {
        flyModeManager.onTick(event);
    }
}
