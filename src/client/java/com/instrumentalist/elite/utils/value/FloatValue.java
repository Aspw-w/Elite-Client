package com.instrumentalist.elite.utils.value;

public class FloatValue extends SettingValue<Float> {
    public final float minimum;
    public final float maximum;
    public final String suffix;

    public FloatValue(String name, float value, float minimum, float maximum, String suffix, DisplayableCondition displayable) {
        super(name, value, displayable);
        this.minimum = minimum;
        this.maximum = maximum;
        this.suffix = suffix;
    }

    public FloatValue(String name, float value, float minimum, float maximum, DisplayableCondition displayable) {
        this(name, value, minimum, maximum, "", displayable);
    }

    public FloatValue(String name, float value, float minimum, float maximum, String suffix) {
        this(name, value, minimum, maximum, suffix, () -> true);
    }

    public FloatValue(String name, float value, float minimum, float maximum) {
        this(name, value, minimum, maximum, "", () -> true);
    }

    public void set(Number newValue) {
        set(newValue.floatValue());
    }
}