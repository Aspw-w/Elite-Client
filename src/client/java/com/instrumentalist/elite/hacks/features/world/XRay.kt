package com.instrumentalist.elite.hacks.features.world

import com.instrumentalist.elite.hacks.Module
import com.instrumentalist.elite.hacks.ModuleCategory
import com.instrumentalist.elite.hacks.ModuleManager
import com.instrumentalist.elite.utils.IMinecraft
import com.instrumentalist.elite.utils.value.BooleanValue
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks.*
import org.lwjgl.glfw.GLFW

class XRay : Module("XRay", ModuleCategory.World, GLFW.GLFW_KEY_F7, false, true) {
    companion object {
        fun hookTransparentOre(blockState: BlockState, original: Boolean): Boolean {
            if (ModuleManager.getModuleState(XRay()))
                return blocks.contains(blockState.block)

            return original
        }

        val blocks = mutableSetOf(
            COAL_ORE,
            COPPER_ORE,
            DIAMOND_ORE,
            EMERALD_ORE,
            GOLD_ORE,
            IRON_ORE,
            LAPIS_ORE,
            REDSTONE_ORE,
            DEEPSLATE_COAL_ORE,
            DEEPSLATE_COPPER_ORE,
            DEEPSLATE_DIAMOND_ORE,
            DEEPSLATE_EMERALD_ORE,
            DEEPSLATE_GOLD_ORE,
            DEEPSLATE_IRON_ORE,
            DEEPSLATE_LAPIS_ORE,
            DEEPSLATE_REDSTONE_ORE,
            COAL_BLOCK,
            COPPER_BLOCK,
            DIAMOND_BLOCK,
            EMERALD_BLOCK,
            GOLD_BLOCK,
            IRON_BLOCK,
            LAPIS_BLOCK,
            REDSTONE_BLOCK,
            RAW_COPPER_BLOCK,
            RAW_GOLD_BLOCK,
            RAW_IRON_BLOCK,
            ANCIENT_DEBRIS,
            NETHER_GOLD_ORE,
            NETHER_QUARTZ_ORE,
            NETHERITE_BLOCK,
            QUARTZ_BLOCK,
            CHEST,
            DISPENSER,
            DROPPER,
            ENDER_CHEST,
            HOPPER,
            TRAPPED_CHEST,
            BLACK_SHULKER_BOX,
            BLUE_SHULKER_BOX,
            BROWN_SHULKER_BOX,
            CYAN_SHULKER_BOX,
            GRAY_SHULKER_BOX,
            GREEN_SHULKER_BOX,
            LIGHT_BLUE_SHULKER_BOX,
            LIGHT_GRAY_SHULKER_BOX,
            LIME_SHULKER_BOX,
            MAGENTA_SHULKER_BOX,
            ORANGE_SHULKER_BOX,
            PINK_SHULKER_BOX,
            PURPLE_SHULKER_BOX,
            RED_SHULKER_BOX,
            SHULKER_BOX,
            WHITE_SHULKER_BOX,
            YELLOW_SHULKER_BOX,
            BEACON,
            CRAFTING_TABLE,
            ENCHANTING_TABLE,
            FURNACE,
            FLOWER_POT,
            JUKEBOX,
            LODESTONE,
            RESPAWN_ANCHOR,
            ANVIL,
            CHIPPED_ANVIL,
            DAMAGED_ANVIL,
            BARREL,
            BLAST_FURNACE,
            BREWING_STAND,
            CARTOGRAPHY_TABLE,
            COMPOSTER,
            FLETCHING_TABLE,
            GRINDSTONE,
            LECTERN,
            LOOM,
            SMITHING_TABLE,
            SMOKER,
            STONECUTTER,
            CAULDRON,
            LAVA_CAULDRON,
            WATER_CAULDRON,
            LAVA,
            WATER,
            END_PORTAL,
            END_PORTAL_FRAME,
            NETHER_PORTAL,
            CHAIN_COMMAND_BLOCK,
            COMMAND_BLOCK,
            REPEATING_COMMAND_BLOCK,
            BOOKSHELF,
            CLAY,
            DRAGON_EGG,
            FIRE,
            SPAWNER,
            TNT
        )
    }

    override fun onDisable() {
        IMinecraft.mc.worldRenderer.reload()
    }

    override fun onEnable() {
        IMinecraft.mc.worldRenderer.reload()
    }
}
