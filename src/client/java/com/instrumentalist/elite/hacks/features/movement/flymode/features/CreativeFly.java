package com.instrumentalist.elite.hacks.features.movement.flymode.features;

import com.instrumentalist.elite.events.features.MotionEvent;
import com.instrumentalist.elite.events.features.TickEvent;
import com.instrumentalist.elite.events.features.UpdateEvent;
import com.instrumentalist.elite.hacks.features.movement.Fly;
import com.instrumentalist.elite.hacks.features.movement.flymode.FlyEvent;
import com.instrumentalist.elite.utils.IMinecraft;
import com.instrumentalist.elite.utils.move.MovementUtil;
import net.minecraft.client.util.InputUtil;

public class CreativeFly implements FlyEvent {

    @Override
    public String getName() {
        return "Creative";
    }

    @Override
    public void onUpdate(UpdateEvent event) {
        if (IMinecraft.mc.player == null) return;

        IMinecraft.mc.player.getAbilities().allowFlying = true;
    }

    @Override
    public void onMotion(MotionEvent event) {
    }

    @Override
    public void onTick(TickEvent event) {
    }
}