package com.github.noonmaru.tap.event

import com.github.noonmaru.tap.collection.SortedList
import org.bukkit.event.Event
import org.bukkit.event.Listener

class EntityHandlerList {
    private val handlerList = SortedList<RegisteredEntityHandler>()
    private var handlers: Array<RegisteredEntityHandler>? = null

    @Synchronized
    fun register(handler: RegisteredEntityHandler) {
        handlers = null
        handlerList.add(handler)
    }

    @Synchronized
    fun unregister(handler: RegisteredEntityHandler) {
        handlers = null
        handlerList.binaryRemove(handler)
    }

    fun unregister(listener: Listener) {
        handlers = null
        handlerList.removeIf { handler: RegisteredEntityHandler ->
            if (listener === handler.listener) {
                handler.remove()
                return@removeIf true
            }

            false
        }
    }

    @Synchronized
    fun bake() {
        if (handlers != null) return
        handlers = handlerList.toTypedArray()
    }

    private val registeredHandlers: Array<RegisteredEntityHandler>
        get() {
            var handlers = handlers

            while (handlers == null) {
                bake()
                handlers = this.handlers
            }

            return handlers
        }

    fun callEvent(event: Event, provider: EventEntityProvider) {
        for (handler in registeredHandlers) {
            if (provider === handler.statement.provider) {
                handler.callEvent(event)
            }
        }
    }

    fun unregisterAll() {
        for (registeredEntityHandler in handlerList) {
            registeredEntityHandler.remove()
        }

        handlerList.clear()
        handlers = null
    }
}