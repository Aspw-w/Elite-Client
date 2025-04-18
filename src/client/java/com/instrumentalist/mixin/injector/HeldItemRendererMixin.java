package com.instrumentalist.mixin.injector;

import com.instrumentalist.elite.hacks.ModuleManager;
import com.instrumentalist.elite.hacks.features.player.Scaffold;
import com.instrumentalist.elite.hacks.features.render.LegacyCombat;
import com.instrumentalist.elite.hacks.features.render.ItemView;
import com.instrumentalist.elite.utils.IMinecraft;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ModelTransformationMode;
import net.minecraft.item.ShieldItem;
import net.minecraft.item.consume.UseAction;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Locale;

@Mixin(HeldItemRenderer.class)
public abstract class HeldItemRendererMixin {

    @Shadow public abstract void renderItem(LivingEntity entity, ItemStack stack, ModelTransformationMode renderMode, boolean leftHanded, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light);

    @Shadow protected abstract void applyEquipOffset(MatrixStack matrices, Arm arm, float equipProgress);

    @Shadow protected abstract void applySwingOffset(MatrixStack matrices, Arm arm, float swingProgress);

    @Shadow private float equipProgressMainHand;

    @Shadow private ItemStack mainHand;

    @Shadow private float prevEquipProgressMainHand;

    @Shadow @Final private MinecraftClient client;

    @Shadow protected abstract void renderArmHoldingItem(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, float equipProgress, float swingProgress, Arm arm);

