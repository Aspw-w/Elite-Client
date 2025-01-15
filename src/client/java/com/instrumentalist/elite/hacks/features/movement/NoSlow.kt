package com.instrumentalist.elite.hacks.features.movement

import com.instrumentalist.elite.events.features.MotionEvent
import com.instrumentalist.elite.events.features.SendPacketEvent
import com.instrumentalist.elite.events.features.TickEvent
import com.instrumentalist.elite.events.features.UpdateEvent
import com.instrumentalist.elite.hacks.Module
import com.instrumentalist.elite.hacks.ModuleCategory
import com.instrumentalist.elite.hacks.ModuleManager
import com.instrumentalist.elite.utils.ChatUtil
import com.instrumentalist.elite.utils.IMinecraft
import com.instrumentalist.elite.utils.move.MovementUtil
import com.instrumentalist.elite.utils.packet.PacketUtil
import com.instrumentalist.elite.utils.rotation.RotationUtil
import com.instrumentalist.elite.utils.value.BooleanValue
import com.instrumentalist.elite.utils.value.ListValue
import net.minecraft.client.input.Input
import net.minecraft.component.DataComponentTypes
import net.minecraft.item.BowItem
import net.minecraft.item.Items
import net.minecraft.item.PotionItem
import net.minecraft.item.SwordItem
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket
import net.minecraft.util.Hand
import org.lwjgl.glfw.GLFW

class NoSlow : Module("No Slow", ModuleCategory.Movement, GLFW.GLFW_KEY_UNKNOWN, false, true) {
    companion object {
        @Setting
        private val mode = ListValue("Mode", arrayOf("Vanilla", "Hypixel"), "Vanilla")

        @Setting
        private val sneak = BooleanValue("Sneak", false) { mode.get().equals("vanilla", true) }

        fun noSlowHook(): Boolean {
            if (ModuleManager.getModuleState(NoSlow())) {
                when (mode.get().lowercase()) {
                    "vanilla" -> return sneak.get() || !IMinecraft.mc.player!!.isSneaking
                    "hypixel" -> return IMinecraft.mc.player!!.mainHandStack.item !is SwordItem && !IMinecraft.mc.player!!.isSneaking
                }
            }

            return false
        }
    }

    private var waitingPacket = false

    override fun tag(): String {
        return mode.get()
    }

    override fun onDisable() {
        waitingPacket = false
    }

    override fun onEnable() {}

    override fun onMotion(event: MotionEvent) {
        if (IMinecraft.mc.player == null) return

        if (mode.get().equals("hypixel", true) && IMinecraft.mc.player!!.mainHandStack.item !is SwordItem && IMinecraft.mc.player!!.isUsingItem)
            event.y += 1E-14
    }

    override fun onSendPacket(event: SendPacketEvent) {
        if (IMinecraft.mc.player == null) return

        val packet = event.packet

        if (mode.get().equals("hypixel", true) && MovementUtil.fallTicks < 2 && (IMinecraft.mc.player!!.mainHandStack.item.components.contains(DataComponentTypes.FOOD) || IMinecraft.mc.player!!.mainHandStack.item == Items.POTION || IMinecraft.mc.player!!.mainHandStack.item is BowItem || IMinecraft.mc.player!!.mainHandStack.item == Items.MILK_BUCKET)) {
            if (packet is PlayerInteractItemC2SPacket) {
                event.cancel()

                if (IMinecraft.mc.player!!.isOnGround)
                    IMinecraft.mc.player!!.jump()

                waitingPacket = true
            }

            if (packet is PlayerInteractBlockC2SPacket)
                event.cancel()
        }
    }

    override fun onUpdate(event: UpdateEvent) {
        if (IMinecraft.mc.player == null) return

        if (mode.get().equals("hypixel", true) && waitingPacket && MovementUtil.fallTicks >= 2 && (IMinecraft.mc.player!!.mainHandStack.item.components.contains(DataComponentTypes.FOOD) || IMinecraft.mc.player!!.mainHandStack.item == Items.POTION || IMinecraft.mc.player!!.mainHandStack.item is BowItem || IMinecraft.mc.player!!.mainHandStack.item == Items.MILK_BUCKET)) {
            val yaw = if (RotationUtil.currentYaw != null) RotationUtil.currentYaw else IMinecraft.mc.player!!.yaw
            val pitch = if (RotationUtil.currentPitch != null) RotationUtil.currentPitch else IMinecraft.mc.player!!.pitch

            PacketUtil.sendPacket(
                PlayerInteractItemC2SPacket(
                    Hand.MAIN_HAND,
                    0,
                    yaw!!,
                    pitch!!
                )
            )

            waitingPacket = false
        }
    }

    override fun onTick(event: TickEvent) {
        if (IMinecraft.mc.player == null) return

        if (mode.get().equals("hypixel", true) && IMinecraft.mc.player!!.isOnGround && waitingPacket)
            IMinecraft.mc.options.jumpKey.isPressed = false
    }
}
