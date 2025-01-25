package com.instrumentalist.elite.hacks.features.movement

import com.instrumentalist.elite.events.features.UpdateEvent
import com.instrumentalist.elite.hacks.Module
import com.instrumentalist.elite.hacks.ModuleCategory
import com.instrumentalist.elite.utils.IMinecraft
import com.instrumentalist.elite.utils.move.MovementUtil
import com.instrumentalist.elite.utils.value.ListValue
import net.minecraft.entity.effect.StatusEffects
import org.lwjgl.glfw.GLFW
import java.util.*

class WaterSpeed : Module("Water Speed", ModuleCategory.Movement, GLFW.GLFW_KEY_UNKNOWN, false, true) {
    @Setting
    private val mode = ListValue("Mode", arrayOf("Vanilla", "Verus"), "Vanilla")

    override fun onDisable() {}
    override fun onEnable() {}

    override fun onUpdate(event: UpdateEvent) {
        if (IMinecraft.mc.player == null || !IMinecraft.mc.player!!.isTouchingWater) return

        when (mode.get().lowercase(Locale.getDefault())) {
            "vanilla" -> MovementUtil.strafe(0.5f)
            "verus" -> {
                if (IMinecraft.mc.player?.hasStatusEffect(StatusEffects.SPEED)!!)
                    MovementUtil.strafe(0.38f)
                else MovementUtil.strafe(0.33f)
            }
        }
    }
}
