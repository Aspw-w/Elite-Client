package com.instrumentalist.elite.hacks.features.movement.speedmode.features;

import com.instrumentalist.elite.events.features.MotionEvent;
import com.instrumentalist.elite.events.features.TickEvent;
import com.instrumentalist.elite.events.features.UpdateEvent;
import com.instrumentalist.elite.hacks.ModuleManager;
import com.instrumentalist.elite.hacks.features.movement.Fly;
import com.instrumentalist.elite.hacks.features.movement.speedmode.SpeedEvent;
import com.instrumentalist.elite.utils.IMinecraft;
import com.instrumentalist.elite.utils.math.TimerUtil;
import com.instrumentalist.elite.utils.move.MovementUtil;
import net.minecraft.entity.effect.StatusEffects;

public class VerusHopSpeed implements SpeedEvent {

    @Override
    public String getName() {
        return "Verus Hop";
    }

    public static int tick = 0;

    @Override
    public void onUpdate(UpdateEvent event) {
        if (IMinecraft.mc.player == null || ModuleManager.getModuleState(new Fly())) return;

        if (IMinecraft.mc.player.isOnGround() && MovementUtil.isMoving())
            IMinecraft.mc.player.jump();

        if (IMinecraft.mc.player.hasStatusEffect(StatusEffects.SPEED))
            MovementUtil.strafe(0.38f);
        else MovementUtil.strafe(0.33f);
    }

    @Override
    public void onMotion(MotionEvent event) {
        if (IMinecraft.mc.player == null || ModuleManager.getModuleState(new Fly())) return;

        tick += 1;

        if (tick > 2) {
            TimerUtil.timerSpeed = 1.15f;
            if (tick > 5) {
                TimerUtil.timerSpeed = 0.9f;
                tick = 0;
            }
        }
    }

    @Override
    public void onTick(TickEvent event) {
        if (IMinecraft.mc.player == null) return;

        if (IMinecraft.mc.player.isOnGround() && !MovementUtil.isMoving())
            IMinecraft.mc.options.jumpKey.setPressed(false);
    }
}