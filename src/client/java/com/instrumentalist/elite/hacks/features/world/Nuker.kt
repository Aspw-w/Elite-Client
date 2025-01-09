package com.instrumentalist.elite.hacks.features.world

import com.instrumentalist.elite.events.features.MotionEvent
import com.instrumentalist.elite.hacks.Module
import com.instrumentalist.elite.hacks.ModuleCategory
import com.instrumentalist.elite.hacks.ModuleManager
import com.instrumentalist.elite.hacks.features.player.AutoTool
import com.instrumentalist.elite.utils.IMinecraft
import com.instrumentalist.elite.utils.entity.PlayerUtil
import com.instrumentalist.elite.utils.math.MSTimer
import com.instrumentalist.elite.utils.math.ToolUtil
import com.instrumentalist.elite.utils.packet.PacketUtil
import com.instrumentalist.elite.utils.rotation.RotationUtil
import com.instrumentalist.elite.utils.value.BooleanValue
import com.instrumentalist.elite.utils.value.IntValue
import com.instrumentalist.elite.utils.value.ListValue
import net.minecraft.block.NoteBlock
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket
import net.minecraft.util.Hand
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import org.lwjgl.glfw.GLFW
import java.util.*
import kotlin.math.sqrt

class Nuker : Module("Nuker", ModuleCategory.World, GLFW.GLFW_KEY_UNKNOWN, false, true) {
    companion object {
        @Setting
        private val mode = ListValue("Mode", arrayOf("Normal", "Floor"), "Normal")

        @Setting
        private val clickMode = ListValue("Click Mode", arrayOf("Break", "Place"), "Break")

        @Setting
        private val range = IntValue("Range", 4, 2, 6, "m")

        @Setting
        private val singleBlock = BooleanValue("Single Block", false)

        @Setting
        private val rotations = BooleanValue("Rotations", true) { singleBlock.get() }

        @Setting
        private val nukeSpeed = IntValue("Nuke Speed", 10, 1, 10) { !singleBlock.get() }

        @Setting
        private val noteBlockOnly = BooleanValue("Note Block Only", false)

        var wasBreaking = false
    }

    private val nukeTimer = MSTimer()
    private var currentBlock: BlockPos? = null
    private val blockQueue = LinkedList<BlockPos>()
    private var breakStartTime = 0L
    private var originalSlot: Int = -1

    override fun onDisable() {
        blockQueue.clear()
        breakStartTime = 0
        currentBlock = null
        if (wasBreaking)
            RotationUtil.reset()
        wasBreaking = false
        if (IMinecraft.mc.player != null && IMinecraft.mc.world != null && originalSlot != -1 && originalSlot != IMinecraft.mc.player!!.inventory.selectedSlot)
            IMinecraft.mc.player!!.inventory.selectedSlot = originalSlot
        originalSlot = -1
    }

    override fun onEnable() {}

