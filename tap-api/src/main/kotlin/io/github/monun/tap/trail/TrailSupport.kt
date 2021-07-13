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

package io.github.monun.tap.trail

import io.github.monun.tap.math.normalizeAndLength
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.util.Vector
import kotlin.math.max

object TrailSupport {
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
}
