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
import net.minecraft.block.BedBlock
import net.minecraft.block.ChestBlock
import net.minecraft.block.entity.BarrelBlockEntity
import net.minecraft.block.entity.BedBlockEntity
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.ChestBlockEntity
import net.minecraft.block.entity.EnderChestBlockEntity
import net.minecraft.block.entity.ShulkerBoxBlockEntity
import net.minecraft.block.entity.TrappedChestBlockEntity
import net.minecraft.block.enums.BedPart
import net.minecraft.block.enums.ChestType
import net.minecraft.client.network.ClientPlayerEntity
import net.minecraft.entity.Entity
import net.minecraft.entity.decoration.ArmorStandEntity
import net.minecraft.entity.player.PlayerEntity
import org.lwjgl.glfw.GLFW
import org.lwjgl.opengl.GL11
import kotlin.streams.asSequence

class BlockESP : Module("Block ESP", ModuleCategory.Render, GLFW.GLFW_KEY_UNKNOWN, false, true) {
    private val chest = BooleanValue("Chest", true)

    private val bed = BooleanValue("Bed", true)

    override fun onDisable() {}
    override fun onEnable() {}

    override fun onRender(event: RenderEvent) {
        if (IMinecraft.mc.player == null || IMinecraft.mc.world == null) return

        val blockEntities = ChunkUtil.getLoadedBlockEntities().asSequence()
            .filter { shouldBoxRender(it) }
            .sortedBy { it.pos.getSquaredDistance(IMinecraft.mc.player!!.pos) }
            .take(5)
            .toMutableList()

        if (blockEntities.isNotEmpty()) {
            GL11.glEnable(GL11.GL_BLEND)
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
            GL11.glEnable(GL11.GL_CULL_FACE)
            GL11.glDisable(GL11.GL_DEPTH_TEST)

            val matrixStack = event.matrix
            val partialTicks = event.partialTicks

            val region: RegionPos = RenderUtil.getCameraRegion()
            val regionVec = region.toVec3d()

            matrixStack.push()
            RenderUtil.applyRegionalRenderOffset(matrixStack, region)

            RenderUtil.renderBlockBox(blockEntities, matrixStack, partialTicks, regionVec)

            matrixStack.pop()

            RenderSystem.setShaderColor(1f, 1f, 1f, 1f)

            GL11.glEnable(GL11.GL_DEPTH_TEST)
            GL11.glDisable(GL11.GL_BLEND)
        }
    }

    private fun shouldBoxRender(blockEntity: BlockEntity): Boolean {
        return chest.get() && (blockEntity is ChestBlockEntity && (blockEntity.cachedState.get(ChestBlock.CHEST_TYPE) == ChestType.SINGLE || blockEntity.cachedState.get(ChestBlock.CHEST_TYPE) != ChestType.SINGLE && blockEntity.cachedState.get(ChestBlock.CHEST_TYPE) == ChestType.LEFT) || blockEntity is EnderChestBlockEntity || blockEntity is ShulkerBoxBlockEntity || blockEntity is BarrelBlockEntity) || bed.get() && blockEntity is BedBlockEntity && blockEntity.cachedState.get(BedBlock.PART) == BedPart.HEAD
    }
}