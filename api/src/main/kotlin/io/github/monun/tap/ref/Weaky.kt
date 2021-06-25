/*
 * Copyright 2021 Monun
 *
 * Licensed under the General Public License, Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://opensource.org/licenses/gpl-3.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package io.github.monun.tap.ref

import java.lang.ref.Reference
import java.lang.ref.WeakReference


interface Weaky<T> : Refery<T>

private class WeakyImpl<T>(initValue: T?, supplier: () -> T) : ReferyImpl<T>(initValue, supplier), Weaky<T> {
    override fun refer(value: T?): Reference<T> = WeakReference(value)
}

fun <T> weaky(initValue: T? = null, supplier: () -> T): Weaky<T> = WeakyImpl(initValue, supplier)

