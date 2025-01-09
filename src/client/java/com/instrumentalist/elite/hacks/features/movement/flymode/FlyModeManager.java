package com.instrumentalist.elite.hacks.features.movement.flymode;

import com.instrumentalist.elite.events.features.MotionEvent;
import com.instrumentalist.elite.events.features.TickEvent;
import com.instrumentalist.elite.events.features.UpdateEvent;
import com.instrumentalist.elite.hacks.features.movement.Fly;
import com.instrumentalist.elite.hacks.features.movement.flymode.features.*;
import com.instrumentalist.elite.utils.value.ListValue;

public class FlyModeManager {

    private final ListValue flyMode;
    public FlyEvent currentMode;

    public FlyModeManager(ListValue flyMode) {
        this.flyMode = flyMode;
        updateCurrentMode();
    }

    private void updateCurrentMode() {
        switch (flyMode.get().toLowerCase()) {
            case "vanilla":
                currentMode = new VanillaFly();
                break;
            case "cubecraft":
                currentMode = new CubecraftFly();
                break;
            case "miniblox":
                currentMode = new MinibloxFly();
                break;
            case "verus 1.8":
                currentMode = new Verus1_8Fly();
                break;
            case "verus jetpack":
                currentMode = new VerusJetPackFly();
                break;
            case "float":
                currentMode = new FloatFly();
                break;
            default:
                currentMode = null;
        }
    }

    public void onUpdate(UpdateEvent event) {
        if (!flyMode.get().equals(currentMode.getName())) {
            Fly.onDisableFunctions();
            updateCurrentMode();
            Fly.onEnableFunctions();
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