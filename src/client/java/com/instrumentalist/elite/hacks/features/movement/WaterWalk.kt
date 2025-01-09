package com.instrumentalist.elite.hacks.features.movement

import com.instrumentalist.elite.events.features.CollisionEvent
import com.instrumentalist.elite.hacks.Module
import com.instrumentalist.elite.hacks.ModuleCategory
import com.instrumentalist.elite.utils.IMinecraft
import com.instrumentalist.elite.utils.move.MovementUtil
import com.instrumentalist.elite.utils.value.ListValue
import net.minecraft.block.Blocks
import net.minecraft.block.FluidBlock
import net.minecraft.entity.effect.StatusEffects
import net.minecraft.util.math.BlockPos
import org.lwjgl.glfw.GLFW
import java.util.*

class WaterWalk : Module("Water Walk", ModuleCategory.Movement, GLFW.GLFW_KEY_UNKNOWN, false, true) {
    @Setting
    private val mode = ListValue("Mode", arrayOf("Vanilla", "Verus"), "Vanilla")

    override fun onDisable() {}
    override fun onEnable() {}

    override fun onCollision(event: CollisionEvent) {
        if (IMinecraft.mc.player == null) return

        when (mode.get().lowercase(Locale.getDefault())) {
            "vanilla" -> {
                if (event.blockState.block is FluidBlock && !IMinecraft.mc.player!!.isSneaking && !IMinecraft.mc.player!!.isTouchingWater && event.blockPos == BlockPos(
                        IMinecraft.mc.player!!.blockPos.x,
                        IMinecraft.mc.player!!.blockPos.y - 1,
                        IMinecraft.mc.player!!.blockPos.z
                    )
                )
                    event.blockState = Blocks.STONE.defaultState
            }

            "verus" -> {
                if (IMinecraft.mc.player!!.isTouchingWater) {
                    if (IMinecraft.mc.player?.hasStatusEffect(StatusEffects.SPEED)!!)
                        MovementUtil.strafe(0.38f)
                    else MovementUtil.strafe(0.33f)
                }
            }
        }
    }
}
