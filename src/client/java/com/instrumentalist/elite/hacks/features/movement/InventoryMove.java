package com.instrumentalist.elite.hacks.features.movement;

import com.instrumentalist.elite.hacks.Module;
import com.instrumentalist.elite.hacks.ModuleCategory;
import com.instrumentalist.elite.hacks.ModuleManager;
import com.instrumentalist.elite.utils.ChatUtil;
import com.instrumentalist.elite.utils.IMinecraft;
import com.instrumentalist.elite.utils.move.MovementUtil;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.option.KeyBinding;
import org.lwjgl.glfw.GLFW;
import xyz.breadloaf.imguimc.customwindow.ModuleRenderable;
import xyz.breadloaf.imguimc.screen.EmptyScreen;

public class InventoryMove extends Module {

    public InventoryMove() {
        super("Inventory Move", ModuleCategory.Movement, GLFW.GLFW_KEY_UNKNOWN, false, true);
    }

    public static void moveFreely() {
        if (!ModuleRenderable.isCommandTab && (ModuleManager.getModuleState(new InventoryMove()) && IMinecraft.mc.currentScreen != null && !(IMinecraft.mc.currentScreen instanceof ChatScreen) || IMinecraft.mc.currentScreen instanceof EmptyScreen)) {
            KeyBinding.updatePressedStates();
            KeyBinding sneakKey = IMinecraft.mc.options.sneakKey;
            sneakKey.setPressed(false);
        }
    }

    @Override
    public void onDisable() {
    }

    @Override
    public void onEnable() {
    }
}
