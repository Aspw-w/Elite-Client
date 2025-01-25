package com.instrumentalist.elite.events;

import com.instrumentalist.elite.events.features.*;

public interface EventListener {
    default void onKey(KeyboardEvent event) {
    }

    default void onUpdate(UpdateEvent event) {
    }

    default void onSendPacket(SendPacketEvent event) {
    }

    default void onReceivedPacket(ReceivedPacketEvent event) {
    }

    default void onAttack(AttackEvent event) {
    }

    default void onRenderHud(RenderHudEvent event) {
    }

    default void onHandleInput(HandleInputEvent event) {
    }

    default void onMotion(MotionEvent event) {
    }

    default void onWorld(WorldEvent event) {
    }

    default void onTick(TickEvent event) {
    }

    default void onRender(RenderEvent event) {
    }

    default void onMouseScroll(MouseScrollEvent event) {
    }

    default void onModifyPacket(ModifyPacketEvent event) {
    }
}