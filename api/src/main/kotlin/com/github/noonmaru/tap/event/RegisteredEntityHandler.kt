package com.github.noonmaru.tap.event

import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.EventException
import org.bukkit.event.Listener

class RegisteredEntityHandler(val statement: HandlerStatement, val listener: Listener) : Comparable<RegisteredEntityHandler> {
    private val generatedTime = System.currentTimeMillis()

    var isValid = true
        private set

    override operator fun compareTo(other: RegisteredEntityHandler): Int {
        val comp = statement.priority.compareTo(other.statement.priority)
        return if (comp != 0) comp else generatedTime.compareTo(other.generatedTime)
    }

    fun callEvent(event: Event) {
        if (statement.isIgnoreCancelled && event is Cancellable && (event as Cancellable).isCancelled) return

        try {
            statement.executor.execute(listener, event)
        } catch (e: EventException) {
            e.printStackTrace()
        }
    }

    fun remove() {
        isValid = false
    }
}