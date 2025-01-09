package com.instrumentalist.elite.hacks.features.combat;

import com.instrumentalist.elite.events.features.UpdateEvent;
import com.instrumentalist.elite.hacks.Module;
import com.instrumentalist.elite.hacks.ModuleCategory;
import com.instrumentalist.elite.hacks.ModuleManager;
import com.instrumentalist.elite.utils.IMinecraft;
import com.instrumentalist.elite.utils.packet.PacketUtil;
import net.minecraft.item.BowItem;
import net.minecraft.item.consume.UseAction;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.lwjgl.glfw.GLFW;

public class FastBow extends Module {

    public FastBow() {
        super("Fast Bow", ModuleCategory.Combat, GLFW.GLFW_KEY_UNKNOWN, false, true);
    }

    @Override
    public void onDisable() {
    }

    @Override
    public void onEnable() {
    }

    @Override
    public void onUpdate(UpdateEvent event) {
        if (IMinecraft.mc.player == null) return;

        if (IMinecraft.mc.player.isUsingItem() && IMinecraft.mc.player.getItemUseTimeLeft() >= 30 && IMinecraft.mc.player.getMainHandStack().getUseAction() == UseAction.BOW) {
            for (int i = 0; i <= 19; i++) {
                PacketUtil.sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(IMinecraft.mc.player.getYaw(), IMinecraft.mc.player.getPitch(), true, IMinecraft.mc.player.horizontalCollision));
            }

            PacketUtil.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, Direction.DOWN));
        }
    }
}
