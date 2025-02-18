package com.instrumentalist.elite.hacks.features.render

import com.instrumentalist.elite.hacks.Module
import com.instrumentalist.elite.hacks.ModuleCategory
import com.instrumentalist.elite.hacks.ModuleManager
import com.instrumentalist.elite.utils.value.BooleanValue
import com.instrumentalist.elite.utils.value.FloatValue
import com.instrumentalist.elite.utils.value.IntValue
import net.minecraft.client.network.ClientPlayerEntity
import net.minecraft.entity.Entity
import org.lwjgl.glfw.GLFW

class ItemView : Module("Item View", ModuleCategory.Render, GLFW.GLFW_KEY_UNKNOWN, false, true) {
    companion object {
        @Setting
        val lowOffHand = BooleanValue("Low Off Hand", true)

        @Setting
        private val slowSwing = BooleanValue("Slow Swing", false)

        @Setting
        private val setSwingSpeed = IntValue("Set Swing Speed", 5, -5, 20) { slowSwing.get() }

        fun hookSwingSpeed(original: Int, entity: Entity): Int {
            if (ModuleManager.getModuleState(ItemView()) && slowSwing.get() && entity is ClientPlayerEntity)
                return 6 + setSwingSpeed.get()

            return original
        }
    }

    override fun onDisable() {}
    override fun onEnable() {}
}
