package com.instrumentalist.elite.hacks.features.render;

import com.instrumentalist.elite.events.features.ReceivedPacketEvent;
import com.instrumentalist.elite.events.features.UpdateEvent;
import com.instrumentalist.elite.hacks.Module;
import com.instrumentalist.elite.hacks.ModuleCategory;
import com.instrumentalist.elite.utils.IMinecraft;
import com.instrumentalist.elite.utils.value.ListValue;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.WorldTimeUpdateS2CPacket;
import org.lwjgl.glfw.GLFW;

import java.lang.reflect.Field;

public class TimeChanger extends Module {

    private final ListValue time = new ListValue(
            "Time of Day",
            new String[]{"Day", "Night", "Midnight", "Sunrise"},
            "Day"
    );

    public TimeChanger() {
        super("Time Changer", ModuleCategory.Render, GLFW.GLFW_KEY_UNKNOWN, false, true);
    }

    @Override
    public void onEnable() {
    }

    @Override
    public void onDisable() {
    }

    @Override
    public void onUpdate(UpdateEvent event) {
        setWorldTime();
    }

    @Override
    public void onReceivedPacket(ReceivedPacketEvent event) {
        if (IMinecraft.mc.world == null) return;

        Packet<?> packet = event.packet;

        if (packet instanceof WorldTimeUpdateS2CPacket) {
            event.cancel();
            setWorldTime();
        }
    }

    private void setWorldTime() {
        if (MinecraftClient.getInstance().world != null) {
            long godTime;

            godTime = switch (time.get().toLowerCase()) {
                case "day" -> 1000;
                case "night" -> 13000;
                case "midnight" -> 18000;
                case "sunrise" -> 0;
                default -> -1;
            };

            if (godTime == -1) return;

            try {
                Field timeOfDayField = MinecraftClient.getInstance().world.getLevelProperties().getClass().getDeclaredField("timeOfDay");
                timeOfDayField.setAccessible(true);
                timeOfDayField.setLong(MinecraftClient.getInstance().world.getLevelProperties(), godTime);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}