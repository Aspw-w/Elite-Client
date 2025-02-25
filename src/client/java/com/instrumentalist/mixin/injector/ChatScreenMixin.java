package com.instrumentalist.mixin.injector;

import com.instrumentalist.elite.hacks.ModuleManager;
import com.instrumentalist.elite.hacks.features.player.ChatCommands;
import net.minecraft.client.gui.screen.ChatScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.breadloaf.imguimc.customwindow.ModuleRenderable;

@Mixin(ChatScreen.class)
public class ChatScreenMixin {

    @Inject(method = "sendMessage", at = @At("HEAD"), cancellable = true)
    private void chatCommandHook(String message, boolean addToHistory, CallbackInfo ci) {
        if (ModuleManager.getModuleState(new ChatCommands()) && message.startsWith(ChatCommands.Companion.getPrefix().get())) {
            ModuleRenderable.executeCommand(message.substring(ChatCommands.Companion.getPrefix().get().length()), true);
            ci.cancel();
        }
    }
}