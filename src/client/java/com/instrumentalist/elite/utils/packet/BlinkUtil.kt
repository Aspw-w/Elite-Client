package com.instrumentalist.elite.utils.packet

import com.instrumentalist.elite.utils.IMinecraft
import com.instrumentalist.elite.utils.move.MovementUtil
import net.minecraft.entity.EntityPose
import net.minecraft.network.packet.Packet
import net.minecraft.util.math.Vec3d

object BlinkUtil {
    val mc = IMinecraft.mc

    val packets = mutableListOf<Packet<*>>()
    private val positions = mutableListOf<Vec3d>()
    private var prevYMotion: Double? = null
    private var isStarted = false
    var limiter = false
    var blinking = false

    fun addPacket(packet: Packet<*>) {
        packets.add(packet)
    }

    fun doBlink() {
        blinking = true
        if (prevYMotion == null)
            prevYMotion = mc.player!!.velocity.y
        if (!isStarted) {
            synchronized(positions) {
                positions.add(
                    Vec3d(
                        mc.player!!.x,
                        mc.player!!.boundingBox.minY + mc.player!!.getEyeHeight(EntityPose.STANDING) / 2,
                        mc.player!!.z
                    )
                )
                positions.add(Vec3d(mc.player!!.x, mc.player!!.boundingBox.minY, mc.player!!.z))
            }
            isStarted = true
            return
        }
        synchronized(positions) { positions.add(Vec3d(mc.player?.x!!, mc.player!!.y, mc.player?.z!!)) }
    }

    fun sync(blinkSync: Boolean, noSyncResetPos: Boolean = true) {
        if (blinkSync) {
            try {
                limiter = true
                while (packets.isNotEmpty()) {
                    PacketUtil.sendPacket(packets.removeFirst())
                }
            } catch (_: Exception) {
                limiter = false
            } finally {
                limiter = false
            }
            synchronized(positions) { positions.clear() }
        } else {
            try {
                limiter = true
                packets.clear()
            } catch (_: Exception) {
                limiter = false
            } finally {
                limiter = false
            }
            if (noSyncResetPos) {
                if (positions.isNotEmpty() && positions.size > 1)
                    mc.player!!.setPosition(positions[1])
                if (prevYMotion != null)
                    MovementUtil.setVelocityY(prevYMotion!!)
            }
        }
    }

    fun stopBlink() {
        positions.clear()
        prevYMotion = null
        isStarted = false
        blinking = false
    }
}