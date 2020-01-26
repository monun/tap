/*
 * Copyright (c) 2020 Noonmaru
 *
 * Licensed under the General Public License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://opensource.org/licenses/gpl-2.0.php
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.github.noonmaru.tap.math

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