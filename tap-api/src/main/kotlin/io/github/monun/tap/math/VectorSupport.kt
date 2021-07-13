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

package io.github.monun.tap.math

import org.bukkit.util.Vector
import kotlin.math.cos
import kotlin.math.sin

/**
 * @author Noonmaru
 */
fun Iterable<Vector>.rotateAroundX(angle: Double) {
    val angleCos = cos(angle)
    val angleSin = sin(angle)

    forEach {
        val y = angleCos * it.y - angleSin * it.z
        val z = angleSin * it.y + angleCos * it.z
        it.y = y
        it.z = z
    }
}

fun Iterable<Vector>.rotateAroundY(angle: Double) {
    val angleCos = cos(angle)
    val angleSin = sin(angle)

    forEach {
        val x = angleCos * it.x + angleSin * it.z
        val z = -angleSin * it.x + angleCos * it.z
        it.x = x
        it.z = z
    }
}

fun Iterable<Vector>.rotateAroundZ(angle: Double) {
    val angleCos = cos(angle)
    val angleSin = sin(angle)

    forEach {
        val x = angleCos * it.x - angleSin * it.y
        val y = angleSin * it.x + angleCos * it.y
        it.x = x
        it.y = y
    }
}

fun Vector.divide(d: Double) {
    x /= d
    y /= d
    z /= d
}

fun Vector.normalizeAndLength(): Double {
    val length = length()

    x /= length
    y /= length
    z /= length

    return length
}