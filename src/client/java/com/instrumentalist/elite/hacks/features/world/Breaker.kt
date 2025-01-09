package com.instrumentalist.elite.hacks.features.world

import com.instrumentalist.elite.events.features.HandleInputEvent
import com.instrumentalist.elite.events.features.MotionEvent
import com.instrumentalist.elite.hacks.Module
import com.instrumentalist.elite.hacks.ModuleCategory
import com.instrumentalist.elite.hacks.ModuleManager
import com.instrumentalist.elite.hacks.features.player.AutoTool
import com.instrumentalist.elite.hacks.features.player.Scaffold
import com.instrumentalist.elite.utils.IMinecraft
import com.instrumentalist.elite.utils.entity.PlayerUtil
import com.instrumentalist.elite.utils.math.TargetUtil
import com.instrumentalist.elite.utils.math.ToolUtil
import com.instrumentalist.elite.utils.packet.PacketUtil
import com.instrumentalist.elite.utils.rotation.RotationUtil
import com.instrumentalist.elite.utils.value.IntValue
import com.instrumentalist.elite.utils.value.ListValue
import net.minecraft.block.BedBlock
import net.minecraft.block.DragonEggBlock
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket
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
        private val range = IntValue("Range", 4, 1, 6, "m")

        var wasBreaking = false
    }

    private var originalSlot: Int = -1

    override fun onDisable() {
        if (IMinecraft.mc.player != null) {
            if (wasBreaking) {
                TargetUtil.noKillAura = false
                RotationUtil.reset()
                wasBreaking = false
            }
            if (originalSlot != -1 && originalSlot != IMinecraft.mc.player?.inventory!!.selectedSlot)
                IMinecraft.mc.player!!.inventory.selectedSlot = originalSlot
        } else wasBreaking = false
        originalSlot = -1
    }

    override fun onEnable() {}

    override fun onHandleInput(event: HandleInputEvent) {
        if (IMinecraft.mc.player == null || IMinecraft.mc.world == null || ModuleManager.getModuleState(Scaffold())) return

        val bed = findNearbyBeds(IMinecraft.mc.player!!.blockPos, range.get())

        if (bed != null) {
            wasBreaking = true
            when (mode.get().lowercase(Locale.getDefault())) {
                "normal" -> {
                    TargetUtil.noKillAura = true
                    if (ModuleManager.getModuleState(AutoTool())) {
                        val bestToolSlot = ToolUtil.findBestTool(bed)
                        if (bestToolSlot != -1 && bestToolSlot != IMinecraft.mc.player!!.inventory.selectedSlot) {
                            if (originalSlot == -1)
                                originalSlot = IMinecraft.mc.player!!.inventory.selectedSlot
                            IMinecraft.mc.player!!.inventory.selectedSlot = bestToolSlot
                        }
                    }
                    RotationUtil.aimAtBlock(
                        Vec3d.ofCenter(bed),
                        45f,
                        true,
                        10f,
                        0f,
                        0f
                    )
                    val hitResult = BlockHitResult(
                        Vec3d(bed.x.toDouble(), bed.y.toDouble(), bed.z.toDouble()),
                        Direction.UP,
                        bed,
                        false
                    )
                    when (clickMode.get().lowercase(Locale.getDefault())) {
                        "break" -> PlayerUtil.destroyBlock(hitResult)
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
                    TargetUtil.noKillAura = true
                    val hypixelPos = BlockPos(bed.x, bed.y + 1, bed.z)
                    if (!IMinecraft.mc.world?.getBlockState(hypixelPos)?.isAir!!) {
                        if (ModuleManager.getModuleState(AutoTool())) {
                            val bestToolSlot = ToolUtil.findBestTool(hypixelPos)
                            if (bestToolSlot != -1 && bestToolSlot != IMinecraft.mc.player!!.inventory.selectedSlot) {
                                if (originalSlot == -1)
                                    originalSlot = IMinecraft.mc.player!!.inventory.selectedSlot
                                IMinecraft.mc.player!!.inventory.selectedSlot = bestToolSlot
                            }
                        }
                        RotationUtil.aimAtBlock(
                            Vec3d.ofCenter(hypixelPos),
                            45f,
                            true,
                            10f,
                            0f,
                            0f
                        )
                        val hitResult = BlockHitResult(
                            Vec3d(hypixelPos.x.toDouble(), hypixelPos.y.toDouble(), hypixelPos.z.toDouble()),
                            Direction.UP,
                            hypixelPos,
                            false
                        )
                        PlayerUtil.destroyBlock(hitResult)
                    } else {
                        if (ModuleManager.getModuleState(AutoTool())) {
                            val bestToolSlot = ToolUtil.findBestTool(bed)
                            if (bestToolSlot != -1 && bestToolSlot != IMinecraft.mc.player!!.inventory.selectedSlot) {
                                if (originalSlot == -1)
                                    originalSlot = IMinecraft.mc.player!!.inventory.selectedSlot
                                IMinecraft.mc.player!!.inventory.selectedSlot = bestToolSlot
                            }
                        }
                        RotationUtil.aimAtBlock(
                            Vec3d.ofCenter(bed),
                            45f,
                            true,
                            10f,
                            0f,
                            0f
                        )
                        val hitResult = BlockHitResult(
                            Vec3d(bed.x.toDouble(), bed.y.toDouble(), bed.z.toDouble()),
                            Direction.UP,
                            bed,
                            false
                        )
                        PlayerUtil.destroyBlock(hitResult)
                    }
                }

                "cubecraft" -> {
                    TargetUtil.noKillAura = true
                    val cubecraftPos = BlockPos(bed.x, bed.y + 1, bed.z)
                    if (!IMinecraft.mc.world?.getBlockState(cubecraftPos)?.isAir!!) {
                        if (ModuleManager.getModuleState(AutoTool())) {
                            val bestToolSlot = ToolUtil.findBestTool(cubecraftPos)
                            if (bestToolSlot != -1 && bestToolSlot != IMinecraft.mc.player!!.inventory.selectedSlot) {
                                if (originalSlot == -1)
                                    originalSlot = IMinecraft.mc.player!!.inventory.selectedSlot
                                IMinecraft.mc.player!!.inventory.selectedSlot = bestToolSlot
                            }
                        }
                        RotationUtil.aimAtBlock(
                            Vec3d.ofCenter(cubecraftPos),
                            45f,
                            true,
                            10f,
                            0f,
                            0f
                        )
                        if (IMinecraft.mc.interactionManager?.updateBlockBreakingProgress(cubecraftPos, Direction.UP)!!) {
                            IMinecraft.mc.player!!.swingHand(Hand.MAIN_HAND)
                            IMinecraft.mc.particleManager.addBlockBreakingParticles(cubecraftPos, Direction.UP)
                        }
                    } else {
                        RotationUtil.aimAtBlock(
                            Vec3d.ofCenter(bed),
                            45f,
                            true,
                            10f,
                            0f,
                            0f
                        )
                        val hitResult = BlockHitResult(
                            Vec3d(bed.x.toDouble(), bed.y.toDouble(), bed.z.toDouble()),
                            Direction.UP,
                            bed,
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
            TargetUtil.noKillAura = false
            RotationUtil.reset()
            wasBreaking = false
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
