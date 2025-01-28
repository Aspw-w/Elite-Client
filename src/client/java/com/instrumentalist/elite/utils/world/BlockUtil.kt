package com.instrumentalist.elite.utils.world

import com.instrumentalist.elite.utils.IMinecraft
import net.minecraft.block.Blocks.*
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.World

object BlockUtil {
    fun isFullSurroundedBlock(world: World, pos: BlockPos): Boolean {
        if (IMinecraft.mc.world == null) return true

        var stage = 0

        for (direction in Direction.entries) {
            val adjacentPos = pos.offset(direction)
            if (!world.getBlockState(adjacentPos).isOf(AIR) && !world.getBlockState(adjacentPos).isOf(WATER) && !world.getBlockState(adjacentPos).isOf(LAVA))
                stage++
            if (stage >= 6)
                return false
        }

        return true
    }
}