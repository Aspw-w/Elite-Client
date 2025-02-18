package com.instrumentalist.elite.hacks.features.render

import com.instrumentalist.elite.events.features.ReceivedPacketEvent
import com.instrumentalist.elite.hacks.Module
import com.instrumentalist.elite.hacks.ModuleCategory
import com.instrumentalist.elite.utils.ChatUtil
import com.instrumentalist.elite.utils.IMinecraft
import com.instrumentalist.elite.utils.entity.squaredDistanceToWithoutY
import net.minecraft.entity.EntityType
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket
import net.minecraft.text.ClickEvent
import net.minecraft.text.HoverEvent
import net.minecraft.text.Text
import net.minecraft.util.math.Vec3d
import org.lwjgl.glfw.GLFW

class LightningDetector : Module("Lightning Detector", ModuleCategory.Render, GLFW.GLFW_KEY_UNKNOWN, true, true) {

    override fun onDisable() {}
    override fun onEnable() {}

    override fun onReceivedPacket(event: ReceivedPacketEvent) {
        if (IMinecraft.mc.player == null || IMinecraft.mc.world == null) return

        val packet = event.packet

        if (packet is EntitySpawnS2CPacket && packet.entityType == EntityType.LIGHTNING_BOLT) {
            val tpMessage = Text.literal("§e> §7(§eClick to Teleport§7) §eDetected lightning at ${packet.x.toInt()} ${packet.y.toInt()} ${packet.z.toInt()} (${
                IMinecraft.mc.player!!.squaredDistanceToWithoutY(
                    packet.x,
                    packet.z
                ).toInt()
            } blocks away)").styled { style ->
                style.withClickEvent(
                    ClickEvent(
                        ClickEvent.Action.SUGGEST_COMMAND,
                        "/tp ${packet.x} ${packet.y} ${packet.z}"
                    )
                ).withHoverEvent(
                    HoverEvent(
                        HoverEvent.Action.SHOW_TEXT,
                        Text.literal("§lClick to easily prepare for teleport to ${packet.x.toInt()}, ${packet.y.toInt()}, ${packet.z.toInt()}")
                    )
                )
            }
            ChatUtil.printModifiedChat(tpMessage)
        }
    }
}
