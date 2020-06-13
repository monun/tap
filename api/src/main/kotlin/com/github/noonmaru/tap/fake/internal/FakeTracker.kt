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

package com.github.noonmaru.tap.fake.internal

import com.comphenix.protocol.events.PacketContainer
import com.github.noonmaru.tap.protocol.sendServerPacket
import com.github.noonmaru.tap.ref.UpstreamReference
import org.bukkit.entity.Player

internal class FakeTracker(
    server: FakeServerImpl,
    val player: Player
) {
    private val serverRef = UpstreamReference(server)
    private val server
        get() = serverRef.get()

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

        trackingEntities.apply {
            for (entity in this) {
                entity.removeTracker(this@FakeTracker)
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
        tracker.player.sendServerPacket(packet)
    }
}