package com.instrumentalist.elite.hacks.features.movement.speedmode.features;

import com.instrumentalist.elite.events.features.MotionEvent;
import com.instrumentalist.elite.events.features.TickEvent;
import com.instrumentalist.elite.events.features.UpdateEvent;
import com.instrumentalist.elite.hacks.ModuleManager;
import com.instrumentalist.elite.hacks.features.exploit.Disabler;
import com.instrumentalist.elite.hacks.features.exploit.disablermode.features.HypixelDisabler;
import com.instrumentalist.elite.hacks.features.movement.Fly;
import com.instrumentalist.elite.hacks.features.movement.speedmode.SpeedEvent;
import com.instrumentalist.elite.hacks.features.player.Scaffold;
import com.instrumentalist.elite.utils.ChatUtil;
import com.instrumentalist.elite.utils.IMinecraft;
import com.instrumentalist.elite.utils.move.MovementUtil;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.StairsBlock;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.util.math.BlockPos;

public class HypixelHopSpeed implements SpeedEvent {

    @Override
    public String getName() {
        return "Hypixel Hop";
    }

    private boolean lowStrafeCheck() {
        return IMinecraft.mc.player != null && IMinecraft.mc.world != null && !IMinecraft.mc.world.getBlockState(IMinecraft.mc.player.getBlockPos().down(1)).isAir() && !(IMinecraft.mc.world.getBlockState(IMinecraft.mc.player.getBlockPos().down(1)).getBlock() instanceof SlabBlock)  && !(IMinecraft.mc.world.getBlockState(IMinecraft.mc.player.getBlockPos().down(1)).getBlock() instanceof StairsBlock);
    }

    public static boolean canLowHop = false;

    @Override
    public void onUpdate(UpdateEvent event) {
        if (IMinecraft.mc.player == null || IMinecraft.mc.world == null || IMinecraft.mc.player.isTouchingWater() || IMinecraft.mc.player.isSpectator() || ModuleManager.getModuleState(new Scaffold()) && (!Scaffold.Companion.getJumped() || Scaffold.Companion.getWasTowering())) {
            canLowHop = false;
            return;
        }

        if (ModuleManager.getModuleState(new Disabler()) && HypixelDisabler.watchDogDisabled) {
            if (IMinecraft.mc.player.isOnGround()) {
                if (MovementUtil.isMoving())
                    IMinecraft.mc.player.jump();

                if (IMinecraft.mc.player.hasStatusEffect(StatusEffects.SPEED))
                    MovementUtil.strafe(0.481f + ((IMinecraft.mc.player.getStatusEffect(StatusEffects.SPEED).getAmplifier() + 1f) * IMinecraft.mc.player.getStatusEffect(StatusEffects.SPEED).getAmplifier() == 0 ? 0.036f : 0.12f));
                else MovementUtil.strafe(0.481f);

                canLowHop = false;
            } else if (!IMinecraft.mc.player.horizontalCollision && !IMinecraft.mc.player.hasStatusEffect(StatusEffects.JUMP_BOOST) && MovementUtil.isMoving()) {
                double baseVelocityY = IMinecraft.mc.player.getVelocity().y;

                switch (MovementUtil.fallTicks) {
                    case 4:
                        if (IMinecraft.mc.world.getBlockState(IMinecraft.mc.player.getBlockPos().up(2)).isAir() && !IMinecraft.mc.player.horizontalCollision) {
                            MovementUtil.setVelocityY(baseVelocityY - 0.039);
                            canLowHop = true;
                        } else canLowHop = false;
                        break;

                    case 5:
                        if (canLowHop)
                            MovementUtil.setVelocityY(baseVelocityY - 0.1916);
                        break;

                    case 6:
                        if (canLowHop && IMinecraft.mc.world.getBlockState(IMinecraft.mc.player.getBlockPos().up(2)).isAir())
                            MovementUtil.setVelocityY(baseVelocityY * 1.016);
                        break;

                    case 7:
                        if (canLowHop && lowStrafeCheck() && IMinecraft.mc.world.getBlockState(IMinecraft.mc.player.getBlockPos().up(2)).isAir()) {
                            MovementUtil.setVelocityY(baseVelocityY / 1.25);

                            if (IMinecraft.mc.player.hasStatusEffect(StatusEffects.SPEED))
                                MovementUtil.strafe(0.305f + ((IMinecraft.mc.player.getStatusEffect(StatusEffects.SPEED).getAmplifier() + 1f) * IMinecraft.mc.player.getStatusEffect(StatusEffects.SPEED).getAmplifier() == 0 ? 0.036f : 0.044f));
                            else MovementUtil.strafe(0.305f);
                        }
                        break;

                    case 8:
                        if (canLowHop && lowStrafeCheck())
                            MovementUtil.setVelocityY(baseVelocityY - 0.0118);

                        canLowHop = false;
                }
            }
        } else {
            if (IMinecraft.mc.player.isOnGround()) {
                if (MovementUtil.isMoving())
                    IMinecraft.mc.player.jump();

                if (IMinecraft.mc.player.hasStatusEffect(StatusEffects.SPEED))
                    MovementUtil.strafe(0.481f + ((IMinecraft.mc.player.getStatusEffect(StatusEffects.SPEED).getAmplifier() + 1f) * IMinecraft.mc.player.getStatusEffect(StatusEffects.SPEED).getAmplifier() == 0 ? 0.036f : 0.12f));
                else MovementUtil.strafe(0.481f);
            } else if (lowStrafeCheck()) {
                switch (MovementUtil.fallTicks) {
                    case 1:
                        MovementUtil.strafe((float) MovementUtil.getBaseMoveSpeed(0.2873));
                        break;

                    case 10:
                        if (IMinecraft.mc.player.hurtTime == 0) {
                            MovementUtil.setVelocityY(-0.28);

                            if (IMinecraft.mc.player.hasStatusEffect(StatusEffects.SPEED))
                                MovementUtil.strafe(0.305f + ((IMinecraft.mc.player.getStatusEffect(StatusEffects.SPEED).getAmplifier() + 1f) * IMinecraft.mc.player.getStatusEffect(StatusEffects.SPEED).getAmplifier() == 0 ? 0.036f : 0.044f));
                            else MovementUtil.strafe(0.305f);
                        }
                        break;

                    case 11:
                        MovementUtil.strafe((float) MovementUtil.getBaseMoveSpeed(0.2713));
                        break;

                    case 12:
                        MovementUtil.stopMoving();
                        break;
                }
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