    @Unique
    private void applyBlockTransformation(final MatrixStack matrices) {
        matrices.translate(-0.15F, 0.16F, 0.15F);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-18.0F));
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(82.0F));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(112.0F));
    }

    @Unique
    private void slightlyTiltItemPosition(AbstractClientPlayerEntity player, Hand hand, ItemStack stack, MatrixStack matrices, boolean legacyCombatToggleCheck) {
        if ((!legacyCombatToggleCheck || ModuleManager.getModuleState(new LegacyCombat()) && LegacyCombat.Companion.getOldItemPosition().get()) && !(stack.getItem() instanceof BlockItem)) {
            final Arm arm = hand == Hand.MAIN_HAND ? player.getMainArm() : player.getMainArm().getOpposite();
            final int direction = arm == Arm.RIGHT ? 1 : -1;

            float itemX = LegacyCombat.Companion.getItemX().get();
            float itemY = LegacyCombat.Companion.getItemY().get();
            float itemZ = LegacyCombat.Companion.getItemZ().get();
            float itemScale = LegacyCombat.Companion.getItemScale().get();

            final float scale = 0.7585F / 0.86F * itemScale;
            matrices.scale(scale, scale, scale);
            matrices.translate(direction * -0.084F + itemX, 0.059F + itemY, 0.08F + itemZ);
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(direction * 5.0F));
        }
    }

    @Inject(method = "renderFirstPersonItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/item/HeldItemRenderer;renderItem(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ModelTransformationMode;ZLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", ordinal = 1))
    private void hookSlightlyTiltItemPosition(AbstractClientPlayerEntity player, float tickDelta, float pitch, Hand hand, float swingProgress, ItemStack stack, float equipProgress, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        this.slightlyTiltItemPosition(player, hand, stack, matrices, true);
    }

    @Inject(method = "renderFirstPersonItem", at = @At("HEAD"), cancellable = true)
    private void itemRendererHook(AbstractClientPlayerEntity player, float tickDelta, float pitch, Hand hand, float swingProgress, ItemStack item, float equipProgress, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        if (ModuleManager.getModuleState(new ItemView()) && ItemView.Companion.getLowOffHand().get() && hand == Hand.OFF_HAND)
            matrices.translate(0f, -0.16f, 0f);

        if (LegacyCombat.Companion.shouldBlock()) {
            if (hand == Hand.MAIN_HAND) {
                ci.cancel();

                matrices.push();

                Arm arm = player.getMainArm();
                float equip = 0f;

                if (LegacyCombat.Companion.getSwordEquip().get())
                    equip = equipProgress;

                switch (LegacyCombat.Companion.getMode().get().toLowerCase(Locale.ROOT)) {
                    case "god":
                        this.applyEquipOffset(matrices, arm, equipProgress);
                        this.applySwingOffset(matrices, arm, swingProgress);

                        this.slightlyTiltItemPosition(player, hand, item, matrices, false);
                        this.applyBlockTransformation(matrices);
                        break;
                    case "old":
                        matrices.translate(-0.05f, 0f, 0f);

                        float n = -0.2f * MathHelper.sin(MathHelper.sqrt(swingProgress) * 3.1415927F);
                        float f = -0.1f * MathHelper.sin(swingProgress * 3.1415927F);

                        matrices.translate(n, 0f, f);

                        this.applyEquipOffset(matrices, arm, equip);
                        this.applySwingOffset(matrices, arm, swingProgress);

                        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(77f));
                        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(-10f));
                        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-80f));

                        if (player.isUsingItem()) {
                            matrices.translate(0.05f, -0.05f, -0.1f);
                            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(20f));
                            matrices.scale(1.1f, 1.1f, 1.1f);
                        }
                        break;

                    case "astra":
                        matrices.translate(-0.05f, 0.08f, 0f);

                        this.applyEquipOffset(matrices, arm, equipProgress / 1.42f);
                        this.applySwingOffset(matrices, arm, swingProgress);

                        float var9 = MathHelper.sin(MathHelper.sqrt(swingProgress) * (float) Math.PI);

                        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-var9 * -30f));
                        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-var9 * 60f));
                        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(-var9 * 32f));

                        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(77f));
                        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(-10f));
                        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-80f));

                        if (player.isUsingItem()) {
                            matrices.translate(0.05f, -0.05f, -0.1f);
                            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(20f));
                            matrices.scale(1.1f, 1.1f, 1.1f);
                        }
                        break;

                    case "slide":
                        matrices.translate(-0.05f, 0.08f, 0f);

                        this.applyEquipOffset(matrices, arm, equipProgress / 1.42f);
                        this.applySwingOffset(matrices, arm, swingProgress);

                        float var111 = MathHelper.sin(MathHelper.sqrt(swingProgress) * (float) Math.PI);

                        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-var111 * -30f));
                        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(77f));
                        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(-10f));
                        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-80f));

                        if (player.isUsingItem()) {
                            matrices.translate(0.05f, -0.05f, -0.1f);
                            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(20f));
                            matrices.scale(1.1f, 1.1f, 1.1f);
                        }
                        break;

                    case "swank":
                        matrices.translate(-0.05f, 0.15f, 0f);

                        this.applyEquipOffset(matrices, arm, equipProgress / 1.42f);
                        this.applySwingOffset(matrices, arm, swingProgress);

                        float var69 = MathHelper.sin(MathHelper.sqrt(swingProgress));

                        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-var69 * -1f));
                        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(77f));
                        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(-2f));
                        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-80f));

                        if (player.isUsingItem()) {
                            matrices.translate(0.05f, -0.05f, -0.1f);
                            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(20f));
                            matrices.scale(1.1f, 1.1f, 1.1f);
                        }
                        break;
                }

                this.renderItem(player, item, ModelTransformationMode.FIRST_PERSON_RIGHT_HAND, false, matrices, vertexConsumers, light);

                matrices.pop();
            } else if (hand == Hand.OFF_HAND && IMinecraft.mc.player != null && IMinecraft.mc.player.getOffHandStack() != null && IMinecraft.mc.player.getOffHandStack().getItem() instanceof ShieldItem) {
                ci.cancel();
            }
        } else if (Scaffold.Companion.getLastSlot() != null && IMinecraft.mc.player != null) {
            if (hand == Hand.MAIN_HAND) {
                if (!ModuleManager.getModuleState(new Scaffold()) && (IMinecraft.mc.player.getInventory().selectedSlot != Scaffold.Companion.getLastSlot() || IMinecraft.mc.player.isUsingItem())) {
                    Scaffold.Companion.setSpoofTick(0);
                    Scaffold.Companion.setLastSlot(null);
                    return;
                }

                ci.cancel();

                matrices.push();

                Arm arm = player.getMainArm();

                if (!IMinecraft.mc.player.getInventory().getStack(Scaffold.Companion.getLastSlot()).isEmpty()) {
                    float f = -0.4F * MathHelper.sin(MathHelper.sqrt(swingProgress) * (float)Math.PI);
                    float g = 0.2F * MathHelper.sin(MathHelper.sqrt(swingProgress) * ((float)Math.PI * 2F));
                    float h = -0.2F * MathHelper.sin(swingProgress * (float)Math.PI);

                    matrices.translate(f, g, h);

                    this.applyEquipOffset(matrices, arm, equipProgress);
                    this.applySwingOffset(matrices, arm, swingProgress);

                    this.slightlyTiltItemPosition(player, hand, item, matrices, true);

                    this.renderItem(player, IMinecraft.mc.player.getInventory().getStack(Scaffold.Companion.getLastSlot()), ModelTransformationMode.FIRST_PERSON_RIGHT_HAND, false, matrices, vertexConsumers, light);
                } else if (!player.isInvisible()) {
                    this.renderArmHoldingItem(matrices, vertexConsumers, light, equipProgress, swingProgress, arm);
                }

                matrices.pop();

                if (!ModuleManager.getModuleState(new Scaffold())) {
                    Scaffold.Companion.setSpoofTick(Scaffold.Companion.getSpoofTick() - 1);
                    if (Scaffold.Companion.getSpoofTick() <= 1) {
                        Scaffold.Companion.setSpoofTick(0);
                        Scaffold.Companion.setLastSlot(null);
                    }
                }
            }
        }
    }

    @Inject(method = "updateHeldItems", at = @At("HEAD"), cancellable = true)
    public void itemSpoofHook(CallbackInfo ci) {
        if (LegacyCombat.Companion.shouldBlock() || ModuleManager.getModuleState(new LegacyCombat()) && LegacyCombat.Companion.getOldEatSwing().get() && IMinecraft.mc.player.isUsingItem() && (IMinecraft.mc.player.getActiveItem().getUseAction() == UseAction.EAT || IMinecraft.mc.player.getActiveItem().getUseAction() == UseAction.DRINK || IMinecraft.mc.player.getActiveItem().getUseAction() == UseAction.BOW)) {
            ci.cancel();

            this.prevEquipProgressMainHand = this.equipProgressMainHand;
            ClientPlayerEntity clientPlayerEntity = this.client.player;
            if (clientPlayerEntity == null) return;
            ItemStack itemStack = clientPlayerEntity.getMainHandStack();

            if (ItemStack.areEqual(this.mainHand, itemStack))
                this.mainHand = itemStack;

            this.equipProgressMainHand += MathHelper.clamp((this.mainHand == itemStack ? 1f : 0.0F) - this.equipProgressMainHand, -0.4F, 0.4F);

            if (this.equipProgressMainHand < 0.1F)
                this.mainHand = itemStack;
        } else if (Scaffold.Companion.getLastSlot() != null && IMinecraft.mc.player != null) {
            ci.cancel();

            this.prevEquipProgressMainHand = this.equipProgressMainHand;
            ClientPlayerEntity clientPlayerEntity = this.client.player;
            if (clientPlayerEntity == null) return;
            ItemStack itemStack = ModuleManager.getModuleState(new Scaffold()) ? IMinecraft.mc.player.getInventory().getStack(Scaffold.Companion.getLastSlot()) : clientPlayerEntity.getMainHandStack();

            if (ItemStack.areEqual(this.mainHand, itemStack))
                this.mainHand = itemStack;

            float f = clientPlayerEntity.getAttackCooldownProgress(1.0F);
            this.equipProgressMainHand += MathHelper.clamp((this.mainHand == itemStack ? (Scaffold.Companion.getLastSlot() != null && IMinecraft.mc.player != null ? 1F : f * f * f) : 0.0F) - this.equipProgressMainHand, -0.4F, 0.4F);

            if (this.equipProgressMainHand < 0.1F)
                this.mainHand = itemStack;
        }
    }
}