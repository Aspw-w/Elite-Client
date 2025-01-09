package com.instrumentalist.elite.hacks.features.movement.speedmode.features;

import com.instrumentalist.elite.events.features.MotionEvent;
import com.instrumentalist.elite.events.features.TickEvent;
import com.instrumentalist.elite.events.features.UpdateEvent;
import com.instrumentalist.elite.hacks.features.movement.Speed;
import com.instrumentalist.elite.hacks.features.movement.speedmode.SpeedEvent;
import com.instrumentalist.elite.utils.IMinecraft;
import com.instrumentalist.elite.utils.math.TimerUtil;
import com.instrumentalist.elite.utils.move.MovementUtil;

public class MinibloxSpeed implements SpeedEvent {

    @Override
    public String getName() {
        return "Miniblox";
    }

    @Override
    public void onUpdate(UpdateEvent event) {
        if (IMinecraft.mc.player == null) return;

        if (IMinecraft.mc.player.isOnGround()) {
            MovementUtil.strafe(0.36f);
            if (MovementUtil.isMoving())
                IMinecraft.mc.player.jump();
        } else {
            switch (MovementUtil.fallTicks) {
                case 3:
                    MovementUtil.setVelocityY(-0.2);
                    break;

                case 5:
                    MovementUtil.strafe(0.7f);
                    MovementUtil.setVelocityY(0.3);
                    break;

                case 10:
                    MovementUtil.strafe(0.8f);
                    MovementUtil.setVelocityY(0.2);
                    break;

                case 18:
                    MovementUtil.strafe(0.6f);
                    MovementUtil.setVelocityY(0.2);
                    break;

                default:
                    MovementUtil.strafe(0.3f);
            }
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