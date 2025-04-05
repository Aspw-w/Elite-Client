package com.instrumentalist.elite.hacks.features.world;

import com.instrumentalist.elite.events.features.MotionEvent;
import com.instrumentalist.elite.events.features.ReceivedPacketEvent;
import com.instrumentalist.elite.events.features.RenderEvent;
import com.instrumentalist.elite.events.features.UpdateEvent;
import com.instrumentalist.elite.hacks.Module;
import com.instrumentalist.elite.hacks.ModuleCategory;
import com.instrumentalist.elite.utils.ChatUtil;
import com.instrumentalist.elite.utils.IMinecraft;
import com.instrumentalist.elite.utils.render.RegionPos;
import com.instrumentalist.elite.utils.render.RenderUtil;
import com.instrumentalist.elite.utils.simulator.PredictUtil;
import com.instrumentalist.elite.utils.value.BooleanValue;
import com.instrumentalist.elite.utils.value.FloatValue;
import com.instrumentalist.elite.utils.value.IntValue;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket;
import net.minecraft.network.packet.s2c.play.ParticleS2CPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

import java.util.List;

public class Predicter extends Module {

    public Predicter() {
        super("Predicter", ModuleCategory.World, GLFW.GLFW_KEY_UNKNOWN, false, true);
    }

    private final IntValue tick = new IntValue("Tick", 5, 1, 20);

    @Override
    public void onDisable() {
    }

    @Override
    public void onEnable() {
    }

    @Override
    public void onRender(RenderEvent event) {
        if (IMinecraft.mc.player == null) return;

        List<Vec3d> paths = PredictUtil.predict(tick.get());

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glDisable(GL11.GL_DEPTH_TEST);

        MatrixStack matrixStack = event.matrix;
        RegionPos region = RegionPos.of(new BlockPos(0, 0, 0));

        matrixStack.push();

        RenderUtil.INSTANCE.applyRegionalRenderOffset(matrixStack, region);

        Vec3d nextRenderStartPath = IMinecraft.mc.player.getPos();

        for (Vec3d path : paths) {
            RenderUtil.INSTANCE.drawLine(nextRenderStartPath, path, matrixStack);
            nextRenderStartPath = path;
        }

        matrixStack.pop();

        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDisable(GL11.GL_BLEND);
    }
}
