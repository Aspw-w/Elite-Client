package com.instrumentalist.elite.hacks.features.nulling;

import com.instrumentalist.elite.events.features.ReceivedPacketEvent;
import com.instrumentalist.elite.events.features.UpdateEvent;
import com.instrumentalist.elite.hacks.Module;
import com.instrumentalist.elite.utils.IMinecraft;
import com.instrumentalist.elite.utils.math.TickTimer;
import com.instrumentalist.elite.utils.packet.PacketUtil;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.RequestCommandCompletionsC2SPacket;
import net.minecraft.network.packet.s2c.play.ChatMessageS2CPacket;
import net.minecraft.network.packet.s2c.play.CommandSuggestionsS2CPacket;
import org.lwjgl.glfw.GLFW;
import xyz.breadloaf.imguimc.customwindow.ModuleRenderable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class PluginsDetector extends Module {

    public PluginsDetector() {
        super("Plugins Detector", null, GLFW.GLFW_KEY_UNKNOWN, false, true);
    }

    private final TickTimer timer = new TickTimer();

    private int step = 0;

    private String[] plugins = null;

    private final List<String> antiCheats = new ArrayList<>(Arrays.asList(
            "nocheatplus",
            "grimac",
            "aac",
            "hawk",
            "intave",
            "horizon",
            "vulcan",
            "spartan",
            "kauri",
            "anticheatreloaded",
            "matrix",
            "themis",
            "negativity"
    )); // lowercase only

    @Override
    public void onEnable() {
        if (IMinecraft.mc.player == null || IMinecraft.mc.world == null) return;

        step = 0;
        timer.reset();
    }

    @Override
    public void onDisable() {
        if (IMinecraft.mc.player == null || IMinecraft.mc.world == null) return;

        if (plugins == null) {
            ModuleRenderable.commandLogs.add("Failed!");
            return;
        }

        if (plugins.length > 0)
            ModuleRenderable.commandLogs.add("Plugins Detected [All: " + plugins.length + ", AC: " + Arrays.stream(plugins).filter(s -> antiCheats.contains(s.toLowerCase(Locale.getDefault()))).count() + "] - " + Arrays.stream(plugins).map(s -> (antiCheats.contains(s.toLowerCase()) ? "AC-" : "") + s).collect(Collectors.joining(", ")));
        else ModuleRenderable.commandLogs.add("Failed!");

        plugins = null;
    }

    @Override
    public void onUpdate(UpdateEvent event) {
        timer.update();
        if (timer.hasTimePassed(20)) {
            timer.reset();
            step++;
        }
        if (timer.tick != 1) return;

        switch (step) {
            case 0: {
                PacketUtil.sendPacket(new RequestCommandCompletionsC2SPacket(0, "/version "));
                break;
            }
            case 1: {
                PacketUtil.sendPacket(new RequestCommandCompletionsC2SPacket(0, "/bukkit:version "));
                break;
            }

            case 2, 3: {
                timer.reset();
                step++;
                break;
            }

            case 4: {
                PacketUtil.sendPacket(new RequestCommandCompletionsC2SPacket(0, "/"));
                break;
            }

            default: {
                this.toggle();
                break;
            }
        }
    }

    @Override
    public void onReceivedPacket(ReceivedPacketEvent event) {
        if (IMinecraft.mc.player == null || IMinecraft.mc.world == null) return;

        Packet<?> packet = event.packet;

        if (packet instanceof CommandSuggestionsS2CPacket) {
            if (step == 0 || step == 1) {
                String[] suggests = ((CommandSuggestionsS2CPacket) packet).suggestions().stream().map(CommandSuggestionsS2CPacket.Suggestion::text).toArray(String[]::new);
                if (suggests.length == 0) return;

                plugins = Arrays.stream(suggests).parallel().filter(s -> s.matches("[a-zA-Z0-9]{1,32}")).toArray(String[]::new);

                if (plugins.length > 0) this.toggle();
                event.cancel();
            } else if (step == 4) {
                String[] suggests = ((CommandSuggestionsS2CPacket) packet).suggestions().stream().map(CommandSuggestionsS2CPacket.Suggestion::text).toArray(String[]::new);
                if (suggests.length == 0) return;

                plugins = Arrays.stream(suggests).parallel().filter(s -> s.matches("[a-z0-9]{1,32}:[a-zA-Z0-9]{1,31}")).map(s -> s.split(":")[0]).toArray(String[]::new);

                if (plugins.length > 0) this.toggle();
                event.cancel();
            }
        } else if (packet instanceof ChatMessageS2CPacket) {
            String content = ((ChatMessageS2CPacket) packet).body().content();
            if (content.matches("Plugins \\([0-9]+\\): ([a-zA-Z0-9]{1,32})(, ([a-zA-Z0-9]{1,32}))*")) {
                plugins = content.replaceFirst("Plugins \\([0-9]+\\): ", "").split(", ");

                this.toggle();
                event.cancel();
            }
        }
    }
}
