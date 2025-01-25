package com.instrumentalist.mixin.injector;

import com.instrumentalist.elite.Client;
import com.instrumentalist.elite.events.features.ModifyPacketEvent;
import com.instrumentalist.elite.events.features.ReceivedPacketEvent;
import com.instrumentalist.elite.events.features.SendPacketEvent;
import com.instrumentalist.elite.utils.packet.PacketUtil;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.OffThreadException;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BundleS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

@Mixin(ClientConnection.class)
public abstract class ClientConnectionMixin {

    @Shadow
    private static <T extends PacketListener> void handlePacket(Packet<T> packet, PacketListener listener) {
    }

    @ModifyVariable(at = @At("HEAD"), method = "send(Lnet/minecraft/network/packet/Packet;Lnet/minecraft/network/PacketCallbacks;)V", argsOnly = true)
    public Packet<?> modifyPacket(Packet<?> packet) {
        final ModifyPacketEvent event = new ModifyPacketEvent(packet);
        Objects.requireNonNull(Client.eventManager).call(event);
        if (event.isCancelled())
            return null;

        return event.packet;
    }

    @Inject(method = "send(Lnet/minecraft/network/packet/Packet;)V", at = @At("HEAD"), cancellable = true)
    private void sendPacketEvent(Packet<?> packet, final CallbackInfo ci) {
        if (PacketUtil.silentPackets.contains(packet)) {
            PacketUtil.silentPackets.remove(packet);
            return;
        }

        final SendPacketEvent event = new SendPacketEvent(packet);
        Objects.requireNonNull(Client.eventManager).call(event);
        if (event.isCancelled())
            ci.cancel();
    }

    @Inject(method = "handlePacket", at = @At("HEAD"), cancellable = true)
    private static void receivedPacketEvent(Packet<?> packet, PacketListener listener, CallbackInfo ci) {
        if (packet instanceof BundleS2CPacket bundleS2CPacket) {
            ci.cancel();
            for (Packet<?> packetInBundle : bundleS2CPacket.getPackets()) {
                try {
                    handlePacket(packetInBundle, listener);
                } catch (OffThreadException ignored) {
                }
            }
            return;
        }

        final ReceivedPacketEvent event = new ReceivedPacketEvent(packet);
        Objects.requireNonNull(Client.eventManager).call(event);
        if (event.isCancelled())
            ci.cancel();
    }
}