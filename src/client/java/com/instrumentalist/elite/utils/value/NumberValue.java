package com.instrumentalist.elite.utils.value;

public class NumberValue<T extends Number> {
    private final String name;
    private final String description;
    private T value;
    private final T min;
    private final T max;
    private final java.util.function.BooleanSupplier visibility;

    public NumberValue(String name, T defaultValue, T min, T max) {
        this(name, defaultValue, min, max, "", () -> true);
    }

    public NumberValue(String name, T defaultValue, T min, T max, String description) {
        this(name, defaultValue, min, max, description, () -> true);
    }

    public NumberValue(String name, T defaultValue, T min, T max, java.util.function.BooleanSupplier visibility) {
        this(name, defaultValue, min, max, "", visibility);
    }

    public NumberValue(String name, T defaultValue, T min, T max, String description, java.util.function.BooleanSupplier visibility) {
        this.name = name;
        this.value = defaultValue;
        this.min = min;
        this.max = max;
        this.description = description;
        this.visibility = visibility;
    }

    public T get() {
        return value;
    }

    public void set(T value) {
        if (value.doubleValue() >= min.doubleValue() && value.doubleValue() <= max.doubleValue()) {
            this.value = value;
        }
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public T getMin() {
        return min;
    }

    public T getMax() {
        return max;
    }

    public boolean isVisible() {
        return visibility.getAsBoolean();
    }
}