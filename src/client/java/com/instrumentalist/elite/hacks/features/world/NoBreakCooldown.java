package com.instrumentalist.elite.hacks.features.world;

import com.instrumentalist.elite.events.features.UpdateEvent;
import com.instrumentalist.elite.hacks.Module;
import com.instrumentalist.elite.hacks.ModuleCategory;
import com.instrumentalist.elite.utils.ChatUtil;
import com.instrumentalist.elite.utils.IMinecraft;
import com.instrumentalist.elite.utils.move.MovementUtil;
import com.instrumentalist.elite.utils.value.KeyBindValue;
import org.lwjgl.glfw.GLFW;

public class NoBreakCooldown extends Module {

    public NoBreakCooldown() {
        super("No Break Cooldown", ModuleCategory.World, GLFW.GLFW_KEY_UNKNOWN, false, true);
    }

    @Override
    public void onDisable() {
    }

    @Override
    public void onEnable() {
    }
}
