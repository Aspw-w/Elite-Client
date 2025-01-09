package xyz.breadloaf.imguimc.debug;

import imgui.ImGui;
import xyz.breadloaf.imguimc.interfaces.Renderable;
import xyz.breadloaf.imguimc.interfaces.Theme;
import xyz.breadloaf.imguimc.theme.ImGuiDarkTheme;

public class DebugRenderable implements Renderable {

    public static boolean showAboutWindow = false;
    public static boolean showDemoWindow = false;
    public static boolean showMetricsWindow = false;
    public static boolean showUserGuide = false;

    @Override
    public String getName() {
        return "Debug Renderable";
    }

    @Override
    public Theme getTheme() {
        return new ImGuiDarkTheme();
    }

    @Override
    public void render() {
        ImGui.begin("imgui controller");

        if (ImGui.checkbox("Show about window", showAboutWindow))
            showAboutWindow = !showAboutWindow;
        if (ImGui.checkbox("Show demo window", showDemoWindow))
            showDemoWindow = !showDemoWindow;
        if (ImGui.checkbox("Show metrics window", showMetricsWindow))
            showMetricsWindow = !showMetricsWindow;
        if (ImGui.checkbox("Show user guide", showUserGuide))
            showUserGuide = !showUserGuide;

        ImGui.end();

        if (showAboutWindow)
            ImGui.showAboutWindow();
        if (showDemoWindow)
            ImGui.showDemoWindow();
        if (showMetricsWindow)
            ImGui.showMetricsWindow();
        if (showUserGuide)
            ImGui.showUserGuide();
    }
}
