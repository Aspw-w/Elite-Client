package com.instrumentalist.elite.hacks.features.movement.flymode.features;

import com.instrumentalist.elite.events.features.MotionEvent;
import com.instrumentalist.elite.events.features.TickEvent;
import com.instrumentalist.elite.events.features.UpdateEvent;
import com.instrumentalist.elite.hacks.features.movement.flymode.FlyEvent;
import com.instrumentalist.elite.utils.IMinecraft;
import com.instrumentalist.elite.utils.move.MovementUtil;
import com.instrumentalist.elite.utils.packet.PacketUtil;
import com.instrumentalist.elite.utils.rotation.RotationUtil;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class Verus1_8Fly implements FlyEvent {

    @Override
    public String getName() {
        return "Verus 1.8";
    }

    public static boolean verusRotating = false;
    public static ItemStack oldSelectedSlotStack = null;

    @Override
    public void onUpdate(UpdateEvent event) {
        if (IMinecraft.mc.player == null) return;

        if (InputUtil.isKeyPressed(IMinecraft.mc.getWindow().getHandle(), InputUtil.fromTranslationKey(IMinecraft.mc.options.sneakKey.getBoundKeyTranslationKey()).getCode())) {
            if (verusRotating) {
                RotationUtil.INSTANCE.reset();
                verusRotating = false;
            }
            return;
        }

        IMinecraft.mc.player.setOnGround(false);

        if (InputUtil.isKeyPressed(IMinecraft.mc.getWindow().getHandle(), InputUtil.fromTranslationKey(IMinecraft.mc.options.jumpKey.getBoundKeyTranslationKey()).getCode())) {
            if (verusRotating) {
                RotationUtil.INSTANCE.reset();
                verusRotating = false;
            }

            MovementUtil.strafe(0.1f);

            IMinecraft.mc.player.setSprinting(false);

            if (IMinecraft.mc.player.age % 2 == 0)
                IMinecraft.mc.player.jump();

            return;
        }

        RotationUtil.INSTANCE.setRotation((IMinecraft.mc.player.getYaw() % 360 + 360) % 360, 90f, 90f);
        verusRotating = true;

        //BlockHitResult hitResult = new BlockHitResult(new Vec3d(IMinecraft.mc.player.getBlockPos().down(2).getX(), IMinecraft.mc.player.getBlockPos().down(2).getY() + 0.5f + (Math.random() * 0.44), IMinecraft.mc.player.getBlockPos().down(2).getZ()), Direction.UP, IMinecraft.mc.player.getBlockPos().down(2), true);
        IMinecraft.mc.player.getInventory().setStack(8, new ItemStack(Items.BARRIER));
        PacketUtil.sendPacket(new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, 0, (IMinecraft.mc.player.getYaw() % 360 + 360) % 360, 90f));

        if (IMinecraft.mc.player.hasStatusEffect(StatusEffects.SPEED))
            MovementUtil.strafe(0.38f);
        else MovementUtil.strafe(0.33f);

        MovementUtil.setVelocityY(0.0);
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