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

package com.github.monun.tap.util

import java.util.*

/**
 * This is an updated version with enhancements made by Daniel Migowski,
 * Andre Bogus, and David Koelle. Updated by David Koelle in 2017.
 *
 * To use this class:
 * Use the static "sort" method from the java.util.Collections class:
 * Collections.sort(your list, new AlphanumComparator());
 */
object AlphanumComparator : Comparator<String?> {
    private fun isDigit(ch: Char): Boolean {
        return ch.toInt() in 48..57
    }

    /** Length of string is passed in for improved efficiency (only need to calculate it once)  */
    private fun getChunk(s: String, slength: Int, marker: Int): String {
        var m = marker
        val chunk = StringBuilder()
        var c = s[m]
        chunk.append(c)
        m++
        if (isDigit(c)) {
            while (m < slength) {
                c = s[m]
                if (!isDigit(c)) break
                chunk.append(c)
                m++
            }
        } else {
            while (m < slength) {
                c = s[m]
                if (isDigit(c)) break
                chunk.append(c)
                m++
            }
        }
        return chunk.toString()
    }

    override fun compare(s1: String?, s2: String?): Int {
        if (s1 == null || s2 == null) {
            return 0
        }
        var thisMarker = 0
        var thatMarker = 0
        val s1Length = s1.length
        val s2Length = s2.length
        while (thisMarker < s1Length && thatMarker < s2Length) {
            val thisChunk = getChunk(s1, s1Length, thisMarker)
            thisMarker += thisChunk.length
            val thatChunk = getChunk(s2, s2Length, thatMarker)
            thatMarker += thatChunk.length

            // If both chunks contain numeric characters, sort them numerically
            var result: Int
            if (isDigit(thisChunk[0]) && isDigit(thatChunk[0])) {
                // Simple chunk comparison by length.
                val thisChunkLength = thisChunk.length
                result = thisChunkLength - thatChunk.length
                // If equal, the first different number counts
                if (result == 0) {
                    for (i in 0 until thisChunkLength) {
                        result = thisChunk[i] - thatChunk[i]
                        if (result != 0) {
                            return result
                        }
                    }
                }
            } else {
                result = thisChunk.compareTo(thatChunk)
            }
            if (result != 0) return result
        }
        return s1Length - s2Length
    }
}