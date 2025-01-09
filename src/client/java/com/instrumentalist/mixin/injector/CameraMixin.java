package com.instrumentalist.mixin.injector;

import com.instrumentalist.elite.hacks.ModuleManager;
import com.instrumentalist.elite.hacks.features.render.CameraNoClip;
import com.instrumentalist.elite.hacks.features.render.Freecam;
import net.minecraft.client.render.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Camera.class)
public abstract class CameraMixin {

    @Inject(method = "clipToSpace", at = @At(value = "HEAD"), cancellable = true)
    public void cameraNoClipHook(float f, CallbackInfoReturnable<Float> ci) {
        if (ModuleManager.getModuleState(new CameraNoClip()) || ModuleManager.getModuleState(new Freecam()) && Freecam.Companion.getCanFly())
            ci.setReturnValue(f);
    }
}