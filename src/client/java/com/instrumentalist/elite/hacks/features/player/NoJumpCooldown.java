package com.instrumentalist.elite.hacks.features.player;

import com.instrumentalist.elite.events.features.UpdateEvent;
import com.instrumentalist.elite.hacks.Module;
import com.instrumentalist.elite.hacks.ModuleCategory;
import com.instrumentalist.elite.utils.IMinecraft;
import org.lwjgl.glfw.GLFW;

public class NoJumpCooldown extends Module {

    public NoJumpCooldown() {
        super("No Jump Cooldown", ModuleCategory.Player, GLFW.GLFW_KEY_UNKNOWN, false, true);
    }

    @Override
    public void onDisable() {
    }

    @Override
    public void onEnable() {
    }

    @Override
    public void onUpdate(UpdateEvent event) {
        if (IMinecraft.mc.player == null || !IMinecraft.mc.player.isOnGround()) return;

        IMinecraft.mc.player.jumpingCooldown = 0;
    }
}
