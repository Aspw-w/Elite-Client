package com.instrumentalist.mixin.injector;

import com.instrumentalist.elite.Client;
import com.instrumentalist.elite.events.features.MouseScrollEvent;
import net.minecraft.client.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

@Mixin(Mouse.class)
public abstract class MouseMixin {
    @Inject(method = "onMouseScroll", at = @At("HEAD"), cancellable = true)
    private void mouseScrollEvent(long window, double horizontal, double vertical, CallbackInfo ci) {
        MouseScrollEvent event = new MouseScrollEvent(horizontal, vertical);
        Objects.requireNonNull(Client.eventManager).call(event);

        if (event.isCancelled())
            ci.cancel();
    }
}