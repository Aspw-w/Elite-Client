package com.instrumentalist.elite.hacks.features.player;

import com.instrumentalist.elite.events.features.MotionEvent;
import com.instrumentalist.elite.events.features.SendPacketEvent;
import com.instrumentalist.elite.events.features.UpdateEvent;
import com.instrumentalist.elite.hacks.Module;
import com.instrumentalist.elite.hacks.ModuleCategory;
import com.instrumentalist.elite.utils.IMinecraft;
import com.instrumentalist.elite.utils.packet.PacketUtil;
import net.minecraft.block.Blocks;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.lwjgl.glfw.GLFW;

public class FastBreak extends Module {

    public FastBreak() {
        super("Fast Break", ModuleCategory.Player, GLFW.GLFW_KEY_UNKNOWN, false, true);
    }

    private static float damage = 0f;
    private static boolean boost = false;
    private static BlockPos pos = null;
    private static Direction facing = null;

    @Override
    public void onDisable() {
        reset();
    }

    @Override
    public void onEnable() {
    }

    @Override
    public void onUpdate(UpdateEvent event) {
        if (IMinecraft.mc.player == null || IMinecraft.mc.world == null || IMinecraft.mc.player.isCreative()) {
            reset();
            return;
        }

        if (boost && pos != null) {
            try {
                damage += IMinecraft.mc.world.getBlockState(pos).calcBlockBreakingDelta(IMinecraft.mc.player, IMinecraft.mc.world, pos) * 1.4f;
            } catch (Exception e) {
                return;
            }

            if (damage >= 1) {
                try {
                    IMinecraft.mc.world.setBlockState(pos, Blocks.AIR.getDefaultState());
                } catch (Exception e) {
                    return;
                }

                Direction breakFacing = facing;

                if (breakFacing == null)
                    breakFacing = Direction.UP;

                PacketUtil.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, pos, breakFacing));

                damage = 0f;
                boost = false;
            }
        }
    }

    @Override
    public void onSendPacket(SendPacketEvent event) {
        if (IMinecraft.mc.player == null || IMinecraft.mc.world == null || IMinecraft.mc.player.isCreative()) {
            reset();
            return;
        }

        Packet<?> packet = event.packet;

        if (packet instanceof PlayerActionC2SPacket) {
            if (((PlayerActionC2SPacket) packet).getAction() == PlayerActionC2SPacket.Action.START_DESTROY_BLOCK) {
                boost = true;
                pos = ((PlayerActionC2SPacket) packet).getPos();
                facing = ((PlayerActionC2SPacket) packet).getDirection();
                damage = 0f;
            } else if (((PlayerActionC2SPacket) packet).getAction() == PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK || ((PlayerActionC2SPacket) packet).getAction() == PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK)
                reset();
        }
    }

    private static void reset() {
        boost = false;
        pos = null;
        facing = null;
        damage = 0f;
    }
}
