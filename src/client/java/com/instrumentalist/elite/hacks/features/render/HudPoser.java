package com.instrumentalist.elite.hacks.features.render;

import com.instrumentalist.elite.hacks.Module;
import com.instrumentalist.elite.hacks.ModuleCategory;
import com.instrumentalist.elite.utils.value.BooleanValue;
import com.instrumentalist.elite.utils.value.FloatValue;
import com.instrumentalist.elite.utils.value.ListValue;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.image.renderable.RenderContext;
import java.util.Arrays;

public class HudPoser extends Module {

    public HudPoser() {
        super("Hud Poser", ModuleCategory.Render, GLFW.GLFW_KEY_UNKNOWN, true, true);
    }

    public static final ListValue statusEffects = new ListValue("Status Effects", Arrays.asList("Down", "Hide").toArray(new String[0]), "Down");

    @Override
    public void onEnable() {
    }

    @Override
    public void onDisable() {
    }
}
