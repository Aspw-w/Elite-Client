package xyz.breadloaf.imguimc.customwindow;

import com.instrumentalist.elite.Client;
import com.instrumentalist.elite.hacks.Module;
import com.instrumentalist.elite.hacks.ModuleCategory;
import com.instrumentalist.elite.hacks.ModuleManager;
import com.instrumentalist.elite.hacks.features.nulling.PluginsDetector;
import com.instrumentalist.elite.hacks.features.exploit.ServerCrasher;
import com.instrumentalist.elite.utils.ChatUtil;
import com.instrumentalist.elite.utils.FileUtil;
import com.instrumentalist.elite.utils.IMinecraft;
import com.instrumentalist.elite.utils.packet.PacketUtil;
import com.instrumentalist.elite.utils.value.*;
import imgui.ImGui;
import imgui.flag.ImGuiInputTextFlags;
import imgui.type.ImInt;
import imgui.type.ImString;
import net.minecraft.client.util.InputUtil;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import org.lwjgl.glfw.GLFW;
import xyz.breadloaf.imguimc.interfaces.Renderable;
import xyz.breadloaf.imguimc.interfaces.Theme;
import xyz.breadloaf.imguimc.theme.ImGuiClassicTheme;
import xyz.breadloaf.imguimc.theme.ImGuiDarkTheme;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.*;
import java.util.List;

public class ModuleRenderable implements Renderable {

    public static List<String> commandLogs = new ArrayList<>();
    public static boolean commandTabJustOpened = false;
    public static boolean isCommandTab = false;
    public static boolean startUpped = false;

    @Override
    public String getName() {
        return "Module Renderable";
    }

    @Override
    public Theme getTheme() {
        return new ImGuiClassicTheme();
    }

