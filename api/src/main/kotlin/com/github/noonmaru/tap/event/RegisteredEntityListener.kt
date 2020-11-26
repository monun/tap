package com.github.noonmaru.tap.event

import com.google.common.collect.ImmutableList
import org.bukkit.event.Listener
import java.util.ArrayList

@Suppress("unused", "MemberVisibilityCanBePrivate", "CanBeParameter")
class RegisteredEntityListener(private val eventEntity: EventEntity, val statement: ListenerStatement, val listener: Listener) {
    val handlers: ImmutableList<RegisteredEntityHandler>

    fun unregister() {
        eventEntity.unregister(this)
    }

    init {
        val handlerStatements = statement.handlerStatements
        val handlers = ArrayList<RegisteredEntityHandler>(handlerStatements.size)

        for (handlerStatement in handlerStatements) {
            handlers.add(RegisteredEntityHandler(handlerStatement, listener))
        }

        this.handlers = ImmutableList.copyOf(handlers)
    }
}