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

package io.github.monun.tap.fake.internal

import io.github.monun.tap.protocol.PacketContainer
import io.github.monun.tap.protocol.sendPacket
import io.github.monun.tap.ref.getValue
import io.github.monun.tap.ref.weaky
import org.bukkit.entity.Player

internal class FakeTracker(
    server: FakeEntityServerImpl,
    val player: Player
) {
    private val server by weaky(server)

    internal var location = player.location
        private set

    internal var valid = player.isOnline

    private val trackingEntities = HashSet<FakeEntityImpl>()

    fun update() {
        if (!valid) {
            if (player.isValid) {
                valid = true
                this.location = player.location
                broadcastSelf()
                return
            }
        }

        val prevLocation = this.location
        val currentLocation = player.location
        this.location = currentLocation

        if (prevLocation.world !== currentLocation.world
            || prevLocation.x != currentLocation.x
            || prevLocation.y != currentLocation.y
            || prevLocation.z != currentLocation.z
        ) {
            broadcastSelf()
        }
    }

    internal fun clear() {
        valid = false

        val player = player

        trackingEntities.apply {
            for (entity in this) {
                entity.removeTracker(this@FakeTracker)
                entity.despawnTo(player)
            }

            clear()
        }
    }

    internal fun broadcastSelf() {
        for (entity in server._entities) {
            entity.offerComputeQueue(this)
        }
    }

    internal fun removeEntity(entity: FakeEntityImpl) {
        this.trackingEntities -= entity
    }

    internal fun addEntity(entity: FakeEntityImpl) {
        this.trackingEntities += entity
    }

    internal fun clearEntities() {
        trackingEntities.clear()
    }

    internal fun destroy() {
        val player = player

        for (entity in trackingEntities) {
            entity.removeTracker(this@FakeTracker)
            entity.despawnTo(player)
        }
        clearEntities()
        valid = false
    }
}

internal fun Iterable<FakeTracker>.sendServerPacketAll(packet: PacketContainer) {
    for (tracker in this) {
        tracker.player.sendPacket(packet)
    }
}