package com.instrumentalist.elite.hacks.features.movement;

import com.instrumentalist.elite.events.features.MotionEvent;
import com.instrumentalist.elite.events.features.TickEvent;
import com.instrumentalist.elite.events.features.UpdateEvent;
import com.instrumentalist.elite.hacks.Module;
import com.instrumentalist.elite.hacks.ModuleCategory;
import com.instrumentalist.elite.hacks.ModuleManager;
import com.instrumentalist.elite.hacks.features.exploit.Disabler;
import com.instrumentalist.elite.hacks.features.movement.speedmode.SpeedModeManager;
import com.instrumentalist.elite.hacks.features.movement.speedmode.features.*;
import com.instrumentalist.elite.utils.IMinecraft;
import com.instrumentalist.elite.utils.math.TimerUtil;
import com.instrumentalist.elite.utils.move.MovementUtil;
import com.instrumentalist.elite.utils.value.BooleanValue;
import com.instrumentalist.elite.utils.value.FloatValue;
import com.instrumentalist.elite.utils.value.ListValue;
import org.lwjgl.glfw.GLFW;

import java.util.Arrays;

public class Speed extends Module {

    public Speed() {
        super("Speed", ModuleCategory.Movement, GLFW.GLFW_KEY_UNKNOWN, false, true);
    }

    @Setting
    private static final ListValue speedMode = new ListValue(
            "Speed Mode",
            Arrays.asList("Vanilla", "Smooth Vanilla", "Hypixel Hop", "Cubecraft Hop", "Verus Hop", "Miniblox").toArray(new String[0]),
            "Vanilla"
    );

    private static final SpeedModeManager speedModeManager = new SpeedModeManager(speedMode);

    // Vanilla
    @Setting
    public static final FloatValue vanillaSpeed = new FloatValue("Speed", 1f, 0.1f, 4f, () -> speedMode.get().equalsIgnoreCase("vanilla") || speedMode.get().equalsIgnoreCase("smooth vanilla"));

    @Setting
    public static final BooleanValue vanillaAutoBHop = new BooleanValue("Auto BHop", true, () -> speedMode.get().equalsIgnoreCase("vanilla"));

    public static void onEnableFunctions() {
    }

    public static void onDisableFunctions() {
        if (speedModeManager.currentMode instanceof VanillaSpeed || speedModeManager.currentMode instanceof CubecraftHopSpeed) {
            if (IMinecraft.mc.player != null)
                MovementUtil.stopMoving();
        } else if (speedModeManager.currentMode instanceof VerusHopSpeed) {
            if (IMinecraft.mc.player != null)
                MovementUtil.stopMoving();
            TimerUtil.reset();
            VerusHopSpeed.tick = 0;
        } else if (speedModeManager.currentMode instanceof MinibloxSpeed) {
            if (IMinecraft.mc.player != null)
                MovementUtil.stopMoving();
        } else if (speedModeManager.currentMode instanceof HypixelHopSpeed) {
            HypixelHopSpeed.canLowHop = false;
        }
    }

    @Override
    public String tag() {
        return speedMode.get();
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
        speedModeManager.onUpdate(event);
    }

    @Override
    public void onMotion(MotionEvent event) {
        speedModeManager.onMotion(event);
    }

    @Override
    public void onTick(TickEvent event) {
        speedModeManager.onTick(event);
    }
}
