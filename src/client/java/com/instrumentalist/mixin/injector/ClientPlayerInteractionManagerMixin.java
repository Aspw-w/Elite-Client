package com.instrumentalist.mixin.injector;

import com.instrumentalist.elite.Client;
import com.instrumentalist.elite.events.features.AttackEvent;
import com.instrumentalist.elite.hacks.ModuleManager;
import com.instrumentalist.elite.hacks.features.movement.Fly;
import com.instrumentalist.elite.hacks.features.world.NoBreakCooldown;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

@Mixin(ClientPlayerInteractionManager.class)
public abstract class ClientPlayerInteractionManagerMixin {

    @Shadow
    private int blockBreakingCooldown;

    @Redirect(method = "updateBlockBreakingProgress", at = @At(value = "FIELD", target = "Lnet/minecraft/client/network/ClientPlayerInteractionManager;blockBreakingCooldown:I", opcode = Opcodes.GETFIELD, ordinal = 0))
    public int noBreakCooldown(ClientPlayerInteractionManager clientPlayerInteractionManager) {
        if (ModuleManager.getModuleState(new NoBreakCooldown()))
            return 0;

        return this.blockBreakingCooldown;
    }

    @Inject(method = "attackEntity", at = @At("HEAD"), cancellable = true)
    public void attackEntity(PlayerEntity player, Entity entity, CallbackInfo ci) {
        AttackEvent event = new AttackEvent(entity);
        Objects.requireNonNull(Client.eventManager).call(event);
        if (event.isCancelled())
            ci.cancel();
    }
}