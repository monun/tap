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

package io.github.monun.tap.util

import kotlin.math.max

/**
 * 버전을 비교합니다.
 *
 * 0.1 < 0.1.1
 *
 * 0.1 == 0.01
 * 
 * 0.1-beta < 0.1-release
 *
 * 0.1-final < 0.1
 * 
 */

enum class MavenVersionIdentifier(val priority: Int) {
    NONE(10),
    RELEASE(9),
    GA(8),
    FINAL(7),
    SNAPSHOT(6),
    RC(5),
    ZETA(4),
    BETA(3),
    ALPHA(2),
    DEV(1)
}

private val String.identifier: MavenVersionIdentifier
    get() {
        return if(equals("0")) {
            MavenVersionIdentifier.NONE
        } else {
            try {
                MavenVersionIdentifier.valueOf(uppercase())
            } catch (e: IllegalArgumentException) {
                throw RuntimeException("No such version identifier found: $this")
            }
        }
    }

private val String.isValidLong: Boolean 
    get() {
        return try {
            toLong(0x10)
            true
        } catch (e: NumberFormatException) {
            false
        }
    }

infix fun String.compareVersion(other: String): Int {
    if(!matches("""^\d+(\.\d+)*(-[a-zA-Z]*)?$""".toRegex()) || !other.matches("""^\d+(\.\d+)*(-[a-zA-Z]*)?$""".toRegex())) {
       throw RuntimeException("The version format does not valid.")
    }
    val split = replace("-", ".").split('.')
    val otherSplit = other.replace("-", ".").split('.')

    loop@ for (i in 0 until max(split.count(), otherSplit.count())) {
        val a = split.getOrNull(i) ?: "0"
        val b = otherSplit.getOrNull(i) ?: "0"
        var compare = 0
        val isLastInIndex = i == max(split.count(), otherSplit.count()) - 1
        
        kotlin.runCatching {
            compare = if(isLastInIndex && (!a.isValidLong || !b.isValidLong)) {
                a.identifier.priority.compareTo(b.identifier.priority)
            } else {
                a.toLong(0x10).compareTo(b.toLong(0x10))
            }
        }.onFailure {
            compare = AlphanumComparator.compare(a, b)
        }
        if (compare != 0) return compare
    }

    return 0
}
