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
import com.instrumentalist.elite.utils.value.*;
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
    private final ListValue uiSpacingMode = new ListValue(
            "UI Spacing Mode",
            new String[]{"None", "Small"},
            "Small"
    );

    @Setting
    private final BooleanValue waterMark = new BooleanValue(
            "Watermark",
            true
    );

    @Setting
    private final ListValue watermarkMode = new ListValue(
            "Watermark Mode",
            new String[]{"Normal", "Radium"},
            "Normal",
            waterMark::get
    );

    @Setting
    private final TextValue clientName = new TextValue(
            "Client Name",
            "Elite",
            waterMark::get
    );

    @Setting
    private final BooleanValue extraInfo = new BooleanValue(
            "Extra Info",
            true
    );

    @Setting
    private final BooleanValue moduleList = new BooleanValue(
            "Module List",
            true
    );

    @Setting
    private final BooleanValue moduleInfo = new BooleanValue(
            "Module Info",
            true
    );

    @Setting
    private final BooleanValue fontShadow = new BooleanValue(
            "Text Shadow",
            true,
            () -> waterMark.get() || extraInfo.get() || moduleList.get()
    );

    @Setting
    private final BooleanValue backGround = new BooleanValue(
            "Back Ground",
            true,
            () -> waterMark.get() || extraInfo.get() || moduleList.get()
    );

    @Setting
    private final ListValue colorMode = new ListValue(
            "Color Mode",
            new String[]{"Static", "Fade", "Rainbow"},
            "Rainbow",
            () -> waterMark.get() || moduleList.get()
    );

    @Setting
    public final ColorValue primaryColor = new ColorValue(
            "Primary Color",
            new Color(255, 0, 0),
            () -> !colorMode.get().equalsIgnoreCase("rainbow")
    );

    @Setting
    public final ColorValue secondaryColor = new ColorValue(
            "Secondary Color",
            new Color(0, 0, 255),
            () -> colorMode.get().equalsIgnoreCase("fade")
    );

    @Setting
    public final FloatValue fadeSpeed = new FloatValue(
            "Fade Speed",
            1.0f, 0.1f, 5.0f,
            () -> !colorMode.get().equalsIgnoreCase("static")
    );

    public static TextRenderer cachedTextRenderer = null;
    public static List<Module> sortedModules;

    private Color getModuleColor(int index, int totalModules) {
        long time = System.currentTimeMillis();
        float fadeCycleDuration = 4000f / fadeSpeed.get();

        switch (colorMode.get().toLowerCase()) {
            case "static":
                return primaryColor.get();

            case "fade":
                float fadeProgress = (time % (long) fadeCycleDuration) / fadeCycleDuration;
                fadeProgress = (fadeProgress + (float) index / totalModules) % 1.0f;

                fadeProgress = 0.5f - 0.5f * (float) Math.cos(fadeProgress * 2 * Math.PI);

                Color darkRed = new Color(22, 255, 173);
                Color brightRed = new Color(255, 0, 128);
                return smoothLoopingColorTransition(darkRed, brightRed, fadeProgress);

            case "rainbow":
                float hueProgress = (time % (long) fadeCycleDuration) / fadeCycleDuration;
                hueProgress = (hueProgress + (float) index / totalModules) % 1.0f;

                float brightness = 0.7f + 0.3f * (float) Math.cos(hueProgress * 2 * Math.PI);

                float saturation = 1.0f;
                return Color.getHSBColor(hueProgress, saturation, brightness);

            default:
                return Color.WHITE;
        }
    }

    private float getSpacingSize() {
        return switch (uiSpacingMode.get().toLowerCase()) {
            case "none" -> 1f;
            case "small" -> 4f;
            default -> 0f;
        };
    }

    private Color smoothLoopingColorTransition(Color start, Color end, float progress) {
        float red = start.getRed() / 255f + (end.getRed() / 255f - start.getRed() / 255f) * progress;
        float green = start.getGreen() / 255f + (end.getGreen() / 255f - start.getGreen() / 255f) * progress;
        float blue = start.getBlue() / 255f + (end.getBlue() / 255f - start.getBlue() / 255f) * progress;

        red = Math.min(1.0f, Math.max(0.0f, red));
        green = Math.min(1.0f, Math.max(0.0f, green));
        blue = Math.min(1.0f, Math.max(0.0f, blue));

        return new Color(red, green, blue);
    }

    private void renderModuleInformation(Matrix4f matrix4f, VertexConsumerProvider vertexConsumerProvider) {
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

        if (IMinecraft.mc.interactionManager != null && (ModuleManager.getModuleState(new Breaker()) && Breaker.Companion.getWasBreaking() || ModuleManager.getModuleState(new CivBreak()) && CivBreak.Companion.getWasBreaking() || ModuleManager.getModuleState(new Nuker()) && Nuker.Companion.getWasBreaking())) {
            String breakingText = "Breaking... " + IMinecraft.mc.interactionManager.getBlockBreakingProgress();
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
            String blinkText = "Blinking... (x" + BlinkUtil.INSTANCE.getPackets().size() + ")";
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
            float murdererTextY = IMinecraft.mc.getWindow().getScaledHeight() / 2f - 220f;
            List<PlayerEntity> murdererList = MurdererDetector.murderers;
            cachedTextRenderer.draw(Text.of("§f[§cMurderers§f]: " + murdererList.size()), IMinecraft.mc.getWindow().getScaledWidth() / 2f - (cachedTextRenderer.getWidth(Text.of("[Murderers]: " + murdererList.size())) / 2f), murdererTextY, Color.WHITE.getRGB(), fontShadow.get(), matrix4f, vertexConsumerProvider, TextRenderer.TextLayerType.SEE_THROUGH, 0, 0);
            if (!murdererList.isEmpty()) {
                for (PlayerEntity murderer : murdererList) {
                    murdererTextY += 10f;
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

    public static void reloadSortedModules(TextRenderer textRenderer) {
        sortedModules = ModuleManager.modules.stream()
                .sorted((module1, module2) -> Double.compare(
                        textRenderer.getWidth(Text.of(module2.moduleName + (module2.tag() != null ? " " + module2.tag() : ""))),
                        textRenderer.getWidth(Text.of(module1.moduleName + (module1.tag() != null ? " " + module1.tag() : "")))
                ))
                .toList();
    }

    @Override
    public void onDisable() {
    }

    @Override
    public void onEnable() {
        updateColorModeUI();
    }

    private void updateColorModeUI() {
    }

    @Override
    public void onRenderHud(RenderHudEvent event) {
        if (IMinecraft.mc.player == null || IMinecraft.mc.world == null) return;

        int bgColor = 0;

        if (backGround.get()) {
            float bgOpacity = IMinecraft.mc.options.getTextBackgroundOpacity(0.3F);
            bgColor = (int) (bgOpacity * 255F) << 24;
        }

        if (cachedTextRenderer == null)
            cachedTextRenderer = event.textRenderer;

        VertexConsumerProvider vertexConsumerProvider = event.vertexConsumers;
        Matrix4f matrix4f = event.matrix4f;

        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formattedNow = now.format(formatter);

        float infoX = getSpacingSize();
        float infoY = getSpacingSize();

        if (waterMark.get()) {
            String watermarkText = null;
            Color textColor = Color.WHITE;

            switch (watermarkMode.get().toLowerCase()) {
                case "normal":
                    watermarkText = "§f" + clientName.get() + " (§c" + formattedNow + "§f)";
                    break;

                case "radium":
                    watermarkText = clientName.get();
                    textColor = getModuleColor(0, 1);
                    break;
            }

            cachedTextRenderer.draw(
                    Text.of(watermarkText),
                    infoX,
                    infoY,
                    textColor.getRGB(),
                    fontShadow.get(),
                    matrix4f,
                    vertexConsumerProvider,
                    TextRenderer.TextLayerType.SEE_THROUGH,
                    bgColor,
                    0
            );
            infoY += 10f;
        }

        if (extraInfo.get()) {
            cachedTextRenderer.draw(Text.of("§f[§cFPS§f]§7: " + IMinecraft.mc.getCurrentFps()), infoX, infoY, Color.WHITE.getRGB(), fontShadow.get(), matrix4f, vertexConsumerProvider, TextRenderer.TextLayerType.SEE_THROUGH, bgColor, 0);
            infoY += 10f;

            cachedTextRenderer.draw(Text.of("§f[§cBPS§f]§7: " + ModuleManager.bps), infoX, infoY, Color.WHITE.getRGB(), fontShadow.get(), matrix4f, vertexConsumerProvider, TextRenderer.TextLayerType.SEE_THROUGH, bgColor, 0);
            infoY += 10f;

            cachedTextRenderer.draw(Text.of("§f[§cServer§f]§7: " + IMinecraft.mc.player.networkHandler.getBrand()), infoX, infoY, Color.WHITE.getRGB(), fontShadow.get(), matrix4f, vertexConsumerProvider, TextRenderer.TextLayerType.SEE_THROUGH, bgColor, 0);
            infoY += 10f;

            if (IMinecraft.mc.player.networkHandler.getServerInfo() != null) {
                cachedTextRenderer.draw(Text.of("§f[§cVersion§f]§7: " + IMinecraft.mc.player.networkHandler.getServerInfo().version.getString()), infoX, infoY, Color.WHITE.getRGB(), fontShadow.get(), matrix4f, vertexConsumerProvider, TextRenderer.TextLayerType.SEE_THROUGH, bgColor, 0);
                infoY += 10f;

                cachedTextRenderer.draw(Text.of("§f[§cPing§f]§7: " + IMinecraft.mc.player.networkHandler.getServerInfo().ping), infoX, infoY, Color.WHITE.getRGB(), fontShadow.get(), matrix4f, vertexConsumerProvider, TextRenderer.TextLayerType.SEE_THROUGH, bgColor, 0);
            } else {
                cachedTextRenderer.draw(Text.of("§f[§cVersion§f]§7: null"), infoX, infoY, Color.WHITE.getRGB(), fontShadow.get(), matrix4f, vertexConsumerProvider, TextRenderer.TextLayerType.SEE_THROUGH, bgColor, 0);
                infoY += 10f;

                cachedTextRenderer.draw(Text.of("§f[§cPing§f]§7: null"), infoX, infoY, Color.WHITE.getRGB(), fontShadow.get(), matrix4f, vertexConsumerProvider, TextRenderer.TextLayerType.SEE_THROUGH, bgColor, 0);
            }
            infoY += 10f;

            cachedTextRenderer.draw(Text.of("§f[§cPlayers§f]§7: " + IMinecraft.mc.player.networkHandler.getCommandSource().getPlayerNames().size()), infoX, infoY, Color.WHITE.getRGB(), fontShadow.get(), matrix4f, vertexConsumerProvider, TextRenderer.TextLayerType.SEE_THROUGH, bgColor, 0);
        }

        if (moduleList.get()) {
            if (sortedModules == null)
                reloadSortedModules(cachedTextRenderer);

            float listY = getSpacingSize();
            int index = 0;

            for (Module module : sortedModules) {
                if (module.showOnArray && ModuleManager.getModuleState(module)) {
                    Color color = getModuleColor(index, sortedModules.size());

                    String text = module.moduleName;

                    if (module.tag() != null)
                        text += "§7 " + module.tag();

                    float textWidth = cachedTextRenderer.getWidth(Text.of(text));
                    float xPos = IMinecraft.mc.getWindow().getScaledWidth() - textWidth - infoX;

                    cachedTextRenderer.draw(Text.of(text), xPos, listY, color.getRGB(), fontShadow.get(), matrix4f, vertexConsumerProvider, TextRenderer.TextLayerType.SEE_THROUGH, bgColor, 0);

                    listY += 10f;
                    index++;
                }
            }
        }

        if (moduleInfo.get())
            renderModuleInformation(matrix4f, vertexConsumerProvider);
    }
}