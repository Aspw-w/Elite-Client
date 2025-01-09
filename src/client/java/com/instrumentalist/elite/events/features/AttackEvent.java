package com.instrumentalist.elite.events.features;

import com.instrumentalist.elite.events.EventArgument;
import com.instrumentalist.elite.events.EventListener;
import net.minecraft.entity.Entity;

import java.util.Objects;

public class AttackEvent extends EventArgument {
    public final Entity entity;

    public AttackEvent(Entity entity) {
        this.entity = entity;
    }

    @Override
    public void call(EventListener listener) {
        Objects.requireNonNull(listener).onAttack(this);
    }
}