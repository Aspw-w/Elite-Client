package com.instrumentalist.elite.hacks.features.render

import com.instrumentalist.elite.hacks.Module
import com.instrumentalist.elite.hacks.ModuleCategory
import com.instrumentalist.elite.hacks.ModuleManager
import com.instrumentalist.elite.utils.value.BooleanValue
import com.instrumentalist.mixin.oringo.IEntityRenderState
import net.minecraft.client.network.ClientPlayerEntity
import net.minecraft.client.render.entity.state.EntityRenderState
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import org.lwjgl.glfw.GLFW

class NameTags : Module("Name Tags", ModuleCategory.Render, GLFW.GLFW_KEY_UNKNOWN, false, true) {
    companion object {
        @Setting
        private val onlyPlayers = BooleanValue("Only Players", true)

        @Setting
        private val local = BooleanValue("Local", false)

        fun forceShouldRenderName(entity: Entity): Boolean {
            return entity is LivingEntity && !entity.isDead && (!onlyPlayers.get() || entity is PlayerEntity) && (local.get() || entity !is ClientPlayerEntity)
        }
    }

    override fun onDisable() {}
    override fun onEnable() {}
}
