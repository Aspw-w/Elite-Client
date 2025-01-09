package com.instrumentalist.elite.utils.math

import com.instrumentalist.elite.utils.IMinecraft
import it.unimi.dsi.fastutil.objects.Object2IntMap
import it.unimi.dsi.fastutil.objects.Object2IntMaps
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import net.minecraft.client.MinecraftClient
import net.minecraft.component.DataComponentTypes
import net.minecraft.enchantment.Enchantment
import net.minecraft.enchantment.Enchantments
import net.minecraft.entity.EquipmentSlot
import net.minecraft.entity.attribute.EntityAttributeModifier
import net.minecraft.entity.attribute.EntityAttributes
import net.minecraft.item.*
import net.minecraft.registry.RegistryKey
import net.minecraft.registry.entry.RegistryEntry
import net.minecraft.util.math.BlockPos

object ToolUtil {
    val mc = IMinecraft.mc

    private val materialStrength = mapOf(
        Items.NETHERITE_SWORD to 5,
        Items.DIAMOND_SWORD to 4,
        Items.IRON_SWORD to 3,
        Items.GOLDEN_SWORD to 2,
        Items.STONE_SWORD to 1,
        Items.WOODEN_SWORD to 0,
    )

    fun findBestTool(pos: BlockPos): Int {
        if (mc.player!!.isCreative) return -1

        val player = MinecraftClient.getInstance().player ?: return -1
        val blockState = player.world.getBlockState(pos)
        var bestSlot = -1
        var bestSpeed = 0.0

        for (i in 0..8) {
            val stack = player.inventory.getStack(i)
            if (stack.item is MiningToolItem || stack.item is ShearsItem) {
                val speed = stack.getMiningSpeedMultiplier(blockState)
                if (speed > bestSpeed) {
                    bestSpeed = speed.toDouble()
                    bestSlot = i
                }
            }
        }

        return if (bestSpeed > 1.0) bestSlot else -1
    }

    fun findBestSword(): Int {
        val player = mc.player ?: return -1
        var bestSwordSlot = -1
        var highestStrength = -1.0

        for (i in 0..8) {
            val stack = player.inventory.getStack(i)
            if (stack.item is SwordItem) {
                val strength = materialStrength[stack.item] ?: 0
                if (strength > highestStrength) {
                    highestStrength = strength.toDouble()
                    bestSwordSlot = i
                }
            }
        }

        return bestSwordSlot
    }

    private fun getEnchantmentLevel(
        itemEnchantments: Object2IntMap<RegistryEntry<Enchantment>>?,
        enchantment: RegistryKey<Enchantment>?
    ): Int {
        for (entry in Object2IntMaps.fastIterable(itemEnchantments)) {
            if (entry.key.matchesKey(enchantment)) return entry.intValue
        }
        return 0
    }

    fun getItemSlotId(itemStack: ItemStack): Int {
        if (itemStack.item is ArmorItem) {
            if (getArmorEquipmentSlot(itemStack) == EquipmentSlot.HEAD) return 5
            if (getArmorEquipmentSlot(itemStack) == EquipmentSlot.CHEST) return 6
            if (getArmorEquipmentSlot(itemStack) == EquipmentSlot.LEGS) return 7
            if (getArmorEquipmentSlot(itemStack) == EquipmentSlot.FEET) return 8
        }
        return itemStack.get(DataComponentTypes.EQUIPPABLE)!!.slot().entitySlotId
    }

    fun isBetterArmor(newArmor: ItemStack, currentArmor: ItemStack): Boolean {
        if (newArmor.item !is ArmorItem || currentArmor.item !is ArmorItem) return false

        val newArmorValue = getArmorScore(newArmor)
        val currentArmorValue = getArmorScore(currentArmor)

        return newArmorValue > currentArmorValue
    }

    fun getArmorEquipmentSlot(stack: ItemStack): EquipmentSlot {
        val armorItem = stack.item.components.get(DataComponentTypes.EQUIPPABLE)
        return armorItem!!.slot
    }

    private fun getArmorScore(itemStack: ItemStack): Int {
        if (itemStack.isEmpty) return 0

        var score = 0

        val enchantments: Object2IntMap<RegistryEntry<Enchantment>> = Object2IntOpenHashMap()

        score += getEnchantmentLevel(enchantments, Enchantments.PROTECTION)
        score += getEnchantmentLevel(enchantments, Enchantments.BLAST_PROTECTION)
        score += getEnchantmentLevel(enchantments, Enchantments.FIRE_PROTECTION)
        score += getEnchantmentLevel(enchantments, Enchantments.PROJECTILE_PROTECTION)
        score += getEnchantmentLevel(enchantments, Enchantments.UNBREAKING)
        score += 2 * getEnchantmentLevel(enchantments, Enchantments.MENDING)

        if (itemStack.contains(DataComponentTypes.ATTRIBUTE_MODIFIERS)) {
            val component = itemStack.get(DataComponentTypes.ATTRIBUTE_MODIFIERS)
            for (modifier in component!!.modifiers()) {
                if (modifier.attribute() === EntityAttributes.ARMOR || modifier.attribute() === EntityAttributes.ARMOR_TOUGHNESS) {
                    val e = modifier.modifier().value()

                    score += when (modifier.modifier().operation()!!) {
                        EntityAttributeModifier.Operation.ADD_VALUE -> e
                        EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE -> e * mc.player!!.getAttributeBaseValue(
                            modifier.attribute()
                        )

                        EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL -> e * score
                    }.toInt()
                }
            }
        }

        return score
    }
}