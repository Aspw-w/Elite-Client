package com.instrumentalist.elite.hacks.features.movement.speedmode.features;

import com.instrumentalist.elite.events.features.MotionEvent;
import com.instrumentalist.elite.events.features.TickEvent;
import com.instrumentalist.elite.events.features.UpdateEvent;
import com.instrumentalist.elite.hacks.Module;
import com.instrumentalist.elite.hacks.features.movement.Speed;
import com.instrumentalist.elite.hacks.features.movement.speedmode.SpeedEvent;
import com.instrumentalist.elite.utils.IMinecraft;
import com.instrumentalist.elite.utils.move.MovementUtil;
import com.instrumentalist.elite.utils.value.BooleanValue;
import com.instrumentalist.elite.utils.value.FloatValue;

public class VanillaSpeed implements SpeedEvent {

    @Override
    public String getName() {
        return "Vanilla";
    }

    @Override
    public void onUpdate(UpdateEvent event) {
        if (IMinecraft.mc.player == null) return;

        if (Speed.vanillaAutoBHop.get() && IMinecraft.mc.player.isOnGround() && MovementUtil.isMoving())
            IMinecraft.mc.player.jump();

        MovementUtil.strafe(Speed.vanillaSpeed.get());
    }

    @Override
    public void onMotion(MotionEvent event) {
    }

    @Override
    public void onTick(TickEvent event) {
        if (IMinecraft.mc.player == null) return;

        if (Speed.vanillaAutoBHop.get() && IMinecraft.mc.player.isOnGround() && MovementUtil.isMoving())
            IMinecraft.mc.options.jumpKey.setPressed(false);
    }
}