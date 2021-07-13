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
import org.bukkit.event.Listener

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