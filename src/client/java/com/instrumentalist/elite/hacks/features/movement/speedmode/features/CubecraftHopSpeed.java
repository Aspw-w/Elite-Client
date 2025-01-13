package com.instrumentalist.elite.hacks.features.movement.speedmode.features;

import com.instrumentalist.elite.events.features.MotionEvent;
import com.instrumentalist.elite.events.features.TickEvent;
import com.instrumentalist.elite.events.features.UpdateEvent;
import com.instrumentalist.elite.hacks.ModuleManager;
import com.instrumentalist.elite.hacks.features.movement.Fly;
import com.instrumentalist.elite.hacks.features.movement.speedmode.SpeedEvent;
import com.instrumentalist.elite.utils.IMinecraft;
import com.instrumentalist.elite.utils.move.MovementUtil;

public class CubecraftHopSpeed implements SpeedEvent {

    @Override
    public String getName() {
        return "Cubecraft Hop";
    }

    @Override
    public void onUpdate(UpdateEvent event) {
        if (IMinecraft.mc.player == null || IMinecraft.mc.player.isTouchingWater() || IMinecraft.mc.player.isSpectator() || ModuleManager.getModuleState(new Fly())) return;

        float strafeMultiplier;

        if (IMinecraft.mc.player.isOnGround() && MovementUtil.isMoving()) {
            IMinecraft.mc.player.jump();
            strafeMultiplier = 0.49f;
        } else strafeMultiplier = 0.3f;

        MovementUtil.strafe(Math.max((float) (strafeMultiplier + MovementUtil.getSpeedEffect() * 0.01), (float) MovementUtil.getBaseMoveSpeed(0.2769)));
    }

    @Override
    public void onMotion(MotionEvent event) {
    }

    @Override
    public void onTick(TickEvent event) {
        if (IMinecraft.mc.player == null) return;

        if (IMinecraft.mc.player.isOnGround())
            IMinecraft.mc.options.jumpKey.setPressed(false);
    }
}