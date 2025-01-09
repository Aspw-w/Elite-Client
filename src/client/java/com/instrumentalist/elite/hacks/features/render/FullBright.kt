package com.instrumentalist.elite.hacks.features.render

import com.instrumentalist.elite.hacks.Module
import com.instrumentalist.elite.hacks.ModuleCategory
import org.lwjgl.glfw.GLFW

class FullBright : Module("Full Bright", ModuleCategory.Render, GLFW.GLFW_KEY_UNKNOWN, false, true) {

    override fun onDisable() {}
    override fun onEnable() {}
}
