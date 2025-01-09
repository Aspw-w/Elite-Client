package xyz.breadloaf.imguimc.screen;

import com.instrumentalist.elite.hacks.ModuleManager;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class EmptyScreen extends Screen {

    public EmptyScreen() {
        super(Text.literal("EmptyScreen"));
    }

    @Override
    public void render(DrawContext drawContext, int mouseX, int mouseY, float delta) {
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void close() {
        ModuleManager.pullDebugScreen();
    }
}