    @Override
    public void render() {
        ImGui.begin("Elite Client");

        try {
            if (!startUpped) {
                ImGui.setWindowSize(800f, 800f);
                startUpped = true;
            }

            if (!FileUtil.INSTANCE.isLatestClient()) {
                ImGui.text("Your client is OUTDATED! Please check network, updates");

                if (Desktop.isDesktopSupported()) {
                    if (ImGui.button("Check updates (Aspw-w/Elite-Client/releases)")) {
                        try {
                            Desktop.getDesktop().browse(new URI("https://github.com/Aspw-w/Elite-Client/releases"));
                        } catch (URISyntaxException e) {
                            System.err.println("Invalid URI syntax: " + e.getMessage());
                        } catch (IOException e) {
                            System.err.println("Failed to open the browser: " + e.getMessage());
                        }
                    }
                } else {
                    ImGui.text("Check updates (github.com/Aspw-w/Elite-Client/releases)");
                }
            } else {
                ImGui.text("Hello, everyone!");
            }

            ImGui.separator();
            ImGui.spacing();

            if (ImGui.beginTabBar("Categories")) {
                try {
                    for (ModuleCategory category : ModuleCategory.values()) {
                        if (category == null) continue;

                        if (ImGui.beginTabItem(category.name())) {
                            try {
                                isCommandTab = false;
                                commandTabJustOpened = false;

                                for (Module module : ModuleManager.modules) {
                                    if (module.moduleCategory != category) continue;

                                    if (ImGui.collapsingHeader(module.moduleName)) {
                                        renderModuleSettings(module);
                                    }
                                }
                            } finally {
                                ImGui.endTabItem();
                            }
                        }
                    }

                    if (ImGui.beginTabItem("Module Configs")) {
                        try {
                            isCommandTab = false;
                            commandTabJustOpened = false;

                            List<Path> moduleConfigs = FileUtil.INSTANCE.getModuleFiles();
                            if (!moduleConfigs.isEmpty()) {
                                ImGui.text("Available Module Configs:");
                                ImGui.beginChild("ModuleConfigsList", 0, 500, true);

                                for (Path config : moduleConfigs) {
                                    String configName = config.getFileName().toString().replace(".json", "");

                                    String showConfigName = configName;
                                    if (showConfigName.equalsIgnoreCase(Client.configManager.configCurrent))
                                        showConfigName = configName + " <- Current";

                                    if (ImGui.selectable(showConfigName)) {
                                        File prevBase = new File(Client.configManager.BASE_DIR, "module-configs");
                                        File prevModuleFile = new File(prevBase, Client.configManager.configCurrent + ".json");
                                        if (prevBase.exists() && prevModuleFile.exists())
                                            Client.configManager.saveConfigFile(Client.configManager.configCurrent, false);

                                        Client.configManager.loadConfig(configName, false);

                                        File base = new File(Client.configManager.BASE_DIR, "module-configs");
                                        File moduleFile = new File(base, configName + ".json");
                                        if (base.exists() && moduleFile.exists())
                                            Client.configManager.saveConfigFile(configName, true);
                                    }
                                }

                                ImGui.endChild();
                            } else {
                                ImGui.text("No module configs found.");
                            }

                            ImGui.spacing();
                            ImGui.separator();

                            ImGui.text("Create a new Module Config:");
                            ImString newConfigName = new ImString(256);
                            if (ImGui.inputText("New Config Name (Enter to create)", newConfigName, ImGuiInputTextFlags.EnterReturnsTrue)) {
                                String configName = newConfigName.get();
                                if (!configName.isEmpty())
                                    Client.configManager.saveConfigFile(configName, true);
                            }
                        } finally {
                            ImGui.endTabItem();
                        }
                    }

                    if (ImGui.beginTabItem("Bind Configs")) {
                        try {
                            isCommandTab = false;
                            commandTabJustOpened = false;

                            List<Path> bindConfigs = FileUtil.INSTANCE.getBindFiles();
                            if (!bindConfigs.isEmpty()) {
                                ImGui.text("Available Bind Configs:");
                                ImGui.beginChild("BindConfigsList", 0, 500, true);

                                for (Path config : bindConfigs) {
                                    String configName = config.getFileName().toString().replace(".json", "");

                                    String showConfigName = configName;
                                    if (showConfigName.equalsIgnoreCase(Client.configManager.bindCurrent))
                                        showConfigName = configName + " <- Current";

                                    if (ImGui.selectable(showConfigName)) {
                                        File prevBase = new File(Client.configManager.BASE_DIR, "bind-configs");
                                        File prevBindFile = new File(prevBase, Client.configManager.bindCurrent + ".json");
                                        if (prevBase.exists() && prevBindFile.exists())
                                            Client.configManager.saveBindFile(Client.configManager.bindCurrent, false);

                                        Client.configManager.loadBind(configName);

                                        File base = new File(Client.configManager.BASE_DIR, "bind-configs");
                                        File bindFile = new File(base, configName + ".json");
                                        if (base.exists() && bindFile.exists())
                                            Client.configManager.saveBindFile(configName, true);
                                    }
                                }

                                ImGui.endChild();
                            } else {
                                ImGui.text("No bind configs found.");
                            }

                            ImGui.spacing();
                            ImGui.separator();

                            ImGui.text("Create a new Bind Config:");
                            ImString newConfigName = new ImString(256);
                            if (ImGui.inputText("New Config Name (Enter to create)", newConfigName, ImGuiInputTextFlags.EnterReturnsTrue)) {
                                String configName = newConfigName.get();
                                if (!configName.isEmpty())
                                    Client.configManager.saveBindFile(configName, true);
                            }
                        } finally {
                            ImGui.endTabItem();
                        }
                    }

                    if (ImGui.beginTabItem("Online Configs")) {
                        try {
                            isCommandTab = false;
                            commandTabJustOpened = false;

                            List<String> moduleConfigs = FileUtil.INSTANCE.getOnlineCfgs();
                            if (!moduleConfigs.isEmpty()) {
                                ImGui.text("Available Online Configs:");
                                ImGui.beginChild("OnlineConfigsList", 0, 500, true);

                                for (String config : moduleConfigs) {
                                    String configName = config.replace(".json", "");

                                    if (ImGui.selectable(configName)) {
                                        File prevBase = new File(Client.configManager.BASE_DIR, "module-configs");
                                        File prevModuleFile = new File(prevBase, Client.configManager.configCurrent + ".json");
                                        if (prevBase.exists() && prevModuleFile.exists())
                                            Client.configManager.saveConfigFile(Client.configManager.configCurrent, false);

                                        Client.configManager.loadConfig(configName, true);

                                        File base = new File(Client.configManager.BASE_DIR, "module-configs");
                                        File moduleFile = new File(base, configName + ".json");
                                        if (base.exists() && moduleFile.exists())
                                            Client.configManager.saveConfigFile(configName, true);
                                    }
                                }

                                ImGui.endChild();
                            } else {
                                ImGui.text("No online configs found.");
                            }
                        } finally {
                            ImGui.endTabItem();
                        }
                    }

                    if (ImGui.beginTabItem("Command")) {
                        try {
                            isCommandTab = true;

                            if (!commandTabJustOpened) {
                                if (commandLogs.isEmpty())
                                    ImGui.setKeyboardFocusHere();
                                else ImGui.setKeyboardFocusHere(1);
                                commandTabJustOpened = true;
                            }

                            ImGui.text("Command Execution");

                            ImGui.beginChild("LogWindow", 0, 500, true);

                            for (String log : commandLogs)
                                ImGui.textWrapped(log);

                            if (ImGui.getScrollY() >= ImGui.getScrollMaxY())
                                ImGui.setScrollHereY(1.0f);

                            ImGui.endChild();

                            ImGui.spacing();
                            ImGui.separator();

                            ImGui.text("Enter a command:");

                            if (commandLogs.isEmpty()) {
                                if (IMinecraft.mc.player != null) {
                                    String sessionName = IMinecraft.mc.player.getName().getString();
                                    commandLogs.add("Your current session username: " + sessionName);
                                } else commandLogs.add("Your session name is NULL!");
                            }

                            ImString commandInput = new ImString(256);
                            if (ImGui.inputText("Command Input", commandInput, ImGuiInputTextFlags.EnterReturnsTrue)) {
                                if (!commandLogs.isEmpty())
                                    commandLogs.add("==================================================");
                                executeCommand(commandInput.get());
                                commandInput.set("");
                                ImGui.setKeyboardFocusHere(-1);
                            }
                        } finally {
                            ImGui.endTabItem();
                        }
                    }

                    if (ImGui.beginTabItem("Credits")) {
                        try {
                            ImGui.text("Made by Aspw");
                            ImGui.text("YouTube: https://www.youtube.com/@Hadveen");
                            ImGui.text("GitHub: https://github.com/Aspw-w");
                            ImGui.text("Discord: https://discord.gg/y8ZDqRxSCy");
                        } finally {
                            ImGui.endTabItem();
                        }
                    }
                } finally {
                    ImGui.endTabBar();
                }
            }
        } finally {
            ImGui.end();
        }
    }

