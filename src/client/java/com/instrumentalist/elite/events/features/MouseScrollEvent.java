package com.instrumentalist.elite.events.features;

import com.instrumentalist.elite.events.EventArgument;
import com.instrumentalist.elite.events.EventListener;
import net.minecraft.entity.Entity;

import java.util.Objects;

public class MouseScrollEvent extends EventArgument {
    public final double horizontal;
    public final double vertical;

    public MouseScrollEvent(double horizontal, double vertical) {
        this.horizontal = horizontal;
        this.vertical = vertical;
    }

    @Override
    public void call(EventListener listener) {
        Objects.requireNonNull(listener).onMouseScroll(this);
    }
}