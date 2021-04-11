/*
 * Copyright 2021 Monun
 *
 * Licensed under the Apache License, Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://opensource.org/licenses/gpl-3.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.monun.tap.ref

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.lang.ref.Reference
import java.lang.ref.WeakReference
import kotlin.reflect.KProperty


interface Refy<T> {
    var value: T?
}

interface Weaky<T> : Refy<T>

abstract class WeakyImpl<T>(initValue: T?, private val supplier: () -> T) : Refy<T> {
    @Suppress("LeakingThis")
    private var ref: Reference<T> = refer(initValue)

    final override var value: T?
        get() = ref.get() ?: supplier()?.also { ref = refer(it) }
        set(value) {
            if (ref.get() !== value) ref = refer(value)
        }

    internal abstract fun refer(value: T?): Reference<T>
}

private class WeakImpl<T>(initValue: T?, supplier: () -> T) : WeakyImpl<T>(initValue, supplier), Weaky<T> {
    override fun refer(value: T?): Reference<T> = WeakReference(value)
}

fun <T> weaky(initValue: T? = null, supplier: () -> T): Weaky<T> = WeakImpl(initValue, supplier)

operator fun <T> Weaky<T>.getValue(thisRef: Any, property: KProperty<*>): T? = value

operator fun <T> Weaky<T>.setValue(thisRef: Any, property: KProperty<*>, value: T?) {
    this.value = value
}