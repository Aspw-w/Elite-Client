package com.instrumentalist.elite.hacks.features.player;

import com.instrumentalist.elite.events.features.UpdateEvent;
import com.instrumentalist.elite.hacks.Module;
import com.instrumentalist.elite.hacks.ModuleCategory;
import com.instrumentalist.elite.utils.IMinecraft;
import com.instrumentalist.elite.utils.rotation.RotationUtil;
import com.instrumentalist.elite.utils.value.FloatValue;
import org.lwjgl.glfw.GLFW;

public class SpinBot extends Module {

    public SpinBot() {
        super("Spin Bot", ModuleCategory.Player, GLFW.GLFW_KEY_UNKNOWN, false, true);
    }

    @Setting
    private final FloatValue spinSpeed = new FloatValue(
            "Spin Speed",
            30f,
            -40f,
            40f
    );

    @Setting
    private final FloatValue pitch = new FloatValue(
            "Pitch",
            90f,
            -90f,
            90f
    );

    private static float spinYaw = 0f;

    @Override
    public void onDisable() {
        if (IMinecraft.mc.player == null) return;

        spinYaw = 0f;
        RotationUtil.INSTANCE.reset();
    }

    @Override
    public void onEnable() {
    }

    @Override
    public void onUpdate(UpdateEvent event) {
        if (IMinecraft.mc.player == null) return;

        if (spinYaw >= 360f || spinYaw <= -360f)
            spinYaw = 0f;

        spinYaw += spinSpeed.get();

        RotationUtil.INSTANCE.setRotation(spinYaw, pitch.get(), 180f, true);
    }
}
