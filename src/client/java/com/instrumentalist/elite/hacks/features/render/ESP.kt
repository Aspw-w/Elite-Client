package com.instrumentalist.elite.hacks.features.render

import com.instrumentalist.elite.events.features.RenderEvent
import com.instrumentalist.elite.hacks.Module
import com.instrumentalist.elite.hacks.ModuleCategory
import com.instrumentalist.elite.hacks.ModuleManager
import com.instrumentalist.elite.hacks.features.player.MurdererDetector
import com.instrumentalist.elite.utils.IMinecraft
import com.instrumentalist.elite.utils.render.RegionPos
import com.instrumentalist.elite.utils.render.RenderUtil
import com.instrumentalist.elite.utils.value.BooleanValue
import com.instrumentalist.elite.utils.world.ChunkUtil
import com.mojang.blaze3d.systems.RenderSystem
import net.minecraft.client.network.ClientPlayerEntity
import net.minecraft.entity.Entity
import net.minecraft.entity.decoration.ArmorStandEntity
import net.minecraft.entity.player.PlayerEntity
import org.lwjgl.glfw.GLFW
import org.lwjgl.opengl.GL11
import kotlin.streams.asSequence

class ESP : Module("ESP", ModuleCategory.Render, GLFW.GLFW_KEY_UNKNOWN, false, true) {
    @Setting
    private val onlyPlayers = BooleanValue("Only Players", true)

    @Setting
    private val local = BooleanValue("Local", false)

    override fun onDisable() {}
    override fun onEnable() {}

    override fun onRender(event: RenderEvent) {
        if (IMinecraft.mc.player == null || IMinecraft.mc.world == null) return

        val boxEntities = IMinecraft.mc.world!!.entities.asSequence()
            .filter { shouldBoxRender(it) }
            .sortedBy { it.pos.squaredDistanceTo(IMinecraft.mc.player!!.pos) }
            .take(5)
            .toMutableList()

        if (boxEntities.isNotEmpty()) {
            GL11.glEnable(GL11.GL_BLEND)
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
            GL11.glDisable(GL11.GL_DEPTH_TEST)

            val matrixStack = event.matrix
            val partialTicks = event.partialTicks

            val region: RegionPos = RenderUtil.getCameraRegion()
            val regionVec = region.toVec3d()

            matrixStack.push()
            RenderUtil.applyRegionalRenderOffset(matrixStack, region)

            RenderUtil.renderEntityBoxes(boxEntities, matrixStack, partialTicks, regionVec)

            matrixStack.pop()

            RenderSystem.setShaderColor(1f, 1f, 1f, 1f)

            GL11.glEnable(GL11.GL_DEPTH_TEST)
            GL11.glDisable(GL11.GL_BLEND)
        }
    }

    private fun shouldBoxRender(entity: Entity): Boolean {
        return (entity !is ArmorStandEntity && (!onlyPlayers.get() || entity is PlayerEntity) || ModuleManager.getModuleState(
            MurdererDetector()
        ) && MurdererDetector.murderers.contains(entity)) && (local.get() && !IMinecraft.mc.options.perspective.isFirstPerson || entity !is ClientPlayerEntity)
    }
}