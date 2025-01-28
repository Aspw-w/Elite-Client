package com.instrumentalist.elite.hacks.features.render

import com.instrumentalist.elite.events.features.*
import com.instrumentalist.elite.hacks.Module
import com.instrumentalist.elite.hacks.ModuleCategory
import com.instrumentalist.elite.utils.IMinecraft
import com.instrumentalist.elite.utils.move.MovementUtil
import com.instrumentalist.elite.utils.packet.PacketUtil
import com.instrumentalist.elite.utils.render.RegionPos
import com.instrumentalist.elite.utils.render.RenderUtil
import com.instrumentalist.elite.utils.rotation.RotationUtil
import com.instrumentalist.elite.utils.value.FloatValue
import com.mojang.blaze3d.systems.RenderSystem
import net.minecraft.client.util.InputUtil
import net.minecraft.entity.Entity
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket
import net.minecraft.util.PlayerInput
import net.minecraft.util.math.Box
import net.minecraft.util.math.Vec3d
import org.joml.Vector3f
import org.lwjgl.glfw.GLFW
import org.lwjgl.opengl.GL11

class Freecam : Module("Freecam", ModuleCategory.Render, GLFW.GLFW_KEY_F8, false, true) {
    companion object {
        @Setting
        private val horizontalSpeed = FloatValue("Horizontal Speed", 2f, 0.1f, 4f)

        @Setting
        private val verticalSpeed = FloatValue("Vertical Speed", 1f, 0.1f, 4f)

        var canFly = false
    }

    private var oldPos: Vec3d? = null
    private var oldYaw: Float? = null
    private var oldPitch: Float? = null
    private var oldOnGround: Boolean? = null

    override fun onDisable() {
        if (IMinecraft.mc.player != null) {
            MovementUtil.stopMoving()
            MovementUtil.setVelocityY(0.0)

            if (oldPos != null)
                IMinecraft.mc.player!!.setPosition(oldPos)
            if (oldYaw != null)
                IMinecraft.mc.player!!.yaw = oldYaw!!
            if (oldPitch != null)
                IMinecraft.mc.player!!.pitch = oldPitch!!
            if (oldOnGround != null)
                IMinecraft.mc.player!!.isOnGround = oldOnGround!!

            IMinecraft.mc.player!!.abilities.flying = false
        }

        canFly = false
        oldPos = null
        oldYaw = null
        oldPitch = null
        oldOnGround = null
    }

    override fun onEnable() {
        if (IMinecraft.mc.player == null) return

        oldPos = IMinecraft.mc.player!!.pos
        oldYaw = if (RotationUtil.currentYaw != null) RotationUtil.currentYaw else IMinecraft.mc.player!!.yaw
        oldPitch = if (RotationUtil.currentPitch != null) RotationUtil.currentPitch else IMinecraft.mc.player!!.pitch
        oldOnGround = IMinecraft.mc.player!!.isOnGround

        MovementUtil.setVelocityY(0.5)
    }

    override fun onWorld(event: WorldEvent) {
        this@Freecam.toggle()
    }

    override fun onMotion(event: MotionEvent) {
        if (IMinecraft.mc.player == null) return

        IMinecraft.mc.player!!.prevStrideDistance = 0f
        IMinecraft.mc.player!!.strideDistance = 0f

        IMinecraft.mc.player!!.abilities.flying = canFly
    }

    override fun onUpdate(event: UpdateEvent) {
        if (IMinecraft.mc.player == null) return

        var yMotion = 0f

        IMinecraft.mc.player!!.input.playerInput = PlayerInput(
            IMinecraft.mc.player!!.input.playerInput.forward,
            IMinecraft.mc.player!!.input.playerInput.backward,
            IMinecraft.mc.player!!.input.playerInput.left,
            IMinecraft.mc.player!!.input.playerInput.right,
            IMinecraft.mc.player!!.input.playerInput.jump,
            false,
            IMinecraft.mc.player!!.input.playerInput.sprint
        )

        IMinecraft.mc.player!!.isSprinting = false
        IMinecraft.mc.player!!.noClip = true

        if (InputUtil.isKeyPressed(IMinecraft.mc.window.handle, InputUtil.fromTranslationKey(IMinecraft.mc.options.jumpKey.boundKeyTranslationKey).code))
            yMotion += verticalSpeed.get()

        if (InputUtil.isKeyPressed(IMinecraft.mc.window.handle, InputUtil.fromTranslationKey(IMinecraft.mc.options.sneakKey.boundKeyTranslationKey).code))
            yMotion -= verticalSpeed.get()

        if (IMinecraft.mc.player!!.velocity.y <= 0)
            canFly = true

        if (canFly) {
            IMinecraft.mc.player!!.isOnGround = true
            MovementUtil.setVelocityY(yMotion.toDouble())
            MovementUtil.smoothStrafe(horizontalSpeed.get())
        } else MovementUtil.smoothStrafe(0.5f)
    }

    override fun onRender(event: RenderEvent) {
        if (IMinecraft.mc.player == null || IMinecraft.mc.world == null || IMinecraft.mc.player!!.age < 100f || oldPos == null) return

        GL11.glEnable(GL11.GL_BLEND)
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
        GL11.glDisable(GL11.GL_DEPTH_TEST)

        val matrixStack = event.matrix

        val region: RegionPos = RenderUtil.getCameraRegion()
        val regionVec = region.toVec3d()

        matrixStack.push()
        RenderUtil.applyRegionalRenderOffset(matrixStack, region)

        RenderUtil.renderSingleBox(oldPos!!, matrixStack, regionVec, Vector3f(0.6f, 1.8f, 0.6f))

        matrixStack.pop()

        RenderSystem.setShaderColor(1f, 1f, 1f, 1f)

        GL11.glEnable(GL11.GL_DEPTH_TEST)
        GL11.glDisable(GL11.GL_BLEND)
    }

    override fun onSendPacket(event: SendPacketEvent) {
        if (IMinecraft.mc.player == null) return

        val packet = event.packet

        if (packet is PlayerMoveC2SPacket) {
            if (oldPos != null) {
                packet.x = oldPos!!.x
                packet.y = oldPos!!.y
                packet.z = oldPos!!.z
            } else oldPos = Vec3d(packet.x, packet.y, packet.z)

            if (oldYaw != null)
                packet.yaw = oldYaw!!
            else oldYaw = packet.yaw

            if (oldPitch != null)
                packet.pitch = oldPitch!!
            else oldPitch = packet.pitch

            if (oldOnGround != null)
                packet.onGround = oldOnGround!!
            else oldOnGround = packet.onGround
        }
    }

    override fun onReceivedPacket(event: ReceivedPacketEvent) {
        if (IMinecraft.mc.player == null) return

        val packet = event.packet

        if (packet is PlayerPositionLookS2CPacket) {
            event.cancel()

            oldPos = packet.change.position
            oldYaw = packet.change.yaw
            oldPitch = packet.change.pitch

            if (oldOnGround == null)
                oldOnGround = true

            PacketUtil.sendPacket(PlayerMoveC2SPacket.Full(oldPos!!.x, oldPos!!.y, oldPos!!.z, oldYaw!!, oldPitch!!, oldOnGround!!, false))
        }
    }

    override fun onTick(event: TickEvent) {
        if (IMinecraft.mc.player == null) return

        IMinecraft.mc.options.jumpKey.isPressed = false
        IMinecraft.mc.options.sneakKey.isPressed = false
    }
}
