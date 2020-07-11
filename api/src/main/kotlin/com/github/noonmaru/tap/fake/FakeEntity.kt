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

package com.github.noonmaru.tap.fake

import org.bukkit.Location
import org.bukkit.entity.Entity
import org.bukkit.inventory.EntityEquipment

interface FakeEntity {
    val server: FakeServer
    val bukkitEntity: Entity
    val location: Location
    val vehicle: FakeEntity?
    val passengers: List<FakeEntity>

    fun addPassenger(passenger: FakeEntity): Boolean

    fun removePassenger(passenger: FakeEntity): Boolean

    fun eject(): Boolean

    fun moveTo(target: Location)

    fun move(x: Double, y: Double, z: Double)

    fun moveAndRotation(x: Double, y: Double, z: Double, yaw: Float, pitch: Float)

    fun <T : Entity> metadata(applier: T.() -> Boolean)

    fun equipment(applier: EntityEquipment.() -> Boolean)

    fun remove()
}