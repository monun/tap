/*
 * Copyright (C) 2022 Monun
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package io.github.monun.tap.fake

import io.github.monun.tap.protocol.AnimationType
import io.github.monun.tap.protocol.PacketContainer
import org.bukkit.EntityEffect
import org.bukkit.Location
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.inventory.EntityEquipment

interface FakeEntity<T : Entity> {
    val server: FakeEntityServer
    val bukkitEntity: T
    val location: Location
    val vehicle: FakeEntity<*>?
    val passengers: List<*>
    val valid: Boolean
    val dead: Boolean
    var isVisible: Boolean

    fun addPassenger(passenger: FakeEntity<*>): Boolean

    fun removePassenger(passenger: FakeEntity<*>): Boolean

    fun eject(): Boolean

    fun moveTo(target: Location)

    fun move(x: Double, y: Double, z: Double) {
        moveTo(location.add(x, y, z))
    }

    fun moveAndRotation(x: Double, y: Double, z: Double, yaw: Float, pitch: Float) {
        moveTo(location.add(x, y, z).apply {
            this.yaw = yaw
            this.pitch = pitch
        })
    }

    fun rotate(yaw: Float, pitch: Float)

    fun updateMetadata(applier: T.() -> Unit)

    fun updateEquipment(applier: EntityEquipment.() -> Unit)

    fun playEffect(data: Byte)

    @Suppress("DEPRECATION")
    fun playEffect(type: EntityEffect) = playEffect(type.data)

    fun playAnimation(action: Int)

    fun playAnimation(action: AnimationType)

    fun excludeTracker(player: Player)

    fun includeTracker(player: Player)

    fun broadcast(packet: () -> PacketContainer)

    fun broadcast(packet: PacketContainer) = broadcast { packet }

    fun broadcastImmediately(packet: PacketContainer) = broadcast { packet }

    fun remove()
}

fun FakeEntity<Player>.updateSkinParts(skinParts: FakeSkinParts) {
    updateMetadata {
        FakeSupportNMS.setSkinParts(this, skinParts.raw)
    }
}