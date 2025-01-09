package com.instrumentalist.elite.hacks.features.combat;

import com.instrumentalist.elite.events.features.UpdateEvent
import com.instrumentalist.elite.hacks.Module
import com.instrumentalist.elite.hacks.ModuleCategory
import com.instrumentalist.elite.hacks.ModuleManager
import com.instrumentalist.elite.hacks.features.player.Scaffold
import com.instrumentalist.elite.utils.ChatUtil
import com.instrumentalist.elite.utils.IMinecraft
import com.instrumentalist.elite.utils.math.TargetUtil
import com.instrumentalist.elite.utils.math.TickTimer
import com.instrumentalist.elite.utils.packet.PacketUtil
import com.instrumentalist.elite.utils.rotation.RotationUtil
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen
import net.minecraft.component.DataComponentTypes
import net.minecraft.component.type.PotionContentsComponent
import net.minecraft.entity.effect.StatusEffect
import net.minecraft.entity.effect.StatusEffects
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.item.SplashPotionItem
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket
import net.minecraft.registry.entry.RegistryEntry
import net.minecraft.util.Hand
import org.lwjgl.glfw.GLFW
import kotlin.random.Random

class AutoPotion : Module("Auto Potion", ModuleCategory.Combat, GLFW.GLFW_KEY_UNKNOWN, false, true) {

    private var wasRotating = false
    private var potted = false
    private var resetPotiHaveAEffect = false
    private var oldSlot = -1
    private var potTimer = TickTimer()

    private val potions = mutableListOf(
        StatusEffects.SPEED,
        StatusEffects.RESISTANCE,
        StatusEffects.REGENERATION,
        StatusEffects.FIRE_RESISTANCE,
        StatusEffects.INVISIBILITY,
        StatusEffects.WATER_BREATHING,
        StatusEffects.STRENGTH,
        StatusEffects.JUMP_BOOST,
        StatusEffects.INSTANT_HEALTH
    )

    override fun onDisable() {
        if (IMinecraft.mc.player == null) return
        TargetUtil.noKillAura = false
        RotationUtil.reset()
        resetPotiHaveAEffect = false
        potted = false
        wasRotating = false
        oldSlot = -1
        potTimer.reset()
    }

    override fun onEnable() {}

    override fun onUpdate(event: UpdateEvent) {
        if (IMinecraft.mc.player != null) {
            potTimer.update()
            if (potTimer.hasTimePassed(40))
                potTimer.reset()
        }

        if (IMinecraft.mc.player == null || !IMinecraft.mc.player!!.isOnGround || IMinecraft.mc.currentScreen is GenericContainerScreen || resetPotiHaveAEffect || potted || ModuleManager.getModuleState(
                Scaffold()
            ) || IMinecraft.mc.player!!.isUsingItem
        ) {
            if (wasRotating) {
                RotationUtil.reset()
                TargetUtil.noKillAura = false
                if (oldSlot != -1)
                    IMinecraft.mc.player!!.inventory!!.selectedSlot = oldSlot
                oldSlot = -1
            }
            if (potTimer.hasTimePassed(20)) {
                potted = false
                potTimer.reset()
            }
            resetPotiHaveAEffect = false
            wasRotating = false
            return
        }

        if (!potTimer.hasTimePassed(4)) return

        val inventory = IMinecraft.mc.player!!.inventory
        val needPots = mutableListOf<RegistryEntry<StatusEffect?>>()
        val shuffledPotions = potions.shuffled(Random)

        for (i in shuffledPotions) {
            if (!IMinecraft.mc.player!!.hasStatusEffect(i) && i != StatusEffects.INSTANT_HEALTH && i != StatusEffects.REGENERATION || IMinecraft.mc.player!!.health <= 10 && (i == StatusEffects.INSTANT_HEALTH || i == StatusEffects.REGENERATION))
                needPots.add(i)
            else if (needPots.contains(i)) needPots.remove(i)
        }

        if (needPots.isEmpty()) {
            if (wasRotating) {
                RotationUtil.reset()
                TargetUtil.noKillAura = false
                wasRotating = false
                if (oldSlot != -1)
                    IMinecraft.mc.player!!.inventory!!.selectedSlot = oldSlot
                oldSlot = -1
                if (potTimer.hasTimePassed(20)) {
                    potted = false
                    potTimer.reset()
                }
            }
            return
        }

        if (IMinecraft.mc.player!!.mainHandStack.item !is SplashPotionItem) {
            val hotbarTargets = arrayOfNulls<ItemStack>(9)

            for (slot in hotbarTargets.indices) {
                val hotbarIndex = findPotInHotbar(inventory, needPots)
                oldSlot = inventory.selectedSlot
                if (hotbarIndex != slot && hotbarIndex != -1) {
                    inventory.selectedSlot = hotbarIndex
                    break
                }
            }
            return
        }

        val mainHandItem = IMinecraft.mc.player!!.mainHandStack.item
        if (mainHandItem is SplashPotionItem) {
            val potionContents = IMinecraft.mc.player!!.mainHandStack.components
                .getOrDefault(
                    DataComponentTypes.POTION_CONTENTS,
                    PotionContentsComponent.DEFAULT
                )

            val containsEffect = potionContents.effects.any { effectInstance ->
                needPots.contains(effectInstance.effectType)
            }

            if (!containsEffect) {
                resetPotiHaveAEffect = true
                return
            }
        }

        TargetUtil.noKillAura = true
        RotationUtil.setRotation((IMinecraft.mc.player!!.yaw % 360 + 360) % 360, 90f)
        if (!wasRotating)
            ModuleManager.pitchTick = 0
        wasRotating = true

        if (!potTimer.hasTimePassed(4) || ModuleManager.pitchTick < 3) return

        PacketUtil.sendPacket(
            PlayerInteractItemC2SPacket(
                Hand.MAIN_HAND,
                0,
                RotationUtil.currentYaw!!,
                RotationUtil.currentPitch!!
            )
        )
        PacketUtil.sendPacket(HandSwingC2SPacket(Hand.MAIN_HAND))

        potted = true
    }

    private fun findPotInHotbar(inventory: PlayerInventory, needPots: MutableList<RegistryEntry<StatusEffect?>>): Int {
        for (i in 0..8) {
            val stack = inventory.getStack(i)
            if (stack.item == Items.SPLASH_POTION) {
                for (u in needPots) {
                    if (hasEffect(stack, u)) return i
                }
            }
        }
        return -1
    }

    private fun hasEffect(
        stack: ItemStack,
        effect: RegistryEntry<StatusEffect?>,
    ): Boolean {
        val potionContents = stack.components
            .getOrDefault(
                DataComponentTypes.POTION_CONTENTS,
                PotionContentsComponent.DEFAULT
            )

        for (effectInstance in potionContents.effects) if (effectInstance.effectType === effect) return true

        return false
    }
}
