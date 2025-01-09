package com.instrumentalist.elite.hacks.features.movement.flymode.features;

import com.instrumentalist.elite.events.features.MotionEvent;
import com.instrumentalist.elite.events.features.TickEvent;
import com.instrumentalist.elite.events.features.UpdateEvent;
import com.instrumentalist.elite.hacks.Module;
import com.instrumentalist.elite.hacks.features.movement.Fly;
import com.instrumentalist.elite.hacks.features.movement.flymode.FlyEvent;
import com.instrumentalist.elite.utils.IMinecraft;
import com.instrumentalist.elite.utils.move.MovementUtil;
import com.instrumentalist.elite.utils.value.FloatValue;
import net.minecraft.client.util.InputUtil;

public class VanillaFly implements FlyEvent {

    @Override
    public String getName() {
        return "Vanilla";
    }

    @Override
    public void onUpdate(UpdateEvent event) {
        if (IMinecraft.mc.player == null) return;

        float yMotion = 0f;

        if (InputUtil.isKeyPressed(IMinecraft.mc.getWindow().getHandle(), InputUtil.fromTranslationKey(IMinecraft.mc.options.jumpKey.getBoundKeyTranslationKey()).getCode()))
            yMotion += Fly.vanillaVSpeed.get();

        if (InputUtil.isKeyPressed(IMinecraft.mc.getWindow().getHandle(), InputUtil.fromTranslationKey(IMinecraft.mc.options.sneakKey.getBoundKeyTranslationKey()).getCode()))
            yMotion -= Fly.vanillaVSpeed.get();

        MovementUtil.setVelocityY((double) yMotion);
        MovementUtil.strafe(Fly.vanillaHSpeed.get());
    }

    @Override
    public void onMotion(MotionEvent event) {
    }

    @Override
    public void onTick(TickEvent event) {
        if (IMinecraft.mc.player == null) return;

        IMinecraft.mc.options.sneakKey.setPressed(false);
    }
}