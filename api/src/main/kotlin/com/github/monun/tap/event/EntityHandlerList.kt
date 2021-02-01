/*
 * Copyright 2021 Monun
 *
 * Licensed under the Apache License, Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-3.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.monun.tap.event

import com.github.monun.tap.collection.SortedList
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