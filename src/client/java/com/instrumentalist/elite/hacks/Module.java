package com.instrumentalist.elite.hacks;

import com.instrumentalist.elite.Client;
import com.instrumentalist.elite.configs.ConfigObject;
import com.instrumentalist.elite.events.EventListener;
import com.instrumentalist.elite.hacks.features.render.Interface;
import com.instrumentalist.elite.utils.ChatUtil;
import com.instrumentalist.elite.utils.value.SettingValue;
import org.lwjgl.glfw.GLFW;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public abstract class Module implements EventListener {
    public final String moduleName;
    public final ModuleCategory moduleCategory;
    public int key;
    public boolean tempEnabled;
    public boolean showOnArray;

    public ConfigObject configObject = new ConfigObject(this);
    public List<SettingValue<?>> settings;

    public Module(String moduleName, ModuleCategory moduleCategory, int key, boolean tempEnabled, boolean showOnArray) {
        this.moduleName = moduleName;
        this.moduleCategory = moduleCategory;
        this.key = key;
        this.tempEnabled = tempEnabled;
        this.showOnArray = showOnArray;

        addSettings();
    }

    private void addSettings() {
        Field[] declaredFields = this.getClass().getDeclaredFields();

        List<SettingValue<?>> settings = new ArrayList<>();
        for (Field declaredField : declaredFields) {
            try {
                declaredField.setAccessible(true);
                Object value = declaredField.get(this);
                if (value instanceof SettingValue<?>) {
                    settings.add((SettingValue<?>) value);
                }
            } catch (Exception e) {
                throw new RuntimeException(String.format("Initializing Setting(%s) of %s failed", this.moduleName, declaredField.getName()));
            }
        }

        this.settings = settings;
    }

    public void setState(boolean state) {
        if (tempEnabled == state) return;

        this.tempEnabled = state;

        if (state) {
            Client.eventManager.register(this);
            onEnable();
        } else {
            Client.eventManager.unregister(this);
            onDisable();
        }
    }

    public void toggle() {
        setState(!this.tempEnabled);

        if (Interface.cachedTextRenderer != null)
            Interface.reloadSortedModules(Interface.cachedTextRenderer);
    }

    public String tag() {
        return null;
    }

    public abstract void onEnable();
    public abstract void onDisable();
}