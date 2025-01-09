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
        private val swingSpeed = IntValue("Swing Speed", 2, -5, 20)

        fun hookSwingSpeed(original: Int, entity: Entity): Int {
            if (ModuleManager.getModuleState(ItemView()) && entity is ClientPlayerEntity)
                return original + swingSpeed.get()

            return original;
        }
    }

    override fun onDisable() {}
    override fun onEnable() {}
}
