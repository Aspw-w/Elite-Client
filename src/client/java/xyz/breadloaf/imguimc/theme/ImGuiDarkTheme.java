package xyz.breadloaf.imguimc.theme;

import imgui.ImGui;
import xyz.breadloaf.imguimc.interfaces.Theme;

public class ImGuiDarkTheme implements Theme {
    @Override
    public void preRender() {
        ImGui.styleColorsDark();
    }

    @Override
    public void postRender() {

    }
}
