/*
 * Copyright (C) 2023 Monun
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package io.github.monun.tap.pdc

import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataType
import java.nio.ByteBuffer
import java.util.*

object ExtraPersistentDataTypes {
    val UUID = createType<ByteArray, UUID>({
        with(ByteBuffer.wrap(it)) {
            UUID(long, long)
        }
    }) {
        ByteBuffer.wrap(ByteArray(16)).apply {
            putLong(it.mostSignificantBits)
            putLong(it.leastSignificantBits)
        }.array()
    }

    val BOOLEAN_ARRAY = createType<ByteArray, BooleanArray>({ array ->
        BooleanArray(array.size) { array[it] != 0.toByte() }
    }) { array ->
        ByteArray(array.size) { if (array[it]) 1 else 0 }
    }
}

inline fun <reified T : Any, reified Z : Any> createType(crossinline fromPrimitive: (T) -> Z, crossinline toPrimitive: (Z) -> T) =
    object : PersistentDataType<T, Z> {
        override fun getPrimitiveType() = T::class.java
        override fun getComplexType() = Z::class.java

        override fun fromPrimitive(primitive: T, context: PersistentDataAdapterContext): Z = fromPrimitive(primitive)

        override fun toPrimitive(complex: Z, context: PersistentDataAdapterContext) = toPrimitive(complex)
    }