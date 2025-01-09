package com.instrumentalist.elite.configs;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.instrumentalist.elite.Client;
import com.instrumentalist.elite.hacks.ModuleManager;
import com.instrumentalist.elite.utils.IMinecraft;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ConfigManager {

    private final Gson gson = new Gson();
    public final File BASE_DIR = new File(IMinecraft.mc.runDirectory, Client.configLocation);
    private final File CLIENT_FILE = new File(BASE_DIR, "client.json");
    public String configCurrent = "default", bindCurrent = "default";

    public void saveConfigFile(String configName, Boolean saveToDefaultFile) {
        this.configCurrent = configName;

        if (saveToDefaultFile)
            this.saveClientJS();

        final JsonObject configObject = new JsonObject();
        ModuleManager.modules.forEach(m -> {
            if (m.configObject != null && m.moduleCategory != null)
                configObject.add(m.moduleName, m.configObject.save().getFirst());
        });

        File base = new File(BASE_DIR, "module-configs");
        File dir = new File(base, this.configCurrent + ".json");

        try {
            if (!base.exists()) base.mkdirs();
            if (!dir.exists()) dir.createNewFile();

            try (FileOutputStream fos = new FileOutputStream(dir)) {
                try (OutputStreamWriter osw = new OutputStreamWriter(fos, StandardCharsets.UTF_8)) {
                    osw.write(gson.toJson(configObject));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void saveBindFile(String bindName, Boolean saveToDefaultFile) {
        this.bindCurrent = bindName;

        if (saveToDefaultFile)
            this.saveClientJS();

        final JsonObject bindObject = new JsonObject();
        ModuleManager.modules.forEach(m -> {
            if (m.configObject != null && m.moduleCategory != null)
                bindObject.add(m.moduleName, m.configObject.save().getSecond());
        });

        File base = new File(BASE_DIR, "bind-configs");
        File bindFile = new File(base, this.bindCurrent + ".json");

        try {
            if (!base.exists()) base.mkdirs();
            if (!bindFile.exists()) bindFile.createNewFile();

            try (FileOutputStream fos = new FileOutputStream(bindFile)) {
                try (OutputStreamWriter osw = new OutputStreamWriter(fos, StandardCharsets.UTF_8)) {
                    osw.write(gson.toJson(bindObject));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void loadConfig(String configName) {
        this.configCurrent = configName;

        File base = new File(BASE_DIR, "module-configs");
        File dir = new File(base, this.configCurrent + ".json");

        if (!base.exists() || !dir.exists()) return;

        this.saveClientJS();

        try {
            try (InputStream is = new FileInputStream(dir)) {
                try (InputStreamReader isr = new InputStreamReader(is, StandardCharsets.UTF_8)) {
                    if (!base.exists() || !dir.exists()) return;
                    final JsonObject configObject = gson.fromJson(isr, JsonObject.class);
                    configObject.entrySet().forEach(entry -> {
                        final String moduleName = entry.getKey();
                        final JsonObject moduleData = entry.getValue().getAsJsonObject();
                        ModuleManager.modules.stream()
                                .filter(m -> m.moduleName.equals(moduleName))
                                .findFirst()
                                .ifPresent(m -> m.configObject.load(moduleData));
                    });
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void loadBind(String bindName) {
        this.bindCurrent = bindName;

        File base = new File(BASE_DIR, "bind-configs");
        File bindFile = new File(base, this.bindCurrent + ".json");

        if (!base.exists() || !bindFile.exists()) return;

        this.saveClientJS();

        try (InputStream is = new FileInputStream(bindFile)) {
            try (InputStreamReader isr = new InputStreamReader(is, StandardCharsets.UTF_8)) {
                if (!base.exists() || !bindFile.exists()) return;
                final JsonObject bindData = gson.fromJson(isr, JsonObject.class);
                bindData.entrySet().forEach(entry -> {
                    final String moduleName = entry.getKey();
                    final JsonObject moduleData = entry.getValue().getAsJsonObject();
                    ModuleManager.modules.stream()
                            .filter(m -> m.moduleName.equals(moduleName))
                            .findFirst()
                            .ifPresent(m -> m.key = moduleData.get("bind").getAsInt());
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void saveClientJS() {
        JsonObject client = new JsonObject();
        client.addProperty("module-config", this.configCurrent);
        client.addProperty("bind-config", this.bindCurrent);

        try {
            if (!BASE_DIR.exists()) BASE_DIR.mkdirs();
            if (!CLIENT_FILE.exists()) CLIENT_FILE.createNewFile();

            try (FileOutputStream fos = new FileOutputStream(CLIENT_FILE)) {
                try (OutputStreamWriter osw = new OutputStreamWriter(fos, StandardCharsets.UTF_8)) {
                    osw.write(gson.toJson(client));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void load() {
        File configBase = new File(BASE_DIR, "module-configs");
        File configDir = new File(configBase, "default.json");
        File bindBase = new File(BASE_DIR, "bind-configs");
        File bindDir = new File(bindBase, "default.json");
        if (!CLIENT_FILE.exists())
            this.saveClientJS();
        if (!configBase.exists() || !configDir.exists())
            saveConfigFile(this.configCurrent, true);
        if (!bindBase.exists() || !bindDir.exists())
            saveBindFile(this.bindCurrent, true);
        freshConfig();
        loadConfig(this.configCurrent);
        loadBind(this.bindCurrent);
    }

    public void freshConfig() {
        try (InputStream is = new FileInputStream(CLIENT_FILE)) {
            try (InputStreamReader isr = new InputStreamReader(is, StandardCharsets.UTF_8)) {
                final JsonObject client = gson.fromJson(isr, JsonObject.class);
                this.configCurrent = client.get("module-config").getAsString();
                this.bindCurrent = client.get("bind-config").getAsString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}