    private void executeCommand(String command) {
        if (command.isBlank()) return;

        if (command.equalsIgnoreCase("help") || command.equalsIgnoreCase("commands")) {
            log("help/commands -> show every commands");
            log("t/toggle <module> -> toggle module");
            log("bind <module> <key> -> bind module");
            log("crash -> crash the server");
            log("pl/plugins -> detect server plugins");
            log("transaction -> debug transaction packets (x10)");
            log("session -> show your current session id");
        } else if (command.toLowerCase().startsWith("t " ) || command.toLowerCase().startsWith("toggle " )) {
            String moduleName = "";

            if (command.toLowerCase().startsWith("t " ))
                moduleName = command.toLowerCase().replace("t ", "").replace(" ", "");
            if (command.toLowerCase().startsWith("toggle " ))
                moduleName = command.toLowerCase().replace("toggle ", "").replace(" ", "");

            for (Module module : ModuleManager.modules) {
                if (moduleName.equals(module.moduleName.toLowerCase().replace(" ", ""))) {
                    module.toggle();
                    if (module.tempEnabled)
                        log("Enabled " + module.moduleName);
                    else log("Disabled " + module.moduleName);
                    return;
                }
            }

            log("Module " + moduleName + " was not found");
        } else if (command.equalsIgnoreCase("crash")) {
            log("Crashing server...");
            for (Module module : ModuleManager.modules) {
                if (module instanceof ServerCrasher && !module.tempEnabled)
                    module.toggle();
            }
        } else if (command.equalsIgnoreCase("transaction")) {
            log("Logging transactions...");
            ModuleManager.gettingTransactions = true;
        } else if (command.equalsIgnoreCase("pl") || command.equalsIgnoreCase("plugins")) {
            log("Detecting plugins...");
            for (Module module : ModuleManager.modules) {
                if (module instanceof PluginsDetector && !module.tempEnabled)
                    module.toggle();
            }
        } else if (command.startsWith("bind ")) {
            String moduleName = "";
            if (command.toLowerCase().startsWith("bind " ))
                moduleName = command.toLowerCase().replace("bind ", "");

            String[] parts = moduleName.split(" ");

            if (parts.length == 2) {
                for (Module module : ModuleManager.modules) {
                    if (Objects.equals(module.moduleName.toLowerCase().replace(" ", ""), parts[0])) {
                        try {
                            int newKey = InputUtil.fromTranslationKey("key.keyboard." + parts[1].toLowerCase()).getCode();
                            module.key = newKey;
                            log("Bound " + newKey + " key to " + module.moduleName + " module");
                        } catch (NumberFormatException ignored) {
                            module.key = GLFW.GLFW_KEY_UNKNOWN;
                            log("Unbound " + module.moduleName + " module");
                        }
                        return;
                    }
                }
            }

            log("Module " + moduleName + " was not found");
        } else if (command.startsWith("vclip ")) {
            String distance = "";
            if (command.toLowerCase().startsWith("vclip " ))
                distance = command.toLowerCase().replace("vclip ", "");

            try {
                double dist = Double.parseDouble(distance);
                if (IMinecraft.mc.player != null) {
                    if (dist > 10 || dist < 10) {
                        for (int i = 0; i <= 9; i++) {
                            PacketUtil.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(IMinecraft.mc.player.getPos().x, IMinecraft.mc.player.getPos().y, IMinecraft.mc.player.getPos().z, true, IMinecraft.mc.player.horizontalCollision));
                        }
                    }
                    IMinecraft.mc.player.setPosition(IMinecraft.mc.player.getPos().x, IMinecraft.mc.player.getPos().y + dist, IMinecraft.mc.player.getPos().z);
                    log("teleported");
                }
            } catch (NumberFormatException ignored) {
                log("tp failed");
            }
        } else if (command.equalsIgnoreCase("session")) {
            if (IMinecraft.mc.getNetworkHandler() != null)
                log(String.valueOf(IMinecraft.mc.getNetworkHandler().getSessionId()));
            else log("Session is null");
        } else {
            log("Unknown command: " + command);
        }
    }

    private void log(String log) {
        commandLogs.add(log);
        if (commandLogs.size() > 50)
            commandLogs.removeFirst();
    }

    private static void renderModuleSettings(Module module) {
        boolean isEnabled = module.tempEnabled;
        if (ImGui.checkbox("Toggle Module##" + module.moduleName, isEnabled)) {
            module.toggle();
        }

        String shownKey;
        ImString currentKey = new ImString(String.valueOf(module.key).toUpperCase(), 256);
        if (module.key != GLFW.GLFW_KEY_UNKNOWN)
            shownKey = GLFW.glfwGetKeyName(module.key, GLFW.glfwGetKeyScancode(module.key));
        else shownKey = "NONE";
        if (shownKey == null)
            shownKey = keyMap.getOrDefault(module.key, "Unknown");
        if (ImGui.inputText("KeyBind (" + shownKey.toUpperCase() + ")##" + module.moduleName, currentKey)) {
            try {
                module.key = InputUtil.fromTranslationKey("key.keyboard." + currentKey.get().toLowerCase()).getCode();
            } catch (NumberFormatException ignored) {
                module.key = GLFW.GLFW_KEY_UNKNOWN;
            }
        }

        List<Field> settings = ModuleManager.getSettings(module);

        if (!settings.isEmpty()) {
            ImGui.separator();
            ImGui.indent();
        }

        for (Field setting : settings) {
            setting.setAccessible(true);
            try {
                Object settingInstance = setting.get(module);
                if (settingInstance instanceof SettingValue<?> settingValue && settingValue.canDisplay.canDisplay()) {
                    renderSetting(settingValue, module.moduleName);
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }

        if (!settings.isEmpty()) {
            ImGui.unindent();
            ImGui.separator();
            ImGui.spacing();
        }
    }

    private static void renderSetting(SettingValue<?> settingValue, String moduleName) {
        switch (settingValue) {
            case BooleanValue booleanValue -> {
                if (ImGui.checkbox(booleanValue.name + "##" + moduleName, booleanValue.get())) {
                    booleanValue.set(!booleanValue.get());
                }
            }
            case TextValue textValue -> {
                ImString currentText = new ImString(textValue.get(), 256);
                if (ImGui.inputText(textValue.name + "##" + moduleName, currentText)) {
                    textValue.set(currentText.get());
                }
            }
            case ListValue listValue -> {
                ImInt selectedIndex = new ImInt(Arrays.asList(listValue.values).indexOf(listValue.get()));
                if (ImGui.combo(listValue.name + "##" + moduleName, selectedIndex, listValue.values)) {
                    listValue.set(listValue.values[selectedIndex.get()]);
                }
            }
            case FloatValue floatValue -> {
                float[] currentFloat = {floatValue.value};
                if (ImGui.sliderFloat(floatValue.name + "##" + moduleName, currentFloat, floatValue.minimum, floatValue.maximum, "%.1f")) {
                    floatValue.set(currentFloat[0]);
                }
            }
            case IntValue intValue -> {
                int[] currentInt = {intValue.value};
                if (ImGui.sliderInt(intValue.name + "##" + moduleName, currentInt, intValue.minimum, intValue.maximum)) {
                    intValue.set(currentInt[0]);
                }
            }
            case KeyBindValue keyBindValue -> {
                String shownKey;
                ImString currentKey = new ImString(String.valueOf(keyBindValue.get()).toUpperCase(), 256);
                if (keyBindValue.get() != GLFW.GLFW_KEY_UNKNOWN)
                    shownKey = GLFW.glfwGetKeyName(keyBindValue.get(), GLFW.glfwGetKeyScancode(keyBindValue.get()));
                else shownKey = "NONE";
                if (shownKey == null)
                    shownKey = keyMap.getOrDefault(keyBindValue.get(), "Unknown");
                if (ImGui.inputText("KeyBind (" + shownKey.toUpperCase() + ")##" + moduleName, currentKey)) {
                    try {
                        int newKey = InputUtil.fromTranslationKey("key.keyboard." + currentKey.get().toLowerCase()).getCode();
                        keyBindValue.set(newKey);
                    } catch (NumberFormatException ignored) {
                        keyBindValue.set(GLFW.GLFW_KEY_UNKNOWN);
                    }
                }
            }
            default -> throw new IllegalStateException("Unexpected value: " + settingValue);
        }
    }

    private static final Map<Integer, String> keyMap = Map.<Integer, String>ofEntries(
            Map.entry(GLFW.GLFW_KEY_UNKNOWN, "NONE"),
            Map.entry(GLFW.GLFW_KEY_SPACE, "Space"),
            Map.entry(GLFW.GLFW_KEY_SLASH, "NONE"),
            Map.entry(GLFW.GLFW_KEY_ESCAPE, "Escape"),
            Map.entry(GLFW.GLFW_KEY_ENTER, "Enter"),
            Map.entry(GLFW.GLFW_KEY_TAB, "Tab"),
            Map.entry(GLFW.GLFW_KEY_BACKSPACE, "Backspace"),
            Map.entry(GLFW.GLFW_KEY_INSERT, "Insert"),
            Map.entry(GLFW.GLFW_KEY_DELETE, "NONE"),
            Map.entry(GLFW.GLFW_KEY_RIGHT, "Right"),
            Map.entry(GLFW.GLFW_KEY_LEFT, "Left"),
            Map.entry(GLFW.GLFW_KEY_DOWN, "Down"),
            Map.entry(GLFW.GLFW_KEY_UP, "Up"),
            Map.entry(GLFW.GLFW_KEY_PAGE_UP, "Page Up"),
            Map.entry(GLFW.GLFW_KEY_PAGE_DOWN, "Page Down"),
            Map.entry(GLFW.GLFW_KEY_HOME, "Home"),
            Map.entry(GLFW.GLFW_KEY_END, "End"),
            Map.entry(GLFW.GLFW_KEY_CAPS_LOCK, "Caps Lock"),
            Map.entry(GLFW.GLFW_KEY_SCROLL_LOCK, "Scroll Lock"),
            Map.entry(GLFW.GLFW_KEY_NUM_LOCK, "Num Lock"),
            Map.entry(GLFW.GLFW_KEY_PRINT_SCREEN, "Print Screen"),
            Map.entry(GLFW.GLFW_KEY_PAUSE, "Pause"),
            Map.entry(GLFW.GLFW_KEY_F1, "F1"),
            Map.entry(GLFW.GLFW_KEY_F2, "F2"),
            Map.entry(GLFW.GLFW_KEY_F3, "F3"),
            Map.entry(GLFW.GLFW_KEY_F4, "F4"),
            Map.entry(GLFW.GLFW_KEY_F5, "F5"),
            Map.entry(GLFW.GLFW_KEY_F6, "F6"),
            Map.entry(GLFW.GLFW_KEY_F7, "F7"),
            Map.entry(GLFW.GLFW_KEY_F8, "F8"),
            Map.entry(GLFW.GLFW_KEY_F9, "F9"),
            Map.entry(GLFW.GLFW_KEY_F10, "F10"),
            Map.entry(GLFW.GLFW_KEY_F11, "F11"),
            Map.entry(GLFW.GLFW_KEY_F12, "F12"),
            Map.entry(GLFW.GLFW_KEY_F13, "F13"),
            Map.entry(GLFW.GLFW_KEY_F14, "F14"),
            Map.entry(GLFW.GLFW_KEY_F15, "F15"),
            Map.entry(GLFW.GLFW_KEY_F16, "F16"),
            Map.entry(GLFW.GLFW_KEY_F17, "F17"),
            Map.entry(GLFW.GLFW_KEY_F18, "F18"),
            Map.entry(GLFW.GLFW_KEY_F19, "F19"),
            Map.entry(GLFW.GLFW_KEY_F20, "F20"),
            Map.entry(GLFW.GLFW_KEY_F21, "F21"),
            Map.entry(GLFW.GLFW_KEY_F22, "F22"),
            Map.entry(GLFW.GLFW_KEY_F23, "F23"),
            Map.entry(GLFW.GLFW_KEY_F24, "F24"),
            Map.entry(GLFW.GLFW_KEY_F25, "F25"),
            Map.entry(GLFW.GLFW_KEY_KP_0, "Keypad 0"),
            Map.entry(GLFW.GLFW_KEY_KP_1, "Keypad 1"),
            Map.entry(GLFW.GLFW_KEY_KP_2, "Keypad 2"),
            Map.entry(GLFW.GLFW_KEY_KP_3, "Keypad 3"),
            Map.entry(GLFW.GLFW_KEY_KP_4, "Keypad 4"),
            Map.entry(GLFW.GLFW_KEY_KP_5, "Keypad 5"),
            Map.entry(GLFW.GLFW_KEY_KP_6, "Keypad 6"),
            Map.entry(GLFW.GLFW_KEY_KP_7, "Keypad 7"),
            Map.entry(GLFW.GLFW_KEY_KP_8, "Keypad 8"),
            Map.entry(GLFW.GLFW_KEY_KP_9, "Keypad 9"),
            Map.entry(GLFW.GLFW_KEY_KP_DECIMAL, "Keypad ."),
            Map.entry(GLFW.GLFW_KEY_KP_DIVIDE, "Keypad /"),
            Map.entry(GLFW.GLFW_KEY_KP_MULTIPLY, "Keypad *"),
            Map.entry(GLFW.GLFW_KEY_KP_SUBTRACT, "Keypad -"),
            Map.entry(GLFW.GLFW_KEY_KP_ADD, "Keypad +"),
            Map.entry(GLFW.GLFW_KEY_KP_ENTER, "Keypad Enter"),
            Map.entry(GLFW.GLFW_KEY_KP_EQUAL, "Keypad ="),
            Map.entry(GLFW.GLFW_KEY_LEFT_SHIFT, "Left Shift"),
            Map.entry(GLFW.GLFW_KEY_LEFT_CONTROL, "Left Control"),
            Map.entry(GLFW.GLFW_KEY_LEFT_ALT, "Left Alt"),
            Map.entry(GLFW.GLFW_KEY_LEFT_SUPER, "Left Super"),
            Map.entry(GLFW.GLFW_KEY_RIGHT_SHIFT, "Right Shift"),
            Map.entry(GLFW.GLFW_KEY_RIGHT_CONTROL, "Right Control"),
            Map.entry(GLFW.GLFW_KEY_RIGHT_ALT, "Right Alt"),
            Map.entry(GLFW.GLFW_KEY_RIGHT_SUPER, "Right Super"),
            Map.entry(GLFW.GLFW_KEY_MENU, "Menu")
    );
}