package com.instrumentalist.elite.hacks.features.player

import com.instrumentalist.elite.events.features.MotionEvent
import com.instrumentalist.elite.events.features.UpdateEvent
import com.instrumentalist.elite.hacks.Module
import com.instrumentalist.elite.hacks.ModuleCategory
import com.instrumentalist.elite.hacks.features.movement.InventoryMove
import com.instrumentalist.elite.utils.ChatUtil
import com.instrumentalist.elite.utils.IMinecraft
import com.instrumentalist.elite.utils.math.ToolUtil
import com.instrumentalist.elite.utils.move.MovementUtil
import com.instrumentalist.elite.utils.packet.BlinkUtil
import com.instrumentalist.elite.utils.value.BooleanValue
import com.instrumentalist.elite.utils.value.IntValue
import net.minecraft.client.gui.screen.ingame.FurnaceScreen
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen
import net.minecraft.client.gui.screen.ingame.InventoryScreen
import net.minecraft.client.option.KeyBinding
import net.minecraft.enchantment.Enchantment
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.entity.EquipmentSlot
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.item.*
import net.minecraft.screen.slot.SlotActionType
import org.lwjgl.glfw.GLFW

class InvManager : Module("Inv Manager", ModuleCategory.Player, GLFW.GLFW_KEY_UNKNOWN, false, true) {
    private val delay = IntValue("Delay", 0, 0, 10)

    private val armorDelay = IntValue("Armor Delay", 0, 0, 10)

    private val cleaner = BooleanValue("Cleaner", true)

    private val onlyInventory = BooleanValue("Only Inventory", false)

    private var tickCounter: Int = 0

    companion object {
        var cleaning = false
    }

    private val itemSlots = mapOf(
        Items.NETHERITE_SWORD to 0,
        Items.DIAMOND_SWORD to 0,
        Items.IRON_SWORD to 0,
        Items.GOLDEN_SWORD to 0,
        Items.STONE_SWORD to 0,
        Items.WOODEN_SWORD to 0,
        Items.NETHERITE_PICKAXE to 1,
        Items.DIAMOND_PICKAXE to 1,
        Items.IRON_PICKAXE to 1,
        Items.GOLDEN_PICKAXE to 1,
        Items.STONE_PICKAXE to 1,
        Items.WOODEN_PICKAXE to 1,
        Items.NETHERITE_AXE to 2,
        Items.DIAMOND_AXE to 2,
        Items.IRON_AXE to 2,
        Items.GOLDEN_AXE to 2,
        Items.STONE_AXE to 2,
        Items.WOODEN_AXE to 2,
        Items.NETHERITE_SHOVEL to 3,
        Items.DIAMOND_SHOVEL to 3,
        Items.IRON_SHOVEL to 3,
        Items.GOLDEN_SHOVEL to 3,
        Items.STONE_SHOVEL to 3,
        Items.WOODEN_SHOVEL to 3
    )

    private val swordMaterialRank = mapOf(
        Items.NETHERITE_SWORD to 6,
        Items.DIAMOND_SWORD to 5,
        Items.IRON_SWORD to 4,
        Items.GOLDEN_SWORD to 3,
        Items.STONE_SWORD to 2,
        Items.WOODEN_SWORD to 1,
    )

    private val pickaxeMaterialRank = mapOf(
        Items.NETHERITE_PICKAXE to 6,
        Items.DIAMOND_PICKAXE to 5,
        Items.IRON_PICKAXE to 4,
        Items.GOLDEN_PICKAXE to 3,
        Items.STONE_PICKAXE to 2,
        Items.WOODEN_PICKAXE to 1
    )

    private val axeMaterialItem = mapOf(
        Items.NETHERITE_AXE to 6,
        Items.DIAMOND_AXE to 5,
        Items.IRON_AXE to 4,
        Items.GOLDEN_AXE to 3,
        Items.STONE_AXE to 2,
        Items.WOODEN_AXE to 1
    )

    private val shovelMaterialItem = mapOf(
        Items.NETHERITE_SHOVEL to 6,
        Items.DIAMOND_SHOVEL to 5,
        Items.IRON_SHOVEL to 4,
        Items.GOLDEN_SHOVEL to 3,
        Items.STONE_SHOVEL to 2,
        Items.WOODEN_SHOVEL to 1
    )

    override fun onDisable() {
        tickCounter = 0
        cleaning = false
    }

