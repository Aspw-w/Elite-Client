package com.instrumentalist.elite.hacks.features.render

import com.instrumentalist.elite.hacks.Module
import com.instrumentalist.elite.hacks.ModuleCategory
import org.lwjgl.glfw.GLFW

class Cape : Module("Cape", ModuleCategory.Render, GLFW.GLFW_KEY_UNKNOWN, true, true) {

    override fun onDisable() {}
    override fun onEnable() {}
}
