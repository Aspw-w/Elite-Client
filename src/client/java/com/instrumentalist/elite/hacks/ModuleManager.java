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
import com.instrumentalist.elite.utils.value.SettingValue;
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

public class ModuleManager implements EventListener {
    public static final List<Module> modules = new ArrayList<>();
    public static boolean isDebugRendering = false;

    public static Float interpolatedYaw = null;
    public static Float interpolatedPitch = null;

    public static int pitchTick = 0;
    private static Long lastTime = 0L;

    public static int transactionCounter = 0;
    public static boolean gettingTransactions = false;

    private Vec3d lastPosition = null;
    private double accumulatedDistance = 0;
    public static DecimalFormat decimalFormat = new DecimalFormat("#.##");
    public static String bps = "0.00";

    public static void onInitialize() {
        addModule(new Speed());
        addModule(new Fly());
        addModule(new NoBreakCooldown());
        addModule(new Interface());
        addModule(new KillAura());
        addModule(new SpinBot());
        addModule(new VanillaSpoofer());
        addModule(new AntiBot());
        addModule(new AutoPotion());
        addModule(new Criticals());
        addModule(new Teams());
        addModule(new Velocity());
        addModule(new Phase());
        addModule(new ServerCrasher());
        addModule(new Spammer());
        addModule(new PerfectHorseJump());
        addModule(new InventoryMove());
        addModule(new WaterSpeed());
        addModule(new NoSlow());
        addModule(new AutoTool());
        addModule(new ChestStealer());
        addModule(new FastLadder());
        addModule(new InvManager());
        addModule(new NoFall());
        addModule(new Sprint());
        addModule(new ESP());
        addModule(new LightningDetector());
        addModule(new CameraNoClip());
        addModule(new LowFireOverlay());
        addModule(new Breaker());
        addModule(new CivBreak());
        addModule(new Timer());
        addModule(new Nuker());
        addModule(new XRay());
        addModule(new Scaffold());
        addModule(new TransactionConfirmBlinker());
        addModule(new ExploitPatcher());
        addModule(new PortalScreen());
        addModule(new FullBright());
        addModule(new TargetStrafe());
        addModule(new Disabler());
        addModule(new LegacyCombat());
        addModule(new Freecam());
        addModule(new Cape());
        addModule(new Predicter());
        addModule(new MurdererDetector());
        addModule(new NoHurtCam());
        addModule(new FastBow());
        addModule(new FastEat());
        addModule(new Zoom());
        addModule(new PathFinder());
        addModule(new NoBuildLimit());
        addModule(new FastBreak());
        addModule(new ItemView());
        addModule(new EntityDesync());
        addModule(new Step());
        addModule(new AntiVoid());
        addModule(new AutoFish());
        addModule(new NameTags());
        addModule(new NoJumpCooldown());
        addModule(new BlockESP());
        addModule(new HudPoser());
        addModule(new TimeChanger());
        addModule(new Scoreboard());
        addModule(new TargetESP());
        addModule(new ChatCommands());
        addModule(new PluginsDetector());

        modules.sort(Comparator.comparing(module -> module.moduleName));

        Client.eventManager.register(new ModuleManager());
    }

    public static void addModule(Module module) {
        modules.add(module);

        // add
        Field[] declaredFields = module.getClass().getDeclaredFields();

        List<SettingValue<?>> settings = new ArrayList<>();
        for (Field declaredField : declaredFields) {
            try {
                declaredField.setAccessible(true);
                Object value = declaredField.get(module);
                if (value instanceof SettingValue<?>) {
                    settings.add((SettingValue<?>) value);
                }
            } catch (Exception e) {
                throw new RuntimeException(String.format("Initializing Setting(%s) of %s failed", module.moduleName, declaredField.getName()));
            }
        }

        module.settings = settings;
    }

    public static List<SettingValue<?>> getSettings(Object module) {
        if (!(module instanceof Module moduleObj)) {
            throw new RuntimeException("The type of this Object is not a Module");
        }

        return moduleObj.settings;
    }

    public static boolean getModuleState(Module module) {
        return modules.stream().anyMatch(m -> m.getClass() == module.getClass() && m.tempEnabled);
    }

    public static Module getModuleByName(String moduleName) {
        for (Module module : modules) {
            if (module.moduleName.equalsIgnoreCase(moduleName)) {
                return module;
            }
        }
        return null;
    }

    public static void pullDebugScreen() {
        Initializer.pullEveryRenderable();
        if (IMinecraft.mc.currentScreen instanceof EmptyScreen)
            IMinecraft.mc.setScreen(null);
        isDebugRendering = false;
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

        for (Module module : modules) {
            if (module.tempEnabled && (module instanceof InvManager || module instanceof ChestStealer || module instanceof Scaffold || module instanceof KillAura))
                module.toggle();
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

        long currentTime = System.nanoTime();
        if (lastTime == 0L) {
            lastTime = currentTime;
        }
        double deltaTime = (currentTime - lastTime) / 1_000_000_000.0;
        lastTime = currentTime;

        if (RotationUtil.INSTANCE.getBaseYaw() >= 180) RotationUtil.INSTANCE.setBaseYaw(-180f);
        else if (RotationUtil.INSTANCE.getBaseYaw() <= -180) RotationUtil.INSTANCE.setBaseYaw(180f);

        if (RotationUtil.INSTANCE.getCurrentYaw() != null && RotationUtil.INSTANCE.getCurrentPitch() != null) {
            interpolatedYaw = RotationUtil.INSTANCE.interpolateAngle(event.yaw, RotationUtil.INSTANCE.getCurrentYaw(), 180f, (float) (20f * deltaTime), false);
            interpolatedPitch = RotationUtil.INSTANCE.interpolateAngle(event.pitch, RotationUtil.INSTANCE.getCurrentPitch(), 180f, (float) (20f * deltaTime), true);

            if (Float.isNaN(interpolatedYaw) || Float.isNaN(interpolatedPitch) || Float.isInfinite(interpolatedYaw) || Float.isInfinite(interpolatedPitch)) {
                RotationUtil.INSTANCE.reset();
                return;
            }

            event.yaw = interpolatedYaw;
            event.pitch = interpolatedPitch;

            IMinecraft.mc.player.bodyYaw = IMinecraft.mc.player.getYaw();

            pitchTick++;
        }
    }
}