package com.instrumentalist.elite.hacks.features.render;

import com.instrumentalist.elite.hacks.Module;
import com.instrumentalist.elite.hacks.ModuleCategory;
import com.instrumentalist.elite.hacks.ModuleManager;
import org.lwjgl.glfw.GLFW;

public class Scoreboard extends Module {

    public Scoreboard() {
        super("Scoreboard", ModuleCategory.Render, GLFW.GLFW_KEY_UNKNOWN, false, true);
    }

    @Override
    public void onEnable() {
    }

    @Override
    public void onDisable() {
    }

    public static boolean isEnabled() {
        return ModuleManager.getModuleState(new Scoreboard());
    }

    public static String modifyScoreboardTitle(String title) {
        if (title.matches(".*\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}.*") || title.matches(".*\\bhypixel\\.net\\b.*")) {
            return title.replaceAll("\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}|\\bhypixel\\.net\\b", "eliteclient.club");
        }
        return title;
    }
}