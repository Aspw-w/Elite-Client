package com.instrumentalist.elite.hacks.features.render;

import com.instrumentalist.elite.hacks.Module;
import com.instrumentalist.elite.hacks.ModuleCategory;
import com.instrumentalist.elite.utils.value.ListValue;
import net.minecraft.client.MinecraftClient;
import org.lwjgl.glfw.GLFW;

import java.lang.reflect.Field;

public class TimeChanger extends Module {

    @Setting
    private final ListValue timeSetting = new ListValue(
            "Time of Day",
            new String[]{"Day", "Night", "Midnight", "Sunrise"},
            "Day"
    );

    public TimeChanger() {
        super("Time Changer", ModuleCategory.Render, GLFW.GLFW_KEY_UNKNOWN, false, false);
    }

    @Override
    public void onEnable() {
        setWorldTime(getTimeValue(timeSetting.get()));
    }

    @Override
    public void onDisable() {
    }

    @Override
    public void onSettingChange() {
        setWorldTime(getTimeValue(timeSetting.get()));
    }

    private long getTimeValue(String time) {
        switch (time) {
            case "Day":
                return 1000;
            case "Night":
                return 13000;
            case "Midnight":
                return 18000;
            case "Sunrise":
                return 0;
            default:
                return 1000;
        }
    }

    private void setWorldTime(long time) {
        if (MinecraftClient.getInstance().world != null) {
            try {
                Field timeOfDayField = MinecraftClient.getInstance().world.getLevelProperties().getClass().getDeclaredField("timeOfDay");
                timeOfDayField.setAccessible(true);
                timeOfDayField.setLong(MinecraftClient.getInstance().world.getLevelProperties(), time);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}