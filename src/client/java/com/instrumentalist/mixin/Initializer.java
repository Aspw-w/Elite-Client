package com.instrumentalist.mixin;

import com.instrumentalist.elite.utils.ChatUtil;
import imgui.ImGui;
import net.fabricmc.api.ClientModInitializer;
import xyz.breadloaf.imguimc.interfaces.Renderable;

import java.util.ArrayList;

public class Initializer implements ClientModInitializer {
    public static ArrayList<Renderable> renderstack = new ArrayList<>();

    @Override
    public void onInitializeClient() {
        ChatUtil.showLog("Initializing elite client");
    }

    public static void pushRenderable(Renderable renderable) {
        renderstack.add(renderable);
    }

    public static void pullEveryRenderable() {
        renderstack.clear();
    }

    /**
     * Check whether game keyboard inputs are being cancelled.
     */
    public static boolean shouldCancelGameKeyboardInputs() {
        return ImGui.isAnyItemActive() || ImGui.isAnyItemFocused();
    }

    public static int getDockId() {
        return ImGui.getID("imgui-mc dockspace");
    }
}
