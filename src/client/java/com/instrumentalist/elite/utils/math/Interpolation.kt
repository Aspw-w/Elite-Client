package com.instrumentalist.elite.utils.math

import kotlin.math.abs

object Interpolation {
    fun lerpWithTime(start: Float, end: Float, delta: Float, deltaTime: Float): Float {
        return start + (delta * deltaTime) * (end - start)
    }

    fun lerp(start: Float, end: Float, delta: Float): Float {
        return start + delta * (end - start)
    }

    fun valueLimitedLerpWithTime(start: Float, end: Float, delta: Float, deltaTime: Float, limit: Int = 320): Float {
        val distance = abs(end - start)

        var lok = 2f

        if (distance >= limit) lok = 1f
        val dynamicDelta = delta + (distance / lok)

        return start + (dynamicDelta * deltaTime) * (end - start)
    }
}