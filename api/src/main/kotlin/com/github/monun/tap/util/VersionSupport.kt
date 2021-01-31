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

package com.github.monun.tap.util

import kotlin.math.max

/**
 * 버전을 비교합니다.
 *
 * 0.1 < 0.1.1
 *
 * 0.1 == 0.01
 */
infix fun String.compareVersion(other: String): Int {
    val split = split('.')
    val otherSplit = other.split('.')

    loop@ for (i in 0 until max(split.count(), otherSplit.count())) {
        val a = split.getOrNull(i) ?: "0"
        val b = otherSplit.getOrNull(i) ?: "0"
        var compare = 0

        kotlin.runCatching {
            compare = a.toLong(0x10).compareTo(b.toLong(0x10))
        }.onFailure {
            compare = AlphanumComparator.compare(a, b)
        }
        if (compare != 0) return compare
    }

    return 0
}