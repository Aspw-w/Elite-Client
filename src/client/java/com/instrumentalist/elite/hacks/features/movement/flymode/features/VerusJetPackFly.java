package com.instrumentalist.elite.hacks.features.movement.flymode.features;

import com.instrumentalist.elite.events.features.MotionEvent;
import com.instrumentalist.elite.events.features.TickEvent;
import com.instrumentalist.elite.events.features.UpdateEvent;
import com.instrumentalist.elite.hacks.Module;
import com.instrumentalist.elite.hacks.features.movement.Fly;
import com.instrumentalist.elite.hacks.features.movement.flymode.FlyEvent;
import com.instrumentalist.elite.utils.IMinecraft;
import com.instrumentalist.elite.utils.move.MovementUtil;
import com.instrumentalist.elite.utils.value.BooleanValue;
import com.instrumentalist.elite.utils.value.FloatValue;
import net.minecraft.client.util.InputUtil;

public class VerusJetPackFly implements FlyEvent {

    @Override
    public String getName() {
        return "Verus JetPack";
    }

    @Override
    public void onUpdate(UpdateEvent event) {
        if (IMinecraft.mc.player == null || Fly.verusJetPackJumpKeyOnly.get() && !InputUtil.isKeyPressed(IMinecraft.mc.getWindow().getHandle(), InputUtil.fromTranslationKey(IMinecraft.mc.options.jumpKey.getBoundKeyTranslationKey()).getCode())) return;

        MovementUtil.strafe(0.1f);

        IMinecraft.mc.player.setSprinting(false);

        if (IMinecraft.mc.player.age % 2 == 0)
            IMinecraft.mc.player.jump();
    }

    @Override
    public void onMotion(MotionEvent event) {
    }

    @Override
    public void onTick(TickEvent event) {
    }
}