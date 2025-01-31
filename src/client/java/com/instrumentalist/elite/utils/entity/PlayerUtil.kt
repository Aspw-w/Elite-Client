package com.instrumentalist.elite.utils.entity

import com.instrumentalist.elite.utils.IMinecraft
import com.instrumentalist.elite.utils.packet.PacketUtil
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket
import net.minecraft.util.Hand
import net.minecraft.util.hit.BlockHitResult

object PlayerUtil {
    fun nukeBlockWithPacket(rayTraceResult: BlockHitResult) {
        val direction = rayTraceResult.side
        val blockPos = rayTraceResult.blockPos

        PacketUtil.sendPacket(
            PlayerActionC2SPacket(
                PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, blockPos, direction
            )
        )
        PacketUtil.sendPacket(HandSwingC2SPacket(Hand.MAIN_HAND))
        PacketUtil.sendPacket(
            PlayerActionC2SPacket(
                PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, blockPos, direction
            )
        )
    }

    fun destroyBlock(rayTraceResult: BlockHitResult) {
        val direction = rayTraceResult.side
        val blockPos = rayTraceResult.blockPos

        if (IMinecraft.mc.interactionManager!!.updateBlockBreakingProgress(blockPos, direction)) {
            IMinecraft.mc.player!!.swingHand(Hand.MAIN_HAND)
            IMinecraft.mc.particleManager.addBlockBreakingParticles(blockPos, direction)
        }
    }
}