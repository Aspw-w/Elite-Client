package com.instrumentalist.elite.hacks.features.player;

import com.instrumentalist.elite.events.features.SendPacketEvent;
import com.instrumentalist.elite.events.features.UpdateEvent;
import com.instrumentalist.elite.hacks.Module;
import com.instrumentalist.elite.hacks.ModuleCategory;
import com.instrumentalist.elite.utils.ChatUtil;
import com.instrumentalist.elite.utils.IMinecraft;
import com.instrumentalist.elite.utils.math.MSTimer;
import com.instrumentalist.elite.utils.packet.PacketUtil;
import net.minecraft.block.Blocks;
import net.minecraft.item.Items;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.lwjgl.glfw.GLFW;

public class AutoFish extends Module {

    public AutoFish() {
        super("Auto Fish", ModuleCategory.Player, GLFW.GLFW_KEY_UNKNOWN, false, true);
    }

    private final MSTimer fishTimer = new MSTimer();

    @Override
    public void onDisable() {
    }

    @Override
    public void onEnable() {
    }

    @Override
    public void onUpdate(UpdateEvent event) {
        if (IMinecraft.mc.player == null || IMinecraft.mc.player.getMainHandStack().getItem() != Items.FISHING_ROD) return;

        if (fishTimer.hasTimePassed(500L) && IMinecraft.mc.interactionManager != null && (IMinecraft.mc.player.fishHook == null || IMinecraft.mc.player.fishHook.getVelocity().x == 0.0 && -0.2 >= IMinecraft.mc.player.fishHook.getVelocity().y && IMinecraft.mc.player.fishHook.getVelocity().z == 0.0)) {
            if (IMinecraft.mc.interactionManager.interactItem(IMinecraft.mc.player, Hand.MAIN_HAND).isAccepted()) {
                IMinecraft.mc.player.swingHand(Hand.MAIN_HAND);
                ChatUtil.printChat("Fished");
                fishTimer.reset();
            }
        }
    }
}
