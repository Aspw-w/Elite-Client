package com.instrumentalist.elite.hacks.features.render

import com.instrumentalist.elite.events.features.RenderEvent
import com.instrumentalist.elite.events.features.WorldEvent
import com.instrumentalist.elite.hacks.Module
import com.instrumentalist.elite.hacks.ModuleCategory
import com.instrumentalist.elite.hacks.ModuleManager
import com.instrumentalist.elite.hacks.features.combat.KillAura
import com.instrumentalist.elite.utils.IMinecraft
import com.instrumentalist.elite.utils.render.RegionPos
import com.instrumentalist.elite.utils.render.RenderUtil
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.util.math.BlockPos
import org.lwjgl.glfw.GLFW
import org.lwjgl.opengl.GL11

class TargetESP : Module("Target ESP", ModuleCategory.Render, GLFW.GLFW_KEY_UNKNOWN, true, true) {

    override fun onDisable() {
        if (IMinecraft.mc.player == null) return

        RenderUtil.starLastFrameTime = System.nanoTime()
        RenderUtil.starRotationAngle = 0.0
        RenderUtil.ringLastFrameTime = System.nanoTime()
        RenderUtil.ringVerticalTime = 0.0
    }

    override fun onEnable() {}

    override fun onWorld(event: WorldEvent) {
        RenderUtil.starLastFrameTime = System.nanoTime()
        RenderUtil.starRotationAngle = 0.0
        RenderUtil.ringLastFrameTime = System.nanoTime()
        RenderUtil.ringVerticalTime = 0.0
    }

    override fun onRender(event: RenderEvent) {
        if (IMinecraft.mc.player == null || IMinecraft.mc.world == null || IMinecraft.mc.player!!.age < 100f) return

        val targets: MutableList<Entity> = mutableListOf()
        if (ModuleManager.getModuleState(KillAura()) && KillAura.closestEntity != null)
            targets.add(KillAura.closestEntity)

        if (targets.isEmpty()) {
            RenderUtil.starLastFrameTime = System.nanoTime()
            RenderUtil.starRotationAngle = 0.0
            RenderUtil.ringLastFrameTime = System.nanoTime()
            RenderUtil.ringVerticalTime = 0.0
            return
        }

        GL11.glEnable(GL11.GL_BLEND)
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
        GL11.glDisable(GL11.GL_DEPTH_TEST)

        val matrixStack = event.matrix
        val partialTicks = event.partialTicks

        val region: RegionPos = RegionPos.of(BlockPos(0, 0, 0))

        matrixStack.push()
        RenderUtil.applyRegionalRenderOffset(matrixStack, region)

        for (i in targets) {
            RenderUtil.drawStarAtEntity(i, matrixStack, partialTicks)
            RenderUtil.drawRisingRingAroundEntity(i, matrixStack, partialTicks)
        }

        matrixStack.pop()

        GL11.glEnable(GL11.GL_DEPTH_TEST)
        GL11.glDisable(GL11.GL_BLEND)
    }
}