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

import com.github.noonmaru.tap.fake.createSpawnPacket
import com.github.noonmaru.tap.fake.setLocation
import com.github.noonmaru.tap.nfake.FakeEntity
import com.github.noonmaru.tap.protocol.EntityPacket
import com.github.noonmaru.tap.protocol.sendServerPacket
import com.github.noonmaru.tap.ref.UpstreamReference
import org.bukkit.Location
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.util.Vector

class FakeEntityImpl(
    world: FakeWorldImpl,
    override val bukkitEntity: Entity,
    location: Location
) : FakeEntity {
    private var worldRef = UpstreamReference(world)

    override val world: FakeWorldImpl
        get() = worldRef.get()

    override val location: Location
        get() = currentLocation.clone()

    private val deltaLocation: Location = location.clone()
    private val previousLocation: Location = location.clone()
    internal val currentLocation: Location = location.clone()

    private val trackers = HashSet<FakeTracker>()

    private var updateLocation = false
    private var updateMeta = false
    private var updateEquipment = false
    private var enqueued = false

    private var valid = true

    override fun moveTo(target: Location) {
        currentLocation.set(target)

        updateLocation = true
        enqueue()
    }

    override fun move(x: Double, y: Double, z: Double) {
        currentLocation.add(x, y, z)

        updateLocation = true
        enqueue()
    }

    override fun moveAndRotation(x: Double, y: Double, z: Double, yaw: Float, pitch: Float) {
        currentLocation.apply {
            add(x, y, z)
            this.yaw = yaw
            this.pitch = pitch
        }

        updateLocation = true
        enqueue()
    }

    private fun enqueue() {
        if (!enqueued) {
            enqueued = true
            world.server.enqueue(this)
        }
    }

    internal fun update() {
        enqueued = false

        if (updateMeta) {
            updateMeta = false

            trackers.sendServerPacketAll(EntityPacket.metadata(bukkitEntity))
        }

        if (updateLocation) {
            updateLocation = false

            updateLocation()
        }
    }

    private fun updateLocation() {
        val bukkitEntity = bukkitEntity

        val from = deltaLocation
        val to = currentLocation

        val deltaX = from.x delta to.x
        val deltaY = from.y delta to.y
        val deltaZ = from.z delta to.z
        val moveDelta = Vector(deltaX / 4096.0, deltaY / 4096.0, deltaZ / 4096.0)

        bukkitEntity.setLocation(to)
        removeOutOfRangeEntities(to)

        if (from.world != to.world) {
            world.removeEntity(this)
            worldRef = UpstreamReference(world.server.adaptWorld(to.world))
        } else if (deltaX < -32768L || deltaX > 32767L || deltaY < -32768L || deltaY > 32767L || deltaZ < -32768L || deltaZ > 32767L) { // Teleport
            from.set(to)
            trackers.sendServerPacketAll(EntityPacket.teleport(bukkitEntity, to))
        } else { //Relative
            val yaw = to.yaw
            val pitch = to.pitch

            val packet = EntityPacket.lookAndRelativeMove(
                bukkitEntity.entityId,
                deltaX.toShort(),
                deltaY.toShort(),
                deltaZ.toShort(),
                yaw,
                pitch,
                false
            )

            trackers.sendServerPacketAll(packet)

            from.apply {
                this.world = to.world
                add(moveDelta)
                this.yaw = to.yaw
                this.pitch = to.pitch
            }
        }

        val trackers = trackers

        world.findNearbyPlayers {

        }

        world.server.findNearbyEntities(currentLocation, 240.0) { entity ->
            if (trackers.add(entity)) {
                entity.addTracker(this)
                entity.spawnTo(player)
            }
        }
    }

    internal fun updateTrackers(center: Location) {

    }

    private fun removeOutOfRangeEntities(center: Location) {
        val maxDistanceSquared = (256.0 * 256.0)
        val i = trackers.iterator()

        while (i.hasNext()) {
            val tracker = i.next()
            val entityLocation = tracker.prevLocation

            if (entityLocation.world != center.world
                || center.distanceSquared(entityLocation) >= maxDistanceSquared
            ) {

                i.remove()
                tracker.removeEntity(this)
                despawnTo(tracker.player)
            }
        }
    }

    internal fun addTracker(tracker: FakeTracker) {
        this.trackers += tracker
    }

    internal fun removeTracker(tracker: FakeTracker) {
        this.trackers -= tracker
    }

    internal fun spawnTo(player: Player) {
        val bukkitEntity = bukkitEntity

        player.sendServerPacket(bukkitEntity.createSpawnPacket())
        player.sendServerPacket(EntityPacket.metadata(bukkitEntity))
    }

    internal fun despawnTo(player: Player) {
        player.sendServerPacket(EntityPacket.destroy(intArrayOf(bukkitEntity.entityId)))
    }

    override fun remove() {
        if (valid) {
            TODO()
            valid = false
        }
    }
}

infix fun Double.delta(to: Double): Long {
    return ((to - this) * 4096.0).toLong()
}

private fun Location.set(loc: Location) {
    world = loc.world
    x = loc.x
    y = loc.y
    z = loc.z
    yaw = loc.yaw
    pitch = loc.pitch
}