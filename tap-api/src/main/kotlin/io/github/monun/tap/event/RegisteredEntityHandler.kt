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

import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.EventException
import org.bukkit.event.Listener

class RegisteredEntityHandler(val statement: HandlerStatement, val listener: Listener) : Comparable<RegisteredEntityHandler> {
    private val generatedTime = System.currentTimeMillis()

    var isValid = true
        private set

    override operator fun compareTo(other: RegisteredEntityHandler): Int {
        val comp = statement.priority.compareTo(other.statement.priority)
        return if (comp != 0) comp else generatedTime.compareTo(other.generatedTime)
    }

    fun callEvent(event: Event) {
        if (statement.isIgnoreCancelled && event is Cancellable && (event as Cancellable).isCancelled) return

        try {
            statement.executor.execute(listener, event)
        } catch (e: EventException) {
            e.printStackTrace()
        }
    }

    fun remove() {
        isValid = false
    }
}