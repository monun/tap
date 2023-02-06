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

import org.bukkit.NamespacedKey
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

@Suppress("UNCHECKED_CAST", "KotlinConstantConditions")
fun <T : Any> getPersistentDataType(type: KClass<T>): PersistentDataType<out Any, T> = when (type) {
    Byte::class -> PersistentDataType.BYTE
    Short::class -> PersistentDataType.SHORT
    Int::class -> PersistentDataType.INTEGER
    Long::class -> PersistentDataType.LONG
    Double::class -> PersistentDataType.DOUBLE
    Float::class -> PersistentDataType.FLOAT

    ByteArray::class -> PersistentDataType.BYTE_ARRAY
    IntArray::class -> PersistentDataType.INTEGER_ARRAY

    String::class -> PersistentDataType.STRING

    PersistentDataContainer::class -> PersistentDataType.TAG_CONTAINER
    Array<PersistentDataContainer>::class -> PersistentDataType.TAG_CONTAINER_ARRAY

    UUID::class -> ExtraPersistentDataTypes.UUID
    BooleanArray::class -> ExtraPersistentDataTypes.BOOLEAN_ARRAY

    else -> throw IllegalArgumentException("Failed to find the corresponding persistent data type: $type")
} as PersistentDataType<out Any, T>

inline operator fun <reified T : Any> PersistentDataContainer.getValue(thisRef: Any?, property: KProperty<*>): T? =
    this[property.name.asNamespacedKey, getPersistentDataType(T::class)]

inline operator fun <reified T : Any> PersistentDataContainer.get(name: String): T? = this[name.asNamespacedKey, getPersistentDataType(T::class)]
inline operator fun <reified T : Any> PersistentDataContainer.set(name: String, value: T) {
    this[name.asNamespacedKey, getPersistentDataType(T::class)] = value
}

val String.asNamespacedKey
    get() = NamespacedKey.minecraft(replace("(?<=[a-zA-Z])[A-Z]".toRegex()) { "_${it.value}" }.lowercase())
