package com.instrumentalist.elite.hacks.features.render;

import com.instrumentalist.elite.events.features.RenderHudEvent;
import com.instrumentalist.elite.hacks.Module;
import com.instrumentalist.elite.hacks.ModuleCategory;
import com.instrumentalist.elite.hacks.ModuleManager;
import com.instrumentalist.elite.hacks.features.exploit.Disabler;
import com.instrumentalist.elite.hacks.features.player.MurdererDetector;
import com.instrumentalist.elite.hacks.features.exploit.disablermode.features.HypixelDisabler;
import com.instrumentalist.elite.hacks.features.player.Scaffold;
import com.instrumentalist.elite.hacks.features.world.Breaker;
import com.instrumentalist.elite.hacks.features.world.CivBreak;
import com.instrumentalist.elite.hacks.features.world.Nuker;
import com.instrumentalist.elite.utils.IMinecraft;
import com.instrumentalist.elite.utils.packet.BlinkUtil;
import com.instrumentalist.elite.utils.value.BooleanValue;
import com.instrumentalist.elite.utils.value.TextValue;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;

import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class Interface extends Module {

    public Interface() {
        super("Interface", ModuleCategory.Render, GLFW.GLFW_KEY_UNKNOWN, true, false);
    }

    @Setting
    private final BooleanValue waterMark = new BooleanValue(
            "Water Mark",
            true
    );

    @Setting
    private final TextValue waterMarkText = new TextValue(
            "Water Mark Text",
            "Elite",
            waterMark::get
    );

    @Setting
    private final BooleanValue information = new BooleanValue(
            "Information",
            true
    );

    @Setting
    private final BooleanValue moduleList = new BooleanValue(
            "Module List",
            true
    );

    @Setting
    private final BooleanValue fontShadow = new BooleanValue(
            "Font Shadow",
            true,
            () -> waterMark.get() || information.get() || moduleList.get()
    );

    public static TextRenderer cachedTextRenderer = null;
    public static List<Module> sortedModules;

    public static void reloadSortedModules(TextRenderer textRenderer) {
        sortedModules = ModuleManager.modules.stream()
                .sorted((module1, module2) -> Double.compare(
                        textRenderer.getWidth(Text.of(module2.moduleName +  (module2.tag() != null ? " " + module2.tag() : ""))),
                        textRenderer.getWidth(Text.of(module1.moduleName +  (module1.tag() != null ? " " + module1.tag() : "")))
                ))
                .toList();
    }

    @Override
    public void onDisable() {
    }

    @Override
    public void onEnable() {
    }

    @Override
    public void onRenderHud(RenderHudEvent event) {
        if (IMinecraft.mc.player == null || IMinecraft.mc.world == null) return;

        if (cachedTextRenderer == null)
            cachedTextRenderer = event.textRenderer;

        VertexConsumerProvider vertexConsumerProvider = event.vertexConsumers;
        Matrix4f matrix4f = event.matrix4f;

        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formattedNow = now.format(formatter);

        float infoY = 4f;

        if (waterMark.get()) {
            cachedTextRenderer.draw(Text.of("§f" + waterMarkText.get() + " (§c" + formattedNow + "§f)"), 4f, infoY, Color.WHITE.getRGB(), fontShadow.get(), matrix4f, vertexConsumerProvider, TextRenderer.TextLayerType.SEE_THROUGH, 0, 0);
            infoY += 10.5f;
        }

        if (information.get()) {
            cachedTextRenderer.draw(Text.of("§f[§cFPS§f]§7: " + IMinecraft.mc.getCurrentFps()), 4f, infoY, Color.WHITE.getRGB(), fontShadow.get(), matrix4f, vertexConsumerProvider, TextRenderer.TextLayerType.SEE_THROUGH, 0, 0);
            infoY += 10.5f;

            cachedTextRenderer.draw(Text.of("§f[§cBPS§f]§7: " + ModuleManager.bps), 4f, infoY, Color.WHITE.getRGB(), fontShadow.get(), matrix4f, vertexConsumerProvider, TextRenderer.TextLayerType.SEE_THROUGH, 0, 0);
            infoY += 10.5f;

            cachedTextRenderer.draw(Text.of("§f[§cServer§f]§7: " + IMinecraft.mc.player.networkHandler.getBrand()), 4f, infoY, Color.WHITE.getRGB(), fontShadow.get(), matrix4f, vertexConsumerProvider, TextRenderer.TextLayerType.SEE_THROUGH, 0, 0);
            infoY += 10.5f;

            if (IMinecraft.mc.player.networkHandler.getServerInfo() != null) {
                cachedTextRenderer.draw(Text.of("§f[§cVersion§f]§7: " + IMinecraft.mc.player.networkHandler.getServerInfo().version.getString()), 4f, infoY, Color.WHITE.getRGB(), fontShadow.get(), matrix4f, vertexConsumerProvider, TextRenderer.TextLayerType.SEE_THROUGH, 0, 0);
                infoY += 10.5f;

                cachedTextRenderer.draw(Text.of("§f[§cPing§f]§7: " + IMinecraft.mc.player.networkHandler.getServerInfo().ping), 4f, infoY, Color.WHITE.getRGB(), fontShadow.get(), matrix4f, vertexConsumerProvider, TextRenderer.TextLayerType.SEE_THROUGH, 0, 0);
                infoY += 10.5f;
            } else {
                cachedTextRenderer.draw(Text.of("§f[§cVersion§f]§7: null"), 4f, infoY, Color.WHITE.getRGB(), fontShadow.get(), matrix4f, vertexConsumerProvider, TextRenderer.TextLayerType.SEE_THROUGH, 0, 0);
                infoY += 10.5f;

                cachedTextRenderer.draw(Text.of("§f[§cPing§f]§7: null"), 4f, infoY, Color.WHITE.getRGB(), fontShadow.get(), matrix4f, vertexConsumerProvider, TextRenderer.TextLayerType.SEE_THROUGH, 0, 0);
                infoY += 10.5f;
            }

            cachedTextRenderer.draw(Text.of("§f[§cPlayers§f]§7: " + IMinecraft.mc.player.networkHandler.getCommandSource().getPlayerNames().size()), 4f, infoY, Color.WHITE.getRGB(), fontShadow.get(), matrix4f, vertexConsumerProvider, TextRenderer.TextLayerType.SEE_THROUGH, 0, 0);

            if (ModuleManager.getModuleState(new Scaffold())) {
                String scaffoldText = Scaffold.Companion.getHotbarStackSize() + " Blocks";

                String[] scaffoldStrings = scaffoldText.split("");

                int index = 0;
                float textLen = IMinecraft.mc.getWindow().getScaledWidth() / 2f;

                for (String text : scaffoldStrings) {
                    float saturation = 1.0f;
                    float brightness = 1.0f;
                    int alpha = 255;
                    long cycleDuration = 3600L;

                    long currentTime = System.currentTimeMillis();
                    float hueShift = ((currentTime % cycleDuration) / (float) cycleDuration) + (index / (float) scaffoldStrings.length);
                    hueShift %= 1.0f;

                    Color color = Color.getHSBColor(hueShift, saturation, brightness);

                    cachedTextRenderer.draw(Text.of(text), textLen - (cachedTextRenderer.getWidth(Text.of(scaffoldText)) / 2f), IMinecraft.mc.getWindow().getScaledHeight() / 2f - 40f, new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha).getRGB(), fontShadow.get(), matrix4f, vertexConsumerProvider, TextRenderer.TextLayerType.SEE_THROUGH, 0, 0);

                    textLen += cachedTextRenderer.getWidth(text);
                    index++;
                }
            }

            if (ModuleManager.getModuleState(new Disabler()) && HypixelDisabler.stuckOnAir && HypixelDisabler.airTicks >= 9 && !HypixelDisabler.watchDogDisabled) {
                String disablingText = "Disabling... " + HypixelDisabler.airStuckTicks + "/20";

                String[] disablerStrings = disablingText.split("");

                int index = 0;
                float textLen = IMinecraft.mc.getWindow().getScaledWidth() / 2f;

                for (String text : disablerStrings) {
                    float saturation = 1.0f;
                    float brightness = 1.0f;
                    int alpha = 255;
                    long cycleDuration = 3600L;

                    long currentTime = System.currentTimeMillis();
                    float hueShift = ((currentTime % cycleDuration) / (float) cycleDuration) + (index / (float) disablerStrings.length);
                    hueShift %= 1.0f;

                    Color color = Color.getHSBColor(hueShift, saturation, brightness);

                    cachedTextRenderer.draw(Text.of(text), textLen - (cachedTextRenderer.getWidth(Text.of(disablingText)) / 2f), IMinecraft.mc.getWindow().getScaledHeight() / 2f - 60f, new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha).getRGB(), fontShadow.get(), matrix4f, vertexConsumerProvider, TextRenderer.TextLayerType.SEE_THROUGH, 0, 0);

                    textLen += cachedTextRenderer.getWidth(text);
                    index++;
                }
            }

            if (ModuleManager.getModuleState(new Breaker()) && Breaker.Companion.getWasBreaking() || ModuleManager.getModuleState(new CivBreak()) && CivBreak.Companion.getWasBreaking() || ModuleManager.getModuleState(new Nuker()) && Nuker.Companion.getWasBreaking()) {
                String breakingText = "Breaking...";

                String[] breakingStrings = breakingText.split("");

                int index = 0;
                float textLen = IMinecraft.mc.getWindow().getScaledWidth() / 2f;

                for (String text : breakingStrings) {
                    float saturation = 1.0f;
                    float brightness = 1.0f;
                    int alpha = 255;
                    long cycleDuration = 3600L;

                    long currentTime = System.currentTimeMillis();
                    float hueShift = ((currentTime % cycleDuration) / (float) cycleDuration) + (index / (float) breakingStrings.length);
                    hueShift %= 1.0f;

                    Color color = Color.getHSBColor(hueShift, saturation, brightness);

                    cachedTextRenderer.draw(Text.of(text), textLen - (cachedTextRenderer.getWidth(Text.of(breakingText)) / 2f), IMinecraft.mc.getWindow().getScaledHeight() / 2f - 80f, new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha).getRGB(), fontShadow.get(), matrix4f, vertexConsumerProvider, TextRenderer.TextLayerType.SEE_THROUGH, 0, 0);

                    textLen += cachedTextRenderer.getWidth(text);
                    index++;
                }
            }

            if (BlinkUtil.INSTANCE.getBlinking()) {
                String blinkText = "Blinking...";

                String[] blinkStrings = blinkText.split("");

                int index = 0;
                float textLen = IMinecraft.mc.getWindow().getScaledWidth() / 2f;

                for (String text : blinkStrings) {
                    float saturation = 1.0f;
                    float brightness = 1.0f;
                    int alpha = 255;
                    long cycleDuration = 3600L;

                    long currentTime = System.currentTimeMillis();
                    float hueShift = ((currentTime % cycleDuration) / (float) cycleDuration) + (index / (float) blinkStrings.length);
                    hueShift %= 1.0f;

                    Color color = Color.getHSBColor(hueShift, saturation, brightness);

                    cachedTextRenderer.draw(Text.of(text), textLen - (cachedTextRenderer.getWidth(Text.of(blinkText)) / 2f), IMinecraft.mc.getWindow().getScaledHeight() / 2f - 100f, new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha).getRGB(), fontShadow.get(), matrix4f, vertexConsumerProvider, TextRenderer.TextLayerType.SEE_THROUGH, 0, 0);

                    textLen += cachedTextRenderer.getWidth(text);
                    index++;
                }
            }

            if (ModuleManager.getModuleState(new MurdererDetector())) {
                float murdererTextY = IMinecraft.mc.getWindow().getScaledHeight() / 2f - 160f;
                List<PlayerEntity> murdererList = MurdererDetector.murderers;

                cachedTextRenderer.draw(Text.of("§f[§cMurderers§f]: " + murdererList.size()), IMinecraft.mc.getWindow().getScaledWidth() / 2f - (cachedTextRenderer.getWidth(Text.of("[Murderers]: " + murdererList.size())) / 2f), murdererTextY, Color.WHITE.getRGB(), fontShadow.get(), matrix4f, vertexConsumerProvider, TextRenderer.TextLayerType.SEE_THROUGH, 0, 0);

                if (!murdererList.isEmpty()) {
                    for (PlayerEntity murderer : murdererList) {
                        murdererTextY += 10.5f;

                        String distance = ModuleManager.decimalFormat.format(IMinecraft.mc.player.distanceTo(murderer));

                        try {
                            Integer.parseInt(distance);
                            distance += ".00";
                        } catch (NumberFormatException ignored) {
                        }

                        cachedTextRenderer.draw(Text.of("§f> " + murderer.getName().getString() + "§7 (" + distance + "m)"), IMinecraft.mc.getWindow().getScaledWidth() / 2f - (cachedTextRenderer.getWidth(Text.of("> " + murderer.getName().getString() + " (" + distance + "m)")) / 2f), murdererTextY, Color.WHITE.getRGB(), fontShadow.get(), matrix4f, vertexConsumerProvider, TextRenderer.TextLayerType.SEE_THROUGH, 0, 0);
                    }
                }
            }
        }

        if (moduleList.get()) {
            if (sortedModules == null)
                reloadSortedModules(cachedTextRenderer);

            float listY = 4;
            int index = 0;

            for (Module module : sortedModules) {
                if (module.showOnArray && ModuleManager.getModuleState(module)) {
                    float saturation = 1.0f;
                    float brightness = 1.0f;
                    int alpha = 255;
                    long cycleDuration = 3600L;

                    long currentTime = System.currentTimeMillis();
                    float hueShift = ((currentTime % cycleDuration) / (float) cycleDuration) + (index / (float) sortedModules.size());
                    hueShift %= 1.0f;

                    Color color = Color.getHSBColor(hueShift, saturation, brightness);

                    String text = module.moduleName;

                    if (module.tag() != null)
                        text += "§7 " + module.tag();

                    cachedTextRenderer.draw(Text.of(text), IMinecraft.mc.getWindow().getScaledWidth() - 4f - cachedTextRenderer.getWidth(Text.of(text)), listY, new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha).getRGB(), fontShadow.get(), matrix4f, vertexConsumerProvider, TextRenderer.TextLayerType.SEE_THROUGH, 0, 0);

                    listY += 10.5f;
                    index++;
                }
            }
        }
    }
}
