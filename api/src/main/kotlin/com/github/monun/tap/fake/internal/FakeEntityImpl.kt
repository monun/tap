/*
 * Copyright 2021 Monun
 *
 * Licensed under the Apache License, Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://opensource.org/licenses/gpl-3.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.monun.tap.fake.internal

import com.github.monun.tap.fake.FakeEntity
import com.github.monun.tap.fake.createSpawnPacket
import com.github.monun.tap.fake.mountedYOffset
import com.github.monun.tap.fake.setLocation
import com.github.monun.tap.protocol.Packet
import com.github.monun.tap.protocol.sendServerPacket
import com.github.monun.tap.ref.UpstreamReference
import com.google.common.collect.ImmutableList
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.inventory.EntityEquipment
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector
import java.util.*

class FakeEntityImpl internal constructor(
    server: FakeEntityServerImpl,
    override val bukkitEntity: Entity,
    location: Location
) : FakeEntity {
    private val serverRef = UpstreamReference(server)

    override val server: FakeEntityServerImpl
        get() = serverRef.get()

    override val location: Location
        get() = currentLocation.clone()

    private val deltaLocation: Location = location.clone()
    private val previousLocation: Location = location.clone()
    private val currentLocation: Location = location.clone()

    private val trackers = HashSet<FakeTracker>()
    private val trackerComputeQueue = ArrayDeque<FakeTracker>()

    override var vehicle: FakeEntityImpl? = null
        private set

    private val _passengers = HashSet<FakeEntityImpl>()
    override val passengers: List<FakeEntity>
        get() = ImmutableList.copyOf(_passengers)

    private val effects = LinkedList<Byte>()

    private var updateTrackers = true
    private var updateLocation = false
    private var updateTeleport = false
    private var updatePassengers = false
    private var updateMeta = false
    private var updateEquipment = false
    private var enqueued = false

    override var valid: Boolean = true
        private set

    override val dead: Boolean
        get() = !valid

    private lateinit var equipmentData: ItemData

    private inner class ItemData {
        private val itemsBySlot = EnumMap<EquipmentSlot, ItemStack>(EquipmentSlot::class.java)

        fun update() {
            val armorStand = bukkitEntity as ArmorStand

            for (slot in EquipmentSlot.values()) {
                val current = armorStand.getItem(slot)
                val old = itemsBySlot.put(slot, current)

                if ((old == null && current.type != Material.AIR)
                    || !current.isSimilar(old)
                ) {
                    trackers.sendServerPacketAll(Packet.entityEquipment(armorStand.entityId, slot, current))
                }
            }
        }
    }

    private var moveTicks = 0

    val isDone: Boolean
        get() = !updateLocation

    override var isVisible: Boolean = true
        get() = field && valid
        set(value) {
            checkState()

            if (field != value) {
                field = value

                updateTrackers = true
                enqueue()
            }
        }

    private val exclusion = Collections.newSetFromMap(WeakHashMap<Player, Boolean>())

    init {
        if (bukkitEntity is ArmorStand) {
            equipmentData = ItemData()
        }
    }

    private fun check(passenger: FakeEntity): FakeEntityImpl {
        val impl = passenger as FakeEntityImpl

        passenger.checkState()
        require(impl.server === this.server)

        return impl
    }

    override fun addPassenger(passenger: FakeEntity): Boolean {
        checkState()
        val impl = check(passenger)

        passenger.let {
            var entity: FakeEntityImpl? = this

            while (entity != null) {
                if (entity === it)
                    return false

                entity = entity.vehicle
            }

            passenger.eject()
        }

        impl.vehicle = this
        _passengers += impl
        updatePassengers = true
        enqueue()

        impl.updateLocation = true
        impl.enqueue()

        return true
    }

    override fun removePassenger(passenger: FakeEntity): Boolean {
        val impl = check(passenger)

        if (impl.vehicle !== this) return false

        impl.vehicle = null
        _passengers -= impl

        updatePassengers = true
        enqueue()

        impl.updateLocation = true
        impl.updateTeleport = true
        impl.enqueue()

        return true
    }

    override fun eject(): Boolean {
        return vehicle?.removePassenger(this) ?: false
    }

    override fun moveTo(target: Location) {
        if (dead || vehicle != null || previousLocation == target) {
            return
        }

        currentLocation.set(target)

        updateLocation = true
        updateTrackers = true
        moveTicks = 20
        enqueue()

        updatePassengerLocation()
    }

    private fun updatePassengerLocation() {
        for (passenger in _passengers) {
            passenger.updateLocation = true
            passenger.updateTrackers = true
            passenger.enqueue()

            passenger.updatePassengerLocation()
        }
    }

    internal fun enqueue() {
        if (!enqueued) {
            enqueued = true
            server.enqueue(this)
        }
    }

    internal fun offerComputeQueue(tracker: FakeTracker) {
        if (tracker in trackers) {
            computeTracker(tracker)
        }

        if (updateTrackers)
            return

        trackerComputeQueue.offer(tracker)
        enqueue()
    }

    internal fun update() {
        enqueued = false

        if (!valid) {
            _passengers.apply {
                for (passenger in this) {
                    passenger.vehicle = null
                }
                clear()
            }
            despawn()
            for (tracker in trackers) {
                tracker.removeEntity(this@FakeEntityImpl)
            }
            clearTrackers()
            return
        }

        if (updateMeta) {
            updateMeta = false

            trackers.sendServerPacketAll(Packet.entityMetadata(bukkitEntity))
        }

        if (updateEquipment) {
            updateEquipment = false

            equipmentData.update()
        }

        if (updatePassengers) {
            updatePassengers = false

            trackers.sendServerPacketAll(
                Packet.mount(
                    bukkitEntity.entityId,
                    _passengers.toIntArray()
                )
            )
        }

        if (updateLocation) {
            val result = updateLocation()

            if (result == MoveResult.TELEPORT
                || result == MoveResult.VEHICLE
                || --moveTicks <= 0
            ) {
                updateLocation = false
                moveTicks = 0
            }
        }

        if (updateTrackers) {
            updateTrackers = false

            updateTrackers()
            trackerComputeQueue.clear()
        }

        if (!isVisible) {
            trackerComputeQueue.clear()
        }

        trackerComputeQueue.let { queue ->
            while (queue.isNotEmpty()) {
                computeTracker(queue.remove())
            }
        }

        effects.let { effects ->
            while (effects.isNotEmpty()) {
                trackers.sendServerPacketAll(Packet.entityStatus(bukkitEntity.entityId, effects.remove()))
            }
        }
    }

    private enum class MoveResult {
        TELEPORT,
        REL_MOVE,
        VEHICLE
    }

    private fun updateLocation(): MoveResult {
        val bukkitEntity = bukkitEntity

        vehicle?.let { vehicle ->
            val yOffset = vehicle.bukkitEntity.mountedYOffset
            deltaLocation.mount(vehicle.deltaLocation, yOffset)
            previousLocation.mount(vehicle.previousLocation, yOffset)
            currentLocation.mount(vehicle.currentLocation, yOffset)
            bukkitEntity.setLocation(deltaLocation)

            for (tracker in trackers) {
                if (tracker !in vehicle.trackers) {
                    tracker.player.sendServerPacket(
                        Packet.entityTeleport(bukkitEntity, deltaLocation)
                    )
                }
            }

            return MoveResult.VEHICLE
        }

        val from = deltaLocation
        val to = currentLocation

        val deltaX = from.x delta to.x
        val deltaY = from.y delta to.y
        val deltaZ = from.z delta to.z
        val moveDelta = Vector(deltaX / 4096.0, deltaY / 4096.0, deltaZ / 4096.0)

        val result =
            if (updateTeleport || from.world !== to.world || deltaX < -32768L || deltaX > 32767L || deltaY < -32768L || deltaY > 32767L || deltaZ < -32768L || deltaZ > 32767L) { // Teleport
                updateTeleport = false
                deltaLocation.set(to)
                bukkitEntity.setLocation(to)
                trackers.sendServerPacketAll(Packet.entityTeleport(bukkitEntity, to))
                MoveResult.TELEPORT
            } else { //Relative move
                val yaw = to.yaw
                val pitch = to.pitch

                val packet = Packet.relEntityMoveLook(
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
        if (!tracker.valid) return

        if (isVisible && tracker.player !in exclusion) {
            val spawnDistanceSquared = 240.0 * 240.0
            val despawnDistanceSquared = 256.0 * 256.0
            val entityLocation = deltaLocation
            val trackerLocation = tracker.location

            if (entityLocation.world === trackerLocation.world) {
                val distanceSquared = entityLocation.distanceSquared(trackerLocation)

                if (distanceSquared < despawnDistanceSquared) {
                    if (distanceSquared < spawnDistanceSquared && trackers.add(tracker)) {
                        tracker.addEntity(this)
                        spawnTo(tracker.player)
                    }

                    return
                }
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
        player.sendServerPacket(Packet.entityMetadata(bukkitEntity))

        if (bukkitEntity is ArmorStand) {
            Packet.entityEquipment(bukkitEntity).forEach { packet ->
                player.sendServerPacket(packet)
            }
        }

        _passengers.let { passengers ->
            if (passengers.isNotEmpty()) {
                player.sendServerPacket(
                    Packet.mount(bukkitEntity.entityId, passengers.toIntArray())
                )
            }
        }

        vehicle?.let { vehicle ->
            player.sendServerPacket(
                Packet.mount(vehicle.bukkitEntity.entityId, vehicle._passengers.toIntArray())
            )
        }
    }

    internal fun despawn() {
        trackers.sendServerPacketAll(Packet.entityDestroy(intArrayOf(bukkitEntity.entityId)))
    }

    internal fun clearTrackers() {
        trackers.clear()
    }

    internal fun despawnTo(player: Player) {
        player.sendServerPacket(Packet.entityDestroy(intArrayOf(bukkitEntity.entityId)))
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : Entity> updateMetadata(applier: T.() -> Unit) {
        if (dead) return

        val entity = bukkitEntity as T
        applier(entity)
        updateMeta = true
        enqueue()
    }

    override fun updateEquipment(applier: EntityEquipment.() -> Unit) {
        if (dead) return

        val living = bukkitEntity as LivingEntity
        living.equipment?.let { equipment ->
            applier(equipment)
            updateEquipment = true
            enqueue()
        }
    }

    override fun playEffect(data: Byte) {
        if (dead) return

        effects += data
        enqueue()
    }

    override fun excludeTracker(player: Player) {
        if (exclusion.add(player)) {
            updateTrackers = true
            enqueue()
        }
    }

    override fun includeTracker(player: Player) {
        if (exclusion.remove(player)) {
            updateTrackers = true
            enqueue()
        }
    }

    fun checkState() {
        require(valid) {
            "Invalid ${this.javaClass.simpleName}@${System.identityHashCode(this).toString(0x10)}"
        }
    }

    override fun remove() {
        if (valid) {
            valid = false
            enqueue()
        }
    }
}

private fun Collection<FakeEntity>.toIntArray(): IntArray {
    val size = count()
    val array = IntArray(size)

    this.forEachIndexed { index, fakeEntity ->
        array[index] = fakeEntity.bukkitEntity.entityId
    }

    return array
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

private fun Location.mount(loc: Location, yOffset: Double) {
    world = loc.world
    x = loc.x
    y = loc.y + yOffset
    z = loc.z
}