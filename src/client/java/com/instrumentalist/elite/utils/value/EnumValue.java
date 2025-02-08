package com.instrumentalist.elite.utils.value;

public class EnumValue<T extends Enum<T>> {
    private final String name;
    private T value;

    public EnumValue(String name, T defaultValue) {
        this.name = name;
        this.value = defaultValue;
    }

    public T get() {
        return value;
    }

    public void set(T value) {
        this.value = value;
    }

    public String getName() {
        return name;
    }
}