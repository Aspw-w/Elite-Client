package com.instrumentalist.elite.utils.value;

import java.awt.Color;
import java.util.function.BooleanSupplier;

public class ColorValue extends SettingValue<Color> {

    public ColorValue(String name, Color value, DisplayableCondition displayable) {
        super(name, value, displayable);
    }

    public ColorValue(String name, Color value) {
        this(name, value, () -> true);
    }

    public float[] getHSB() {
        return Color.RGBtoHSB(value.getRed(), value.getGreen(), value.getBlue(), null);
    }

    public void setHSB(float hue, float saturation, float brightness) {
        this.value = new Color(Color.HSBtoRGB(
                Math.min(Math.max(hue, 0f), 1f),
                Math.min(Math.max(saturation, 0f), 1f),
                Math.min(Math.max(brightness, 0f), 1f)
        ));
    }

    public String toHex() {
        return String.format("#%02x%02x%02x%02x", value.getRed(), value.getGreen(), value.getBlue(), value.getAlpha());
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