package xyz.breadloaf.imguimc.imgui;

import com.instrumentalist.elite.utils.IMinecraft;
import com.instrumentalist.mixin.Initializer;
import imgui.*;
import imgui.flag.*;
import imgui.gl3.ImGuiImplGl3;
import imgui.glfw.ImGuiImplGlfw;
import net.minecraft.client.util.Window;
import xyz.breadloaf.imguimc.interfaces.Renderable;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiConfigFlags;

import static org.lwjgl.glfw.GLFW.glfwGetCurrentContext;
import static org.lwjgl.glfw.GLFW.glfwMakeContextCurrent;

public class ImguiLoader {
    private static ImGuiImplGlfw imGuiGlfw = null;

    private static ImGuiImplGl3 imGuiGl3 = null;

    private static long windowHandle;

    public static void onGlfwInit(long handle) {
        initializeImGui(handle);
        imGuiGlfw = new ImGuiImplGlfw();
        imGuiGlfw.init(handle,true);
        imGuiGl3 = new ImGuiImplGl3();
        imGuiGl3.init();
        windowHandle = handle;
    }

    public static void onFrameRender() {
        imGuiGlfw.newFrame();
        ImGui.newFrame();

        setupDocking();

        //user render code

        for (Renderable renderable: Initializer.renderstack) {
            renderable.getTheme().preRender();
            renderable.render();
            renderable.getTheme().postRender();
        }

        //end of user code

        finishDocking();

        ImGui.render();
        endFrame(windowHandle);
    }

    private static void setupDocking() {
        int windowFlags = ImGuiWindowFlags.NoDocking;

        Window window = IMinecraft.mc.getWindow();

        ImGui.setNextWindowPos(window.getX(), window.getY(), ImGuiCond.Always);
        ImGui.setNextWindowSize(window.getWidth(), window.getHeight());
        windowFlags |= ImGuiWindowFlags.NoTitleBar | ImGuiWindowFlags.NoCollapse | ImGuiWindowFlags.NoResize | ImGuiWindowFlags.NoMove |
                ImGuiWindowFlags.NoBringToFrontOnFocus | ImGuiWindowFlags.NoNavFocus | ImGuiWindowFlags.NoBackground;

        ImGui.begin("imgui-mc docking host window", windowFlags);

        ImGui.dockSpace(Initializer.getDockId(), 0, 0, ImGuiDockNodeFlags.PassthruCentralNode |
                ImGuiDockNodeFlags.NoCentralNode | ImGuiDockNodeFlags.NoDockingInCentralNode);
    }

    private static void finishDocking() {
        ImGui.end();
    }

    private static void initializeImGui(long glHandle) {
        ImGui.createContext();

        final ImGuiIO io = ImGui.getIO();

        io.setIniFilename(null);                               // We don't want to save .ini file
        io.addConfigFlags(ImGuiConfigFlags.NavEnableKeyboard); // Enable Keyboard Controls
        io.addConfigFlags(ImGuiConfigFlags.DockingEnable);     // Enable Docking
        io.addConfigFlags(ImGuiConfigFlags.ViewportsEnable);   // Enable Multi-Viewport / Platform Windows
        io.setConfigViewportsNoTaskBarIcon(true);

        final ImFontAtlas fontAtlas = io.getFonts();
        final ImFontConfig fontConfig = new ImFontConfig(); // Natively allocated object, should be explicitly destroyed

        fontConfig.setGlyphRanges(fontAtlas.getGlyphRangesCyrillic());

        fontAtlas.addFontDefault();

        fontConfig.setMergeMode(true); // When enabled, all fonts added with this config would be merged with the previously added font
        fontConfig.setPixelSnapH(true);

        fontConfig.destroy();

        if (io.hasConfigFlags(ImGuiConfigFlags.ViewportsEnable)) {
            final ImGuiStyle style = ImGui.getStyle();
            style.setWindowRounding(0.0f);
            style.setColor(ImGuiCol.WindowBg, ImGui.getColorU32(ImGuiCol.WindowBg, 1));
        }
    }

    private static void endFrame(long windowPtr) {
        // After Dear ImGui prepared a draw data, we use it in the LWJGL3 renderer.
        // At that moment ImGui will be rendered to the current OpenGL context.
        imGuiGl3.renderDrawData(ImGui.getDrawData());

        if (ImGui.getIO().hasConfigFlags(ImGuiConfigFlags.ViewportsEnable)) {
            final long backupWindowPtr = glfwGetCurrentContext();
            ImGui.updatePlatformWindows();
            ImGui.renderPlatformWindowsDefault();
            glfwMakeContextCurrent(backupWindowPtr);
        }

        //glfwSwapBuffers(windowPtr);
        //glfwPollEvents();
    }
}
