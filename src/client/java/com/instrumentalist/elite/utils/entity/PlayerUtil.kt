package com.instrumentalist.elite.utils.entity

import com.instrumentalist.elite.utils.IMinecraft
import com.instrumentalist.elite.utils.packet.PacketUtil
import net.minecraft.entity.effect.StatusEffectUtil
import net.minecraft.entity.effect.StatusEffects
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

    fun swingHandWithoutPacket(hand: Hand) {
        if (IMinecraft.mc.player == null) return
        if (!IMinecraft.mc.player!!.handSwinging || IMinecraft.mc.player!!.handSwingTicks >= getSnglSwingHandDuration() / 2 || IMinecraft.mc.player!!.handSwingTicks < 0) {
            IMinecraft.mc.player!!.handSwingTicks = -1
            IMinecraft.mc.player!!.handSwinging = true
            IMinecraft.mc.player!!.preferredHand = hand
        }
    }

    private fun getSnglSwingHandDuration(): Int {
        return if (StatusEffectUtil.hasHaste(IMinecraft.mc.player)) {
            6 - (1 + StatusEffectUtil.getHasteAmplifier(IMinecraft.mc.player))
        } else {
            if (IMinecraft.mc.player!!.hasStatusEffect(StatusEffects.MINING_FATIGUE)) 6 + (1 + IMinecraft.mc.player!!.getStatusEffect(StatusEffects.MINING_FATIGUE)
                !!.amplifier) * 2 else 6
        }
    }
}