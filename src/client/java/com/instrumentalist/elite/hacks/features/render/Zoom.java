package com.instrumentalist.elite.hacks.features.render;

import com.instrumentalist.elite.events.features.KeyboardEvent;
import com.instrumentalist.elite.events.features.MouseScrollEvent;
import com.instrumentalist.elite.events.features.ReceivedPacketEvent;
import com.instrumentalist.elite.events.features.RenderHudEvent;
import com.instrumentalist.elite.hacks.Module;
import com.instrumentalist.elite.hacks.ModuleCategory;
import com.instrumentalist.elite.hacks.ModuleManager;
import com.instrumentalist.elite.utils.ChatUtil;
import com.instrumentalist.elite.utils.IMinecraft;
import com.instrumentalist.elite.utils.math.Interpolation;
import com.instrumentalist.elite.utils.value.KeyBindValue;
import net.minecraft.client.option.SimpleOption;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket;
import net.minecraft.network.packet.s2c.play.ParticleS2CPacket;
import org.lwjgl.glfw.GLFW;

public class Zoom extends Module {

    public Zoom() {
        super("Zoom", ModuleCategory.Render, GLFW.GLFW_KEY_UNKNOWN, true, true);
    }

    @Setting
    private final KeyBindValue zoomKey = new KeyBindValue("Zoom Key", GLFW.GLFW_KEY_R);

    private static boolean zooming = false;
    private static Float prevFov = null;
    private static Float zoomedFov = null;
    private static boolean zoomed = false;
    private static float zoomInFov = 20f;
    private static long lastFrameTime = System.nanoTime();

    public static boolean shouldZoom() {
        return ModuleManager.getModuleState(new Zoom()) && zooming;
    }

    public static float zoomFovHook(float basedFov) {
        if (zooming && zoomedFov == null) {
            prevFov = basedFov;
            zoomedFov = basedFov;
        }

        if (shouldZoom())
            return zoomedFov;

        return basedFov;
    }

    @Override
    public void onDisable() {
        zooming = false;
        prevFov = null;
        zoomedFov = null;
        lastFrameTime = System.nanoTime();
        zoomed = false;
        zoomInFov = 20f;
    }

    @Override
    public void onEnable() {
    }

    @Override
    public void onKey(KeyboardEvent event) {
        if (IMinecraft.mc.player == null) return;

        if (event.key == zoomKey.get() && event.action == GLFW.GLFW_PRESS && IMinecraft.mc.currentScreen == null) {
            zooming = true;
            zoomed = true;
        }

        if (event.key == zoomKey.get() && event.action == GLFW.GLFW_RELEASE)
            zooming = false;
    }

    @Override
    public void onRenderHud(RenderHudEvent event) {
        if (IMinecraft.mc.player == null) return;

        long currentTime = System.nanoTime();
        float deltaTime = (currentTime - lastFrameTime) / 1e9f;
        lastFrameTime = currentTime;

        if (zooming) {
            if (zoomedFov != null) {
                if (zoomInFov <= 3f)
                    zoomInFov = 3f;
                if (zoomInFov >= 160f)
                    zoomInFov = 160f;
                zoomedFov = Interpolation.INSTANCE.lerpWithTime(prevFov, zoomInFov, 8f, deltaTime);
                prevFov = zoomedFov;
                IMinecraft.mc.options.smoothCameraEnabled = true;
            }
        } else if (zoomed) {
            zoomedFov = null;
            prevFov = null;
            zoomed = false;
            lastFrameTime = System.nanoTime();
            zoomInFov = 20f;
            IMinecraft.mc.options.smoothCameraEnabled = false;
        }
    }

    @Override
    public void onMouseScroll(MouseScrollEvent event) {
        if (IMinecraft.mc.player == null) return;

        if (zooming && zoomedFov != null) {
            float scroll = - (float) event.vertical * 5f;
            zoomInFov += scroll;
            event.cancel();
        }
    }
}
