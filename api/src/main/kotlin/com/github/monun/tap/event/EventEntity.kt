/*
 * Copyright 2021 Monun
 *
 * Licensed under the Apache License, Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://opensource.org/licenses/gpl-3.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.monun.tap.event

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
