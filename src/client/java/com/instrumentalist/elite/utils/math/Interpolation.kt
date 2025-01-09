package com.instrumentalist.elite.utils.math

object Interpolation {
    fun lerpWithTime(start: Float, end: Float, delta: Float, deltaTime: Float): Float {
        return start + (delta * deltaTime) * (end - start)
    }

    fun lerp(start: Float, end: Float, delta: Float): Float {
        return start + delta * (end - start)
    }
}