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

package io.github.monun.tap.math

import org.bukkit.Location
import org.bukkit.util.Vector

fun Location.copy(other: Location) {
    world = other.world
    set(other.x, other.y, other.z)
    yaw = other.yaw
    pitch = other.pitch
}

infix fun Location.vector(target: Location): Vector {
    return Vector(target.x - x, target.y - y, target.z - z)
}