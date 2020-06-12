/*
 * Copyright (c) $date.year Noonmaru
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

package com.github.noonmaru.tap.fake

import org.bukkit.Location
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Entity

interface FakeEntity {
    val server: FakeServer
    val bukkitEntity: Entity
    val location: Location

    fun moveTo(target: Location)

    fun move(x: Double, y: Double, z: Double)

    fun moveAndRotation(x: Double, y: Double, z: Double, yaw: Float, pitch: Float)

    fun <T : Entity> metadata(test: T.() -> Boolean)

    fun armorStandItem(test: ArmorStand.() -> Boolean)

    fun remove()
}