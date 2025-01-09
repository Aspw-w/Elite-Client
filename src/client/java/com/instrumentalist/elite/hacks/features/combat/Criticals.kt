package com.instrumentalist.elite.hacks.features.combat;

import com.instrumentalist.elite.events.features.AttackEvent
import com.instrumentalist.elite.hacks.Module
import com.instrumentalist.elite.hacks.ModuleCategory
import com.instrumentalist.elite.hacks.ModuleManager
import com.instrumentalist.elite.hacks.features.movement.Fly
import com.instrumentalist.elite.utils.IMinecraft
import com.instrumentalist.elite.utils.packet.PacketUtil
import com.instrumentalist.elite.utils.value.BooleanValue
import com.instrumentalist.elite.utils.value.ListValue
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket
import net.minecraft.util.PlayerInput
import net.minecraft.util.math.BlockPos
import org.lwjgl.glfw.GLFW
import java.util.*

class Criticals : Module("Criticals", ModuleCategory.Combat, GLFW.GLFW_KEY_UNKNOWN, false, true) {
    @Setting
    private val mode = ListValue("Mode", arrayOf("Packet"), "Packet")

    @Setting
    private val cooldownCheck = BooleanValue("CooldownCheck", false)

    override fun tag(): String {
        return mode.get()
    }

    override fun onDisable() {}
    override fun onEnable() {}

    override fun onAttack(event: AttackEvent) {
        if (!IMinecraft.mc.player?.isOnGround!! || ModuleManager.getModuleState(Fly()) || ModuleManager.getModuleState(KillAura()) && KillAura.closestEntity != null && KillAura.tpReach.get()) return
        if (cooldownCheck.get() && IMinecraft.mc.player!!.getAttackCooldownProgress(0.0f) < 1.0f) return

        if (mode.get().equals("packet", true)) {
            PacketUtil.sendPacket(
                PlayerMoveC2SPacket.PositionAndOnGround(
                    IMinecraft.mc.player?.pos?.x!!,
                    IMinecraft.mc.player?.pos?.y!! + 0.0625,
                    IMinecraft.mc.player?.pos?.z!!,
                    true, IMinecraft.mc.player!!.horizontalCollision
                )
            )
            PacketUtil.sendPacket(
                PlayerMoveC2SPacket.PositionAndOnGround(
                    IMinecraft.mc.player?.pos?.x!!,
                    IMinecraft.mc.player?.pos?.y!!,
                    IMinecraft.mc.player?.pos?.z!!,
                    false, IMinecraft.mc.player!!.horizontalCollision
                )
            )
            PacketUtil.sendPacket(
                PlayerMoveC2SPacket.PositionAndOnGround(
                    IMinecraft.mc.player?.pos?.x!!,
                    IMinecraft.mc.player?.pos?.y!! + 1.1E-5,
                    IMinecraft.mc.player?.pos?.z!!,
                    false, IMinecraft.mc.player!!.horizontalCollision
                )
            )
            PacketUtil.sendPacket(
                PlayerMoveC2SPacket.PositionAndOnGround(
                    IMinecraft.mc.player?.pos?.x!!,
                    IMinecraft.mc.player?.pos?.y!!,
                    IMinecraft.mc.player?.pos?.z!!,
                    false, IMinecraft.mc.player!!.horizontalCollision
                )
            )
        }
    }
}
