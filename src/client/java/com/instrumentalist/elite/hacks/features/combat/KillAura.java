package com.instrumentalist.elite.hacks.features.combat;

import com.instrumentalist.elite.Client;
import com.instrumentalist.elite.events.features.AttackEvent;
import com.instrumentalist.elite.events.features.HandleInputEvent;
import com.instrumentalist.elite.hacks.Module;
import com.instrumentalist.elite.hacks.ModuleCategory;
import com.instrumentalist.elite.utils.ChatUtil;
import com.instrumentalist.elite.utils.IMinecraft;
import com.instrumentalist.elite.utils.entity.PlayerUtil;
import com.instrumentalist.elite.utils.math.TargetUtil;
import com.instrumentalist.elite.utils.move.MovementUtil;
import com.instrumentalist.elite.utils.packet.BlinkUtil;
import com.instrumentalist.elite.utils.packet.PacketUtil;
import com.instrumentalist.elite.utils.pathfinder.MainPathFinder;
import com.instrumentalist.elite.utils.rotation.RotationUtil;
import com.instrumentalist.elite.utils.value.*;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ShieldItem;
import net.minecraft.item.SwordItem;
import net.minecraft.network.packet.c2s.play.*;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

import java.util.*;

public class KillAura extends Module {

    public KillAura() {
        super("Kill Aura", ModuleCategory.Combat, GLFW.GLFW_KEY_UNKNOWN, false, true);
    }

    @Setting
    private final FloatValue targetRange = new FloatValue(
            "Target Range",
            4f,
            0f,
            8f
    );

    @Setting
    private final FloatValue attackRange = new FloatValue(
            "Attack Range",
            3f,
            0f,
            6f
    );

    @Setting
    private final ListValue apsMode = new ListValue(
            "APS Mode",
            Arrays.asList("Cooldown", "Randomized CPS", "Hurt Math", "No Delay").toArray(new String[0]),
            "Randomized CPS"
    );

    @Setting
    private final IntValue maxCps = new IntValue(
            "Max CPS",
            12,
            1,
            20,
            () -> apsMode.get().equalsIgnoreCase("randomized cps")
    );

    @Setting
    private final IntValue minCps = new IntValue(
            "Min CPS",
            6,
            1,
            20,
            () -> apsMode.get().equalsIgnoreCase("randomized cps")
    );

    @Setting
    private final ListValue swingOrderMode = new ListValue(
            "Swing Order Mode",
            Arrays.asList("1.8", "1.9.x", "No Order").toArray(new String[0]),
            "1.9.x"
    );

    @Setting
    public static final ListValue autoBlockMode = new ListValue(
            "Auto Block Mode",
            Arrays.asList("Vanilla", "Hypixel Full", "No Order").toArray(new String[0]),
            "No Order"
    );

    @Setting
    private final BooleanValue rotations = new BooleanValue(
            "Rotations",
            true
    );

    @Setting
    private final FloatValue rotationSpeed = new FloatValue(
            "Rotation Speed",
            40f,
            1f,
            180f,
            rotations::get
    );

    @Setting
    private final BooleanValue randomizedRotation = new BooleanValue(
            "Randomized Rotation",
            true,
            rotations::get
    );

    @Setting
    private final FloatValue randomTurnSpeed = new FloatValue(
            "Random Turn Speed",
            15,
            1,
            20,
            () -> rotations.get() && randomizedRotation.get()
    );

    @Setting
    private final BooleanValue onlyPlayers = new BooleanValue(
            "Only Players",
            false
    );

    @Setting
    public static final BooleanValue tpReach = new BooleanValue(
            "TP Reach",
            false
    );

    @Setting
    private final FloatValue tpExtendedReach = new FloatValue(
            "TP Extended Reach",
            40f,
            0f,
            100f,
            tpReach::get
    );

    @Setting
    private final BooleanValue tpBack = new BooleanValue(
            "TP Back",
            true,
            tpReach::get
    );

    public static Entity closestEntity = null;

    private long lastAttackTime = 0;
    private int randomDelay = 0;
    private int abTick = 0;
    private boolean wasBlinking = false;
    private boolean wasTargeting = false;
    private boolean wasPacketBlocking = false;
    private int unBlockTick = 0;
    public static boolean isBlocking = false;

    private float getRealTargetReach() {
        float reach = targetRange.get();

        if (IMinecraft.mc.player.isCreative() && reach <= 4.5f)
            reach += 1.5f;

        if (tpReach.get())
            reach += tpExtendedReach.get();

        return reach;
    }

    private float getRealAttackReach() {
        float reach = attackRange.get();

        if (IMinecraft.mc.player.isCreative() && reach <= 4.5f)
            reach += 1.5f;

        if (tpReach.get())
            reach += tpExtendedReach.get();

        return reach;
    }

