package com.instrumentalist.elite.utils.value;

import com.instrumentalist.elite.hacks.ModuleManager;
import com.instrumentalist.elite.hacks.features.render.Interface;
import com.instrumentalist.elite.utils.ChatUtil;

import java.awt.Color;
import java.util.Arrays;

public abstract class SettingValue<T> {
    public final String name;
    public T value;
    public final DisplayableCondition canDisplay;

    public SettingValue(String name, T value, DisplayableCondition canDisplay) {
        this.name = name;
        this.value = value;
        this.canDisplay = canDisplay;
    }

    public void set(T newValue) {
        if (newValue.equals(value)) return;

        T oldValue = get();

        try {
            onChange(oldValue, newValue);
            changeValue(newValue);
            onChanged(oldValue, newValue);
        } catch (Exception e) {
            ChatUtil.showLog("[ValueSystem (" + name + ")]: " + e.getClass().getName() + " (" + e.getMessage() + ") [" + oldValue + " >> " + newValue + "]");
        }
    }

    public T get() {
        return value;
    }

    protected void changeValue(T value) {
        this.value = value;
    }

    protected void onChange(T oldValue, T newValue) {}

    protected void onChanged(T oldValue, T newValue) {
        if (Interface.cachedTextRenderer != null)
            Interface.reloadSortedModules(Interface.cachedTextRenderer);
    }

    @FunctionalInterface
    public interface DisplayableCondition {
        boolean canDisplay();
    }
}