    override fun onMotion(event: MotionEvent) {
        if (IMinecraft.mc.player == null || IMinecraft.mc.world == null) return

        val playerPos = IMinecraft.mc.player!!.blockPos
        val startY = if (mode.get().equals("normal", true)) -range.get() else 0

        if (currentBlock != null) {
            if (wasBreaking && singleBlock.get()) {
                if (rotations.get())
                    RotationUtil.aimAtBlock(
                        Vec3d.ofCenter(currentBlock),
                        90f,
                        false,
                        10f,
                        0f,
                        0f
                    )
                if (ModuleManager.getModuleState(AutoTool()) && clickMode.get().equals("break", true)) {
                    val bestToolSlot = ToolUtil.findBestTool(currentBlock!!)
                    if (bestToolSlot != -1 && bestToolSlot != IMinecraft.mc.player!!.inventory.selectedSlot) {
                        if (originalSlot == -1)
                            originalSlot = IMinecraft.mc.player!!.inventory.selectedSlot
                        IMinecraft.mc.player!!.inventory.selectedSlot = bestToolSlot
                    }
                }
            }

            if (clickMode.get()
                    .equals("break", true) && System.currentTimeMillis() - breakStartTime > 500 || clickMode.get()
                    .equals("place", true) && !IMinecraft.mc.world!!.getBlockState(currentBlock).isAir
            ) {
                if (originalSlot != -1 && originalSlot != IMinecraft.mc.player!!.inventory.selectedSlot) {
                    IMinecraft.mc.player!!.inventory.selectedSlot = originalSlot
                    originalSlot = -1
                }
                currentBlock = null
                blockQueue.clear()
            }
        } else {
            if (wasBreaking) {
                if (originalSlot != -1 && originalSlot != IMinecraft.mc.player!!.inventory.selectedSlot) {
                    IMinecraft.mc.player!!.inventory.selectedSlot = originalSlot
                    originalSlot = -1
                }
                wasBreaking = false
            }
            val potentialBlocks = mutableListOf<BlockPos>()
            for (x in -range.get()..range.get()) {
                for (y in startY..range.get()) {
                    for (z in -range.get()..range.get()) {
                        val distance = sqrt((x * x + y * y + z * z).toDouble())
                        if (distance <= range.get()) {
                            val targetPos = playerPos.add(x, y, z)
                            if ((!noteBlockOnly.get() || IMinecraft.mc.world!!.getBlockState(targetPos).block is NoteBlock) && (clickMode.get()
                                    .equals("break", true) && !IMinecraft.mc.world!!.isAir(targetPos) && !IMinecraft.mc.world!!.isWater(
                                    targetPos
                                ) || clickMode.get().equals("place", true) && (!noteBlockOnly.get() && IMinecraft.mc.world!!.isAir(targetPos) || noteBlockOnly.get() && IMinecraft.mc.world!!.getBlockState(targetPos).block is NoteBlock))
                            )
                                potentialBlocks.add(targetPos)
                        }
                    }
                }
            }
            if (potentialBlocks.isEmpty()) {
                if (originalSlot != -1 && originalSlot != IMinecraft.mc.player!!.inventory.selectedSlot) {
                    IMinecraft.mc.player!!.inventory.selectedSlot = originalSlot
                    originalSlot = -1
                }
                RotationUtil.reset()
                wasBreaking = false
                return
            }
            potentialBlocks.sortBy { it.getSquaredDistance(playerPos) }
            blockQueue.clear()
            blockQueue.addAll(potentialBlocks)
        }

        if (singleBlock.get()) {
            val playerPosVec = IMinecraft.mc.player!!.pos
            val potentialBlocks = mutableListOf<BlockPos>()

            blockQueue.clear()
            for (x in -range.get()..range.get()) {
                for (y in startY..range.get()) {
                    for (z in -range.get()..range.get()) {
                        val distance = sqrt((x * x + y * y + z * z).toDouble())
                        if (distance <= range.get()) {
                            val targetPos = playerPos.add(x, y, z)
                            val targetVec = Vec3d.ofCenter(targetPos)

                            if (clickMode.get()
                                    .equals("place", true) && !noteBlockOnly.get() && playerPosVec.squaredDistanceTo(
                                    targetVec
                                ) < 3
                            ) continue

                            if ((!noteBlockOnly.get() || IMinecraft.mc.world!!.getBlockState(targetPos).block is NoteBlock) &&
                                (clickMode.get().equals(
                                    "break",
                                    true
                                ) && !IMinecraft.mc.world!!.isAir(targetPos) && !IMinecraft.mc.world!!.isWater(targetPos) ||
                                        clickMode.get().equals(
                                            "place",
                                            true
                                        ) && (!noteBlockOnly.get() && IMinecraft.mc.world!!.isAir(targetPos) ||
                                        noteBlockOnly.get() && IMinecraft.mc.world!!.getBlockState(targetPos).block is NoteBlock))
                            ) {
                                potentialBlocks.add(targetPos)
                            }
                        }
                    }
                }
            }

            if (potentialBlocks.isEmpty()) {
                if (originalSlot != -1 && originalSlot != IMinecraft.mc.player!!.inventory.selectedSlot) {
                    IMinecraft.mc.player!!.inventory.selectedSlot = originalSlot
                    originalSlot = -1
                }
                RotationUtil.reset()
                wasBreaking = false
                return
            }

            potentialBlocks.sortBy { it.getSquaredDistance(playerPos) }

            currentBlock = potentialBlocks[0]

            if (currentBlock != null) {
                if (clickMode.get().equals("break", true) && !IMinecraft.mc.world!!.isAir(currentBlock) && !IMinecraft.mc.world!!.isWater(currentBlock) ||
                    clickMode.get().equals("place", true) && (!noteBlockOnly.get() && IMinecraft.mc.world!!.isAir(currentBlock) ||
                            noteBlockOnly.get() && IMinecraft.mc.world!!.getBlockState(currentBlock).block is NoteBlock)
                ) {
                    wasBreaking = true
                    breakStartTime = System.currentTimeMillis()

                    val hitResult = BlockHitResult(
                        Vec3d(currentBlock!!.x.toDouble(), currentBlock!!.y.toDouble(), currentBlock!!.z.toDouble()),
                        IMinecraft.mc.player!!.horizontalFacing,
                        currentBlock,
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
                                PacketUtil.sendPacket(HandSwingC2SPacket(Hand.MAIN_HAND))
                        }
                    }
                } else currentBlock = null
            }
        } else {
            if (nukeTimer.hasTimePassed(1000L / (nukeSpeed.get() + 2))) {
                while (blockQueue.isNotEmpty()) {
                    val targetPos = blockQueue.poll()
                    if (clickMode.get().equals("break", true) && !IMinecraft.mc.world!!.isAir(targetPos) && !IMinecraft.mc.world!!.isWater(
                            targetPos
                        ) || clickMode.get().equals("place", true) && (!noteBlockOnly.get() && IMinecraft.mc.world!!.isAir(targetPos) || noteBlockOnly.get() && IMinecraft.mc.world!!.getBlockState(targetPos).block is NoteBlock)
                    ) {
                        wasBreaking = true

                        val hitResult = BlockHitResult(
                            Vec3d(targetPos.x.toDouble(), targetPos.y.toDouble(), targetPos.z.toDouble()),
                            IMinecraft.mc.player!!.horizontalFacing,
                            targetPos,
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
                                    PacketUtil.sendPacket(HandSwingC2SPacket(Hand.MAIN_HAND))
                            }
                        }
                    }
                }

                nukeTimer.reset()
            }
        }
    }
}
