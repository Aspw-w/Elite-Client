package com.instrumentalist.elite.hacks.features.combat

import com.instrumentalist.elite.events.features.ReceivedPacketEvent
import com.instrumentalist.elite.events.features.UpdateEvent
import com.instrumentalist.elite.events.features.WorldEvent
import com.instrumentalist.elite.hacks.Module
import com.instrumentalist.elite.hacks.ModuleCategory
import com.instrumentalist.elite.hacks.ModuleManager
import com.instrumentalist.elite.hacks.features.movement.Fly
import com.instrumentalist.elite.hacks.features.movement.Speed
import com.instrumentalist.elite.utils.IMinecraft
import com.instrumentalist.elite.utils.move.MovementUtil
import com.instrumentalist.elite.utils.value.ListValue
import net.minecraft.client.util.InputUtil
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket
import net.minecraft.util.PlayerInput
import org.lwjgl.glfw.GLFW
import java.util.*

class Velocity : Module("Velocity", ModuleCategory.Combat, GLFW.GLFW_KEY_UNKNOWN, false, true) {
    @Setting
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
                    event.cancel()
                    if (!ModuleManager.getModuleState(Speed()) && IMinecraft.mc.player!!.velocity.y >= 0 || IMinecraft.mc.player!!.isOnGround)
                        MovementUtil.setVelocityY(packet.velocityY / 8000.0)
                }
            }
        }
    }
}
