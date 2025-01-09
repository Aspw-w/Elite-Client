package com.instrumentalist.elite.events;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class EventManager {

    private final List<EventListener> LISTENER_REGISTRY;

    public EventManager() {
        super();
        this.LISTENER_REGISTRY = new CopyOnWriteArrayList<>();
    }

    public void call(final EventArgument argument) {
        new ArrayList<>(this.LISTENER_REGISTRY).forEach(argument::call);
    }

    public void register(final EventListener listener) {
        this.LISTENER_REGISTRY.add(listener);
    }

    public boolean unregister(final EventListener listener) {
        return this.LISTENER_REGISTRY.remove(listener);
    }
}