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

package com.github.monun.tap.trail

import com.github.monun.tap.math.normalizeAndLength
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.util.Vector
import kotlin.math.max

fun trail(
    start: Location,
    vector: Vector,
    interval: Double,
    trailer: (world: World, x: Double, y: Double, z: Double) -> Unit
) {
    val length = vector.normalizeAndLength()
    val count = max(1, (length / interval).toInt())
    val world = start.world
    val x = start.x
    val y = start.y
    val z = start.z
    val deltaX = vector.x * interval
    val deltaY = vector.y * interval
    val deltaZ = vector.z * interval

    for (i in 0 until count) {
        trailer(
            world,
            x + deltaX * i,
            y + deltaY * i,
            z + deltaZ * i
        )
    }
}

fun trail(
    start: Location,
    end: Location,
    interval: Double,
    trailer: (world: World, x: Double, y: Double, z: Double) -> Unit
) {
    require(start.world === end.world) { "Differing worlds" }

    trail(start, Vector(end.x - start.x, end.y - start.y, end.z - start.z), interval, trailer)
}