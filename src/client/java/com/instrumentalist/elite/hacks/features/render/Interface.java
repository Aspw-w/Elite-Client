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

                fadeProgress = 1 * (float) Math.cos(fadeProgress * 2 * Math.PI);

                Color primary = primaryColor.get();
                Color secondary = secondaryColor.get();
                return smoothLoopingColorTransition(primary, secondary, fadeProgress);
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
        int screenWidth = IMinecraft.mc.getWindow().getScaledWidth();
        int screenHeight = IMinecraft.mc.getWindow().getScaledHeight();
        long currentTime = System.currentTimeMillis();
        long cycleDuration = 3600L;

        if (ModuleManager.getModuleState(new Scaffold())) {
            String scaffoldText = Scaffold.Companion.getHotbarStackSize() + " Blocks";
            renderColoredText(scaffoldText, screenWidth / 2f, screenHeight / 2f - 40f, currentTime, cycleDuration, matrix4f, vertexConsumerProvider);
        }

        if (ModuleManager.getModuleState(new Disabler()) && HypixelDisabler.stuckOnAir && HypixelDisabler.airTicks >= 9 && !HypixelDisabler.watchDogDisabled) {
            String disablingText = "Disabling... " + HypixelDisabler.airStuckTicks + "/20";
            renderColoredText(disablingText, screenWidth / 2f, screenHeight / 2f - 60f, currentTime, cycleDuration, matrix4f, vertexConsumerProvider);
        }

        if (IMinecraft.mc.interactionManager != null &&
                (ModuleManager.getModuleState(new Breaker()) && Breaker.Companion.getWasBreaking() ||
                        ModuleManager.getModuleState(new CivBreak()) && CivBreak.Companion.getWasBreaking() ||
                        ModuleManager.getModuleState(new Nuker()) && Nuker.Companion.getWasBreaking())) {
            String breakingText = "Breaking... " + IMinecraft.mc.interactionManager.getBlockBreakingProgress();
            renderColoredText(breakingText, screenWidth / 2f, screenHeight / 2f - 80f, currentTime, cycleDuration, matrix4f, vertexConsumerProvider);
        }

        if (BlinkUtil.INSTANCE.getBlinking()) {
            String blinkText = "Blinking... (x" + BlinkUtil.INSTANCE.getPackets().size() + ")";
            renderColoredText(blinkText, screenWidth / 2f, screenHeight / 2f - 100f, currentTime, cycleDuration, matrix4f, vertexConsumerProvider);
        }

        if (ModuleManager.getModuleState(new MurdererDetector())) {
            float murdererTextY = screenHeight / 2f - 220f;
            List<PlayerEntity> murdererList = MurdererDetector.murderers;
            String murdererHeader = "§f[§cMurderers§f]: " + murdererList.size();
            cachedTextRenderer.draw(Text.of(murdererHeader), screenWidth / 2f - (cachedTextRenderer.getWidth(Text.of(murdererHeader)) / 2f), murdererTextY, Color.WHITE.getRGB(), fontShadow.get(), matrix4f, vertexConsumerProvider, TextRenderer.TextLayerType.SEE_THROUGH, 0, 0);

            for (PlayerEntity murderer : murdererList) {
                murdererTextY += 10f;
                String distance = ModuleManager.decimalFormat.format(IMinecraft.mc.player.distanceTo(murderer));
                cachedTextRenderer.draw(Text.of("§f> " + murderer.getName().getString() + "§7 (" + distance + "m)"),
                        screenWidth / 2f - (cachedTextRenderer.getWidth(Text.of("> " + murderer.getName().getString() + " (" + distance + "m)")) / 2f),
                        murdererTextY, Color.WHITE.getRGB(), fontShadow.get(), matrix4f, vertexConsumerProvider, TextRenderer.TextLayerType.SEE_THROUGH, 0, 0);
            }
        }
    }

    private void renderColoredText(String text, float centerX, float y, long currentTime, long cycleDuration, Matrix4f matrix4f, VertexConsumerProvider vertexConsumerProvider) {
        float textWidth = cachedTextRenderer.getWidth(Text.of(text));
        float startX = centerX - textWidth / 2f;
        float hueBase = (currentTime % cycleDuration) / (float) cycleDuration;

        for (int i = 0; i < text.length(); i++) {
            float hueShift = (hueBase + i / (float) text.length()) % 1.0f;
            Color color = Color.getHSBColor(hueShift, 1.0f, 1.0f);
            cachedTextRenderer.draw(Text.of(String.valueOf(text.charAt(i))), startX, y, new Color(color.getRed(), color.getGreen(), color.getBlue(), 255).getRGB(), fontShadow.get(), matrix4f, vertexConsumerProvider, TextRenderer.TextLayerType.SEE_THROUGH, 0, 0);
            startX += cachedTextRenderer.getWidth(String.valueOf(text.charAt(i)));
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
        if (IMinecraft.mc.player == null || IMinecraft.mc.world == null || IMinecraft.mc.options.hudHidden) return;

        int bgColor = 0;

        if (cachedTextRenderer == null)
            cachedTextRenderer = event.textRenderer;

        VertexConsumerProvider vertexConsumerProvider = event.vertexConsumers;
        Matrix4f matrix4f = event.matrix4f;

        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formattedNow = now.format(formatter);

        float infoX = getSpacingSize();
        float infoY = getSpacingSize();

        if (backGround.get()) {
            float bgOpacity = IMinecraft.mc.options.getTextBackgroundOpacity(0.3F);
            bgColor = (int) (bgOpacity * 255F) << 24;
        }

        if (waterMark.get()) {
            String watermarkText = null;
            Color textColor = Color.WHITE;

            int originalBgColor = bgColor;

            switch (watermarkMode.get().toLowerCase()) {
                case "normal":
                    watermarkText = "§f" + clientName.get() + " (§c" + formattedNow + "§f)";
                    break;

                case "radium":
                    watermarkText = clientName.get();
                    textColor = getModuleColor(0, 1);
                    bgColor = 0x00000000;
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

            bgColor = originalBgColor;

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