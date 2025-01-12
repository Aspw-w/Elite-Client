package com.instrumentalist.elite;

import com.instrumentalist.elite.configs.ConfigManager;
import com.instrumentalist.elite.events.EventManager;
import com.instrumentalist.elite.hacks.Module;
import com.instrumentalist.elite.hacks.ModuleManager;
import com.instrumentalist.elite.utils.ChatUtil;
import com.instrumentalist.elite.utils.FileUtil;

public class Client {

    public static String clientVersion = "1.2";

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

        FileUtil.INSTANCE.doCfgNetLoader();

        ModuleManager.onInitialize();
        for (Module module : ModuleManager.modules) {
            for (int i = 0; i <= 1; i++) {
                module.toggle();
            }
        } // fixes module state
        ChatUtil.showLog("Initialized Module Manager");

        configManager.load();
        ChatUtil.showLog("Loaded config");

        FileUtil.INSTANCE.updateCheck();

        ChatUtil.showLog("Loaded Elite Client " + clientVersion);
        loaded = true;
    }
}
