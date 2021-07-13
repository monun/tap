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

import com.google.common.collect.MapMaker
import io.github.monun.tap.event.EventTools.getRegistrationClass
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
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
        require(entity.isValid || (entity is Player && entity.isOnline)) { "Invalid entity: $entity" }

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