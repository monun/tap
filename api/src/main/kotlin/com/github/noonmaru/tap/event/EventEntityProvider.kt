package com.github.noonmaru.tap.event

import com.github.noonmaru.tap.event.EventTools.getGenericEventType
import org.bukkit.event.Event

class EventEntityProvider internal constructor(val eventClass: Class<*>, val provider: EntityProvider<Event>) {
    internal constructor(provider: EntityProvider<Event>) : this(getGenericEventType(provider.javaClass), provider)
}