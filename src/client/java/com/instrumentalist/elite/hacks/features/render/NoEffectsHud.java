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
    }

    @Override
    public void onDisable() {
    }

    public static boolean isEnabled() {
        return ModuleManager.getModuleState(new NoEffectsHud());
    }
}
