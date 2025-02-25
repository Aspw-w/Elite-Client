package com.instrumentalist.elite.hacks.features.player

import com.instrumentalist.elite.events.features.UpdateEvent
import com.instrumentalist.elite.hacks.Module
import com.instrumentalist.elite.hacks.ModuleCategory
import com.instrumentalist.elite.utils.IMinecraft
import com.instrumentalist.elite.utils.math.MSTimer
import com.instrumentalist.elite.utils.math.RandomUtil
import com.instrumentalist.elite.utils.value.BooleanValue
import com.instrumentalist.elite.utils.value.IntValue
import com.instrumentalist.elite.utils.value.TextValue
import org.lwjgl.glfw.GLFW

class ChatCommands : Module("Chat Commands", ModuleCategory.Player, GLFW.GLFW_KEY_UNKNOWN, true, true) {
    companion object {
        @Setting
        val prefix = TextValue("Prefix", ".")
    }

    override fun onDisable() {}
    override fun onEnable() {}
}