    @Override
    public String tag() {
        if (apsMode.get().equalsIgnoreCase("randomized cps"))
            return minCps.get() + "-" + maxCps.get();
        else return apsMode.get();
    }

    @Override
    public void onDisable() {
        if (IMinecraft.mc.player != null)
            resetPacketUnblocking();

        wasPacketBlocking = false;
        wasTargeting = false;
        unBlockTick = 0;
        isBlocking = false;

        reset();
    }

    @Override
    public void onEnable() {
    }

    @Override
    public void onHandleInput(HandleInputEvent event) {
        if (IMinecraft.mc.player == null || IMinecraft.mc.world == null) return;

        if (TargetUtil.noKillAura || IMinecraft.mc.player.isSpectator()) {
            wasTargeting = false;
            unBlockTick = 0;
            isBlocking = false;
            reset();
            return;
        }

        List<Entity> worldEntities = new ArrayList<>();;

        for (Entity entity : IMinecraft.mc.world.getEntities()) {
            if (entity instanceof LivingEntity && !((LivingEntity) entity).isDead() && !(entity instanceof ArmorStandEntity) && !(entity instanceof ClientPlayerEntity) && !TargetUtil.isBot((LivingEntity) entity) && !TargetUtil.isTeammate((LivingEntity) entity) && IMinecraft.mc.player.distanceTo(entity) <= getRealTargetReach() && (!onlyPlayers.get() || entity instanceof PlayerEntity)) {
                worldEntities.add(entity);
            }
        }

        if (worldEntities.isEmpty()) {
            if (autoBlockMode.get().equalsIgnoreCase("vanilla"))
                resetPacketUnblocking();

            resetVisualBlocking();
            reset();
            return;
        }

        List<Entity> sortedEntities = worldEntities.stream().sorted(Comparator.comparingDouble(entity -> IMinecraft.mc.player.distanceTo(entity))).toList();

        closestEntity = sortedEntities.getFirst();

        if (rotations.get())
            RotationUtil.INSTANCE.aimAtEntity(closestEntity, rotationSpeed.get(), randomizedRotation.get(), randomTurnSpeed.get(), 0f, 0.2f);

        if (IMinecraft.mc.player.distanceTo(closestEntity) > getRealAttackReach()) {
            resetVisualBlocking();

            switch (autoBlockMode.get().toLowerCase()) {
                case "vanilla":
                    resetPacketUnblocking();
                    break;

                case "hypixel full":
                    if (wasBlinking) {
                        BlinkUtil.INSTANCE.sync(true, true);
                        BlinkUtil.INSTANCE.stopBlink();
                        wasBlinking = false;
                    }
                    break;
            }

            return;
        }

        wasTargeting = true;
        isBlocking = true;
        unBlockTick = 0;

        if (attackCooldownMath(closestEntity)) {
            float yaw = RotationUtil.INSTANCE.getCurrentYaw() != null ? RotationUtil.INSTANCE.getCurrentYaw() : IMinecraft.mc.player.getYaw();
            float pitch = RotationUtil.INSTANCE.getCurrentPitch() != null ? RotationUtil.INSTANCE.getCurrentPitch() : IMinecraft.mc.player.getPitch();

            abTick++;

            switch (autoBlockMode.get().toLowerCase()) {
                case "vanilla":
                    if (IMinecraft.mc.player.getOffHandStack().getItem() instanceof ShieldItem)
                        PacketUtil.sendPacket(new PlayerInteractItemC2SPacket(Hand.OFF_HAND, 0, yaw, pitch));
                    else if (IMinecraft.mc.player.getMainHandStack().getItem() instanceof SwordItem && !wasPacketBlocking)
                        PacketUtil.sendPacket(new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, 0, yaw, pitch));
                    break;

                case "hypixel full":
                    if (IMinecraft.mc.player.getMainHandStack().getItem() instanceof SwordItem && abTick >= 1) {
                        BlinkUtil.INSTANCE.doBlink();
                        PacketUtil.sendPacket(new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, 0, yaw, pitch));
                        wasBlinking = true;
                    }
                    break;
            }

            attackToEntity(closestEntity, tpReach.get() && IMinecraft.mc.player.distanceTo(closestEntity) >= attackRange.get());

            switch (autoBlockMode.get().toLowerCase()) {
                case "vanilla":
                    if (IMinecraft.mc.player.getOffHandStack().getItem() instanceof ShieldItem)
                        PacketUtil.sendPacket(new PlayerInteractItemC2SPacket(Hand.OFF_HAND, 0, yaw, pitch));
                    else if (IMinecraft.mc.player.getMainHandStack().getItem() instanceof SwordItem && wasPacketBlocking)
                        PacketUtil.sendPacket(new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, 0, yaw, pitch));
                    wasPacketBlocking = true;
                    break;

