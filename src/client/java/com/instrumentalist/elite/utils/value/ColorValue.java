package com.instrumentalist.elite.utils.value;

import java.awt.Color;
import java.util.function.BooleanSupplier;

public class ColorValue {
    private final String name;
    private Color value;
    private final BooleanSupplier visibility;

    public ColorValue(String name, Color defaultValue) {
        this(name, defaultValue, () -> true);
    }

    public ColorValue(String name, Color defaultValue, BooleanSupplier visibility) {
        this.name = name;
        this.value = defaultValue;
        this.visibility = visibility;
    }

    public Color get() {
        return value;
    }

    public void set(Color value) {
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public boolean isVisible() {
        return visibility.getAsBoolean();
    }

    // Helper methods for color components
    public int getRed() {
        return value.getRed();
    }

    public int getGreen() {
        return value.getGreen();
    }

    public int getBlue() {
        return value.getBlue();
    }

    public int getAlpha() {
        return value.getAlpha();
    }

    public void setRed(int red) {
        this.value = new Color(
                Math.min(Math.max(red, 0), 255),
                getGreen(),
                getBlue(),
                getAlpha()
        );
    }

    public void setGreen(int green) {
        this.value = new Color(
                getRed(),
                Math.min(Math.max(green, 0), 255),
                getBlue(),
                getAlpha()
        );
    }

    public void setBlue(int blue) {
        this.value = new Color(
                getRed(),
                getGreen(),
                Math.min(Math.max(blue, 0), 255),
                getAlpha()
        );
    }

    public void setAlpha(int alpha) {
        this.value = new Color(
                getRed(),
                getGreen(),
                getBlue(),
                Math.min(Math.max(alpha, 0), 255)
        );
    }

    // HSB conversion methods
    public float[] getHSB() {
        return Color.RGBtoHSB(getRed(), getGreen(), getBlue(), null);
    }

    public void setHSB(float hue, float saturation, float brightness) {
        this.value = new Color(Color.HSBtoRGB(
                Math.min(Math.max(hue, 0f), 1f),
                Math.min(Math.max(saturation, 0f), 1f),
                Math.min(Math.max(brightness, 0f), 1f)
        ));
    }

    // Hex conversion methods
    public String toHex() {
        return String.format("#%02x%02x%02x%02x", getRed(), getGreen(), getBlue(), getAlpha());
    }

    public void fromHex(String hex) {
        try {
            hex = hex.replace("#", "");
            if (hex.length() == 6) {
                hex = hex + "ff"; // Add alpha if not present
            }
            int color = Integer.parseInt(hex, 16);
            this.value = new Color(
                    (color >> 24) & 0xFF,
                    (color >> 16) & 0xFF,
                    (color >> 8) & 0xFF,
                    color & 0xFF
            );
        } catch (Exception e) {
            // Invalid hex color, keep current value
        }
    }
}