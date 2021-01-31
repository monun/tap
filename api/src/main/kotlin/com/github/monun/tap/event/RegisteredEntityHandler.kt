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