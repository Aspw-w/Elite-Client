package com.instrumentalist.elite.events

abstract class EventArgument protected constructor() {
    var isCancelled = false
        private set

    fun cancel() {
        isCancelled = true
    }

    abstract fun call(listener: EventListener)
}