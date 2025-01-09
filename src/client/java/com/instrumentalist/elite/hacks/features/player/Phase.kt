package com.instrumentalist.elite.hacks.features.player

import com.instrumentalist.elite.events.features.UpdateEvent
import com.instrumentalist.elite.hacks.Module
import com.instrumentalist.elite.hacks.ModuleCategory
import com.instrumentalist.elite.utils.IMinecraft
import com.instrumentalist.elite.utils.math.TickTimer
import com.instrumentalist.elite.utils.math.TimerUtil
import com.instrumentalist.elite.utils.move.MovementUtil
import com.instrumentalist.elite.utils.packet.PacketUtil
import com.instrumentalist.elite.utils.value.ListValue
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.Vec3d
import org.lwjgl.glfw.GLFW
import java.util.*

class Phase : Module("Phase", ModuleCategory.Player, GLFW.GLFW_KEY_UNKNOWN, false, true) {
    @Setting
    private val mode = ListValue("Mode", arrayOf("NCP", "AAC 4"), "NCP")

    private var isNcpClipping = false
    private val ncpTickTimer = TickTimer()

    override fun tag(): String {
        return mode.get()
    }

    override fun onDisable() {
        isNcpClipping = false
        ncpTickTimer.reset()

        if (IMinecraft.mc.player == null) return

        when (mode.get().lowercase(Locale.getDefault())) {
            "ncp" -> TimerUtil.reset()

            "aac 4" -> {
                PacketUtil.sendPacket(
                    PlayerMoveC2SPacket.PositionAndOnGround(
                        IMinecraft.mc.player!!.x,
                        IMinecraft.mc.player!!.y - 0.00000001,
                        IMinecraft.mc.player!!.z,
                        false, IMinecraft.mc.player!!.horizontalCollision
                    )
                )
                PacketUtil.sendPacket(
                    PlayerMoveC2SPacket.PositionAndOnGround(
                        IMinecraft.mc.player!!.x,
                        IMinecraft.mc.player!!.y - 1,
                        IMinecraft.mc.player!!.z,
                        false, IMinecraft.mc.player!!.horizontalCollision
                    )
                )
            }
        }
    }

    override fun onEnable() {
        if (IMinecraft.mc.player == null) return

        if (mode.get().equals("aac 4", true)) this@Phase.toggle()
    }

    override fun onUpdate(event: UpdateEvent) {
        if (IMinecraft.mc.player == null) return

        if (mode.get().equals("ncp", true)) {
            if (IMinecraft.mc.player!!.horizontalCollision) isNcpClipping = true
            if (!isNcpClipping) return

            ncpTickTimer.update()

            if (ncpTickTimer.hasTimePassed(3)) {
                MovementUtil.stopMoving()
                TimerUtil.reset()
                ncpTickTimer.reset()
                isNcpClipping = false
            } else if (ncpTickTimer.hasTimePassed(1)) {
                val offset = if (ncpTickTimer.hasTimePassed(2)) 1.7 else 0.06
                val direction = Math.toRadians((IMinecraft.mc.player!!.yaw % 360 + 360) % 360.toDouble()).toFloat()

                val newPos = Vec3d(
                    IMinecraft.mc.player!!.x + (-MathHelper.sin(direction) * offset),
                    IMinecraft.mc.player!!.y,
                    IMinecraft.mc.player!!.z + (MathHelper.cos(direction) * offset)
                )

                TimerUtil.timerSpeed = 0.3f
                MovementUtil.stopMoving()
                IMinecraft.mc.player!!.setPosition(newPos.x, newPos.y, newPos.z)
            }
        }
    }
}
