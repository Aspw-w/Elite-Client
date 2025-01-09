package com.instrumentalist.elite.utils.rotation

import com.instrumentalist.elite.hacks.ModuleManager
import com.instrumentalist.elite.utils.ChatUtil
import com.instrumentalist.elite.utils.IMinecraft
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.Vec3d
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.sqrt
import kotlin.random.Random

object RotationUtil {
    val mc = IMinecraft.mc

    var currentYaw: Float? = null
    var currentPitch: Float? = null
    private var isRotating = false

    fun reset() {
        if (isRotating) {
            currentYaw = null
            currentPitch = null
            ModuleManager.pitchTick = 0
            ModuleManager.interpolatedYaw = null
            ModuleManager.interpolatedPitch = null
            isRotating = false
        }
    }

    fun getRotationsEntity(entity: LivingEntity): Pair<Float, Float> {
        return getRotations(entity.x, entity.y + entity.standingEyeHeight - 0.4, entity.z)
    }

    private fun getRotations(posX: Double, posY: Double, posZ: Double): Pair<Float, Float> {
        val player = mc.player!!
        val x = posX - player.x
        val y = posY - (player.y + player.standingEyeHeight.toDouble())
        val z = posZ - player.z
        val dist = MathHelper.sqrt((x * x + z * z).toFloat()).toDouble()
        val yaw = (atan2(z, x) * 180.0 / 3.141592653589793).toFloat() - 90.0f
        val pitch = (-(atan2(y, dist) * 180.0 / 3.141592653589793)).toFloat()
        return Pair(yaw, pitch)
    }

    fun getCameraRotationVec(): Vec3d {
        return getRotationVector(IMinecraft.mc.player!!.yaw, IMinecraft.mc.player!!.pitch)
    }

    private fun getRotationVector(pitch: Float, yaw: Float): Vec3d {
        val f = pitch * 0.017453292f
        val g = -yaw * 0.017453292f
        val h = MathHelper.cos(g)
        val i = MathHelper.sin(g)
        val j = MathHelper.cos(f)
        val k = MathHelper.sin(f)
        return Vec3d((i * j).toDouble(), (-k).toDouble(), (h * j).toDouble())
    }

    private fun smoothRotate(current: Float, target: Float, speed: Float): Float {
        var delta = (target - current).wrapDegrees()
        if (delta > speed) delta = speed
        if (delta < -speed) delta = -speed
        return current + delta
    }

    private fun calculateRotations(
        playerX: Double, playerY: Double, playerZ: Double,
        targetX: Double, targetY: Double, targetZ: Double
    ): Pair<Float, Float> {
        val diffX = targetX - playerX
        val diffY = targetY - playerY
        val diffZ = targetZ - playerZ

        val distanceXZ = sqrt(diffX * diffX + diffZ * diffZ)
        val yaw = Math.toDegrees(atan2(diffZ, diffX)) - 90.0
        val pitch = -Math.toDegrees(atan2(diffY, distanceXZ))
        return yaw.toFloat() to pitch.toFloat()
    }

    fun smoothLookAt(
        playerX: Double, playerY: Double, playerZ: Double,
        targetX: Double, targetY: Double, targetZ: Double,
        speed: Float
    ): Pair<Float, Float> {
        val (targetYaw, targetPitch) = calculateRotations(playerX, playerY, playerZ, targetX, targetY, targetZ)

        val baseYaw = if (currentYaw != null) currentYaw!! else mc.player!!.yaw
        val basePitch = if (currentPitch != null) currentPitch!! else mc.player!!.pitch

        val newYaw = smoothRotate(baseYaw, targetYaw, speed)
        val newPitch = smoothRotate(basePitch, targetPitch, speed)

        return newYaw to newPitch
    }

    private fun Float.wrapDegrees(): Float {
        var angle = this % 360
        if (angle >= 180) angle -= 360
        if (angle < -180) angle += 360
        return angle
    }

    fun setRotation(targetYaw: Float, targetPitch: Float, speed: Float = 90f) {
        if (currentYaw == null)
            currentYaw = mc.player?.yaw!! + 0.001f
        if (currentPitch == null)
            currentPitch = mc.player?.pitch!! + 0.001f

        isRotating = true

        val rotYaw = smoothRotation(currentYaw!!, targetYaw, speed)
        val rotPitch = smoothRotation(currentPitch!!, targetPitch, speed)

        applyHumanLikeRotation(rotYaw, rotPitch)
    }

    private fun getRandomOffset(maxOffset: Float): Float {
        return Random.nextFloat() * maxOffset - maxOffset / 2
    }

