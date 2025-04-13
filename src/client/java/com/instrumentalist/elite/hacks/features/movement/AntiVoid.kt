package com.instrumentalist.elite.hacks.features.movement

import com.instrumentalist.elite.events.features.ReceivedPacketEvent
import com.instrumentalist.elite.events.features.UpdateEvent
import com.instrumentalist.elite.events.features.WorldEvent
import com.instrumentalist.elite.hacks.Module
import com.instrumentalist.elite.hacks.ModuleCategory
import com.instrumentalist.elite.utils.ChatUtil
import com.instrumentalist.elite.utils.IMinecraft
import com.instrumentalist.elite.utils.entity.isFallingToVoid
import com.instrumentalist.elite.utils.math.TickTimer
import com.instrumentalist.elite.utils.move.MovementUtil
import com.instrumentalist.elite.utils.packet.BlinkUtil
import com.instrumentalist.elite.utils.value.BooleanValue
import com.instrumentalist.elite.utils.value.IntValue
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket
import org.lwjgl.glfw.GLFW

class AntiVoid : Module(
    "Anti Void",
    ModuleCategory.Movement,
    GLFW.GLFW_KEY_UNKNOWN,
    false,
    true
) {
    private val stopXZ = BooleanValue("StopXZ", true)

    private val distance = IntValue("Distance", 6, 0, 10)

    private var canTick = 0
    private var unSafeY: Int? = null

    override fun onDisable() {
        if (IMinecraft.mc.player == null) return
        BlinkUtil.sync(true, false)
        BlinkUtil.stopBlink()
        canTick = 0
        unSafeY = null
    }

    override fun onEnable() {}

    override fun onWorld(event: WorldEvent) {
        canTick = 0
        unSafeY = null
    }

    override fun onUpdate(event: UpdateEvent) {
        if (IMinecraft.mc.player == null || canTick > 0 || !IMinecraft.mc.player!!.isFallingToVoid() || IMinecraft.mc.player!!.age <= 50 || IMinecraft.mc.player!!.abilities.flying || IMinecraft.mc.player!!.isSneaking || IMinecraft.mc.player!!.isSpectator || IMinecraft.mc.player!!.isTouchingWater) {
            if (unSafeY != null) {
                BlinkUtil.sync(true, false)
                BlinkUtil.stopBlink()
                unSafeY = null
            }
            canTick--
            return
        }

        if (unSafeY == null)
            unSafeY = IMinecraft.mc.player!!.blockPos.y - distance.get()
        BlinkUtil.doBlink()
        if (IMinecraft.mc.player!!.blockPos.y <= unSafeY!!) {
            BlinkUtil.sync(false, true)
            if (stopXZ.get())
                MovementUtil.stopMoving()
        }
    }

    override fun onReceivedPacket(event: ReceivedPacketEvent) {
        if (IMinecraft.mc.player == null || IMinecraft.mc.player!!.age <= 50) return

        val packet = event.packet

        if (packet is PlayerPositionLookS2CPacket && BlinkUtil.blinking && unSafeY != null) {
            canTick = 80
            ChatUtil.printChat("AntiVoid flagged (reset tick = $canTick)")
            BlinkUtil.sync(true)
            BlinkUtil.stopBlink()
            if (stopXZ.get())
                MovementUtil.stopMoving()
            unSafeY = null
        }
    }
}
