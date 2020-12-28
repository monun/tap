/*
 * Copyright (c) 2020 Noonmaru
 *
 *  Licensed under the General Public License, Version 3.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://opensource.org/licenses/gpl-3.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.noonmaru.tap.event

import com.github.noonmaru.tap.event.EventTools.getRegistrationClass
import com.google.common.collect.MapMaker
import org.bukkit.entity.Entity
import org.bukkit.event.Event
import org.bukkit.event.EventPriority
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.plugin.EventExecutor
import org.bukkit.plugin.Plugin

@Suppress("unused")
class EntityEventManager @JvmOverloads constructor(private val plugin: Plugin, private val priority: EventPriority = EventPriority.NORMAL) {
    private val statements = HashMap<Class<*>, ListenerStatement>()

    private val listeners = HashMap<Class<*>, EventListener>()

    private val entities = MapMaker().weakKeys().makeMap<Entity, EventEntity>()

    private val eventExecutor = EventExecutor { listener: Listener, event: Event ->
        (listener as EventListener).onEvent(event)
    }

    fun registerEvents(entity: Entity, listener: Listener): RegisteredEntityListener {
        require(entity.isValid) { "Invalid entity: $entity" }

        val listenerStatement = createRegisteredListenerStatement(listener.javaClass)
        val eventEntity = entities.computeIfAbsent(entity) { EventEntity() }

        val registeredEntityListener = RegisteredEntityListener(eventEntity, listenerStatement, listener)
        eventEntity.register(registeredEntityListener)

        return registeredEntityListener
    }

    private fun createRegisteredListenerStatement(listenerClass: Class<*>): ListenerStatement {
        return statements.computeIfAbsent(listenerClass) { clazz: Class<*> ->
            val statement = ListenerStatement.getOrCreate(clazz)

            for (statementStatement in statement.handlerStatements) {
                registerEvent(statementStatement)
            }

            statement
        }
    }

    private fun registerEvent(statement: HandlerStatement) {
        val registrationClass = statement.registrationClass

        val listener = listeners.computeIfAbsent(registrationClass) { clazz: Class<*> ->
            val newListener = EventListener()
            plugin.server.pluginManager.registerEvent(clazz.asSubclass(Event::class.java), newListener, priority, eventExecutor, plugin, false)

            newListener
        }

        listener.addProvider(statement.provider)
    }

    fun unregisterEvent(entity: Entity, listener: Listener) {
        val eventEntity = entities[entity]

        if (eventEntity != null) {
            val statement = statements[listener.javaClass]

            if (statement != null) {
                eventEntity.unregister(statement, listener)
            }
        }
    }

    fun unregisterAll() {
        for (eventEntity in entities.values) {
            eventEntity.unregisterAll()
        }

        for (listener in listeners.values) {
            HandlerList.unregisterAll(listener)
        }

        entities.clear()
        listeners.clear()
        statements.clear()
    }

    private inner class EventListener : Listener {
        private val providers = LinkedHashSet<EventEntityProvider>()

        private var bake: Array<EventEntityProvider>? = null

        fun onEvent(event: Event) {
            for (provider in getBake()) {
                val eventClass = event.javaClass

                if (provider.eventClass.isAssignableFrom(eventClass)) {
                    val entity = provider.provider.getFrom(event)

                    if (entity != null) {
                        val eventEntity = entities[entity]

                        if (eventEntity != null) {
                            val regClass = getRegistrationClass(eventClass)
                            val handlers = eventEntity.getHandlerList(regClass)

                            handlers?.callEvent(event, provider)
                        }
                    }
                }
            }
        }

        private fun getBake(): Array<EventEntityProvider> {
            val bake = bake
            return bake ?: providers.toTypedArray().also { this.bake = it }
        }

        fun addProvider(provider: EventEntityProvider) {
            providers.add(provider)
            bake = null
        }
    }
}