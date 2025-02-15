package com.instrumentalist.elite.hacks.features.world

import com.instrumentalist.elite.events.features.MotionEvent
import com.instrumentalist.elite.events.features.SendPacketEvent
import com.instrumentalist.elite.events.features.UpdateEvent
import com.instrumentalist.elite.hacks.Module
import com.instrumentalist.elite.hacks.ModuleCategory
import com.instrumentalist.elite.hacks.ModuleManager
import com.instrumentalist.elite.hacks.features.combat.KillAura
import com.instrumentalist.elite.hacks.features.player.AutoTool
import com.instrumentalist.elite.hacks.features.player.Scaffold
import com.instrumentalist.elite.utils.ChatUtil
import com.instrumentalist.elite.utils.IMinecraft
import com.instrumentalist.elite.utils.entity.PlayerUtil
import com.instrumentalist.elite.utils.math.TargetUtil
import com.instrumentalist.elite.utils.math.ToolUtil
import com.instrumentalist.elite.utils.packet.PacketUtil
import com.instrumentalist.elite.utils.rotation.RotationUtil
import com.instrumentalist.elite.utils.value.FloatValue
import com.instrumentalist.elite.utils.value.IntValue
import com.instrumentalist.elite.utils.value.ListValue
import net.minecraft.block.BedBlock
import net.minecraft.block.DragonEggBlock
import net.minecraft.block.enums.BedPart
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket
import net.minecraft.util.Hand
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.Vec3d
import org.lwjgl.glfw.GLFW
import java.util.*

class Breaker : Module("Breaker", ModuleCategory.World, GLFW.GLFW_KEY_UNKNOWN, false, true) {
    companion object {
        @Setting
        private val mode = ListValue("Mode", arrayOf("Normal", "Hypixel", "Cubecraft"), "Normal")

        @Setting
        private val clickMode =
            ListValue("Click Mode", arrayOf("Break", "Place"), "Break") { mode.get().equals("normal", true) }

        @Setting
        private val block = ListValue("Block", arrayOf("Bed", "Egg"), "Bed") { mode.get().equals("normal", true) }

        @Setting
        private val range = FloatValue("Range", 4f, 1f, 6f, "m")

        var wasBreaking = false
    }

    private var hypTick = 0
    private var cachedBedPos: BlockPos? = null
    private var cachedSecondPos: BlockPos? = null
    private var originalSlot: Int = -1
    private var progress = false
    private var secondProgress = false

    override fun onDisable() {
        if (IMinecraft.mc.player != null) {
            if (wasBreaking) {
                IMinecraft.mc.interactionManager!!.currentBreakingProgress = -1f
                TargetUtil.noKillAura = false
                RotationUtil.reset()
                if (secondProgress && cachedSecondPos != null) {
                    if (IMinecraft.mc.world!!.getBlockState(cachedSecondPos).isAir) {
                        PacketUtil.sendPacketAsSilent(
                            PlayerActionC2SPacket(
                                PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, cachedSecondPos, Direction.UP
                            )
                        )
                        IMinecraft.mc.player!!.swingHand(Hand.MAIN_HAND)
                        IMinecraft.mc.interactionManager!!.breakBlock(cachedSecondPos)
                    } else
                        PacketUtil.sendPacketAsSilent(
                            PlayerActionC2SPacket(
                                PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK, cachedSecondPos, Direction.UP
                            )
                        )
                }
                if (progress && cachedBedPos != null) {
                    if (IMinecraft.mc.world!!.getBlockState(cachedBedPos).isAir) {
                        PacketUtil.sendPacketAsSilent(
                            PlayerActionC2SPacket(
                                PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, cachedBedPos, Direction.UP
                            )
                        )
                        IMinecraft.mc.player!!.swingHand(Hand.MAIN_HAND)
                        IMinecraft.mc.interactionManager!!.breakBlock(cachedBedPos)
                    } else
                        PacketUtil.sendPacketAsSilent(
                            PlayerActionC2SPacket(
                                PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK, cachedBedPos, Direction.UP
                            )
                        )
                }
                wasBreaking = false
            }
            if (originalSlot != -1 && originalSlot != IMinecraft.mc.player?.inventory!!.selectedSlot)
                IMinecraft.mc.player!!.inventory.selectedSlot = originalSlot
        } else wasBreaking = false
        cachedSecondPos = null
        cachedBedPos = null
        originalSlot = -1
        hypTick = 0
        progress = false
        secondProgress = false
    }

