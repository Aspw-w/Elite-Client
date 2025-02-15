package xyz.breadloaf.imguimc.customwindow;

import com.instrumentalist.elite.hacks.ModuleManager;
import com.instrumentalist.elite.hacks.features.combat.KillAura;
import com.instrumentalist.elite.hacks.features.render.Interface;
import com.instrumentalist.elite.utils.IMinecraft;
import com.instrumentalist.elite.utils.math.TargetUtil;
import imgui.ImGui;
import imgui.flag.ImGuiCond;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import xyz.breadloaf.imguimc.interfaces.Renderable;
import xyz.breadloaf.imguimc.interfaces.Theme;
import xyz.breadloaf.imguimc.screen.EmptyScreen;
import xyz.breadloaf.imguimc.theme.ImGuiClassicTheme;

import java.util.Collections;
import java.util.List;

public class TargetRenderable implements Renderable {
    private static boolean initialized = false;

    @Override
    public String getName() {
        return "Target Renderable";
    }

    @Override
    public Theme getTheme() {
        return new ImGuiClassicTheme();
    }

    @Override
    public void render() {
        if (IMinecraft.mc.player == null || IMinecraft.mc.world == null || IMinecraft.mc.currentScreen instanceof EmptyScreen || !ModuleManager.getModuleState(new Interface())) return;

        List<Entity> targets = TargetUtil.getSingletonTargetsAsList();
        if (targets.isEmpty()) return;

        ImGui.begin("Target Info", ImGuiCond.Always);

        if (!initialized) {
            ImGui.setWindowSize(300f, 160f);
            ImGui.setWindowPos(200f, 200f);
            initialized = true;
        }

        for (Entity target : targets) {
            if (target instanceof LivingEntity livingTarget) {
                renderTargetInfo(livingTarget);
            }
        }

        ImGui.end();
    }

    private void renderTargetInfo(LivingEntity target) {
        ImGui.text("Name: " + target.getName().getString());
        ImGui.text(String.format("Health: %.1f / %.1f", target.getHealth(), target.getMaxHealth()));
        ImGui.text("Type: " + target.getType().getTranslationKey());

        if (target instanceof PlayerEntity player) {
            renderPlayerInfo(player);
        }
    }

    private void renderPlayerInfo(PlayerEntity player) {
        ImGui.text("UUID: " + player.getUuidAsString());

        if (player instanceof AbstractClientPlayerEntity clientPlayer) {
            ImGui.text("Skin: " + clientPlayer.getSkinTextures());
        }

        if (IMinecraft.mc.getNetworkHandler() != null) {
            PlayerListEntry entry = IMinecraft.mc.getNetworkHandler().getPlayerListEntry(player.getUuid());
            if (entry != null) {
                ImGui.text("Ping: " + entry.getLatency());
            }
        }
    }
}
