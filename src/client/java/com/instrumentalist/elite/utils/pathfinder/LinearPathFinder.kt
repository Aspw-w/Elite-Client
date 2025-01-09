package com.instrumentalist.elite.utils.pathfinder

import com.instrumentalist.elite.utils.ChatUtil
import com.instrumentalist.elite.utils.IMinecraft
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import kotlin.math.sqrt

object LinearPathFinder {
    fun getPaths(startPos: BlockPos, endPos: BlockPos, blockPerStep: Int, maxSteps: Int): ArrayList<Vec3d> {
        val path: ArrayList<Vec3d> = ArrayList()

        if (IMinecraft.mc.player == null || maxSteps <= 0) return path

        val dx = (endPos.x - startPos.x).toDouble()
        val dy = (endPos.y - startPos.y).toDouble()
        val dz = (endPos.z - startPos.z).toDouble()

        val totalDistance = sqrt(dx * dx + dy * dy + dz * dz)

        val totalSteps = (totalDistance / blockPerStep).toInt().coerceAtMost(maxSteps)

        for (i in 1..totalSteps) {
            val t = i.toDouble() / totalSteps
            val px = startPos.x + t * dx
            val py = startPos.y + t * dy
            val pz = startPos.z + t * dz
            path.add(Vec3d(px, py, pz))
        }

        return path
    }
}