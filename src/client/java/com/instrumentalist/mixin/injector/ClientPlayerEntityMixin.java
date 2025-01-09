package com.instrumentalist.mixin.injector;

import com.instrumentalist.elite.Client;
import com.instrumentalist.elite.events.features.MotionEvent;
import com.instrumentalist.elite.hacks.features.exploit.PortalScreen;
import com.instrumentalist.elite.hacks.features.movement.InventoryMove;
import com.instrumentalist.elite.hacks.features.movement.NoSlow;
import com.instrumentalist.elite.hacks.features.movement.PerfectHorseJump;
import com.instrumentalist.elite.hacks.features.movement.Sprint;
import com.instrumentalist.elite.utils.simulator.PredictUtil;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.mojang.authlib.GameProfile;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.input.Input;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

@Mixin(ClientPlayerEntity.class)
public abstract class ClientPlayerEntityMixin extends PlayerEntity {

    public ClientPlayerEntityMixin(World world, BlockPos pos, float yaw, GameProfile gameProfile) {
        super(world, pos, yaw, gameProfile);
    }

    @Shadow
    protected abstract void sendSprintingPacket();

    @Shadow
    public double lastX;
    @Shadow
    public double lastBaseY;
    @Shadow
    public double lastZ;
    @Shadow
    public float lastYaw;
    @Shadow
    public float lastPitch;
    @Shadow
    public boolean lastOnGround;
    @Final
    @Shadow
    public ClientPlayNetworkHandler networkHandler;

    @Shadow
    protected abstract boolean isCamera();

    @Shadow
    public int ticksSinceLastPositionPacketSent;
    @Shadow
    private boolean autoJumpEnabled;

    @Shadow
    private boolean lastHorizontalCollision;

    @Shadow
    @Final
    protected MinecraftClient client;

    @Shadow public Input input;

    @ModifyExpressionValue(method = "tickNausea", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/Screen;shouldPause()Z"))
    private boolean portalScreenHook(boolean original) {
        return PortalScreen.screenOpenLimitHook(original);
    }

    @Inject(method = "tickMovement", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;isUsingItem()Z", ordinal = 0))
    private void noSlowHook(CallbackInfo callbackInfo) {
        final Input input = this.input;

        if (NoSlow.Companion.noSlowHook()) {
            input.movementForward *= 5f;
            input.movementSideways *= 5f;
        }
    }

    @ModifyReturnValue(method = "getMountJumpStrength", at = @At("RETURN"))
    private float horseJumpHook(float original) {
        return PerfectHorseJump.modifiedHorseJump(original);
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void invMoveHook(CallbackInfo ci) {
        InventoryMove.moveFreely();
    }

    @ModifyExpressionValue(method = "sendSprintingPacket", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;isSprinting()Z"))
    private boolean silentSprintHook(boolean original) {
        return Sprint.Companion.silentSprintHook(original);
    }

    /**
     * @author Aspw
     * @reason Motion Event
     */
    @Overwrite
    public final void sendMovementPackets() {
        if (!PredictUtil.predicting)
            this.sendSprintingPacket();

        if (this.isCamera()) {
            MotionEvent event = new MotionEvent(this.getX(), this.getY(), this.getZ(), this.getYaw(), this.getPitch(), this.isOnGround());
            if (!PredictUtil.predicting)
                Objects.requireNonNull(Client.eventManager).call(event);
            if (event.isCancelled()) return;

            double d = event.x - this.lastX;
            double e = event.y - this.lastBaseY;
            double f = event.z - this.lastZ;
            double g = (double) (event.yaw - this.lastYaw);
            double h = (double) (event.pitch - this.lastPitch);
            ++this.ticksSinceLastPositionPacketSent;
            boolean bl = MathHelper.squaredMagnitude(d, e, f) > MathHelper.square(2.0E-4) || this.ticksSinceLastPositionPacketSent >= 20;
            boolean bl2 = g != 0.0 || h != 0.0;

            if (!PredictUtil.predicting) {
                if (bl && bl2) {
                    this.networkHandler.sendPacket(new PlayerMoveC2SPacket.Full(event.x, event.y, event.z, event.yaw, event.pitch, event.onGround, this.horizontalCollision));
                } else if (bl) {
                    this.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(event.x, event.y, event.z, event.onGround, this.horizontalCollision));
                } else if (bl2) {
                    this.networkHandler.sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(event.yaw, event.pitch, event.onGround, this.horizontalCollision));
                } else if (this.lastOnGround != event.onGround || this.lastHorizontalCollision != this.horizontalCollision) {
                    this.networkHandler.sendPacket(new PlayerMoveC2SPacket.OnGroundOnly(event.onGround, this.horizontalCollision));
                }
            }

            if (bl) {
                this.lastX = event.x;
                this.lastBaseY = event.y;
                this.lastZ = event.z;
                this.ticksSinceLastPositionPacketSent = 0;
            }

            if (bl2) {
                this.lastYaw = event.yaw;
                this.lastPitch = event.pitch;
            }

            this.lastOnGround = event.onGround;
            this.lastHorizontalCollision = this.horizontalCollision;
            this.autoJumpEnabled = (Boolean) this.client.options.getAutoJump().getValue();
        }
    }
}