    override fun onEnable() {}

    override fun onSendPacket(event: SendPacketEvent) {
        if (IMinecraft.mc.player == null || IMinecraft.mc.world == null || ModuleManager.getModuleState(Scaffold())) return

        val packet = event.packet

        if (wasBreaking && packet is PlayerActionC2SPacket && (packet.action == PlayerActionC2SPacket.Action.START_DESTROY_BLOCK || packet.action == PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK || packet.action == PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK))
            event.cancel()
    }

    override fun onMotion(event: MotionEvent) {
        if (IMinecraft.mc.player == null || IMinecraft.mc.world == null || ModuleManager.getModuleState(Scaffold())) return

        if (cachedBedPos == null)
            cachedBedPos = findNearbyBeds(IMinecraft.mc.player!!.blockPos, range.get().toInt() + 1)

        if (cachedBedPos != null && !IMinecraft.mc.world!!.getBlockState(cachedBedPos).isAir && (range.get() * 10) >= IMinecraft.mc.player!!.squaredDistanceTo(Vec3d(cachedBedPos!!.x.toDouble(), cachedBedPos!!.y.toDouble(), cachedBedPos!!.z.toDouble()))) {
            wasBreaking = true
            when (mode.get().lowercase(Locale.getDefault())) {
                "normal" -> {
                    TargetUtil.noKillAura = true
                    if (ModuleManager.getModuleState(AutoTool())) {
                        val bestToolSlot = ToolUtil.findBestTool(cachedBedPos!!)
                        if (bestToolSlot != -1 && bestToolSlot != IMinecraft.mc.player!!.inventory.selectedSlot) {
                            if (originalSlot == -1)
                                originalSlot = IMinecraft.mc.player!!.inventory.selectedSlot
                            IMinecraft.mc.player!!.inventory.selectedSlot = bestToolSlot
                        }
                    }
                    RotationUtil.aimAtBlock(
                        Vec3d.ofCenter(cachedBedPos),
                        45f,
                        true,
                        10f,
                        0f,
                        0f
                    )
                    val hitResult = BlockHitResult(
                        Vec3d(cachedBedPos!!.x.toDouble(), cachedBedPos!!.y.toDouble(), cachedBedPos!!.z.toDouble()),
                        Direction.UP,
                        cachedBedPos,
                        false
                    )
                    when (clickMode.get().lowercase(Locale.getDefault())) {
                        "break" -> {
                            if (progress)
                                PlayerUtil.destroyBlock(hitResult)
                            else {
                                PacketUtil.sendPacketAsSilent(PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, cachedBedPos, Direction.UP))
                                progress = true
                            }
                        }
                        "place" -> {
                            if (IMinecraft.mc.interactionManager!!.interactBlock(
                                    IMinecraft.mc.player!!,
                                    Hand.MAIN_HAND,
                                    hitResult
                                ).isAccepted
                            )
                                IMinecraft.mc.player!!.swingHand(Hand.MAIN_HAND)
                        }
                    }
                }

                "hypixel" -> {
                    if (cachedSecondPos == null) {
                        cachedSecondPos = BlockPos(cachedBedPos!!.x, cachedBedPos!!.y + 1, cachedBedPos!!.z)
                        hypTick = 0
                    }
                    if (secondProgress && progress) {
                        PacketUtil.sendPacketAsSilent(
                            PlayerActionC2SPacket(
                                PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK, cachedBedPos, Direction.UP
                            )
                        )
                        if (IMinecraft.mc.world!!.getBlockState(cachedSecondPos).isAir)
                            PacketUtil.sendPacketAsSilent(
                                PlayerActionC2SPacket(
                                    PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, cachedSecondPos, Direction.UP
                                )
                            )
                        else
                            PacketUtil.sendPacketAsSilent(
                                PlayerActionC2SPacket(
                                    PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK, cachedSecondPos, Direction.UP
                                )
                            )
                        cachedSecondPos = null
                        secondProgress = false
                        progress = false
                        hypTick = 0
                    } else if (!IMinecraft.mc.world?.getBlockState(cachedSecondPos)?.isAir!! && (range.get() * 10) >= IMinecraft.mc.player!!.squaredDistanceTo(Vec3d(cachedSecondPos!!.x.toDouble(), cachedSecondPos!!.y.toDouble(), cachedSecondPos!!.z.toDouble()))) {
                        if (ModuleManager.getModuleState(AutoTool())) {
                            val bestToolSlot = ToolUtil.findBestTool(cachedSecondPos!!)
                            if (bestToolSlot != -1 && bestToolSlot != IMinecraft.mc.player!!.inventory.selectedSlot) {
                                if (originalSlot == -1)
                                    originalSlot = IMinecraft.mc.player!!.inventory.selectedSlot
                                IMinecraft.mc.player!!.inventory.selectedSlot = bestToolSlot
                            }
                        }
                        val hitResult = BlockHitResult(
                            Vec3d(cachedSecondPos!!.x.toDouble(), cachedSecondPos!!.y.toDouble(), cachedSecondPos!!.z.toDouble()),
                            Direction.UP,
                            cachedSecondPos!!,
                            false
                        )
                        hypTick++
                        if (secondProgress) {
                            PlayerUtil.destroyBlock(hitResult)
                            if (hypTick >= 3) {
                                TargetUtil.noKillAura = false
                                if (!ModuleManager.getModuleState(KillAura()) || KillAura.closestEntity == null)
                                    RotationUtil.aimAtBlock(
                                        Vec3d.ofCenter(cachedSecondPos),
                                        90f,
                                        false,
                                        10f,
                                        0f,
                                        0f
                                    )
                            }
                        } else {
                            TargetUtil.noKillAura = true
                            repeat(4) {
                                RotationUtil.aimAtBlock(
                                    Vec3d.ofCenter(cachedSecondPos!!),
                                    90f,
                                    false,
                                    10f,
                                    0f,
                                    0f
                                )
                            }
                            PacketUtil.sendPacketAsSilent(PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, cachedSecondPos!!, Direction.UP))
                            secondProgress = true
                        }
                    } else {
                        if (secondProgress && cachedSecondPos != null) {
                            if (IMinecraft.mc.world!!.getBlockState(cachedSecondPos).isAir)
                                PacketUtil.sendPacketAsSilent(
                                    PlayerActionC2SPacket(
                                        PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, cachedSecondPos, Direction.UP
                                    )
                                )
                            else
                                PacketUtil.sendPacketAsSilent(
                                    PlayerActionC2SPacket(
                                        PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK, cachedSecondPos, Direction.UP
                                    )
                                )
                            if (originalSlot != -1 && originalSlot != IMinecraft.mc.player!!.inventory.selectedSlot) {
                                IMinecraft.mc.player!!.inventory.selectedSlot = originalSlot
                                originalSlot = -1
                            }
                            cachedSecondPos = null
                            secondProgress = false
                            hypTick = 0
                        }
                        if (ModuleManager.getModuleState(AutoTool())) {
                            val bestToolSlot = ToolUtil.findBestTool(cachedBedPos!!)
                            if (bestToolSlot != -1 && bestToolSlot != IMinecraft.mc.player!!.inventory.selectedSlot) {
                                if (originalSlot == -1)
                                    originalSlot = IMinecraft.mc.player!!.inventory.selectedSlot
                                IMinecraft.mc.player!!.inventory.selectedSlot = bestToolSlot
                            }
                        }
                        val hitResult = BlockHitResult(
                            Vec3d(cachedBedPos!!.x.toDouble(), cachedBedPos!!.y.toDouble(), cachedBedPos!!.z.toDouble()),
                            Direction.UP,
                            cachedBedPos,
                            false
                        )
                        hypTick++
                        if (progress) {
                            PlayerUtil.destroyBlock(hitResult)
                            if (hypTick >= 3) {
                                TargetUtil.noKillAura = false
                                if (!ModuleManager.getModuleState(KillAura()) || KillAura.closestEntity == null)
                                    RotationUtil.aimAtBlock(
                                        Vec3d.ofCenter(cachedBedPos),
                                        90f,
                                        false,
                                        10f,
                                        0f,
                                        0f
                                    )
                            }
                        } else {
                            TargetUtil.noKillAura = true
                            repeat(4) {
                                RotationUtil.aimAtBlock(
                                    Vec3d.ofCenter(cachedBedPos),
                                    90f,
                                    false,
                                    10f,
                                    0f,
                                    0f
                                )
                            }
                            PacketUtil.sendPacketAsSilent(PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, cachedBedPos, Direction.UP))
                            progress = true
                        }
                    }
                }

