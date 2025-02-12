package xyz.breadloaf.imguimc.customwindow;

import com.instrumentalist.elite.hacks.ModuleManager;
import com.instrumentalist.elite.hacks.features.combat.KillAura;
import com.instrumentalist.elite.utils.IMinecraft;
import imgui.ImGui;
import imgui.flag.ImGuiCond;
import imgui.type.ImString;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.hit.HitResult;
import xyz.breadloaf.imguimc.interfaces.Renderable;
import xyz.breadloaf.imguimc.interfaces.Theme;
import xyz.breadloaf.imguimc.theme.ImGuiClassicTheme;

import java.util.ArrayList;
import java.util.List;

public class TargetRenderable implements Renderable {

    private static boolean startUpped = false;

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
        if (IMinecraft.mc.player == null || IMinecraft.mc.world == null) return;

        List<Entity> targets = new ArrayList<>();
        if (ModuleManager.getModuleState(new KillAura()) && KillAura.closestEntity != null)
            targets.add(KillAura.closestEntity);
        else if (IMinecraft.mc.targetedEntity != null)
            targets.add(IMinecraft.mc.targetedEntity);

        if (targets.isEmpty()) return;

        for (Entity target : targets) {
            if (target instanceof LivingEntity livingTarget) {
                ImGui.begin("Target Info", ImGuiCond.Always);

                if (!startUpped) {
                    ImGui.setWindowSize(300f, 160f);
                    ImGui.setWindowPos(200f, 200f);
                    startUpped = true;
                }

                String name = target.getName().getString();
                ImGui.text("Name: " + name);

                float health = livingTarget.getHealth();
                float maxHealth = livingTarget.getMaxHealth();
                ImGui.text(String.format("Health: %.1f / %.1f", health, maxHealth));

                String entityType = target.getType().getTranslationKey();
                ImGui.text("Type: " + entityType);

                if (target instanceof PlayerEntity player) {
                    ImGui.text("UUID: " + player.getUuidAsString());

                    if (player instanceof AbstractClientPlayerEntity clientPlayer)
                        ImGui.text("Skin: " + clientPlayer.getSkinTextures());

                    if (IMinecraft.mc.getNetworkHandler() != null) {
                        PlayerListEntry entry = IMinecraft.mc.getNetworkHandler().getPlayerListEntry(target.getUuid());
                        if (entry != null)
                            ImGui.text("Ping: " + entry.getLatency());
                    }
                }

                ImGui.end();
            }
        }
    }
}