package com.instrumentalist.elite.hacks.features.player

import com.instrumentalist.elite.events.features.UpdateEvent
import com.instrumentalist.elite.hacks.Module
import com.instrumentalist.elite.hacks.ModuleCategory
import com.instrumentalist.elite.utils.IMinecraft
import com.instrumentalist.elite.utils.math.TimerUtil
import com.instrumentalist.elite.utils.move.MovementUtil
import com.instrumentalist.elite.utils.value.BooleanValue
import com.instrumentalist.elite.utils.value.FloatValue
import com.instrumentalist.elite.utils.value.ListValue
import org.lwjgl.glfw.GLFW
import java.util.*

class FastLadder : Module("Fast Ladder", ModuleCategory.Player, GLFW.GLFW_KEY_UNKNOWN, false, true) {
    private val mode = ListValue("Mode", arrayOf("Vanilla", "Timer"), "Vanilla")

    private val resetY = BooleanValue("ResetY", true) { mode.get().equals("vanilla", true) }

    private val speed = FloatValue("Speed", 0.4f, 0.2f, 1f) { mode.get().equals("vanilla", true) }

    private val timerSpeed = FloatValue("TimerSpeed", 2f, 1.1f, 3f) { mode.get().equals("timer", true) }

    private var wasClimbing = false

    override fun onDisable() {
        if (IMinecraft.mc.player == null) return

        wasClimbing = false
        TimerUtil.reset()
    }

    override fun onEnable() {}

    override fun onUpdate(event: UpdateEvent) {
        if (IMinecraft.mc.player == null) return

        when (mode.get().lowercase(Locale.getDefault())) {
            "vanilla" -> {
                if (IMinecraft.mc.player!!.isClimbing && (IMinecraft.mc.player!!.horizontalCollision || IMinecraft.mc.options.jumpKey.isPressed)) {
                    MovementUtil.setVelocityY(speed.get().toDouble())
                    wasClimbing = true
                } else if (wasClimbing) {
                    if (resetY.get())
                        MovementUtil.setVelocityY(-0.1)
                    wasClimbing = false
                }
            }

            "timer" -> {
                if (IMinecraft.mc.player!!.isClimbing && (IMinecraft.mc.player!!.horizontalCollision || IMinecraft.mc.options.jumpKey.isPressed)) {
                    TimerUtil.timerSpeed = timerSpeed.get()
                    wasClimbing = true
                } else if (wasClimbing) {
                    TimerUtil.reset()
                    wasClimbing = false
                }
            }
        }
    }
}
