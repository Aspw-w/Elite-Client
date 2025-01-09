package com.instrumentalist.elite.utils.value;

public class BooleanValue extends SettingValue<Boolean> {
    public BooleanValue(String name, Boolean value, DisplayableCondition displayable) {
        super(name, value, displayable);
    }

    public BooleanValue(String name, Boolean value) {
        this(name, value, () -> true);
    }
}