    override fun onEnable() {}

    override fun onUpdate(event: UpdateEvent) {
        val player = IMinecraft.mc.player ?: return

        if (onlyInventory.get() && IMinecraft.mc.currentScreen !is InventoryScreen || IMinecraft.mc.currentScreen is GenericContainerScreen || IMinecraft.mc.currentScreen is FurnaceScreen) {
            cleaning = false
            tickCounter = 0
            return
        }

        if (tickCounter > 0) {
            cleaning = true
            tickCounter--
            return
        }

        val inventory = player.inventory

        val hotbarTargets = arrayOfNulls<ItemStack>(9)

        for (i in inventory.main.indices) {
            val stack = inventory.getStack(i)
            val slot = itemSlots[stack.item]
            if (slot != null) {
                val targetStack = hotbarTargets[slot]
                if (targetStack == null || compareItems(stack, targetStack) > 0) {
                    hotbarTargets[slot] = stack
                }
            }
        }

        for (slot in hotbarTargets.indices) {
            val targetStack = hotbarTargets[slot] ?: continue
            val hotbarIndex = findItemInHotbar(inventory, targetStack.item)

            if (hotbarIndex != slot) {
                val inventoryIndex = inventory.main.indexOfFirst {
                    it.item == targetStack.item && compareItems(it, targetStack) == 0
                }

                if (inventoryIndex != -1) {
                    cleaning = true
                    swapItems(inventoryIndex, slot)
                    tickCounter = delay.get()
                    return
                }
            }
        }

        val armorCandidates = mutableMapOf<EquipmentSlot, Pair<ItemStack, Int>>()

        for (i in 0..35) {
            val stack = inventory.getStack(i)
            if (stack.item is ArmorItem) {
                val armorEquipSlot = ToolUtil.getArmorEquipmentSlot(stack)
                val currentBest = armorCandidates[armorEquipSlot]
                if (currentBest == null || ToolUtil.isBetterArmor(stack, currentBest.first)) {
                    armorCandidates[armorEquipSlot] = Pair(stack, i)
                }
            }
        }

        for ((slot, candidate) in armorCandidates) {
            val (bestArmor, inventoryIndex) = candidate
            val currentArmor = player.getEquippedStack(slot)

            if (currentArmor.isEmpty || ToolUtil.isBetterArmor(bestArmor, currentArmor)) {
                if (currentArmor.isEmpty || isBetterArmorWithEnchant(bestArmor, currentArmor)) {
                    cleaning = true
                    val armorSlot = ToolUtil.getItemSlotId(currentArmor)
                    IMinecraft.mc.interactionManager!!.clickSlot(
                        player.currentScreenHandler.syncId,
                        armorSlot,
                        0,
                        SlotActionType.THROW,
                        player
                    )
                    tickCounter = armorDelay.get()
                    return
                }

                cleaning = true
                val slotIndex = if (inventoryIndex < 9) inventoryIndex + 36 else inventoryIndex
                IMinecraft.mc.interactionManager!!.clickSlot(
                    player.currentScreenHandler.syncId,
                    slotIndex,
                    0,
                    SlotActionType.QUICK_MOVE,
                    player
                )
                tickCounter = delay.get()
                return
            }
        }

        if (cleaner.get()) {
            val cleanerTargets = arrayOfNulls<ItemStack>(9)

            val allowedItems = setOf(
                Items.ENDER_PEARL, Items.ENDER_EYE, Items.TRIDENT, Items.MACE, Items.BOW, Items.CROSSBOW,
                Items.ARROW, Items.GOLDEN_APPLE, Items.ENCHANTED_GOLDEN_APPLE, Items.APPLE,
                Items.MUSHROOM_STEW, Items.BREAD, Items.PORKCHOP, Items.COOKED_PORKCHOP, Items.GOLDEN_CARROT,
                Items.CARROT, Items.POTATO, Items.BAKED_POTATO, Items.COOKED_BEEF, Items.BEEF,
                Items.COOKED_CHICKEN, Items.CHICKEN, Items.COOKED_MUTTON, Items.MUTTON, Items.COOKED_RABBIT,
                Items.RABBIT, Items.RABBIT_STEW, Items.BEETROOT, Items.BEETROOT_SOUP, Items.MELON_SLICE,
                Items.PUMPKIN_PIE, Items.COOKIE, Items.SWEET_BERRIES, Items.COD, Items.COOKED_COD,
                Items.SALMON, Items.COOKED_SALMON, Items.TROPICAL_FISH, Items.PUFFERFISH, Items.HONEY_BOTTLE,
                Items.GLOW_BERRIES, Items.DRIED_KELP, Items.ROTTEN_FLESH, Items.POISONOUS_POTATO,
                Items.COMPASS,
                Items.RECOVERY_COMPASS,
                Items.WATER_BUCKET,
                Items.ELYTRA,
                Items.IRON_INGOT,
                Items.DIAMOND,
                Items.EMERALD,
                Items.GOLD_INGOT,
                Items.NETHERITE_INGOT
            )

            for (i in 0..35) {
                val stack = inventory.getStack(i)
                val slot = itemSlots[stack.item]
                if (slot != null) {
                    val targetStack = cleanerTargets[slot]
                    if (targetStack == null || compareItems(stack, targetStack) > 0) {
                        cleanerTargets[slot] = stack
                    }
                }
            }

            for (i in 0..35) {
                val stack = inventory.getStack(i)
                if (!stack.isEmpty && stack.item !is BlockItem && stack.item !is SpawnEggItem && stack.item !is PotionItem && stack.item !is SplashPotionItem && stack.item !is LingeringPotionItem && stack.item !in allowedItems && !cleanerTargets.contains(stack)) {
                    cleaning = true
                    val button = if (stack.count > 1) 1 else 0
                    val slot = if (i < 9) i + 36 else i
                    IMinecraft.mc.interactionManager!!.clickSlot(
                        player.currentScreenHandler.syncId,
                        slot,
                        button,
                        SlotActionType.THROW,
                        player
                    )
                    tickCounter = delay.get()
                    return
                }
            }
        }

        cleaning = false
    }