                case "hypixel full":
                    if (IMinecraft.mc.player.getMainHandStack().getItem() instanceof SwordItem && abTick >= 1) {
                        BlinkUtil.INSTANCE.sync(true, true);
                        PacketUtil.sendPacket(new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, 0, yaw, pitch));
                        BlinkUtil.INSTANCE.stopBlink();
                        abTick = 0;
                    }
                    break;
            }
        }
    }

    private boolean attackCooldownMath(Entity target) {
        switch (apsMode.get().toLowerCase()) {
            case "cooldown":
                return IMinecraft.mc.player.getAttackCooldownProgress(0f) >= 1f;

            case "randomized cps":
                long currentTime = System.currentTimeMillis();
                if (lastAttackTime == 0 || currentTime - lastAttackTime >= randomDelay) {
                    lastAttackTime = currentTime;
                    randomDelay = (int) (1000.0 / (minCps.get() + Math.random() * (maxCps.get() - minCps.get())));
                    return true;
                }
                break;

            case "no delay":
                return true;

            case "hurt math":
                return ((LivingEntity) target).hurtTime <= 5;
        }

        return false;
    }

    private void resetPacketUnblocking() {
        if (wasPacketBlocking) {
            int oldSlot = IMinecraft.mc.player.getInventory().selectedSlot;
            if (oldSlot == 0) {
                PacketUtil.sendPacket(new UpdateSelectedSlotC2SPacket(oldSlot + 1));
                PacketUtil.sendPacket(new UpdateSelectedSlotC2SPacket(oldSlot));
            } else {
                PacketUtil.sendPacket(new UpdateSelectedSlotC2SPacket(oldSlot - 1));
                PacketUtil.sendPacket(new UpdateSelectedSlotC2SPacket(oldSlot));
            }
            wasPacketBlocking = false;
        }
    }

    private void resetVisualBlocking() {
        if (wasTargeting) {
            unBlockTick++;

            switch (unBlockTick) {
                case 1, 2, 3, 4, 5:
                    PlayerUtil.INSTANCE.swingHandWithoutPacket(Hand.MAIN_HAND);
                    break;

                case 6:
                    isBlocking = false;
                    break;
            }

            if (unBlockTick >= 9) {
                isBlocking = false;
                unBlockTick = 0;
                wasTargeting = false;
            }
        }
    }

    private void attackToEntity(Entity target, boolean tpMode) {
        ArrayList<Vec3d> paths = null;

        if (tpMode) {
            paths = MainPathFinder.computePath(IMinecraft.mc.player.getPos(), closestEntity.getPos());

            if (paths == null || paths.isEmpty()) return;

            for (Vec3d path : paths) {
                PacketUtil.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(path.x, path.y, path.z, true, IMinecraft.mc.player.horizontalCollision));
            }

            if (!tpBack.get())
                IMinecraft.mc.player.setPosition(paths.getLast());
        }

        switch (swingOrderMode.get().toLowerCase()) {
            case "1.8":
                IMinecraft.mc.player.swingHand(Hand.MAIN_HAND);
                IMinecraft.mc.interactionManager.attackEntity(IMinecraft.mc.player, target);
                break;

            case "1.9.x":
                IMinecraft.mc.interactionManager.attackEntity(IMinecraft.mc.player, target);
                IMinecraft.mc.player.swingHand(Hand.MAIN_HAND);
                break;

            case "no order":
                IMinecraft.mc.interactionManager.attackEntity(IMinecraft.mc.player, target);
                break;
        }

        if (IMinecraft.mc.player.getMainHandStack().hasEnchantments())
            IMinecraft.mc.player.addEnchantedHitParticles(target);
        if (IMinecraft.mc.player.getVelocity().y < -0.1)
            IMinecraft.mc.player.addCritParticles(target);

        if (tpMode && !paths.isEmpty() && tpBack.get()) {
            List<Vec3d> reversedPaths = paths.reversed();

            for (Vec3d path : reversedPaths) {
                PacketUtil.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(path.x, path.y, path.z, true, IMinecraft.mc.player.horizontalCollision));
            }
        }
    }

    private void reset() {
        if (closestEntity != null)
            RotationUtil.INSTANCE.reset();

        if (autoBlockMode.get().equalsIgnoreCase("hypixel full") && wasBlinking) {
            BlinkUtil.INSTANCE.sync(true, true);
            BlinkUtil.INSTANCE.stopBlink();
        }

        lastAttackTime = 0;
        randomDelay = 0;
        abTick = 0;

        closestEntity = null;
        wasBlinking = false;
    }
}
