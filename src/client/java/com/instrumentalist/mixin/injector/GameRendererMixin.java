package com.instrumentalist.mixin.injector;

import com.instrumentalist.elite.Client;
import com.instrumentalist.elite.events.features.RenderEvent;
import com.instrumentalist.elite.hacks.ModuleManager;
import com.instrumentalist.elite.hacks.features.render.Freecam;
import com.instrumentalist.elite.hacks.features.render.NoHurtCam;
import com.instrumentalist.elite.hacks.features.render.Zoom;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {

    @Inject(at = @At(value = "FIELD", target = "Lnet/minecraft/client/render/GameRenderer;renderHand:Z", opcode = Opcodes.GETFIELD, ordinal = 0), method = "renderWorld(Lnet/minecraft/client/render/RenderTickCounter;)V")
    private void renderEvent(RenderTickCounter tickCounter, CallbackInfo ci, @Local(ordinal = 2) Matrix4f matrix4f3, @Local(ordinal = 1) float tickDelta) {
        MatrixStack matrixStack = new MatrixStack();
        matrixStack.multiplyPositionMatrix(matrix4f3);
        RenderEvent event = new RenderEvent(matrixStack, tickDelta);
        Objects.requireNonNull(Client.eventManager).call(event);
    }

    @Inject(method = "tiltViewWhenHurt", at = @At("HEAD"), cancellable = true)
    private void noHurtCamHook(MatrixStack matrixStack, float f, CallbackInfo ci) {
        if (ModuleManager.getModuleState(new NoHurtCam()))
            ci.cancel();
    }

    @Inject(at = @At(value = "RETURN", ordinal = 1), method = "getFov(Lnet/minecraft/client/render/Camera;FZ)F", cancellable = true)
    private void zoomHook(Camera camera, float tickDelta, boolean changingFov, CallbackInfoReturnable<Float> ci) {
        if (Zoom.shouldZoom())
            ci.setReturnValue(Zoom.zoomFovHook(ci.getReturnValueF()));
    }

    @Inject(method = "renderHand", at = @At("HEAD"), cancellable = true)
    private void hideHand(Camera camera, float tickDelta, Matrix4f matrix4f, CallbackInfo ci) {
        if (ModuleManager.getModuleState(new Freecam()) && Freecam.Companion.getCanFly() || Zoom.shouldZoom())
            ci.cancel();
    }
}
