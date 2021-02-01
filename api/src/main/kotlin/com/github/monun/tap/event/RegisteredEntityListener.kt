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

import com.google.common.collect.ImmutableList
import org.bukkit.event.Listener
import java.util.ArrayList

@Suppress("unused", "MemberVisibilityCanBePrivate", "CanBeParameter")
class RegisteredEntityListener(private val eventEntity: EventEntity, val statement: ListenerStatement, val listener: Listener) {
    val handlers: ImmutableList<RegisteredEntityHandler>

    fun unregister() {
        eventEntity.unregister(this)
    }

    init {
        val handlerStatements = statement.handlerStatements
        val handlers = ArrayList<RegisteredEntityHandler>(handlerStatements.size)

        for (handlerStatement in handlerStatements) {
            handlers.add(RegisteredEntityHandler(handlerStatement, listener))
        }

        this.handlers = ImmutableList.copyOf(handlers)
    }
}