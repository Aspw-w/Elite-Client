package com.instrumentalist.mixin.injector;

import com.mojang.blaze3d.systems.RenderSystem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.breadloaf.imguimc.imgui.ImguiLoader;

@Mixin(value = RenderSystem.class, remap = false)
public abstract class RenderSystemMixin {

    @Inject(method= "flipFrame", at = @At("HEAD"))
    private static void runTickTail(CallbackInfo ci) {
        ImguiLoader.onFrameRender();
    }
}