    private fun smoothRotation(current: Float, target: Float, speed: Float): Float {
        val diff = ((target - current + 540) % 360) - 180
        val maxStep = abs(diff).coerceAtMost(speed)
        return (current + maxStep * (diff / abs(diff))) % 360
    }

    private fun addJitter(value: Float, jitterAmount: Float): Float {
        return value + (Random.nextFloat() * jitterAmount - jitterAmount / 2)
    }

    private fun humanizeRotation(
        currentYaw: Float,
        currentPitch: Float,
        targetYaw: Float,
        targetPitch: Float,
        speed: Float,
        jitterAmount: Float,
    ): Pair<Float, Float> {
        val newYaw = smoothRotation(currentYaw, addJitter(targetYaw, jitterAmount), speed)
        val newPitch = smoothRotation(currentPitch, addJitter(targetPitch, jitterAmount), speed)
        return Pair(newYaw, newPitch)
    }

    private fun applyHumanLikeRotation(yaw: Float, pitch: Float) {
        currentYaw = yaw + getRandomOffset(0.1f)
        currentPitch = pitch + getRandomOffset(0.1f)
    }

    private fun applyTimingDelay(minDelay: Long, maxDelay: Long) {
        val delay = Random.nextLong(minDelay, maxDelay)
        Thread.sleep(delay)
    }

    fun aimAtEntity(
        target: Entity,
        speed: Float,
        random: Boolean,
        randomSpeed: Float,
        baseMaxOffset: Float,
        jitterAmount: Float = 1.0f
    ) {
        Thread {
            val adjustedMaxOffset = baseMaxOffset * (speed / 10)
            val targetCenterX = target.x + getRandomOffset(adjustedMaxOffset)
            val targetCenterY = target.y + target.height / 2 + getRandomOffset(adjustedMaxOffset)
            val targetCenterZ = target.z + getRandomOffset(adjustedMaxOffset)

            val playerEyeY = mc.player?.y!! + mc.player?.standingEyeHeight!!

            val xDiff = targetCenterX - mc.player?.x!!
            val yDiff = targetCenterY - playerEyeY
            val zDiff = targetCenterZ - mc.player?.z!!
            val distance = sqrt(xDiff * xDiff + zDiff * zDiff)

            val targetYaw = (atan2(zDiff, xDiff) * (180.0 / Math.PI)).toFloat() - 90.0f
            val targetPitch = -(atan2(yDiff, distance) * (180.0 / Math.PI)).toFloat()

            isRotating = true

            if (currentYaw == null)
                currentYaw = mc.player?.yaw ?: 0f
            if (currentPitch == null)
                currentPitch = mc.player?.pitch ?: 0f

            if (currentPitch!! > 90) {
                currentPitch = 90f
                return@Thread
            }

            if (currentPitch!! < -90) {
                currentPitch = -90f
                return@Thread
            }

            var rotSpeed = if (random) (speed + Random.nextFloat() * randomSpeed) else speed

            if (rotSpeed <= 0) rotSpeed = 0f

            val (newYaw, newPitch) = humanizeRotation(
                currentYaw!!,
                currentPitch!!,
                targetYaw,
                targetPitch,
                rotSpeed,
                jitterAmount
            )

            currentYaw = newYaw
            currentPitch = newPitch

            applyHumanLikeRotation(currentYaw!!, currentPitch!!)
            applyTimingDelay(50, 100)
        }.start()
    }

    private fun normalizeAngle(angle: Float, pitchMode: Boolean): Float {
        if (pitchMode) {
            var normalized = angle % 360
            if (normalized <= -180)
                normalized += 360
            if (normalized > 180)
                normalized -= 360
            return normalized
        } else return (angle % 360 + 360) % 360
    }

    fun interpolateAngle(current: Float, target: Float, maxSpeed: Float, interpolationStep: Float, pitchMode: Boolean): Float {
        val normalizedCurrent = normalizeAngle(current, pitchMode)
        val normalizedTarget = normalizeAngle(target, pitchMode)

        var delta = normalizedTarget - normalizedCurrent

        if (delta > 180) delta -= 360
        if (delta < -180) delta += 360

        val clampedDelta = delta.coerceIn(-maxSpeed * interpolationStep, maxSpeed * interpolationStep)

        val interpolatedAngle = normalizedCurrent + clampedDelta

        return normalizeAngle(interpolatedAngle, pitchMode)
    }

