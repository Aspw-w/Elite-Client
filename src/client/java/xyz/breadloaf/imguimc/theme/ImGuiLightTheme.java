package xyz.breadloaf.imguimc.theme;

import imgui.ImGui;
import xyz.breadloaf.imguimc.interfaces.Theme;

public class ImGuiLightTheme implements Theme {
    @Override
    public void preRender() {
        ImGui.styleColorsLight();
    }

    @Override
    public void postRender() {

    }
}
