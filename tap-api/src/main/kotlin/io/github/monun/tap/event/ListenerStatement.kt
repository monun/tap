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

import com.google.common.collect.ImmutableList
import com.google.common.reflect.TypeToken
import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.plugin.EventExecutor
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import java.util.*

@Suppress("unused")
class ListenerStatement(val listenerClass: Class<*>, handlerStatements: ArrayList<HandlerStatement>) {
    val handlerStatements: ImmutableList<HandlerStatement> = ImmutableList.copyOf(handlerStatements)

    companion object {
        private val STATEMENTS: MutableMap<Class<*>, ListenerStatement> = WeakHashMap()

        @JvmStatic
        fun getOrCreate(listenerClass: Class<*>): ListenerStatement {
            return STATEMENTS.computeIfAbsent(listenerClass) {
                val mod = listenerClass.modifiers

                require(Modifier.isPublic(mod)) { "EntityListener modifier must be public" }

                val handlerStatements = ArrayList<HandlerStatement>()
                val methods = listenerClass.methods

                @Suppress("UnstableApiUsage")
                val supers = TypeToken.of(listenerClass).types.rawTypes()

                for (method in methods) {
                    for (superClass in supers) {
                        if (!Listener::class.java.isAssignableFrom(superClass))
                            break

                        try {
                            val real = superClass.getDeclaredMethod(method.name, *method.parameterTypes)

                            if (real.isAnnotationPresent(EventHandler::class.java)) {
                                try {
                                    handlerStatements.add(createHandlerStatement(method))
                                } catch (e: Exception) {
                                    throw IllegalArgumentException("Failed to create HandlerStatement for $real")
                                }

                                break
                            }
                        } catch (ignored: NoSuchMethodException) {
                        } catch (ignored: SecurityException) {
                        }
                    }
                }
                ListenerStatement(listenerClass, handlerStatements)
            }
        }

        private fun createHandlerStatement(method: Method): HandlerStatement {
            val parameterTypes = method.parameterTypes

            require(parameterTypes.size == 1) { "EntityHandler methods must require a single argument: $method" }

            val eventClass = parameterTypes[0]

            require(Event::class.java.isAssignableFrom(eventClass)) { "'${eventClass.name}' is not event class: $method" }

            val handler = method.getAnnotation(EventHandler::class.java)
            val registrationClass = EventTools.getRegistrationClass(eventClass)

            val targetEntity = method.getAnnotation(TargetEntity::class.java)
            val provider = if (targetEntity == null) {
                EventTools.findDefaultProvider(eventClass)
            } else {
                EventTools.getOrCreateCustomProvide(targetEntity.value.java)
            }

            val executor = EventExecutor.create(method, eventClass.asSubclass(Event::class.java))

            return HandlerStatement(eventClass, registrationClass, provider, handler.priority, handler.ignoreCancelled, executor)
        }
    }
}
