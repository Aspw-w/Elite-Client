package com.instrumentalist.elite.hacks.features.combat

import com.instrumentalist.elite.events.features.ReceivedPacketEvent
import com.instrumentalist.elite.hacks.Module
import com.instrumentalist.elite.hacks.ModuleCategory
import com.instrumentalist.elite.hacks.ModuleManager
import com.instrumentalist.elite.hacks.features.movement.Fly
import com.instrumentalist.elite.hacks.features.movement.Speed
import com.instrumentalist.elite.utils.IMinecraft
import com.instrumentalist.elite.utils.move.MovementUtil
import com.instrumentalist.elite.utils.packet.PacketUtil
import com.instrumentalist.elite.utils.value.ListValue
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket
import org.lwjgl.glfw.GLFW
import java.util.*

class Velocity : Module("Velocity", ModuleCategory.Combat, GLFW.GLFW_KEY_UNKNOWN, false, true) {
    private val mode = ListValue("Mode", arrayOf("Standard", "Hypixel"), "Standard")

    override fun tag(): String {
        return mode.get()
    }

    override fun onDisable() {}
    override fun onEnable() {}

    override fun onReceivedPacket(event: ReceivedPacketEvent) {
        if (IMinecraft.mc.player == null) return

        val packet = event.packet

        when (mode.get().lowercase(Locale.getDefault())) {
            "standard" -> {
                if (packet is EntityVelocityUpdateS2CPacket && packet.entityId == IMinecraft.mc.player!!.id)
                    event.cancel()
            }

            "hypixel" -> {
                if (!ModuleManager.getModuleState(Fly()) && packet is EntityVelocityUpdateS2CPacket && packet.entityId == IMinecraft.mc.player!!.id) {
                    if (ModuleManager.getModuleState(Speed()) && (MovementUtil.fallTicks >= 7 || IMinecraft.mc.player!!.isOnGround) || !ModuleManager.getModuleState(Speed()))
                        MovementUtil.setVelocityY(packet.velocityY / 8000.0)
                    else PacketUtil.sendPacket(PlayerMoveC2SPacket.OnGroundOnly(false, false))
                    event.cancel()
                }
            }
        }
    }
}
