package com.instrumentalist.elite.hacks;

import com.instrumentalist.elite.Client;
import com.instrumentalist.elite.events.EventListener;
import com.instrumentalist.elite.events.features.*;
import com.instrumentalist.elite.hacks.features.combat.*;
import com.instrumentalist.elite.hacks.features.exploit.*;
import com.instrumentalist.elite.hacks.features.movement.*;
import com.instrumentalist.elite.hacks.features.nulling.PluginsDetector;
import com.instrumentalist.elite.hacks.features.player.*;
import com.instrumentalist.elite.hacks.features.render.*;
import com.instrumentalist.elite.hacks.features.world.*;
import com.instrumentalist.elite.hacks.features.world.Timer;
import com.instrumentalist.elite.utils.IMinecraft;
import com.instrumentalist.elite.utils.packet.BlinkUtil;
import com.instrumentalist.elite.utils.rotation.RotationUtil;
import com.instrumentalist.mixin.Initializer;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.common.CommonPongC2SPacket;
import net.minecraft.network.packet.c2s.play.*;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;
import xyz.breadloaf.imguimc.customwindow.ModuleRenderable;
import xyz.breadloaf.imguimc.screen.EmptyScreen;

import java.io.File;
import java.lang.reflect.Field;
import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;

public class ModuleManager implements EventListener {
    public static final List<Module> modules = new ArrayList<>();
    public static boolean isDebugRendering = false;

    public static int pitchTick = 0;

    public static int transactionCounter = 0;
    public static boolean gettingTransactions = false;

    private Vec3d lastPosition = null;
    private double accumulatedDistance = 0;
    public static DecimalFormat decimalFormat = new DecimalFormat("#.##");
    public static String bps = "0.00";

    public static void onInitialize() {
        modules.add(new Speed());
        modules.add(new Fly());
        modules.add(new NoBreakCooldown());
        modules.add(new Interface());
        modules.add(new KillAura());
        modules.add(new SpinBot());
        modules.add(new VanillaSpoofer());
        modules.add(new AntiBot());
        modules.add(new AutoPotion());
        modules.add(new Criticals());
        modules.add(new Teams());
        modules.add(new Velocity());
        modules.add(new Phase());
        modules.add(new ServerCrasher());
        modules.add(new Spammer());
        modules.add(new PerfectHorseJump());
        modules.add(new InventoryMove());
        modules.add(new WaterWalk());
        modules.add(new NoSlow());
        modules.add(new AutoTool());
        modules.add(new ChestStealer());
        modules.add(new FastLadder());
        modules.add(new InvManager());
        modules.add(new NoFall());
        modules.add(new Sprint());
        modules.add(new ESP());
        modules.add(new LightningDetector());
        modules.add(new TargetESP());
        modules.add(new CameraNoClip());
        modules.add(new LowFireOverlay());
        modules.add(new Breaker());
        modules.add(new CivBreak());
        modules.add(new Timer());
        modules.add(new Nuker());
        modules.add(new XRay());
        modules.add(new Scaffold());
        modules.add(new TransactionConfirmBlinker());
        modules.add(new ExploitPatcher());
        modules.add(new PortalScreen());
        modules.add(new FullBright());
        modules.add(new TargetStrafe());
        modules.add(new Disabler());
        modules.add(new LegacyCombat());
        modules.add(new Freecam());
        modules.add(new Cape());
        modules.add(new Predicter());
        modules.add(new MurdererDetector());
        modules.add(new NoHurtCam());
        modules.add(new FastBow());
        modules.add(new FastEat());
        modules.add(new Zoom());
        modules.add(new PathFinder());
        modules.add(new NoBuildLimit());
        modules.add(new FastBreak());
        modules.add(new ItemView());
        modules.add(new EntityDesync());
        modules.add(new Step());

        // Not shown for click gui (category is NULL)
        modules.add(new PluginsDetector());

        // Sort with alphabet
        modules.sort(Comparator.comparing(module -> module.moduleName));

        Client.eventManager.register(new ModuleManager());
    }

    public static List<Field> getSettings(Object module) {
        Field[] declaredFields = module.getClass().getDeclaredFields();

        return Arrays.stream(declaredFields)
                .filter(field -> field.isAnnotationPresent(Module.Setting.class))
                .collect(Collectors.toList());
    }

    public static boolean getModuleState(Module module) {
        for (Module i : modules) {
            if (Objects.equals(i.moduleName, module.moduleName)) return i.tempEnabled;
        }
        return false;
    }

    public static void pullDebugScreen() {
        Initializer.pullEveryRenderable();
        if (IMinecraft.mc.currentScreen instanceof EmptyScreen)
            IMinecraft.mc.setScreen(null);
        isDebugRendering = false;
        ModuleRenderable.commandTabJustOpened = false;
        ModuleRenderable.isCommandTab = false;
        transactionCounter = 0;
        gettingTransactions = false;
    }