    fun aimAtBlock(
        pos: Vec3d,
        speed: Float,
        random: Boolean,
        randomSpeed: Float,
        baseMaxOffset: Float,
        jitterAmount: Float = 1.0f,
        minDelay: Long = 50,
        maxDelay: Long = 100,
    ) {
        Thread {
            val adjustedMaxOffset = baseMaxOffset * (speed / 10)
            val targetX = pos.x + getRandomOffset(adjustedMaxOffset)
            val targetY = pos.y + getRandomOffset(adjustedMaxOffset)
            val targetZ = pos.z + getRandomOffset(adjustedMaxOffset)

            val playerEyeY = mc.player?.y!! + mc.player?.standingEyeHeight!!

            val xDiff = targetX - mc.player?.x!!
            val yDiff = targetY - playerEyeY
            val zDiff = targetZ - mc.player?.z!!
            val distance = sqrt(xDiff * xDiff + zDiff * zDiff)

            val targetYaw = (atan2(zDiff, xDiff) * (180.0 / Math.PI)).toFloat() - 90.0f
            val targetPitch = -(atan2(yDiff, distance) * (180.0 / Math.PI)).toFloat()

            isRotating = true

            if (currentYaw == null)
                currentYaw = mc.player?.yaw ?: 0f
            if (currentPitch == null)
                currentPitch = mc.player?.pitch ?: 0f

            if (currentPitch!! > 90) {
                currentPitch = 90f
                return@Thread
            }

            if (currentPitch!! < -90) {
                currentPitch = -90f
                return@Thread
            }

            var rotSpeed = if (random) (speed + Random.nextFloat() * randomSpeed) else speed

            if (rotSpeed <= 0) rotSpeed = 0f

            val (newYaw, newPitch) = humanizeRotation(
                currentYaw!!,
                currentPitch!!,
                targetYaw,
                targetPitch,
                rotSpeed,
                jitterAmount
            )

            currentYaw = newYaw
            currentPitch = newPitch

            applyHumanLikeRotation(currentYaw!!, currentPitch!!)
            applyTimingDelay(minDelay, maxDelay)
        }.start()
    }

    fun scaffoldRotation(
        pos: Vec3d,
        speed: Float,
        random: Boolean,
        randomSpeed: Float,
        minDelay: Long = 50,
        maxDelay: Long = 100,
    ) {
        Thread {
            val playerPos = mc.player!!.blockPos
            var closestBlock: BlockPos? = null
            var closestDistance = Double.MAX_VALUE

            for (x in -3..3) {
                for (y in -3..3) {
                    for (z in -3..3) {
                        val currentPos = playerPos.add(x, y, z)
                        if (!mc.world!!.isAir(currentPos)) {
                            val distance = pos.squaredDistanceTo(Vec3d.ofCenter(currentPos))
                            if (distance < closestDistance) {
                                closestDistance = distance
                                closestBlock = currentPos
                            }
                        }
                    }
                }
            }

            if (closestBlock == null)
                return@Thread

            val targetBlock = closestBlock
            val targetPos = Vec3d.ofCenter(targetBlock)
            val playerEyePos = mc.player!!.pos.add(0.0, mc.player!!.standingEyeHeight.toDouble(), 0.0)

            val xDiff = targetPos.x - playerEyePos.x
            val yDiff = targetPos.y - playerEyePos.y - 2f
            val zDiff = targetPos.z - playerEyePos.z
            val distance = sqrt(xDiff * xDiff + zDiff * zDiff)

            val targetYaw = (atan2(zDiff, xDiff) * (180.0 / Math.PI)).toFloat() - 90.0f
            var targetPitch = -(atan2(yDiff, distance) * (180.0 / Math.PI)).toFloat()

            if (targetPitch >= 89)
                targetPitch = 89f

            isRotating = true

            if (currentYaw == null)
                currentYaw = mc.player?.yaw ?: 0f
            if (currentPitch == null)
                currentPitch = mc.player?.pitch ?: 0f

            if (currentPitch!! > 90) {
                currentPitch = 90f
                return@Thread
            }

            if (currentPitch!! < -90) {
                currentPitch = -90f
                return@Thread
            }

            var rotSpeed = if (random) (speed + Random.nextFloat() * randomSpeed) else speed

            if (rotSpeed <= 0) rotSpeed = 0f

            val (newYaw, newPitch) = humanizeRotation(
                currentYaw!!,
                currentPitch!!,
                targetYaw,
                targetPitch,
                rotSpeed,
                0.1f
            )

            currentYaw = newYaw
            currentPitch = newPitch

            applyHumanLikeRotation(currentYaw!!, currentPitch!!)
            applyTimingDelay(minDelay, maxDelay)

            try {
                val yawDiff = abs(currentYaw!! - targetYaw)
                val pitchDiff = abs(currentPitch!! - targetPitch)
            } catch (_: Exception) {
            }
        }.start()
    }
}