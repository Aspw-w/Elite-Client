package com.instrumentalist.elite.hacks.features.player

import com.instrumentalist.elite.events.features.MotionEvent
import com.instrumentalist.elite.events.features.SendPacketEvent
import com.instrumentalist.elite.events.features.UpdateEvent
import com.instrumentalist.elite.hacks.Module
import com.instrumentalist.elite.hacks.ModuleCategory
import com.instrumentalist.elite.utils.ChatUtil
import com.instrumentalist.elite.utils.IMinecraft
import com.instrumentalist.elite.utils.move.MovementUtil
import com.instrumentalist.elite.utils.packet.PacketUtil
import com.instrumentalist.elite.utils.value.ListValue
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket
import org.lwjgl.glfw.GLFW
import java.util.*
import kotlin.math.abs

class NoFall : Module("No Fall", ModuleCategory.Player, GLFW.GLFW_KEY_UNKNOWN, false, true) {
    @Setting
    private val mode = ListValue("Mode", arrayOf("Packet", "Spoof", "No Ground"), "Packet")

    private var insane = false
    private var fallTicks = 0

    override fun tag(): String {
        return mode.get()
    }

    override fun onDisable() {
        insane = false
        fallTicks = 0
    }

    override fun onEnable() {}

    override fun onMotion(event: MotionEvent) {
        if (IMinecraft.mc.player == null || IMinecraft.mc.player!!.isSpectator || IMinecraft.mc.player!!.isGliding) {
            fallTicks = 0
            return
        }

        when (mode.get().lowercase(Locale.getDefault())) {
            "packet" -> {
                if (IMinecraft.mc.player!!.isOnGround) {
                    fallTicks = 0
                    return
                }

                if (IMinecraft.mc.player!!.velocity.y < -0.4) {
                    fallTicks++
                    if (fallTicks > 2) {
                        event.onGround = true
                        fallTicks = 0
                    }
                }
            }

            "spoof" -> event.onGround = true
            "no ground" -> event.onGround = false
        }
    }
}