    @Override
    public void onWorld(WorldEvent event) {
        if (Client.loaded) {
            File moduleBase = new File(Client.configManager.BASE_DIR, "module-configs");
            File moduleFile = new File(moduleBase, Client.configManager.configCurrent + ".json");

            File bindBase = new File(Client.configManager.BASE_DIR, "bind-configs");
            File bindFile = new File(bindBase, Client.configManager.bindCurrent + ".json");

            Client.configManager.freshConfig();

            if (moduleBase.exists() && moduleFile.exists())
                Client.configManager.saveConfigFile(Client.configManager.configCurrent, true);

            if (bindBase.exists() && bindFile.exists())
                Client.configManager.saveBindFile(Client.configManager.bindCurrent, true);
        }

        BlinkUtil.INSTANCE.sync(true, true);
        BlinkUtil.INSTANCE.stopBlink();

        RotationUtil.INSTANCE.reset();
    }

    @Override
    public void onTick(TickEvent event) {
        if (IMinecraft.mc.player == null) return;

        Vec3d currentPosition = IMinecraft.mc.player.getPos();

        if (lastPosition != null) {
            double deltaX = currentPosition.x - lastPosition.x;
            double deltaZ = currentPosition.z - lastPosition.z;
            double distance = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);

            accumulatedDistance += distance;
        }

        lastPosition = currentPosition;

        String formattedBps = decimalFormat.format(accumulatedDistance * 20);

        try {
            Integer.parseInt(formattedBps);
            formattedBps += ".00";
        } catch (NumberFormatException ignored) {
        }

        bps = formattedBps;

        accumulatedDistance = 0;
    }

    @Override
    public void onSendPacket(SendPacketEvent event) {
        if (IMinecraft.mc.player == null || IMinecraft.mc.world == null) return;

        Packet<?> packet = event.packet;

        if (gettingTransactions && packet instanceof CommonPongC2SPacket) {
            transactionCounter += 1;
            ModuleRenderable.commandLogs.add(((CommonPongC2SPacket) packet).getParameter() + " (x" + transactionCounter + ")");
            if (transactionCounter >= 10) {
                ModuleRenderable.commandLogs.add("Logged 10x transactions!");
                transactionCounter = 0;
                gettingTransactions = false;
            }
        }

        if (BlinkUtil.INSTANCE.getBlinking() && !BlinkUtil.INSTANCE.getLimiter()) {
            if (packet instanceof PlayerMoveC2SPacket || ModuleManager.getModuleState(new KillAura()) && KillAura.closestEntity != null && (KillAura.autoBlockMode.get().equalsIgnoreCase("vanilla") || KillAura.autoBlockMode.get().equalsIgnoreCase("hypixel full")) && packet instanceof PlayerInteractBlockC2SPacket)
                event.cancel();

            if (packet instanceof PlayerMoveC2SPacket.PositionAndOnGround || packet instanceof PlayerMoveC2SPacket.LookAndOnGround || packet instanceof PlayerMoveC2SPacket.Full || packet instanceof PlayerMoveC2SPacket.OnGroundOnly || packet instanceof ClientCommandC2SPacket || packet instanceof PlayerActionC2SPacket || packet instanceof PlayerInteractItemC2SPacket || ModuleManager.getModuleState(new TransactionConfirmBlinker()) && packet instanceof CommonPongC2SPacket) {
                event.cancel();
                BlinkUtil.INSTANCE.addPacket(packet);
            }
        }
    }

    @Override
    public void onKey(KeyboardEvent event) {
        if ((event.key == GLFW.GLFW_KEY_RIGHT_SHIFT || event.key == GLFW.GLFW_KEY_ESCAPE) && event.action == GLFW.GLFW_PRESS) {
            if (!isDebugRendering) {
                if (event.key != GLFW.GLFW_KEY_RIGHT_SHIFT) return;
                Initializer.pushRenderable(new ModuleRenderable());
                isDebugRendering = true;
            } else if (event.key == GLFW.GLFW_KEY_ESCAPE && !(IMinecraft.mc.currentScreen instanceof EmptyScreen) || event.key == GLFW.GLFW_KEY_RIGHT_SHIFT)
                pullDebugScreen();
        }

        if (IMinecraft.mc.currentScreen == null) {
            for (Module m : modules) {
                if (event.key == m.key && event.action == GLFW.GLFW_PRESS)
                    m.toggle();
            }
        }
    }

    @Override
    public void onMotion(MotionEvent event) {
        if (IMinecraft.mc.player == null) return;

        if (isDebugRendering && IMinecraft.mc.currentScreen == null)
            IMinecraft.mc.setScreen(new EmptyScreen());

        if (RotationUtil.INSTANCE.getBaseYaw() >= 180) RotationUtil.INSTANCE.setBaseYaw(-180f);
        else if (RotationUtil.INSTANCE.getBaseYaw() <= -180) RotationUtil.INSTANCE.setBaseYaw(180f);

        if (RotationUtil.INSTANCE.getCurrentYaw() != null && RotationUtil.INSTANCE.getCurrentPitch() != null) {
            if (RotationUtil.INSTANCE.getCurrentYaw().isNaN() || RotationUtil.INSTANCE.getCurrentPitch().isNaN() || RotationUtil.INSTANCE.getCurrentYaw().isInfinite() || RotationUtil.INSTANCE.getCurrentPitch().isInfinite()) {
                RotationUtil.INSTANCE.reset();
                return;
            }

            event.yaw = RotationUtil.INSTANCE.getCurrentYaw();
            event.pitch = RotationUtil.INSTANCE.getCurrentPitch();

            IMinecraft.mc.player.bodyYaw = IMinecraft.mc.player.getYaw();

            pitchTick++;
        }
    }
}
