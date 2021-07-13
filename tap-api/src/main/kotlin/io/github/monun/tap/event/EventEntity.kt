/*
 * Tap
 * Copyright (C) 2021 Monun
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package io.github.monun.tap.event

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
