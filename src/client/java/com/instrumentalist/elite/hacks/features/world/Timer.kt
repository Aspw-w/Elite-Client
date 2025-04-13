package com.instrumentalist.elite.hacks.features.world

import com.instrumentalist.elite.events.features.TickEvent
import com.instrumentalist.elite.hacks.Module
import com.instrumentalist.elite.hacks.ModuleCategory
import com.instrumentalist.elite.utils.math.TimerUtil
import com.instrumentalist.elite.utils.move.MovementUtil
import com.instrumentalist.elite.utils.value.BooleanValue
import com.instrumentalist.elite.utils.value.FloatValue
import com.instrumentalist.elite.utils.value.ListValue
import org.lwjgl.glfw.GLFW
import java.util.*

class Timer : Module("Timer", ModuleCategory.World, GLFW.GLFW_KEY_UNKNOWN, false, true) {
    companion object {
        val mode = ListValue("Mode", arrayOf("Vanilla"), "Vanilla")

        private val speed = FloatValue("Speed", 1.5f, 0.1f, 10f) { mode.get().equals("vanilla", true) }

        private val moveOnly = BooleanValue("Move Only", false) { mode.get().equals("vanilla", true) }
    }

    override fun onDisable() {
        TimerUtil.reset()
    }

    override fun onEnable() {}

    override fun onTick(event: TickEvent) {
        when (mode.get().lowercase(Locale.getDefault())) {
            "vanilla" -> {
                if (moveOnly.get() && !MovementUtil.isMoving())
                    TimerUtil.reset()
                else TimerUtil.timerSpeed = speed.get()
            }
        }
    }
}
