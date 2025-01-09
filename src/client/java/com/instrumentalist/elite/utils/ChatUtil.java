package com.instrumentalist.elite.utils;

import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.slf4j.LoggerFactory;

import java.awt.*;

public class ChatUtil {

    public static void showLog(String string) {
        LoggerFactory.getLogger("Elite Logger").info(string);
    }

    public static void printChat(String string) {
        if (IMinecraft.mc.world == null) return;
        IMinecraft.mc.inGameHud.getChatHud().addMessage(Text.literal("> " + string).withColor(Color.YELLOW.getRGB()));
    }

    public static void printModifiedChat(MutableText mutableText) {
        if (IMinecraft.mc.world == null) return;
        IMinecraft.mc.inGameHud.getChatHud().addMessage(mutableText);
    }
}
