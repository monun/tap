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

package com.github.monun.tap.event

import com.google.common.collect.ImmutableList
import com.google.common.reflect.TypeToken
import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.plugin.EventExecutor
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import java.util.ArrayList
import java.util.WeakHashMap

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
