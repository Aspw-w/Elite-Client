package com.instrumentalist.elite.hacks.features.movement.speedmode;

import com.instrumentalist.elite.events.features.MotionEvent;
import com.instrumentalist.elite.events.features.TickEvent;
import com.instrumentalist.elite.events.features.UpdateEvent;
import com.instrumentalist.elite.hacks.features.movement.Speed;
import com.instrumentalist.elite.hacks.features.movement.speedmode.features.*;
import com.instrumentalist.elite.utils.value.ListValue;

public class SpeedModeManager {

    private final ListValue speedMode;
    public SpeedEvent currentMode;

    public SpeedModeManager(ListValue speedMode) {
        this.speedMode = speedMode;
        updateCurrentMode();
    }

    private void updateCurrentMode() {
        switch (speedMode.get().toLowerCase()) {
            case "vanilla":
                currentMode = new VanillaSpeed();
                break;
            case "smooth vanilla":
                currentMode = new SmoothVanillaSpeed();
                break;
            case "hypixel hop":
                currentMode = new HypixelHopSpeed();
                break;
            case "cubecraft hop":
                currentMode = new CubecraftHopSpeed();
                break;
            case "verus hop":
                currentMode = new VerusHopSpeed();
                break;
            case "miniblox":
                currentMode = new MinibloxSpeed();
                break;
            default:
                currentMode = null;
        }
    }

    public void onUpdate(UpdateEvent event) {
        if (!speedMode.get().equals(currentMode.getName())) {
            Speed.onDisableFunctions();
            updateCurrentMode();
            Speed.onEnableFunctions();
        }

        if (currentMode != null)
            currentMode.onUpdate(event);
    }

    public void onMotion(MotionEvent event) {
        if (currentMode != null)
            currentMode.onMotion(event);
    }

    public void onTick(TickEvent event) {
        if (currentMode != null)
            currentMode.onTick(event);
    }
}