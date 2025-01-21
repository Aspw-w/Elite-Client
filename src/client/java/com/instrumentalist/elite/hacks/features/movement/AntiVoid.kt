package com.instrumentalist.elite.hacks.features.movement

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
import org.lwjgl.glfw.GLFW

class AntiVoid : Module(
    "Anti Void",
    ModuleCategory.Movement,
    GLFW.GLFW_KEY_UNKNOWN,
    false,
    true
) {
    @Setting
    private val stopXZ = BooleanValue("StopXZ", true)

    @Setting
    private val distance = IntValue("Distance", 6, 0, 10)

    private var unSafeY: Int? = null

    override fun onDisable() {
        if (IMinecraft.mc.player == null) return
        BlinkUtil.sync(true, false)
        BlinkUtil.stopBlink()
        unSafeY = null
    }

    override fun onEnable() {}

    override fun onWorld(event: WorldEvent) {
        unSafeY = null
    }

    override fun onUpdate(event: UpdateEvent) {
        if (IMinecraft.mc.player == null || !IMinecraft.mc.player!!.isFallingToVoid() || IMinecraft.mc.player!!.age <= 50f || IMinecraft.mc.player!!.abilities.flying || IMinecraft.mc.player!!.isSneaking || IMinecraft.mc.player!!.isSpectator || IMinecraft.mc.player!!.isTouchingWater) {
            if (unSafeY != null) {
                BlinkUtil.sync(true, false)
                BlinkUtil.stopBlink()
                unSafeY = null
            }
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
}
