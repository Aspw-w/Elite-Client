package com.instrumentalist.mixin.injector;

import com.instrumentalist.elite.utils.math.TimerUtil;
import net.minecraft.client.render.RenderTickCounter;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RenderTickCounter.Dynamic.class)
public abstract class RenderTickCounterDynamicMixin {
    @Shadow
    private float lastFrameDuration;

    @Inject(at = @At(value = "FIELD", target = "Lnet/minecraft/client/render/RenderTickCounter$Dynamic;prevTimeMillis:J", opcode = Opcodes.PUTFIELD, ordinal = 0), method = "beginRenderTick(J)I")
    public void onBeginRenderTick(long timeMillis, CallbackInfoReturnable<Integer> ci) {
        if (TimerUtil.timerSpeed > 0)
            lastFrameDuration *= TimerUtil.timerSpeed;
    }
}