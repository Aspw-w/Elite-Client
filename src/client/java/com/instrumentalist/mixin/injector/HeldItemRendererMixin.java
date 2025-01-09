package com.instrumentalist.mixin.injector;

import com.instrumentalist.elite.hacks.ModuleManager;
import com.instrumentalist.elite.hacks.features.render.ItemView;
import com.instrumentalist.elite.hacks.features.render.LegacyCombat;
import com.instrumentalist.elite.utils.IMinecraft;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ModelTransformationMode;
import net.minecraft.item.ShieldItem;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HeldItemRenderer.class)
public abstract class HeldItemRendererMixin {

    @Shadow public abstract void renderItem(LivingEntity entity, ItemStack stack, ModelTransformationMode renderMode, boolean leftHanded, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light);

    @Shadow protected abstract void applyEquipOffset(MatrixStack matrices, Arm arm, float equipProgress);

    @Inject(method = "renderFirstPersonItem", at = @At("HEAD"), cancellable = true)
    private void legacyCombatHook(AbstractClientPlayerEntity player, float tickDelta, float pitch, Hand hand, float swingProgress, ItemStack item, float equipProgress, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        if (ModuleManager.getModuleState(new ItemView()) && ItemView.Companion.getLowOffHand().get() && hand == Hand.OFF_HAND)
            matrices.translate(0f, -0.16f, 0f);

        if (LegacyCombat.Companion.shouldBlock()) {
            if (hand == Hand.MAIN_HAND) {
                ci.cancel();

                matrices.push();

                Arm arm = player.getMainArm();

                matrices.translate(-0.05f, -0.05f, 0f);

                this.applyEquipOffset(matrices, arm, 0f);

                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(77f));
                matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(-10f));

                float var6 = MathHelper.sin(swingProgress * swingProgress * (float) Math.PI);
                float var7 = MathHelper.sin(MathHelper.sqrt(swingProgress) * (float) Math.PI);

                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(var6 * -20.0F));
                matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(var7 * -20.0F));
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(var7 * -69.0F));

                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-80f));

                float scale = 1.2F;
                matrices.scale(scale, scale, scale);

                this.renderItem(player, item, ModelTransformationMode.FIRST_PERSON_RIGHT_HAND, false, matrices, vertexConsumers, light);

                matrices.pop();
            } else if (hand == Hand.OFF_HAND && IMinecraft.mc.player != null && IMinecraft.mc.player.getOffHandStack() != null && IMinecraft.mc.player.getOffHandStack().getItem() instanceof ShieldItem) {
                ci.cancel();
            }
        }
    }
}