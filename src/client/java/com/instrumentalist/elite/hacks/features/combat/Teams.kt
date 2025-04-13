package com.instrumentalist.elite.hacks.features.combat

import com.instrumentalist.elite.hacks.Module
import com.instrumentalist.elite.hacks.ModuleCategory
import com.instrumentalist.elite.utils.IMinecraft
import com.instrumentalist.elite.utils.entity.getArmorColor
import com.instrumentalist.elite.utils.entity.stripMinecraftColorCodes
import com.instrumentalist.elite.utils.value.BooleanValue
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.text.Text
import org.lwjgl.glfw.GLFW

class Teams : Module("Teams", ModuleCategory.Combat, GLFW.GLFW_KEY_UNKNOWN, false, true) {
    companion object {
        private val scoreboardTeam = BooleanValue("ScoreboardTeam", true)

        private val nameColor = BooleanValue("NameColor", false)

        private val prefix = BooleanValue("Prefix", false)

        private val armorColor = BooleanValue("ArmorColor", false)

        private val helmet = BooleanValue("Helmet", true) { armorColor.get() }

        private val chestPlate = BooleanValue("ChestPlate", true) { armorColor.get() }

        private val leggings = BooleanValue("Leggings", true) { armorColor.get() }

        private val boots = BooleanValue("Boots", true) { armorColor.get() }

        fun isInClientPlayersTeam(entity: LivingEntity): Boolean {
            if (scoreboardTeam.get() && IMinecraft.mc.player!!.isTeammate(entity))
                return true

            val clientDisplayName = IMinecraft.mc.player!!.displayName
            val targetDisplayName = entity.displayName

            if (clientDisplayName == null || targetDisplayName == null)
                return false

            return nameColor.get() && checkName(clientDisplayName, targetDisplayName) || prefix.get() && checkPrefix(
                targetDisplayName,
                clientDisplayName
            ) || armorColor.get() && checkArmor(entity)
        }

        private fun checkName(clientDisplayName: Text, targetDisplayName: Text): Boolean {
            val targetColor = clientDisplayName.style.color
            val clientColor = targetDisplayName.style.color

            return targetColor != null && clientColor != null && targetColor == clientColor
        }

        private fun checkPrefix(targetDisplayName: Text, clientDisplayName: Text): Boolean {
            val targetName = targetDisplayName.string
                .stripMinecraftColorCodes()
            val clientName = clientDisplayName.string
                .stripMinecraftColorCodes()
            val targetSplit = targetName.split(" ")
            val clientSplit = clientName.split(" ")

            return targetSplit.size > 1 && clientSplit.size > 1 && targetSplit[0] == clientSplit[0]
        }

        private fun checkArmor(entity: LivingEntity): Boolean {
            if (entity !is PlayerEntity) return false

            val hasMatchingArmorColor = listOf(
                helmet.get() to 3,
                chestPlate.get() to 2,
                leggings.get() to 1,
                boots.get() to 0
            ).any { (enabled, slot) ->
                enabled && matchesArmorColor(entity, slot)
            }

            return hasMatchingArmorColor
        }

        private fun matchesArmorColor(player: PlayerEntity, armorSlot: Int): Boolean {
            val ownStack = player.inventory.getArmorStack(armorSlot)
            val otherStack = player.inventory.getArmorStack(armorSlot)

            val ownColor = ownStack.getArmorColor() ?: return false
            val otherColor = otherStack.getArmorColor() ?: return false

            return ownColor == otherColor
        }
    }

    override fun onDisable() {}
    override fun onEnable() {}
}