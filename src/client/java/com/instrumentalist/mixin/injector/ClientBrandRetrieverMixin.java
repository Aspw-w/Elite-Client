package com.instrumentalist.mixin.injector;

import com.instrumentalist.elite.hacks.features.exploit.VanillaSpoofer;
import net.minecraft.client.ClientBrandRetriever;
import net.minecraft.obfuscate.DontObfuscate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(ClientBrandRetriever.class)
public abstract class ClientBrandRetrieverMixin {

    /**
     * @author Aspw
     * @reason Client Brand Spoofer
     */
    @Overwrite(remap = false)
    @DontObfuscate
    public static String getClientModName() {
        return new VanillaSpoofer().getCustomBrand();
    }
}