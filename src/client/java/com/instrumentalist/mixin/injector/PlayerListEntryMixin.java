package com.instrumentalist.mixin.injector;

import com.instrumentalist.elite.hacks.ModuleManager;
import com.instrumentalist.elite.hacks.features.render.Cape;
import com.instrumentalist.elite.utils.IMinecraft;
import com.mojang.authlib.GameProfile;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerListEntry.class)
public abstract class PlayerListEntryMixin {

    @Shadow
    @Final
    private GameProfile profile;

    @Inject(method = "getSkinTextures", at = @At("RETURN"), cancellable = true)
    private void getSkinTextures(CallbackInfoReturnable<SkinTextures> ci) {
        var textures = ci.getReturnValue();
        if (!ModuleManager.getModuleState(new Cape()) || IMinecraft.mc.player != null && !profile.getId().equals(IMinecraft.mc.player.getGameProfile().getId()) || textures.capeTexture() != null) return;
        Identifier capeFile = Identifier.of("elite", "cape.png");
        ci.setReturnValue(new SkinTextures(textures.texture(), textures.textureUrl(), capeFile,
                capeFile, textures.model(), textures.secure()));
    }
}