package com.instrumentalist.elite.utils.math;

public final class MSTimer {
    private long time = -1L;

    public boolean hasTimePassed(final long MS) {
        return System.currentTimeMillis() >= time + MS;
    }

    public void reset() {
        time = System.currentTimeMillis();
    }
}