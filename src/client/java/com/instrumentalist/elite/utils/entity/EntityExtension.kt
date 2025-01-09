package com.instrumentalist.elite.utils.entity

import com.instrumentalist.elite.hacks.ModuleManager
import com.instrumentalist.elite.hacks.features.movement.Fly
import com.instrumentalist.elite.utils.IMinecraft
import net.minecraft.component.type.DyedColorComponent
import net.minecraft.entity.Entity
import net.minecraft.item.ItemStack
import net.minecraft.registry.tag.ItemTags
import net.minecraft.util.math.MathHelper
import net.minecraft.util.shape.VoxelShapes
import java.util.regex.Pattern

private val COLOR_PATTERN = Pattern.compile("(?i)§[0-9A-FK-OR]")

fun Entity.distanceToWithoutY(entity: Entity): Float {
    val f: Float = (this.x - entity.x).toFloat()
    val h: Float = (this.z - entity.z).toFloat()
    return MathHelper.sqrt(f * f + h * h)
}

fun Entity.squaredDistanceToWithoutY(x: Double, z: Double): Float {
    val d = this.x - x
    val f = this.z - z
    return MathHelper.sqrt((d * d + f * f).toFloat())
}

fun String.stripMinecraftColorCodes(): String {
    return COLOR_PATTERN.matcher(this).replaceAll("")
}

fun ItemStack.getArmorColor(): Int? {
    return if (isIn(ItemTags.DYEABLE)) {
        DyedColorComponent.getColor(this, -6265536)
    } else {
        null
    }
}