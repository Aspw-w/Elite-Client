package com.instrumentalist.elite.hacks.features.movement

import com.instrumentalist.elite.events.features.UpdateEvent
import com.instrumentalist.elite.hacks.Module
import com.instrumentalist.elite.hacks.ModuleCategory
import com.instrumentalist.elite.hacks.ModuleManager
import com.instrumentalist.elite.utils.IMinecraft
import com.instrumentalist.elite.utils.move.MovementUtil
import com.instrumentalist.elite.utils.value.BooleanValue
import net.minecraft.client.network.ClientPlayerEntity
import net.minecraft.entity.player.PlayerEntity
import org.lwjgl.glfw.GLFW

class Sprint : Module("Sprint", ModuleCategory.Movement, GLFW.GLFW_KEY_UNKNOWN, false, true) {
    companion object {
        @Setting
        private val autoSprint = BooleanValue("Auto Sprint", true)

        @Setting
        val multiDirection = BooleanValue("Multi Direction", true) { autoSprint.get() }

        @Setting
        private val keepSprint = BooleanValue("Keep Sprint", true)

        @Setting
        private val silentSprint = BooleanValue("Silent", true)

        @Setting
        private val wallCheck = BooleanValue("Wall Check", false)

        fun keepSprintHook(entity: PlayerEntity): Boolean {
            return !(ModuleManager.getModuleState(Sprint()) && keepSprint.get() && entity is ClientPlayerEntity)
        }

        fun silentSprintHook(original: Boolean): Boolean {
            if (ModuleManager.getModuleState(Sprint()) && silentSprint.get() && !IMinecraft.mc.player!!.isTouchingWater) return false
            return original
        }
    }

    override fun onDisable() {}
    override fun onEnable() {}

    override fun onUpdate(event: UpdateEvent) {
        if (IMinecraft.mc.player == null) return

        if (autoSprint.get() && MovementUtil.isMoving() && (multiDirection.get() || IMinecraft.mc.player!!.input.movementForward > 0) && !IMinecraft.mc.player!!.isSprinting && (wallCheck.get() && !IMinecraft.mc.player!!.horizontalCollision || !wallCheck.get()))
            IMinecraft.mc.player!!.isSprinting = true
    }
}
