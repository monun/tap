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

package com.github.monun.tap.hash


infix fun Int.pair(least: Int): Long {
    return (toLong() and 0xFFFFFFFFL) or ((least.toLong() and 0xFFFFFFFFL) shl 32)
}

fun Long.mostInt() = (this ushr 32 and 0xFFFFFFFFL).toInt()

fun Long.leastInt() = (this and 0xFFFFFFFFL).toInt()