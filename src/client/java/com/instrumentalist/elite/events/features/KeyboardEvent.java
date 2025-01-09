package com.instrumentalist.elite.events.features;

import com.instrumentalist.elite.events.EventArgument;
import com.instrumentalist.elite.events.EventListener;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class KeyboardEvent extends EventArgument {

    public final int key, action;

    public KeyboardEvent(int key, int action) {
        this.key = key;
        this.action = action;
    }

    @Override
    public void call(@Nullable EventListener listener) {
        Objects.requireNonNull(listener).onKey(this);
    }
}
