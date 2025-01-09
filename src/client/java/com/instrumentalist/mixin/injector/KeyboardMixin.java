package com.instrumentalist.mixin.injector;

import com.instrumentalist.elite.Client;
import com.instrumentalist.elite.events.features.KeyboardEvent;
import com.instrumentalist.elite.utils.render.RenderUtil;
import com.instrumentalist.mixin.Initializer;
import net.minecraft.client.Keyboard;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.breadloaf.imguimc.imgui.ImguiLoader;

import java.awt.*;
import java.util.Objects;

@Mixin(Keyboard.class)
public abstract class KeyboardMixin {

    @Inject(method = "setup", at = @At("TAIL"))
    public void setup(long l, CallbackInfo ci) {
        ImguiLoader.onGlfwInit(l);
    }

    @Inject(method = "onKey", at = @At("HEAD"), cancellable = true)
    public void onKey(long l, int i, int j, int k, int m, CallbackInfo ci) {
        if (Initializer.shouldCancelGameKeyboardInputs())
            ci.cancel();

        if (!String.valueOf(i).equals("-1")) {
            final KeyboardEvent event = new KeyboardEvent(i, k);
            Objects.requireNonNull(Client.eventManager).call(event);
        }
    }

    @Inject(method = "onChar", at = @At("HEAD"), cancellable = true)
    public void onChar(long l, int i, int j, CallbackInfo ci) {
        if (Initializer.shouldCancelGameKeyboardInputs())
            ci.cancel();
    }
}
