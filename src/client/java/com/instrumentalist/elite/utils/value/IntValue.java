package com.instrumentalist.elite.utils.value;

public class IntValue extends SettingValue<Integer> {
    public final int minimum;
    public final int maximum;
    public final String suffix;

    public IntValue(String name, int value, int minimum, int maximum, String suffix, DisplayableCondition displayable) {
        super(name, value, displayable);
        this.minimum = minimum;
        this.maximum = maximum;
        this.suffix = suffix;
    }

    public IntValue(String name, int value, int minimum, int maximum, DisplayableCondition displayable) {
        this(name, value, minimum, maximum, "", displayable);
    }

    public IntValue(String name, int value, int minimum, int maximum, String suffix) {
        this(name, value, minimum, maximum, suffix, () -> true);
    }

    public IntValue(String name, int value, int minimum, int maximum) {
        this(name, value, minimum, maximum, "", () -> true);
    }

    public void set(Number newValue) {
        set(newValue.intValue());
    }
}
