package com.github.noonmaru.tap.event

import org.bukkit.event.Listener
import java.util.concurrent.ConcurrentHashMap

class EventEntity {
    private val slots = ConcurrentHashMap<Class<*>, EntityHandlerList>()

    fun register(registeredEntityListener: RegisteredEntityListener) {
        for (handler in registeredEntityListener.handlers) {
            val statement = handler.statement
            val handlerList = slots.computeIfAbsent(statement.registrationClass) { EntityHandlerList() }
            handlerList.register(handler)
        }
    }

    fun unregister(registeredListener: RegisteredEntityListener) {
        for (handler in registeredListener.handlers) {
            handler.remove()
            val handlerList = slots[handler.statement.registrationClass]
            handlerList?.unregister(handler)
        }
    }

    fun unregister(statement: ListenerStatement, listener: Listener) {
        for (handlerStatement in statement.handlerStatements) {
            val handlerList = slots[handlerStatement.registrationClass]
            handlerList?.unregister(listener)
        }
    }

    fun getHandlerList(eventClass: Class<*>): EntityHandlerList? {
        return slots[eventClass]
    }

    fun unregisterAll() {
        for (handlerList in slots.values) {
            handlerList.unregisterAll()
        }

        slots.clear()
    }
}
