package com.instrumentalist.elite.hacks.features.render;

import com.instrumentalist.elite.hacks.Module;
import com.instrumentalist.elite.hacks.ModuleCategory;
import com.instrumentalist.elite.utils.IMinecraft;
import org.lwjgl.glfw.GLFW;

public class TimeChanger extends Module {

    // Constructor to set the module's name, category, keybind, etc.
    public TimeChanger() {
        super("Time Changer", ModuleCategory.World, GLFW.GLFW_KEY_UNKNOWN, false, true);
    }

    @Override
    public void onEnable() {
        // Set the time to day when the module is enabled
        setTime(0);  // 0 is the Minecraft time for day (1000 ticks).
    }

    @Override
    public void onDisable() {
        // Optionally, you could set the time back to default when the module is disabled
        setTime(13000);  // 13000 is the Minecraft time for night (13000 ticks).
    }

    // Utility method to set time
    private void setTime(long time) {
        if (IMinecraft.mc.world != null) {
            IMinecraft.mc.world.setTime(time);  // This sets the world's time in ticks
        }
    }
}
