package com.instrumentalist.elite.hacks.features.movement.flymode.features;

import com.instrumentalist.elite.events.features.MotionEvent;
import com.instrumentalist.elite.events.features.TickEvent;
import com.instrumentalist.elite.events.features.UpdateEvent;
import com.instrumentalist.elite.hacks.features.movement.Fly;
import com.instrumentalist.elite.hacks.features.movement.flymode.FlyEvent;
import com.instrumentalist.elite.utils.IMinecraft;
import com.instrumentalist.elite.utils.math.MSTimer;
import com.instrumentalist.elite.utils.math.TickTimer;
import com.instrumentalist.elite.utils.move.MovementUtil;
import com.instrumentalist.elite.utils.packet.BlinkUtil;
import net.minecraft.client.util.InputUtil;

public class CubecraftFly implements FlyEvent {

    @Override
    public String getName() {
        return "Cubecraft";
    }

    private final MSTimer cubeTimer = new MSTimer();
    public static TickTimer blinkTimer = new TickTimer();

    @Override
    public void onUpdate(UpdateEvent event) {
        if (IMinecraft.mc.player == null) return;

        BlinkUtil.INSTANCE.doBlink();
        blinkTimer.update();
        if (blinkTimer.hasTimePassed(30)) {
            BlinkUtil.INSTANCE.sync(true, false);
            blinkTimer.reset();
        }

        long needTicks;

        if (InputUtil.isKeyPressed(IMinecraft.mc.getWindow().getHandle(), InputUtil.fromTranslationKey(IMinecraft.mc.options.sneakKey.getBoundKeyTranslationKey()).getCode()))
            needTicks = 750L;
        else if (InputUtil.isKeyPressed(IMinecraft.mc.getWindow().getHandle(), InputUtil.fromTranslationKey(IMinecraft.mc.options.jumpKey.getBoundKeyTranslationKey()).getCode()))
            needTicks = 300L;
        else needTicks = 509L;

        if (cubeTimer.hasTimePassed(needTicks)) {
            IMinecraft.mc.player.setOnGround(true);
            MovementUtil.strafe(Math.max((float) (0.56f + MovementUtil.getSpeedEffect() * 0.01), (float) MovementUtil.getBaseMoveSpeed(0.2763)));
            IMinecraft.mc.player.jump();
            cubeTimer.reset();
        }
    }

    @Override
    public void onMotion(MotionEvent event) {
    }

    @Override
    public void onTick(TickEvent event) {
        if (IMinecraft.mc.player == null) return;

        IMinecraft.mc.options.jumpKey.setPressed(false);
        IMinecraft.mc.options.sneakKey.setPressed(false);
    }
}