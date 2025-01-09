package com.instrumentalist.elite.utils.packet;

import com.instrumentalist.elite.utils.IMinecraft;
import net.minecraft.network.packet.Packet;

import java.util.ArrayList;

public class PacketUtil {
    public static ArrayList<Packet<?>> silentPackets = new ArrayList<>();

    public static void sendPacket(Packet<?> packet) {
        if (IMinecraft.mc.getNetworkHandler() == null) return;

        IMinecraft.mc.getNetworkHandler().sendPacket(packet);
    }

    public static void sendPacketAsSilent(Packet<?> packet) {
        if (IMinecraft.mc.getNetworkHandler() == null) return;

        silentPackets.add(packet);
        IMinecraft.mc.getNetworkHandler().sendPacket(packet);
    }
}
