package com.instrumentalist.elite.utils.render

import com.instrumentalist.elite.hacks.features.player.MurdererDetector
import com.instrumentalist.elite.utils.IMinecraft
import com.instrumentalist.elite.utils.math.TargetUtil
import com.mojang.blaze3d.systems.RenderSystem
import net.minecraft.client.gl.ShaderProgramKeys
import net.minecraft.client.render.BufferRenderer
import net.minecraft.client.render.Camera
import net.minecraft.client.render.VertexFormat
import net.minecraft.client.render.VertexFormats
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.Vec3d
import org.joml.Vector3f
import kotlin.math.cos
import kotlin.math.sin

object RenderUtil {
    val mc = IMinecraft.mc

    var starLastFrameTime: Long = System.nanoTime()
    var starRotationAngle: Double = 0.0

    var ringLastFrameTime: Long = System.nanoTime()
    var ringVerticalTime: Double = 0.0

    fun renderBoxes(
        entityList: MutableList<Entity>,
        matrixStack: MatrixStack,
        partialTicks: Float,
        regionVec: Vec3d
    ) {
        if (entityList.isEmpty()) return

        val extraSize = 0.1f
        val bb = Box(-0.5, 0.0, -0.5, 0.5, 1.0, 0.5)

        for (entity in entityList) {
            val lerpedPos = getLerpedPos(entity, partialTicks).subtract(regionVec)

            matrixStack.push()

            matrixStack.translate(lerpedPos.x, lerpedPos.y, lerpedPos.z)
            matrixStack.scale(entity.width + extraSize, entity.height + extraSize, entity.width + extraSize)

            if (entity is PlayerEntity && MurdererDetector.murderers.contains(entity))
                RenderSystem.setShaderColor(1f, 0f, 0f, 1f)
            else if (entity is LivingEntity && TargetUtil.isTeammate(entity))
                RenderSystem.setShaderColor(0.5f, 1f, 0f, 1f)
            else if (entity is LivingEntity && TargetUtil.isBot(entity))
                RenderSystem.setShaderColor(0.6f, 0.6f, 0.6f, 1f)
            else if (entity is LivingEntity && entity.hurtTime >= 5)
                RenderSystem.setShaderColor(1f, 0f, 0f, 0.8f)
            else RenderSystem.setShaderColor(0f, 1f, 0f, 0.8f)

            drawOutlinedBox(bb, matrixStack)

            matrixStack.pop()
        }
    }

    fun renderSingleBox(
        vec: Vec3d,
        matrixStack: MatrixStack,
        regionVec: Vec3d,
        scale: Vector3f
    ) {
        val pos = vec.subtract(regionVec)
        val bb = Box(-0.5, 0.0, -0.5, 0.5, 1.0, 0.5)

        matrixStack.push()

        matrixStack.translate(pos.x, pos.y, pos.z)
        matrixStack.scale(scale.x, scale.y, scale.z)

        RenderSystem.setShaderColor(1f, 1f, 0f, 1f)

        drawOutlinedBox(bb, matrixStack)

        matrixStack.pop()
    }

    private fun drawOutlinedBox(bb: Box, matrixStack: MatrixStack) {
        val matrix = matrixStack.peek().positionMatrix
        val tessellator = RenderSystem.renderThreadTesselator()
        RenderSystem.setShader(ShaderProgramKeys.POSITION)

        val bufferBuilder = tessellator
            .begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION)

        val vertices = listOf(
            Triple(bb.minX, bb.minY, bb.minZ), Triple(bb.maxX, bb.minY, bb.minZ),
            Triple(bb.maxX, bb.minY, bb.minZ), Triple(bb.maxX, bb.minY, bb.maxZ),
            Triple(bb.maxX, bb.minY, bb.maxZ), Triple(bb.minX, bb.minY, bb.maxZ),
            Triple(bb.minX, bb.minY, bb.maxZ), Triple(bb.minX, bb.minY, bb.minZ),

            Triple(bb.minX, bb.minY, bb.minZ), Triple(bb.minX, bb.maxY, bb.minZ),
            Triple(bb.maxX, bb.minY, bb.minZ), Triple(bb.maxX, bb.maxY, bb.minZ),
            Triple(bb.maxX, bb.minY, bb.maxZ), Triple(bb.maxX, bb.maxY, bb.maxZ),
            Triple(bb.minX, bb.minY, bb.maxZ), Triple(bb.minX, bb.maxY, bb.maxZ),

            Triple(bb.minX, bb.maxY, bb.minZ), Triple(bb.maxX, bb.maxY, bb.minZ),
            Triple(bb.maxX, bb.maxY, bb.minZ), Triple(bb.maxX, bb.maxY, bb.maxZ),
            Triple(bb.maxX, bb.maxY, bb.maxZ), Triple(bb.minX, bb.maxY, bb.maxZ),
            Triple(bb.minX, bb.maxY, bb.maxZ), Triple(bb.minX, bb.maxY, bb.minZ)
        )

