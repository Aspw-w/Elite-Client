package com.instrumentalist.elite.hacks.features.player

import com.instrumentalist.elite.events.features.AttackEvent
import com.instrumentalist.elite.events.features.SendPacketEvent
import com.instrumentalist.elite.events.features.UpdateEvent
import com.instrumentalist.elite.hacks.Module
import com.instrumentalist.elite.hacks.ModuleCategory
import com.instrumentalist.elite.utils.IMinecraft
import com.instrumentalist.elite.utils.math.ToolUtil
import com.instrumentalist.elite.utils.value.BooleanValue
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket
import net.minecraft.util.hit.HitResult
import net.minecraft.util.math.BlockPos
import org.lwjgl.glfw.GLFW

class AutoTool : Module("Auto Tool", ModuleCategory.Player, GLFW.GLFW_KEY_UNKNOWN, false, true) {
    @Setting
    private val autoSword = BooleanValue("Auto Sword", true)

    private var pos: BlockPos? = null
    private var originalSlot: Int = -1

    override fun onDisable() {
        pos = null

        if (IMinecraft.mc.player != null && originalSlot != -1 && originalSlot != IMinecraft.mc.player!!.inventory.selectedSlot)
            IMinecraft.mc.player!!.inventory.selectedSlot = originalSlot

        originalSlot = -1
    }

    override fun onEnable() {}

    override fun onAttack(event: AttackEvent) {
        if (autoSword.get()) {
            val bestSwordSlot = ToolUtil.findBestSword()
            if (bestSwordSlot != -1 && IMinecraft.mc.player!!.inventory.selectedSlot != bestSwordSlot)
                IMinecraft.mc.player!!.inventory.selectedSlot = bestSwordSlot
        }
    }

    override fun onUpdate(event: UpdateEvent) {
        if (IMinecraft.mc.player == null) return

        if (pos != null) {
            if (!IMinecraft.mc.options.attackKey.isPressed || IMinecraft.mc.crosshairTarget!!.type != HitResult.Type.BLOCK || IMinecraft.mc.options.useKey.isPressed) {
                if (originalSlot != -1 && originalSlot != IMinecraft.mc.player!!.inventory.selectedSlot) {
                    IMinecraft.mc.player!!.inventory.selectedSlot = originalSlot
                    originalSlot = -1
                }
                pos = null
                return
            }

            val bestToolSlot = ToolUtil.findBestTool(pos!!)
            if (bestToolSlot != -1 && bestToolSlot != IMinecraft.mc.player!!.inventory.selectedSlot) {
                if (originalSlot == -1)
                    originalSlot = IMinecraft.mc.player!!.inventory.selectedSlot
                IMinecraft.mc.player!!.inventory.selectedSlot = bestToolSlot
            }
        }
    }

    override fun onSendPacket(event: SendPacketEvent) {
        val packet = event.packet

        if (packet is PlayerActionC2SPacket && packet.action == PlayerActionC2SPacket.Action.START_DESTROY_BLOCK)
            pos = packet.pos
    }
}
