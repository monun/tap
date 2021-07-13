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

import io.github.monun.tap.collection.SortedList
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