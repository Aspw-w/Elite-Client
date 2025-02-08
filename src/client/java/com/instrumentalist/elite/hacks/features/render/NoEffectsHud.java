package com.instrumentalist.elite.hacks.features.render;

import com.instrumentalist.elite.hacks.Module;
import com.instrumentalist.elite.hacks.ModuleCategory;
import com.instrumentalist.elite.hacks.ModuleManager;
import org.lwjgl.glfw.GLFW;

public class NoEffectsHud extends Module {

    public NoEffectsHud() {
        super("No Effects Hud", ModuleCategory.Render, GLFW.GLFW_KEY_UNKNOWN, false, true);
    }

    @Override
    public void onEnable() {
        // Placeholder, add logic for when the module is enabled if necessary
    }

    @Override
    public void onDisable() {
        // Placeholder, add logic for when the module is disabled if necessary
    }

    // Utility method to get module state
    public static boolean isEnabled() {
        return ModuleManager.getModuleState(new NoEffectsHud());
    }
}
