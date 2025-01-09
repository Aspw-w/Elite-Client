package com.instrumentalist.elite;

import com.instrumentalist.elite.configs.ConfigManager;
import com.instrumentalist.elite.events.EventManager;
import com.instrumentalist.elite.hacks.Module;
import com.instrumentalist.elite.hacks.ModuleManager;
import com.instrumentalist.elite.utils.ChatUtil;

public class Client {

    public static EventManager eventManager = null;
    public static ConfigManager configManager = null;
    public static boolean loaded = false;
    public static String configLocation = "Elite";

    public static void inject() {
        ChatUtil.showLog("Started loading Elite...");

        eventManager = new EventManager();
        ChatUtil.showLog("Initialized Event Manager");

        configManager = new ConfigManager();
        ChatUtil.showLog("Initialized Config Manager");

        ModuleManager.onInitialize();
        for (Module module : ModuleManager.modules) {
            for (int i = 0; i <= 1; i++) {
                module.toggle();
            }
        } // fixes module state (temporary)
        ChatUtil.showLog("Initialized Module Manager");

        configManager.load();
        ChatUtil.showLog("Loaded config");

        ChatUtil.showLog("Loaded Elite.");
        loaded = true;
    }
}
