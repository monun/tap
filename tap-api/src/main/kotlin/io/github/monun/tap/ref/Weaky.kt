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

package io.github.monun.tap.ref

import org.jetbrains.annotations.NotNull
import java.lang.ref.ReferenceQueue
import java.lang.ref.WeakReference
import kotlin.reflect.KProperty

class Weaky<T> : WeakReference<T> {
    internal constructor(referent: T) : super(referent)
    internal constructor(
        referent: T, q: ReferenceQueue<in T>
    ) : super(referent, q)

    @NotNull
    override fun get(): T {
        return super.get()
            ?: throw IllegalStateException("Cannot get reference as it has already been Garbage Collected")
    }

    override fun hashCode(): Int {
        return get().hashCode()
    }

    override fun equals(other: Any?): Boolean {
        return get() == other
    }

    override fun toString(): String {
        return get().toString()
    }
}

fun <T> weaky(referent: T) = Weaky(referent)

fun <T> weaky(referent: T, queue: ReferenceQueue<in T>) = Weaky(referent, queue)

operator fun <T> Weaky<T>.getValue(thisRef: Any?, property: KProperty<*>): T {
    return get()
}