    private fun findItemInHotbar(inventory: PlayerInventory, item: Item): Int {
        for (i in 0..8) {
            if (inventory.getStack(i).item == item) {
                return i
            }
        }
        return -1
    }

    private fun compareItems(stack1: ItemStack, stack2: ItemStack): Int {
        val item1 = stack1.item
        val item2 = stack2.item

        var rank1 = 0
        var rank2 = 0

        if (item1 is SwordItem) {
            rank1 = swordMaterialRank[item1]!!
            rank2 = swordMaterialRank[item2]!!
        }

        if (item1 is MiningToolItem && item2 is MiningToolItem) {
            if (item1 is PickaxeItem && item2 is PickaxeItem) {
                rank1 = pickaxeMaterialRank[item1]!!
                rank2 = pickaxeMaterialRank[item2]!!
            } else if (item1 is AxeItem && item2 is AxeItem) {
                rank1 = axeMaterialItem[item1]!!
                rank2 = axeMaterialItem[item2]!!
            } else if (item1 is ShovelItem && item2 is ShovelItem) {
                rank1 = shovelMaterialItem[item1]!!
                rank2 = shovelMaterialItem[item2]!!
            }
        }

        if (rank1 != rank2)
            return rank1.compareTo(rank2)

        if (item1 == item2) {
            val ench1 = totalEnchantmentLevels(stack1)
            val ench2 = totalEnchantmentLevels(stack2)
            if (ench1 != ench2) return ench1.compareTo(ench2)
        }

        return 0
    }

    private fun totalEnchantmentLevels(stack: ItemStack): Int {
        val enchantments = EnchantmentHelper.getEnchantments(stack)
        var total = 0
        for (entry in enchantments.enchantmentEntries) {
            total += entry.intValue  // level
        }
        return total
    }

    private fun isBetterArmorWithEnchant(a: ItemStack, b: ItemStack): Boolean {
        if (ToolUtil.isBetterArmor(a, b)) return true
        if (ToolUtil.isBetterArmor(b, a)) return false
        return totalEnchantmentLevels(a) > totalEnchantmentLevels(b)
    }

    private fun swapItems(from: Int, to: Int) {
        val syncId = IMinecraft.mc.player!!.currentScreenHandler.syncId
        val fromSlot = if (from < 9) from + 36 else from
        if (fromSlot != to + 36) {
            IMinecraft.mc.interactionManager!!.clickSlot(
                syncId,
                fromSlot,
                to,
                SlotActionType.SWAP,
                IMinecraft.mc.player
            )
        }
    }
}
