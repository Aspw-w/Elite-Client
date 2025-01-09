package xyz.breadloaf.imguimc.theme;

import imgui.ImGui;
import xyz.breadloaf.imguimc.interfaces.Theme;

public class ImGuiClassicTheme implements Theme {
    @Override
    public void preRender() {
        ImGui.styleColorsClassic();
    }

    @Override
    public void postRender() {

    }
}
