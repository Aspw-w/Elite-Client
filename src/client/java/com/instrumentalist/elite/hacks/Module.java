package com.instrumentalist.elite.hacks;

import com.instrumentalist.elite.Client;
import com.instrumentalist.elite.configs.ConfigObject;
import com.instrumentalist.elite.events.EventListener;
import com.instrumentalist.elite.hacks.features.render.Interface;
import com.instrumentalist.elite.utils.value.SettingValue;

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