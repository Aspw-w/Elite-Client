package com.instrumentalist.elite.hacks.features.movement;

import com.instrumentalist.elite.events.features.UpdateEvent;
import com.instrumentalist.elite.hacks.Module;
import com.instrumentalist.elite.hacks.ModuleCategory;
import com.instrumentalist.elite.hacks.ModuleManager;
import com.instrumentalist.elite.utils.IMinecraft;
import com.instrumentalist.elite.utils.move.MovementUtil;
import com.instrumentalist.elite.utils.value.BooleanValue;
import com.instrumentalist.elite.utils.value.FloatValue;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class PerfectHorseJump extends Module {

    public PerfectHorseJump() {
        super("Perfect Horse Jump", ModuleCategory.Movement, GLFW.GLFW_KEY_UNKNOWN, false, true);
    }

    public static float modifiedHorseJump(float original) {
        if (ModuleManager.getModuleState(new PerfectHorseJump()))
            return 1f;

        return original;
    }

    @Override
    public void onDisable() {
    }

    @Override
    public void onEnable() {
    }
}