        for ((x, y, z) in vertices) {
            bufferBuilder.vertex(matrix, x.toFloat(), y.toFloat(), z.toFloat())
        }

        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end())
    }

    fun drawStarAtEntity(entity: Entity, matrixStack: MatrixStack, partialTicks: Float) {
        val interpolatedX = entity.prevX + (entity.x - entity.prevX) * partialTicks
        val interpolatedY = entity.prevY + (entity.y - entity.prevY) * partialTicks
        val interpolatedZ = entity.prevZ + (entity.z - entity.prevZ) * partialTicks

        val matrix = matrixStack.peek().positionMatrix
        val tessellator = RenderSystem.renderThreadTesselator()
        RenderSystem.setShader(ShaderProgramKeys.POSITION)

        val bufferBuilder = tessellator
            .begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION)

        val radius = entity.width + 1.0
        val innerRadius = radius * 0.5

        val centerX = interpolatedX
        val centerY = interpolatedY + 0.01
        val centerZ = interpolatedZ

        val currentTime = System.nanoTime()
        val deltaTime = (currentTime - starLastFrameTime) / 1_000_000_000.0
        starLastFrameTime = currentTime

        starRotationAngle += deltaTime * 150.0
        if (starRotationAngle > 360.0) starRotationAngle -= 360.0

        val currentRotation = Math.toRadians(starRotationAngle)

        val vertices = mutableListOf<Triple<Double, Double, Double>>()
        for (i in 0 until 10) {
            val angle = Math.toRadians(36.0 * i) + currentRotation
            val r = if (i % 2 == 0) radius else innerRadius
            val x = centerX + r * cos(angle)
            val z = centerZ + r * sin(angle)
            vertices.add(Triple(x, centerY, z))
        }

        for (i in vertices.indices) {
            val (x1, y1, z1) = vertices[i]
            val (x2, y2, z2) = vertices[(i + 1) % vertices.size]
            bufferBuilder.vertex(matrix, x1.toFloat(), y1.toFloat(), z1.toFloat())
            bufferBuilder.vertex(matrix, x2.toFloat(), y2.toFloat(), z2.toFloat())
        }

        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end())
    }

    fun drawRisingRingAroundEntity(entity: Entity, matrixStack: MatrixStack, partialTicks: Float) {
        val interpolatedX = entity.prevX + (entity.x - entity.prevX) * partialTicks
        val interpolatedY = entity.prevY + (entity.y - entity.prevY) * partialTicks
        val interpolatedZ = entity.prevZ + (entity.z - entity.prevZ) * partialTicks

        val matrix = matrixStack.peek().positionMatrix
        val tessellator = RenderSystem.renderThreadTesselator()
        RenderSystem.setShader(ShaderProgramKeys.POSITION)

        val bufferBuilder = tessellator
            .begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION)

        val radius = entity.width + 0.2
        val ringSegments = 36
        val verticalAmplitude = entity.height * 0.5
        val verticalSpeed = 5

        val currentTime = System.nanoTime()
        val deltaTime = (currentTime - ringLastFrameTime) / 1_000_000_000.0
        ringLastFrameTime = currentTime

        ringVerticalTime += deltaTime * verticalSpeed
        if (ringVerticalTime > Math.PI * 2) ringVerticalTime -= Math.PI * 2

        val verticalOffset = sin(ringVerticalTime) * verticalAmplitude

        val centerX = interpolatedX
        val centerY = interpolatedY + entity.height * 0.5 + verticalOffset
        val centerZ = interpolatedZ

        val vertices = mutableListOf<Triple<Double, Double, Double>>()
        for (i in 0 until ringSegments) {
            val angle = Math.toRadians(360.0 / ringSegments * i)
            val x = centerX + radius * cos(angle)
            val z = centerZ + radius * sin(angle)
            vertices.add(Triple(x, centerY, z))
        }

        for (i in vertices.indices) {
            val (x1, y1, z1) = vertices[i]
            val (x2, y2, z2) = vertices[(i + 1) % vertices.size]
            bufferBuilder.vertex(matrix, x1.toFloat(), y1.toFloat(), z1.toFloat())
            bufferBuilder.vertex(matrix, x2.toFloat(), y2.toFloat(), z2.toFloat())
        }

        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end())
    }

    fun drawLine(start: Vec3d, end: Vec3d, matrixStack: MatrixStack) {
        val matrix = matrixStack.peek().positionMatrix
        val tessellator = RenderSystem.renderThreadTesselator()
        RenderSystem.setShader(ShaderProgramKeys.POSITION)

        val bufferBuilder = tessellator
            .begin(VertexFormat.DrawMode.DEBUG_LINE_STRIP, VertexFormats.POSITION)

        bufferBuilder.vertex(matrix, start.x.toFloat(), start.y.toFloat(), start.z.toFloat())
        bufferBuilder.vertex(matrix, end.x.toFloat(), end.y.toFloat(), end.z.toFloat())

        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end())
    }

    fun applyRegionalRenderOffset(
        matrixStack: MatrixStack,
        region: RegionPos
    ) {
        val offset: Vec3d = region.toVec3d().subtract(getCameraPos())
        matrixStack.translate(offset.x, offset.y, offset.z)
    }

    private fun getCameraPos(): Vec3d {
        val camera: Camera = mc.blockEntityRenderDispatcher.camera ?: return Vec3d.ZERO

        return camera.pos
    }

    fun getCameraRegion(): RegionPos {
        return RegionPos.of(getCameraBlockPos())
    }

    private fun getCameraBlockPos(): BlockPos? {
        val camera: Camera = mc.blockEntityRenderDispatcher.camera ?: return BlockPos.ORIGIN

        return camera.blockPos
    }

    private fun getLerpedPos(entity: Entity, partialTicks: Float): Vec3d {
        if (entity.isRemoved) return entity.pos

        val x = MathHelper.lerp(partialTicks.toDouble(), entity.lastRenderX, entity.x)
        val y = MathHelper.lerp(partialTicks.toDouble(), entity.lastRenderY, entity.y)
        val z = MathHelper.lerp(partialTicks.toDouble(), entity.lastRenderZ, entity.z)
        return Vec3d(x, y, z)
    }
}