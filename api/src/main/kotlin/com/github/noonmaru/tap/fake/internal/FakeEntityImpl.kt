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

import com.github.noonmaru.tap.fake.FakeEntity
import com.github.noonmaru.tap.fake.createSpawnPacket
import com.github.noonmaru.tap.fake.setLocation
import com.github.noonmaru.tap.protocol.EntityPacket
import com.github.noonmaru.tap.protocol.sendServerPacket
import com.github.noonmaru.tap.ref.UpstreamReference
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector
import java.util.*

class FakeEntityImpl internal constructor(
    server: FakeServerImpl,
    override val bukkitEntity: Entity,
    location: Location
) : FakeEntity {
    private val serverRef = UpstreamReference(server)

    override val server: FakeServerImpl
        get() = serverRef.get()

    override val location: Location
        get() = currentLocation.clone()

    private val deltaLocation: Location = location.clone()
    private val previousLocation: Location = location.clone()
    private val currentLocation: Location = location.clone()

    private val trackers = HashSet<FakeTracker>()
    private val trackerComputeQueue = ArrayDeque<FakeTracker>()

    private var updateLocation = false
    private var updateTrackers = true
    private var updateMeta = false
    private var updateArmorStandItem = false
    private var enqueued = false

    internal var valid = true
        private set

    private lateinit var armorStandItemData: ItemData

    private var moveTicks = 0

    val isDone: Boolean
        get() = !updateLocation

    private inner class ItemData {
        private val itemsBySlot = EnumMap<EquipmentSlot, ItemStack>(EquipmentSlot::class.java)

        internal fun update() {
            val armorStand = bukkitEntity as ArmorStand

            for (slot in EquipmentSlot.values()) {
                val current = armorStand.getItem(slot)
                val old = itemsBySlot.put(slot, current)

                if ((old == null && current.type != Material.AIR)
                    || !current.isSimilar(old)
                ) {
                    trackers.sendServerPacketAll(EntityPacket.equipment(armorStand.entityId, slot, current))
                }
            }
        }
    }

    init {
        if (bukkitEntity is ArmorStand) {
            armorStandItemData = ItemData()
        }
    }

    override fun moveTo(target: Location) {
        if (previousLocation == target) {
            return
        }

        currentLocation.set(target)

        updateLocation = true
        moveTicks = 20
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

    internal fun enqueue() {
        if (!enqueued) {
            enqueued = true
            server.enqueue(this)
        }
    }

    internal fun offerComputeQueue(tracker: FakeTracker) {
        if (updateTrackers)
            return

        trackerComputeQueue.offer(tracker)
        enqueue()
    }

    internal fun update() {
        enqueued = false

        if (!valid) {
            for (tracker in trackers) {
                tracker.removeEntity(this@FakeEntityImpl)
            }
            despawn()
            clearTrackers()
            return
        }

        if (updateMeta) {
            updateMeta = false

            trackers.sendServerPacketAll(EntityPacket.metadata(bukkitEntity))
        }

        if (updateArmorStandItem) {
            updateArmorStandItem = false

            armorStandItemData.update()
        }

        if (updateLocation) {
            val result = updateLocation()

            if (result == MoveResult.TELEPORT || --moveTicks <= 0) {
                updateLocation = false
            }
        }

        if (updateTrackers) {
            updateTrackers = false

            updateTrackers()
            trackerComputeQueue.clear()
        }

        trackerComputeQueue.let { queue ->
            while (queue.isNotEmpty()) {
                computeTracker(queue.remove())
            }
        }
    }

    private enum class MoveResult {
        TELEPORT,
        REL_MOVE,
    }

    private fun updateLocation(): MoveResult {
        val bukkitEntity = bukkitEntity

        val from = deltaLocation
        val to = currentLocation

        val deltaX = from.x delta to.x
        val deltaY = from.y delta to.y
        val deltaZ = from.z delta to.z
        val moveDelta = Vector(deltaX / 4096.0, deltaY / 4096.0, deltaZ / 4096.0)

        val result =
            if (from.world !== to.world || deltaX < -32768L || deltaX > 32767L || deltaY < -32768L || deltaY > 32767L || deltaZ < -32768L || deltaZ > 32767L) { // Teleport
                deltaLocation.set(to)
                bukkitEntity.setLocation(to)
                trackers.sendServerPacketAll(EntityPacket.teleport(bukkitEntity, to))
                MoveResult.TELEPORT
            } else { //Relative move
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

                deltaLocation.apply {
                    add(moveDelta)
                    this.yaw = to.yaw
                    this.pitch = to.pitch
                }
                bukkitEntity.setLocation(deltaLocation)

                trackers.sendServerPacketAll(packet)
                MoveResult.REL_MOVE
            }

        previousLocation.set(currentLocation)
        return result
    }

    private fun updateTrackers() {
        for (tracker in server.trackers) {
            computeTracker(tracker)
        }
    }

    private fun computeTracker(tracker: FakeTracker) {
        if (tracker.dead) return

        val spawnDistance = 240.0 * 240.0
        val despawnDistance = 256.0 * 256.0
        val entityLocation = currentLocation
        val trackerLocation = tracker.location

        if (entityLocation.world === trackerLocation.world) {
            val distance = entityLocation.distance(trackerLocation)

            if (distance < despawnDistance) {
                if (distance < spawnDistance && trackers.add(tracker)) {
                    tracker.addEntity(this)
                    spawnTo(tracker.player)
                }

                return
            }
        }

        if (trackers.remove(tracker)) {
            tracker.removeEntity(this)
            despawnTo(tracker.player)
        }
    }

    internal fun removeTracker(tracker: FakeTracker) {
        trackers -= tracker
    }

    private fun spawnTo(player: Player) {
        val bukkitEntity = bukkitEntity

        player.sendServerPacket(bukkitEntity.createSpawnPacket())
        player.sendServerPacket(EntityPacket.metadata(bukkitEntity))

        if (bukkitEntity is ArmorStand) {
            EntityPacket.equipment(bukkitEntity).forEach { packet ->
                player.sendServerPacket(packet)
            }
        }
    }

    internal fun despawn() {
        trackers.sendServerPacketAll(EntityPacket.destroy(intArrayOf(bukkitEntity.entityId)))
    }

    internal fun clearTrackers() {
        trackers.clear()
    }

    internal fun despawnTo(player: Player) {
        player.sendServerPacket(EntityPacket.destroy(intArrayOf(bukkitEntity.entityId)))
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : Entity> metadata(test: T.() -> Boolean) {
        val entity = bukkitEntity as T

        if (test.invoke(entity)) {
            updateMeta = true
            enqueue()
        }
    }

    override fun armorStandItem(test: ArmorStand.() -> Boolean) {
        val entity = bukkitEntity as ArmorStand

        if (test.invoke(entity)) {
            updateArmorStandItem = true
            enqueue()
        }
    }

    override fun remove() {
        if (valid) {
            valid = false
            enqueue()
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