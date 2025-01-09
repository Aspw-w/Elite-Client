package com.instrumentalist.elite.utils.math;

public final class TickTimer {
    public int tick;

    public TickTimer() {
        this.tick = 0;
    }

    public void update() {
        tick++;
    }

    public void reset() {
        tick = 0;
    }

    public boolean hasTimePassed(final int ticks) {
        return tick >= ticks;
    }
}