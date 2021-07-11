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
        if(equals("0")) {
            return MavenVersionIdentifier.NONE
        } else {
            return try {
                MavenVersionIdentifier.valueOf(uppercase())
            } catch (e: IllegalArgumentException) {
                throw RuntimeException("No such version identifier found: ${this}")
            }
        }
    }

private val String.isValidLong: Boolean 
    get() {
        try {
            toLong(0x10)
            return true
        }
        catch (e: NumberFormatException) {
            return false
        }
    }
infix fun String.compareVersion(other: String): Int {
    if(!matches("""^\d+(\.\d+)*(\-[a-zA-Z]*)?$""".toRegex()) || !other.matches("""^\d+(\.\d+)*(\-[a-zA-Z]*)?$""".toRegex())) {
       throw RuntimeException("버전 양식에 맞지 않습니다.");
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
