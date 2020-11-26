package com.github.noonmaru.tap.event

import org.bukkit.event.EventPriority
import org.bukkit.plugin.EventExecutor

class HandlerStatement(
        val eventClass: Class<*>,
        val registrationClass: Class<*>,
        val provider: EventEntityProvider,
        val priority: EventPriority,
        val isIgnoreCancelled: Boolean,
        val executor: EventExecutor
)