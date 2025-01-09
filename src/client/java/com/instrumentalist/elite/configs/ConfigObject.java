package com.instrumentalist.elite.configs;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.instrumentalist.elite.hacks.Module;
import com.instrumentalist.elite.hacks.ModuleManager;
import com.instrumentalist.elite.utils.value.*;
import com.mojang.datafixers.util.Pair;

import java.util.Objects;

public class ConfigObject {

    private final Module module;

    public ConfigObject(Module module) {
        this.module = module;
    }

    public Pair<JsonObject, JsonObject> save() {
        JsonObject jo = new JsonObject();
        JsonObject bjo = new JsonObject();
        jo.addProperty("toggle", module.tempEnabled);
        bjo.addProperty("bind", module.key);
        JsonArray ja = new JsonArray();
        ModuleManager.getSettings(module).stream().map(setting -> {
            try {
                setting.setAccessible(true);
                return setting.get(module);
            } catch (IllegalAccessException e) {
                return null;
            }
        }).forEach(setting -> {
            if (setting == null) return;
            if (setting instanceof SettingValue<?>) {
                JsonObject jsonObject = new JsonObject();
                switch (setting) {
                    case TextValue textValue -> jsonObject.addProperty(textValue.name, textValue.value);
                    case BooleanValue booleanValue ->
                            jsonObject.addProperty(booleanValue.name, booleanValue.value);
                    case FloatValue floatValue -> jsonObject.addProperty(floatValue.name, floatValue.value);
                    case IntValue intValue -> jsonObject.addProperty(intValue.name, intValue.value);
                    case ListValue listValue -> jsonObject.addProperty(listValue.name, listValue.value);
                    case KeyBindValue keyBindValue -> jsonObject.addProperty(keyBindValue.name, keyBindValue.value);
                    default -> {
                    }
                }
                if (!jsonObject.isEmpty()) ja.add(jsonObject);
            }
        });
        jo.add("settings", ja);
        return Pair.of(jo, bjo);
    }

    public void load(JsonObject jsonObject) {
        jsonObject.entrySet().forEach(config -> {
            if (Objects.equals(config.getKey(), "toggle")) module.setState(config.getValue().getAsBoolean());
            if (Objects.equals(config.getKey(), "settings")) {
                config.getValue().getAsJsonArray().forEach(jsonElement -> jsonElement.getAsJsonObject().entrySet().forEach(setting -> ModuleManager.getSettings(module).stream().map(m -> {
                    try {
                        m.setAccessible(true);
                        return m.get(module);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return null;
                    }
                }).forEach(m -> {
                    if (!(m instanceof SettingValue<?>)) return;
                    switch (m) {
                        case TextValue textValue when Objects.equals(textValue.name, setting.getKey()) ->
                                textValue.set(setting.getValue().getAsString());
                        case BooleanValue booleanValue when Objects.equals(booleanValue.name, setting.getKey()) ->
                                booleanValue.set(setting.getValue().getAsBoolean());
                        case FloatValue floatValue when Objects.equals(floatValue.name, setting.getKey()) ->
                                floatValue.set(setting.getValue().getAsFloat());
                        case IntValue intValue when Objects.equals(intValue.name, setting.getKey()) ->
                                intValue.set(setting.getValue().getAsInt());
                        case ListValue listValue when Objects.equals(listValue.name, setting.getKey()) ->
                                listValue.set(setting.getValue().getAsString());
                        case KeyBindValue keyBindValue when Objects.equals(keyBindValue.name, setting.getKey()) ->
                                keyBindValue.set(setting.getValue().getAsInt());
                        default -> {
                        }
                    }
                })));
            }
        });
    }

}
