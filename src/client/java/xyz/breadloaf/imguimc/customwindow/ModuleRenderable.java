package xyz.breadloaf.imguimc.customwindow;

import com.instrumentalist.elite.Client;
import com.instrumentalist.elite.hacks.Module;
import com.instrumentalist.elite.hacks.ModuleCategory;
import com.instrumentalist.elite.hacks.ModuleManager;
import com.instrumentalist.elite.hacks.features.nulling.PluginsDetector;
import com.instrumentalist.elite.hacks.features.exploit.ServerCrasher;
import com.instrumentalist.elite.hacks.features.player.ChatCommands;
import com.instrumentalist.elite.utils.ChatUtil;
import com.instrumentalist.elite.utils.FileUtil;
import com.instrumentalist.elite.utils.IMinecraft;
import com.instrumentalist.elite.utils.packet.PacketUtil;
import com.instrumentalist.elite.utils.value.*;
import imgui.ImGui;
import imgui.flag.ImGuiCond;
import imgui.flag.ImGuiInputTextFlags;
import imgui.type.ImInt;
import imgui.type.ImString;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.text.Text;
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

    private static ImString searchQuery = new ImString(256);
    public static List<String> commandLogs = new ArrayList<>();
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
        if (!FileUtil.INSTANCE.isLatestClient()) {
            try {
                ImGui.begin("Updates Window", ImGuiCond.Always);

                if (!startUpped)
                    ImGui.setWindowSize(400f, 60f);

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
            } finally {
                ImGui.end();
            }
        }

        try {
            ImGui.begin("Modules Window", ImGuiCond.Always);

            if (!startUpped) {
                ImGui.setWindowSize(420f, 500f);
                ImGui.setWindowPos(520f, 200f);
            }

            if (ImGui.beginTabBar("Module Categories")) {
                try {
                    for (ModuleCategory category : ModuleCategory.values()) {
                        if (category == null) continue;

                        if (ImGui.beginTabItem(category.name())) {
                            try {
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

                    if (ImGui.beginTabItem("Search")) {
                        try {
                            ImGui.text("Search Modules:");

                            ImGui.inputText("Search", searchQuery);

                            String query = searchQuery.get().toLowerCase().replace(" ", "");
                            ImGui.spacing();

                            if (!query.isEmpty()) {
                                for (Module module : ModuleManager.modules) {
                                    if (module.moduleCategory == null) continue;

                                    if (module.moduleName.replace(" ", "").toLowerCase().contains(query) || module.moduleCategory.name().toLowerCase().contains(query)) {
                                        if (ImGui.collapsingHeader(module.moduleName))
                                            renderModuleSettings(module);
                                    }
                                }
                            } else {
                                for (Module module : ModuleManager.modules) {
                                    if (module.moduleCategory == null) continue;

                                    if (ImGui.collapsingHeader(module.moduleName))
                                        renderModuleSettings(module);
                                }
                            }
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

        try {
            ImGui.begin("Command Window", ImGuiCond.Always);

            if (!startUpped) {
                ImGui.setWindowSize(420f, 400f);
                ImGui.setWindowPos(950f, 200f);
            }

            if (ImGui.beginTabBar("Command Categories")) {
                try {
                    if (ImGui.beginTabItem("Command")) {
                        try {
                            ImGui.text("Command Execution");

                            ImGui.beginChild("LogWindow", 0, 250, true);

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
                                executeCommand(commandInput.get(), false);
                                commandInput.set("");
                                ImGui.setKeyboardFocusHere(-1);
                            }
                        } finally {
                            ImGui.endTabItem();
                        }
                    }

                    if (ImGui.beginTabItem("Multi Play")) {
                        try {
                            if (ImGui.button("Force disconnect from server")) {
                                if (IMinecraft.mc.world != null)
                                    IMinecraft.mc.world.disconnect();
                            }

                            ImGui.text("Player List");
                            ImGui.beginChild("PlayerList", 0, 250, true);

                            if (IMinecraft.mc.world != null && IMinecraft.mc.getNetworkHandler() != null) {
                                List<AbstractClientPlayerEntity> sortedPlayerEntities = IMinecraft.mc.world.getPlayers().stream().sorted(Comparator.comparing(playerEntity -> playerEntity.getName().getString(), String.CASE_INSENSITIVE_ORDER)).toList();
                                for (AbstractClientPlayerEntity playerEntity : sortedPlayerEntities) {
                                    PlayerListEntry entry = IMinecraft.mc.getNetworkHandler().getPlayerListEntry(playerEntity.getUuid());
                                    String showString = entry != null && playerEntity instanceof ClientPlayerEntity ? "[You] Name: " + playerEntity.getName().getString() + ", Ping: " + entry.getLatency() : entry != null ? "Name: " + playerEntity.getName().getString() + ", Ping: " + entry.getLatency() : "Name: " + playerEntity.getName().getString() + ", Ping: null";
                                    if (ImGui.collapsingHeader(showString)) {
                                        ImGui.separator();
                                        ImGui.indent();

                                        if (ImGui.button("Kill##"))
                                            IMinecraft.mc.getNetworkHandler().sendChatCommand("kill " + playerEntity.getName().getString());
                                        if (ImGui.button("Teleport##"))
                                            IMinecraft.mc.getNetworkHandler().sendChatCommand("tp " + playerEntity.getName().getString());
                                        if (ImGui.button("Crash##"))
                                            IMinecraft.mc.getNetworkHandler().sendChatCommand("execute at " + playerEntity.getName().getString() + " run particle minecraft:explosion ~ ~ ~ 0.1 0.1 0.1 0.01 100000000 force");

                                        ImGui.unindent();
                                        ImGui.separator();
                                        ImGui.spacing();
                                    }
                                }
                            }

                            ImGui.endChild();
                        } finally {
                            ImGui.endTabItem();
                        }
                    }

                    if (ImGui.beginTabItem("Credits")) {
                        try {
                            ImGui.text("Made by Aspw and Noah");
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

        try {
            ImGui.begin("Configs Window", ImGuiCond.Always);

            if (!startUpped) {
                ImGui.setWindowSize(420f, 400f);
                ImGui.setWindowPos(1380f, 200f);
                startUpped = true;
            }

            if (ImGui.beginTabBar("Config Categories")) {
                try {
                    if (ImGui.beginTabItem("Module Configs")) {
                        try {
                            List<Path> moduleConfigs = FileUtil.INSTANCE.getModuleFiles();
                            if (!moduleConfigs.isEmpty()) {
                                ImGui.text("Available Module Configs:");
                                ImGui.beginChild("ModuleConfigsList", 0, 250, true);

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
                            if (ImGui.inputText("New Config Name", newConfigName, ImGuiInputTextFlags.EnterReturnsTrue)) {
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
                            List<Path> bindConfigs = FileUtil.INSTANCE.getBindFiles();
                            if (!bindConfigs.isEmpty()) {
                                ImGui.text("Available Bind Configs:");
                                ImGui.beginChild("BindConfigsList", 0, 250, true);

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
                            if (ImGui.inputText("New Config Name", newConfigName, ImGuiInputTextFlags.EnterReturnsTrue)) {
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
                            List<String> moduleConfigs = FileUtil.INSTANCE.getOnlineCfgs();
                            if (!moduleConfigs.isEmpty()) {
                                ImGui.text("Available Online Configs:");
                                ImGui.beginChild("OnlineConfigsList", 0, 250, true);

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
                } finally {
                    ImGui.endTabBar();
                }
            }
        } finally {
            ImGui.end();
        }
    }

    private static void showLog(Boolean chat, String message) {
        if (chat) {
            ChatUtil.printChat(message);
            IMinecraft.mc.inGameHud.getChatHud().addToMessageHistory(message);
        } else log(message);
    }

    public static void executeCommand(String command, Boolean chatMode) {
        if (command.isBlank()) {
            if (chatMode)
                showLog(true, ChatCommands.Companion.getPrefix().get() + "help to show every commands");
            return;
        }

        if (command.equalsIgnoreCase("help") || command.equalsIgnoreCase("commands")) {
            showLog(chatMode, "help/commands -> show every commands");
            showLog(chatMode, "t/toggle <module> -> toggle module");
            showLog(chatMode, "bind <module> <key> -> bind module");
            showLog(chatMode, "crash -> crash the server");
            showLog(chatMode, "pl/plugins -> detect server plugins");
            showLog(chatMode, "transaction -> debug transaction packets (x10)");
            showLog(chatMode, "session -> show your current session id");
            showLog(chatMode, "vclip <height> -> teleport up from current position");
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
                        showLog(chatMode, "Enabled " + module.moduleName);
                    else showLog(chatMode, "Disabled " + module.moduleName);
                    return;
                }
            }

            showLog(chatMode, "Module " + moduleName + " was not found");
        } else if (command.equalsIgnoreCase("crash")) {
            showLog(chatMode, "Crashing server...");
            for (Module module : ModuleManager.modules) {
                if (module instanceof ServerCrasher && !module.tempEnabled)
                    module.toggle();
            }
        } else if (command.equalsIgnoreCase("transaction")) {
            showLog(chatMode, "Logging transactions...");
            ModuleManager.gettingTransactions = true;
        } else if (command.equalsIgnoreCase("pl") || command.equalsIgnoreCase("plugins")) {
            showLog(chatMode, "Detecting plugins...");
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
                            showLog(chatMode, "Bound " + newKey + " key to " + module.moduleName + " module");
                        } catch (NumberFormatException ignored) {
                            module.key = GLFW.GLFW_KEY_UNKNOWN;
                            showLog(chatMode, "Unbound " + module.moduleName + " module");
                        }
                        return;
                    }
                }
            }

            showLog(chatMode, "Module " + moduleName + " was not found");
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
                    showLog(chatMode, "teleported");
                }
            } catch (NumberFormatException ignored) {
                showLog(chatMode, "tp failed");
            }
        } else if (command.equalsIgnoreCase("session")) {
            if (IMinecraft.mc.getNetworkHandler() != null)
                showLog(chatMode, String.valueOf(IMinecraft.mc.getNetworkHandler().getSessionId()));
            else showLog(chatMode, "Session is null");
        } else {
            showLog(chatMode, "Unknown command: " + command);
        }
    }

    private static void log(String log) {
        commandLogs.add(log);
        if (commandLogs.size() > 50)
            commandLogs.removeFirst();
    }

    private static void renderModuleSettings(Module module) {
        boolean isEnabled = module.tempEnabled;
        if (ImGui.checkbox("Toggle Module##" + module.moduleName, isEnabled)) {
            module.toggle();
        }

        if (ImGui.checkbox("Show on array##", module.showOnArray))
            module.showOnArray = !module.showOnArray;

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

        List<SettingValue<?>> settings = ModuleManager.getSettings(module);

        if (!settings.isEmpty()) {
            ImGui.separator();
            ImGui.indent();
        }

        for (SettingValue<?> settingValue : settings) {
            if (settingValue.canDisplay.canDisplay()) {
                renderSetting(settingValue, module.moduleName);
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
            case ColorValue colorValue -> {
                float[] currentColor = {
                        colorValue.value.getRed() / 255f,
                        colorValue.value.getGreen() / 255f,
                        colorValue.value.getBlue() / 255f
                };
                ImGui.setNextItemWidth(150);
                if (ImGui.colorPicker3(colorValue.name + "##" + moduleName, currentColor)) {
                    Color newColor = new Color(
                            (int) (currentColor[0] * 255),
                            (int) (currentColor[1] * 255),
                            (int) (currentColor[2] * 255)
                    );
                    colorValue.set(newColor);
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
                if (ImGui.inputText(keyBindValue.name + " (" + shownKey.toUpperCase() + ")##" + moduleName, currentKey)) {
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