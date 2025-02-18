package com.instrumentalist.elite.hacks.features.render

import com.instrumentalist.elite.hacks.Module
import com.instrumentalist.elite.hacks.ModuleCategory
import com.instrumentalist.elite.hacks.ModuleManager
import com.instrumentalist.elite.hacks.features.combat.KillAura
import com.instrumentalist.elite.utils.IMinecraft
import com.instrumentalist.elite.utils.value.BooleanValue
import com.instrumentalist.elite.utils.value.ListValue
import net.minecraft.block.*
import net.minecraft.item.SwordItem
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.hit.HitResult
import org.lwjgl.glfw.GLFW

class LegacyCombat : Module("Legacy Combat", ModuleCategory.Render, GLFW.GLFW_KEY_UNKNOWN, true, true) {
    companion object {
        @Setting
        val mode = ListValue("Mode", arrayOf("God", "Old", "Astra", "Slide", "Swank"), "God")

        @Setting
        val swordEquip = BooleanValue("Sword Equip", false) { mode.get().equals("old", true) }

        @Setting
        val oldEatSwing = BooleanValue("1.7 Eat Swing", true)

        @Setting
        val oldItemPosition = BooleanValue("1.7 Item Position", true)

        @Setting
        val swingHandWhileDigging = BooleanValue("Swing Hand While Digging", true)

        fun shouldBlock(): Boolean {
            val hitResult = IMinecraft.mc.player!!.raycast(5.0, 0.0f, false)
            if (hitResult.type == HitResult.Type.BLOCK) {
                val blockPos = (hitResult as BlockHitResult).blockPos
                val block = IMinecraft.mc.world!!.getBlockState(blockPos).block
                if ((!ModuleManager.getModuleState(KillAura()) || !KillAura.isBlocking) && (block is ChestBlock || block is EnderChestBlock || block is ShulkerBoxBlock || block is FurnaceBlock || block is CraftingTableBlock || block is CrafterBlock || block is SmokerBlock || block is BlastFurnaceBlock || block is CartographyTableBlock || block is AnvilBlock || block is BellBlock || block is BeaconBlock || block is DragonEggBlock || block is LeverBlock || block is EnchantingTableBlock || block is ButtonBlock || block is GrindstoneBlock || block is LoomBlock || block is NoteBlock || block is FenceGateBlock || block is DoorBlock || block is TrapdoorBlock || block is StonecutterBlock || block is SignBlock || block is WallSignBlock || block is HangingSignBlock || block is WallHangingSignBlock || block is RepeaterBlock || block is ComparatorBlock || block is DispenserBlock || block is JigsawBlock || block is CommandBlock || block is StructureBlock || block is HopperBlock || block is BedBlock || block is BarrelBlock || block is CakeBlock || block is CandleCakeBlock || block is BrewingStandBlock || block is DaylightDetectorBlock))
                    return false
            }

            return IMinecraft.mc.player != null && IMinecraft.mc.world != null && ModuleManager.getModuleState(LegacyCombat()) && IMinecraft.mc.player!!.mainHandStack.item is SwordItem && (IMinecraft.mc.options.useKey.isPressed || ModuleManager.getModuleState(KillAura()) && KillAura.isBlocking)
        }
    }

    override fun tag(): String {
        return mode.get()
    }

    override fun onDisable() {}
    override fun onEnable() {}
}
