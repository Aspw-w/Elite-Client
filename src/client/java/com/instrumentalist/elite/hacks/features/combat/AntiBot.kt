package com.instrumentalist.elite.hacks.features.combat

import com.instrumentalist.elite.events.features.ReceivedPacketEvent
import com.instrumentalist.elite.events.features.TickEvent
import com.instrumentalist.elite.events.features.UpdateEvent
import com.instrumentalist.elite.events.features.WorldEvent
import com.instrumentalist.elite.hacks.Module
import com.instrumentalist.elite.hacks.ModuleCategory
import com.instrumentalist.elite.utils.IMinecraft
import com.instrumentalist.elite.utils.math.TickTimer
import com.instrumentalist.elite.utils.value.ListValue
import com.mojang.authlib.GameProfile
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ArmorItem
import net.minecraft.item.ItemStack
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket
import net.minecraft.network.packet.s2c.play.PlayerRemoveS2CPacket
import org.lwjgl.glfw.GLFW
import java.util.*

class AntiBot :
    Module("Anti Bot", ModuleCategory.Combat, GLFW.GLFW_KEY_UNKNOWN, false, true) {
    companion object {
        private val mode = ListValue("Mode", arrayOf("Advanced", "Hypixel", "Shotbow"), "Advanced")

        private val suspectList = HashSet<UUID>()
        private var botList = HashSet<UUID>()

        fun inBotList(playerEntity: LivingEntity): Boolean {
            return botList.contains(playerEntity.uuid) || mode.get().equals("advanced", true) && !IMinecraft.mc.networkHandler!!.playerUuids.contains(playerEntity.uuid)
        }
    }

    private var armorTimer = TickTimer()
    private var armorChecker = false

    override fun onDisable() {
        botList.clear()
        suspectList.clear()
        armorTimer.reset()
        armorChecker = false
    }

    override fun onEnable() {}

    override fun onWorld(event: WorldEvent) {
        botList.clear()
        suspectList.clear()
        armorTimer.reset()
        armorChecker = false
    }

    override fun onUpdate(event: UpdateEvent) {
        if (IMinecraft.mc.player == null) return

        if (mode.get().equals("hypixel", true)) {
            val listPlayerProfiles = mutableListOf<UUID>()

            for (i in IMinecraft.mc.networkHandler!!.playerList) {
                listPlayerProfiles.add(i.profile.id)
            }

            for (i in IMinecraft.mc.world!!.players) {
                val profile = i.gameProfile.id ?: continue
                if (profile !in listPlayerProfiles && !botList.contains(profile))
                    botList.add(profile)
                else if (profile in listPlayerProfiles && botList.contains(profile))
                    botList.remove(profile)
            }
        }
    }

    override fun onTick(event: TickEvent) {
        if (IMinecraft.mc.player == null) return

        if (mode.get().equals("shotbow", true)) {
            if (suspectList.isEmpty()) return
            for (entity in IMinecraft.mc.world!!.players) {
                if (!suspectList.contains(entity.uuid))
                    continue

                var armor: MutableIterable<ItemStack>? = null

                if (!isFullyArmored(entity)) {
                    armor = entity.armorItems
                    armorChecker = true
                }

                if (armorChecker) {
                    armorTimer.update()

                    if (armorTimer.hasTimePassed(2)) {
                        if ((isFullyArmored(entity) || updatesArmor(
                                entity,
                                armor
                            )) && entity.gameProfile.properties.isEmpty
                        )
                            botList.add(entity.uuid)

                        suspectList.remove(entity.uuid)

                        armorTimer.reset()
                        armorChecker = false
                    }
                }
            }
        }
    }

    override fun onReceivedPacket(event: ReceivedPacketEvent) {
        if (IMinecraft.mc.player == null) return

        val packet = event.packet

        if (mode.get().equals("shotbow", true)) {
            if (packet is PlayerListS2CPacket) {
                for (entry in packet.playerAdditionEntries) {
                    val profile = entry.profile ?: continue

                    if (entry.latency < 2 || profile.properties?.isEmpty == false || isGameProfileUnique(profile))
                        continue

                    if (isDuplicated(profile)) {
                        botList.add(entry.profileId)
                        continue
                    }

                    suspectList.add(entry.profileId)
                }
            } else if (packet is PlayerRemoveS2CPacket) {
                for (uuid in packet.profileIds) {
                    if (suspectList.contains(uuid))
                        suspectList.remove(uuid)

                    if (botList.contains(uuid))
                        botList.remove(uuid)
                }
            }
        }
    }

    private fun isGameProfileUnique(originalProfile: GameProfile): Boolean {
        return IMinecraft.mc.networkHandler!!.playerList.count { it.profile.name == originalProfile.name && it.profile.id == originalProfile.id } == 1
    }

    private fun isDuplicated(originalProfile: GameProfile): Boolean {
        return IMinecraft.mc.networkHandler!!.playerList.count { it.profile.name == originalProfile.name && it.profile.id != originalProfile.id } == 1
    }

    private fun isFullyArmored(entity: PlayerEntity): Boolean {
        return (0..3).all {
            val stack = entity.inventory.getArmorStack(it)
            stack.item is ArmorItem && stack.hasEnchantments()
        }
    }

    private fun updatesArmor(entity: PlayerEntity, prevArmor: MutableIterable<ItemStack>?): Boolean {
        return prevArmor != entity.armorItems
    }
}