                "cubecraft" -> {
                    TargetUtil.noKillAura = true
                    if (cachedSecondPos == null)
                        cachedSecondPos = BlockPos(cachedBedPos!!.x, cachedBedPos!!.y + 1, cachedBedPos!!.z)
                    if (secondProgress && progress) {
                        PacketUtil.sendPacketAsSilent(
                            PlayerActionC2SPacket(
                                PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK, cachedBedPos, Direction.UP
                            )
                        )
                        if (IMinecraft.mc.world!!.getBlockState(cachedSecondPos).isAir)
                            PacketUtil.sendPacketAsSilent(
                                PlayerActionC2SPacket(
                                    PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, cachedSecondPos, Direction.UP
                                )
                            )
                        else
                            PacketUtil.sendPacketAsSilent(
                                PlayerActionC2SPacket(
                                    PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK, cachedSecondPos, Direction.UP
                                )
                            )
                        cachedSecondPos = null
                        secondProgress = false
                        progress = false
                    } else if (!IMinecraft.mc.world?.getBlockState(cachedSecondPos!!)?.isAir!! && (range.get() * 10) >= IMinecraft.mc.player!!.squaredDistanceTo(Vec3d(cachedSecondPos!!.x.toDouble(), cachedSecondPos!!.y.toDouble(), cachedSecondPos!!.z.toDouble()))) {
                        if (ModuleManager.getModuleState(AutoTool())) {
                            val bestToolSlot = ToolUtil.findBestTool(cachedSecondPos!!)
                            if (bestToolSlot != -1 && bestToolSlot != IMinecraft.mc.player!!.inventory.selectedSlot) {
                                if (originalSlot == -1)
                                    originalSlot = IMinecraft.mc.player!!.inventory.selectedSlot
                                IMinecraft.mc.player!!.inventory.selectedSlot = bestToolSlot
                            }
                        }
                        RotationUtil.aimAtBlock(
                            Vec3d.ofCenter(cachedSecondPos!!),
                            45f,
                            true,
                            10f,
                            0f,
                            0f
                        )
                        val hitResult = BlockHitResult(
                            Vec3d(cachedSecondPos!!.x.toDouble(), cachedSecondPos!!.y.toDouble(), cachedSecondPos!!.z.toDouble()),
                            Direction.UP,
                            cachedSecondPos!!,
                            false
                        )
                        if (progress)
                            PlayerUtil.destroyBlock(hitResult)
                        else {
                            PacketUtil.sendPacketAsSilent(PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, cachedSecondPos, Direction.UP))
                            progress = true
                        }
                    } else {
                        if (secondProgress && cachedSecondPos != null) {
                            if (IMinecraft.mc.world!!.getBlockState(cachedSecondPos).isAir)
                                PacketUtil.sendPacketAsSilent(
                                    PlayerActionC2SPacket(
                                        PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, cachedSecondPos, Direction.UP
                                    )
                                )
                            else
                                PacketUtil.sendPacketAsSilent(
                                    PlayerActionC2SPacket(
                                        PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK, cachedSecondPos, Direction.UP
                                    )
                                )
                            if (originalSlot != -1 && originalSlot != IMinecraft.mc.player!!.inventory.selectedSlot) {
                                IMinecraft.mc.player!!.inventory.selectedSlot = originalSlot
                                originalSlot = -1
                            }
                            cachedSecondPos = null
                            secondProgress = false
                        }
                        RotationUtil.aimAtBlock(
                            Vec3d.ofCenter(cachedBedPos),
                            45f,
                            true,
                            10f,
                            0f,
                            0f
                        )
                        val hitResult = BlockHitResult(
                            Vec3d(cachedBedPos!!.x.toDouble(), cachedBedPos!!.y.toDouble(), cachedBedPos!!.z.toDouble()),
                            Direction.UP,
                            cachedBedPos,
                            false
                        )
                        if (IMinecraft.mc.interactionManager!!.interactBlock(
                                IMinecraft.mc.player!!,
                                Hand.MAIN_HAND,
                                hitResult
                            ).isAccepted
                        )
                            IMinecraft.mc.player!!.swingHand(Hand.MAIN_HAND)
                    }
                }
            }
        } else if (wasBreaking) {
            if (originalSlot != -1 && originalSlot != IMinecraft.mc.player!!.inventory.selectedSlot) {
                IMinecraft.mc.player!!.inventory.selectedSlot = originalSlot
                originalSlot = -1
            }
            IMinecraft.mc.interactionManager!!.currentBreakingProgress = -1f
            if (!mode.get().equals("hypixel", true)) {
                TargetUtil.noKillAura = false
                RotationUtil.reset()
            }
            if (secondProgress && cachedSecondPos != null) {
                if (IMinecraft.mc.world!!.getBlockState(cachedSecondPos).isAir) {
                    if (mode.get().equals("hypixel", true)) {
                        TargetUtil.noKillAura = true
                        repeat(4) {
                            RotationUtil.aimAtBlock(
                                Vec3d.ofCenter(cachedSecondPos),
                                90f,
                                false,
                                10f,
                                0f,
                                0f
                            )
                        }
                        hypTick = 810
                    }
                    PacketUtil.sendPacketAsSilent(
                        PlayerActionC2SPacket(
                            PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, cachedSecondPos, Direction.UP
                        )
                    )
                    IMinecraft.mc.player!!.swingHand(Hand.MAIN_HAND)
                    IMinecraft.mc.interactionManager!!.breakBlock(cachedSecondPos)
                } else {
                    PacketUtil.sendPacketAsSilent(
                        PlayerActionC2SPacket(
                            PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK, cachedSecondPos, Direction.UP
                        )
                    )
                    if (mode.get().equals("hypixel", true))
                        hypTick = 810
                }
            }
            if (progress && cachedBedPos != null) {
                if (IMinecraft.mc.world!!.getBlockState(cachedBedPos).isAir) {
                    if (mode.get().equals("hypixel", true)) {
                        TargetUtil.noKillAura = true
                        repeat(4) {
                            RotationUtil.aimAtBlock(
                                Vec3d.ofCenter(cachedBedPos),
                                90f,
                                false,
                                10f,
                                0f,
                                0f
                            )
                        }
                        hypTick = 810
                    }
                    PacketUtil.sendPacketAsSilent(
                        PlayerActionC2SPacket(
                            PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, cachedBedPos, Direction.UP
                        )
                    )
                    IMinecraft.mc.player!!.swingHand(Hand.MAIN_HAND)
                    IMinecraft.mc.interactionManager!!.breakBlock(cachedBedPos)
                } else {
                    PacketUtil.sendPacketAsSilent(
                        PlayerActionC2SPacket(
                            PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK, cachedBedPos, Direction.DOWN
                        )
                    )
                    if (mode.get().equals("hypixel", true))
                        hypTick = 810
                }
            }
            cachedSecondPos = null
            cachedBedPos = null
            secondProgress = false
            progress = false
            wasBreaking = false
            if (hypTick != 810)
                hypTick = 0
        } else {
            if (hypTick != 0) {
                if (mode.get().equals("hypixel", true)) {
                    if (hypTick >= 810)
                        hypTick++
                    if (hypTick >= 820) {
                        TargetUtil.noKillAura = false
                        RotationUtil.reset()
                        hypTick = 0
                    }
                } else hypTick = 0
            }
        }
    }

    private fun findNearbyBeds(playerPos: BlockPos, radius: Int): BlockPos? {
        for (x in -radius..radius) {
            for (y in -radius..radius) {
                for (z in -radius..radius) {
                    val pos = playerPos.add(x, y, z)
                    val blockState = IMinecraft.mc.world?.getBlockState(pos)

                    if ((mode.get().equals("hypixel", true) || mode.get().equals("normal", true) && block.get()
                            .equals("bed", true)) && blockState?.block is BedBlock || (mode.get()
                            .equals("cubecraft", true) || mode.get().equals("normal", true) && block.get()
                            .equals("egg", true)) && blockState?.block is DragonEggBlock
                    )
                        return pos
                }
            }
        }
        return null
    }
}
