package com.instrumentalist.elite.hacks.features.movement.speedmode;

import com.instrumentalist.elite.events.features.MotionEvent;
import com.instrumentalist.elite.events.features.TickEvent;
import com.instrumentalist.elite.events.features.UpdateEvent;

public interface SpeedEvent {
    String getName();
    void onUpdate(UpdateEvent event);
    void onMotion(MotionEvent event);
    void onTick(TickEvent event);
}