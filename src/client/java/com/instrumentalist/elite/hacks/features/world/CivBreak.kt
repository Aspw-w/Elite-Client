package com.instrumentalist.elite.hacks.features.world

import com.instrumentalist.elite.events.features.MotionEvent
import com.instrumentalist.elite.events.features.SendPacketEvent
import com.instrumentalist.elite.events.features.WorldEvent
import com.instrumentalist.elite.hacks.Module
import com.instrumentalist.elite.hacks.ModuleCategory
import com.instrumentalist.elite.hacks.ModuleManager
import com.instrumentalist.elite.hacks.features.player.AutoTool
import com.instrumentalist.elite.hacks.features.player.Scaffold
import com.instrumentalist.elite.utils.ChatUtil
import com.instrumentalist.elite.utils.IMinecraft
import com.instrumentalist.elite.utils.entity.PlayerUtil
import com.instrumentalist.elite.utils.math.MSTimer
import com.instrumentalist.elite.utils.math.TargetUtil
import com.instrumentalist.elite.utils.math.ToolUtil
import com.instrumentalist.elite.utils.packet.PacketUtil
import com.instrumentalist.elite.utils.rotation.RotationUtil
import com.instrumentalist.elite.utils.value.BooleanValue
import com.instrumentalist.elite.utils.value.FloatValue
import com.instrumentalist.elite.utils.value.IntValue
import com.instrumentalist.elite.utils.value.ListValue
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket
import net.minecraft.util.Hand
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.Vec3d
import org.lwjgl.glfw.GLFW
import java.util.*

class CivBreak : Module("Civ Break", ModuleCategory.World, GLFW.GLFW_KEY_UNKNOWN, false, true) {
    companion object {
        private val range = FloatValue("Range", 3f, 0.1f, 6f, "m")

        private val fastSpeed = IntValue("Fast Speed", 10, 1, 10)

        private val rotations = BooleanValue("Rotations", true)

        var wasBreaking = false
    }

    private var rotateTimer = MSTimer()
    private var originalSlot: Int = -1
    private var pos: BlockPos? = null
    private var direction: Direction? = null

    override fun onDisable() {
        if (wasBreaking && IMinecraft.mc.player != null) {
            if (originalSlot != -1 && originalSlot != IMinecraft.mc.player!!.inventory.selectedSlot) {
                IMinecraft.mc.player!!.inventory.selectedSlot = originalSlot
                originalSlot = -1
            }
            TargetUtil.noKillAura = false
        }
        if (wasBreaking)
            RotationUtil.reset()
        wasBreaking = false
        pos = null
        direction = null
    }

    override fun onEnable() {}

    override fun onWorld(event: WorldEvent) {
        if (wasBreaking) {
            if (originalSlot != -1 && originalSlot != IMinecraft.mc.player!!.inventory.selectedSlot) {
                IMinecraft.mc.player!!.inventory.selectedSlot = originalSlot
                originalSlot = -1
            }
            TargetUtil.noKillAura = false
        }
        if (wasBreaking)
            RotationUtil.reset()
        wasBreaking = false
        pos = null
        direction = null
    }

    override fun onMotion(event: MotionEvent) {
        if (IMinecraft.mc.player == null || IMinecraft.mc.world == null || ModuleManager.getModuleState(Scaffold())) return

        if (pos == null || direction == null || IMinecraft.mc.world!!.getBlockState(pos).isAir || (range.get() * 10) < IMinecraft.mc.player!!.squaredDistanceTo(
                Vec3d(
                    pos!!.x.toDouble(),
                    pos!!.y.toDouble(),
                    pos!!.z.toDouble()
                )
            )
        ) {
            if (wasBreaking) {
                if (rotations.get())
                    RotationUtil.reset()
                if (originalSlot != -1 && originalSlot != IMinecraft.mc.player!!.inventory.selectedSlot) {
                    IMinecraft.mc.player!!.inventory.selectedSlot = originalSlot
                    originalSlot = -1
                }
                TargetUtil.noKillAura = false
                wasBreaking = false
            }
            return
        }

        if (rotations.get())
            RotationUtil.aimAtBlock(
                Vec3d.ofCenter(pos),
                45f,
                true,
                10f,
                0f,
                0f
            )

        wasBreaking = true
        TargetUtil.noKillAura = true

        if (ModuleManager.getModuleState(AutoTool())) {
            val bestToolSlot = ToolUtil.findBestTool(pos!!)
            if (bestToolSlot != -1 && bestToolSlot != IMinecraft.mc.player!!.inventory.selectedSlot) {
                if (originalSlot == -1)
                    originalSlot = IMinecraft.mc.player!!.inventory.selectedSlot
                IMinecraft.mc.player!!.inventory.selectedSlot = bestToolSlot
            }
        }

        if (rotateTimer.hasTimePassed(1000L / (fastSpeed.get() + 2))) {
            PacketUtil.sendPacket(HandSwingC2SPacket(Hand.MAIN_HAND))
            repeat(2) {
                PacketUtil.sendPacket(
                    PlayerActionC2SPacket(
                        PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK,
                        pos,
                        direction
                    )
                )
            }

            rotateTimer.reset()
        }
    }

    override fun onSendPacket(event: SendPacketEvent) {
        val packet = event.packet

        if (packet is PlayerActionC2SPacket && packet.action == PlayerActionC2SPacket.Action.START_DESTROY_BLOCK) {
            pos = packet.pos
            direction = packet.direction
        }
    }
}