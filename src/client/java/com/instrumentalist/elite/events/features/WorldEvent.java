package com.instrumentalist.elite.events.features;

import com.instrumentalist.elite.events.EventArgument;
import com.instrumentalist.elite.events.EventListener;

import java.util.Objects;

public class WorldEvent extends EventArgument {

    @Override
    public void call(EventListener listener) {
        Objects.requireNonNull(listener).onWorld(this);
    }
}
