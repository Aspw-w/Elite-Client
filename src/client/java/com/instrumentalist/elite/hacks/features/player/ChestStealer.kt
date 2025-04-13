package com.instrumentalist.elite.hacks.features.player

import com.instrumentalist.elite.events.features.MotionEvent
import com.instrumentalist.elite.events.features.UpdateEvent
import com.instrumentalist.elite.hacks.Module
import com.instrumentalist.elite.hacks.ModuleCategory
import com.instrumentalist.elite.utils.IMinecraft
import com.instrumentalist.elite.utils.math.ToolUtil
import com.instrumentalist.elite.utils.value.IntValue
import net.minecraft.item.*
import net.minecraft.screen.GenericContainerScreenHandler
import net.minecraft.screen.slot.SlotActionType
import org.lwjgl.glfw.GLFW
import java.util.*

class ChestStealer : Module("Chest Stealer", ModuleCategory.Player, GLFW.GLFW_KEY_UNKNOWN, false, true) {

    private val stealDelay = IntValue("Steal Delay", 0, 0, 10)

    private val closeDelay = IntValue("Close Delay", 2, 0, 10)

    private val random: Random = Random()
    private var tickCounter: Int = 0
    private var closeCounter: Int = 0

    override fun onDisable() {
        tickCounter = 0
        closeCounter = 0
    }

    override fun onEnable() {}

    override fun onUpdate(event: UpdateEvent) {
        if (IMinecraft.mc.player == null) return

        if (tickCounter < stealDelay.get()) {
            tickCounter++
            return
        }

        tickCounter = 0

        if (IMinecraft.mc.player!!.currentScreenHandler is GenericContainerScreenHandler) {
            val screenHandler = IMinecraft.mc.player?.currentScreenHandler as GenericContainerScreenHandler

            val slotIndices = (0 until screenHandler.slots.size).toMutableList()
            slotIndices.shuffle(random)

            var itemStolen = false

            for (i in slotIndices) {
                if (i < 27) {
                    val slot = screenHandler.getSlot(i)
                    if (slot.hasStack() && shouldSteal(slot.stack)) {
                        IMinecraft.mc.interactionManager!!.clickSlot(
                            screenHandler.syncId,
                            slot.id,
                            0,
                            SlotActionType.QUICK_MOVE,
                            IMinecraft.mc.player
                        )
                        itemStolen = true
                        break
                    }
                }
            }

            if (!itemStolen) {
                if (closeCounter < closeDelay.get()) {
                    closeCounter++
                    return
                }

                closeCounter = 0

                IMinecraft.mc.player?.closeHandledScreen()
            }
        }
    }

    private fun shouldSteal(stack: ItemStack): Boolean {
        if (stack.item is BlockItem || stack.item is PotionItem || stack.item is SplashPotionItem || stack.item is LingeringPotionItem || stack.item == Items.GOLDEN_APPLE || stack.item == Items.ENCHANTED_GOLDEN_APPLE || stack.item == Items.ENDER_PEARL || stack.item == Items.ENDER_EYE)
            return true

        if (stack.item is ArmorItem) {
            val armorSlot = ToolUtil.getArmorEquipmentSlot(stack)
            val currentArmor = IMinecraft.mc.player!!.getEquippedStack(armorSlot)
            if (currentArmor != null && !currentArmor.isEmpty && !ToolUtil.isBetterArmor(stack, currentArmor)) {
                return false
            }
        }

        IMinecraft.mc.player?.inventory?.main?.forEach { invStack ->
            if (ItemStack.areEqual(invStack, stack) && invStack.count >= stack.count)
                return false
        }

        return true
    }
}