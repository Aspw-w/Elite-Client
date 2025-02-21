package com.instrumentalist.elite.hacks.features.movement.flymode.features;

import com.instrumentalist.elite.events.features.MotionEvent;
import com.instrumentalist.elite.events.features.TickEvent;
import com.instrumentalist.elite.events.features.UpdateEvent;
import com.instrumentalist.elite.hacks.features.movement.flymode.FlyEvent;
import com.instrumentalist.elite.utils.IMinecraft;
import com.instrumentalist.elite.utils.math.TimerUtil;
import com.instrumentalist.elite.utils.move.MovementUtil;

public class HypixelPredictionFly implements FlyEvent {

    @Override
    public String getName() {
        return "Hypixel Prediction";
    }

    public static int tick = 0;
    public static int awa = 0;
    public static int man = 0;

    @Override
    public void onUpdate(UpdateEvent event) {
        if (IMinecraft.mc.player == null) return;

        tick++;

        if (tick >= 2) {
            if (IMinecraft.mc.player.isOnGround() && MovementUtil.isMoving())
                IMinecraft.mc.player.jump();
            else event.cancel();
            TimerUtil.timerSpeed = 1.1f;
            tick = 0;
        } else {
            if (!IMinecraft.mc.player.isOnGround() && MovementUtil.isMoving())
                MovementUtil.smoothStrafe(0.6f);
            TimerUtil.timerSpeed = 0.6f;
        }
    }

    @Override
    public void onMotion(MotionEvent event) {
    }

    @Override
    public void onTick(TickEvent event) {
        if (IMinecraft.mc.player == null) return;

        if (IMinecraft.mc.player.isOnGround() && MovementUtil.isMoving())
            IMinecraft.mc.options.jumpKey.setPressed(false);
    }
}