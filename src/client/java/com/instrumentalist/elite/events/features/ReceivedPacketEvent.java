package com.instrumentalist.elite.events.features;

import com.instrumentalist.elite.events.EventArgument;
import com.instrumentalist.elite.events.EventListener;
import net.minecraft.network.packet.Packet;

import java.util.Objects;

public class ReceivedPacketEvent extends EventArgument {
    public final Packet<?> packet;

    public ReceivedPacketEvent(Packet<?> packet) {
        this.packet = packet;
    }

    @Override
    public void call(EventListener listener) {
        Objects.requireNonNull(listener).onReceivedPacket(this);
    }
}