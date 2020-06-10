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

package com.github.noonmaru.tap.nfake.internal

import com.comphenix.protocol.events.PacketContainer
import com.github.noonmaru.tap.protocol.sendServerPacket
import com.github.noonmaru.tap.ref.UpstreamReference
import org.bukkit.Location
import org.bukkit.entity.Player

internal class FakeTracker(
    server: FakeServerImpl,
    val player: Player
) {
    private val serverRef = UpstreamReference(server)
    private val server
        get() = serverRef.get()

    internal var prevLocation = player.location
    private val trackingEntities = HashSet<FakeEntityImpl>()

    fun update() {
        val prevLocation = this.prevLocation
        val currentLocation = player.location
        this.prevLocation = currentLocation

        if (prevLocation.world != currentLocation.world
            || prevLocation.x != currentLocation.x
            || prevLocation.y != currentLocation.y
            || prevLocation.z != currentLocation.z
        ) {
            removeOutOfRangeEntities(currentLocation)

            val trackingEntities = trackingEntities

            server.adaptWorld(currentLocation.world).findNearbyEntities(currentLocation, 240.0) { entity ->
                if (trackingEntities.add(entity)) {
                    entity.addTracker(this)
                    entity.spawnTo(player)
                }
            }
        }
    }

    private fun removeOutOfRangeEntities(center: Location) {
        val maxDistanceSquared = (256.0 * 256.0)
        val i = trackingEntities.iterator()

        while (i.hasNext()) {
            val entity = i.next()
            val entityLocation = entity.currentLocation

            if (entityLocation.world != center.world
                || center.distanceSquared(entityLocation) >= maxDistanceSquared
            ) {

                i.remove()
                entity.removeTracker(this)
                entity.despawnTo(player)
            }
        }
    }

    internal fun removeEntity(entity: FakeEntityImpl) {
        this.trackingEntities -= entity
    }
}

internal fun Iterable<FakeTracker>.sendServerPacketAll(packet: PacketContainer) {
    for (tracker in this) {
        tracker.player.sendServerPacket(packet)
    }
}