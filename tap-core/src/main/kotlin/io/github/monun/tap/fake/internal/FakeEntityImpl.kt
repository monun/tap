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

import com.google.common.collect.ImmutableList
import io.github.monun.tap.fake.FakeEntity
import io.github.monun.tap.fake.createSpawnPacket
import io.github.monun.tap.fake.mountedYOffset
import io.github.monun.tap.fake.setLocation
import io.github.monun.tap.protocol.AnimationType
import io.github.monun.tap.protocol.PacketSupport
import io.github.monun.tap.protocol.sendPacket
import io.github.monun.tap.ref.getValue
import io.github.monun.tap.ref.weaky
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
    override val server by weaky(server)

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
                    trackers.sendServerPacketAll(
                        PacketSupport.entityEquipment(
                            armorStand.entityId,
                            mapOf(slot to current)
                        )
                    )
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

            trackers.sendServerPacketAll(PacketSupport.entityMetadata(bukkitEntity))
        }

        if (updateEquipment) {
            updateEquipment = false

            equipmentData.update()
        }

        if (updatePassengers) {
            updatePassengers = false

            trackers.sendServerPacketAll(
                PacketSupport.mount(
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
                trackers.sendServerPacketAll(PacketSupport.entityStatus(bukkitEntity.entityId, effects.remove()))
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
                    tracker.player.sendPacket(
                        PacketSupport.entityTeleport(bukkitEntity, deltaLocation)
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
                trackers.sendServerPacketAll(PacketSupport.entityTeleport(bukkitEntity, to))
                MoveResult.TELEPORT
            } else {
                val yaw = to.yaw
                val pitch = to.pitch

                val packet = if (deltaX == 0L && deltaY == 0L && deltaZ == 0L && from.pitch == to.pitch) {
                    PacketSupport.entityHeadLook(bukkitEntity.entityId, yaw)
                } else {
                    PacketSupport.relEntityMoveLook(
                        bukkitEntity.entityId,
                        deltaX.toShort(),
                        deltaY.toShort(),
                        deltaZ.toShort(),
                        yaw,
                        pitch,
                        false
                    )
                }

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

        player.sendPacket(bukkitEntity.createSpawnPacket())
        player.sendPacket(PacketSupport.entityMetadata(bukkitEntity))

        if (bukkitEntity is ArmorStand) {
            PacketSupport.entityEquipment(bukkitEntity).let { packet ->
                player.sendPacket(packet)
            }
        }

        _passengers.let { passengers ->
            if (passengers.isNotEmpty()) {
                player.sendPacket(
                    PacketSupport.mount(bukkitEntity.entityId, passengers.toIntArray())
                )
            }
        }

        vehicle?.let { vehicle ->
            player.sendPacket(
                PacketSupport.mount(vehicle.bukkitEntity.entityId, vehicle._passengers.toIntArray())
            )
        }
    }

    internal fun despawn() {
        trackers.sendServerPacketAll(PacketSupport.removeEntity((bukkitEntity.entityId)))
    }

    internal fun clearTrackers() {
        trackers.clear()
    }

    internal fun despawnTo(player: Player) {
        player.sendPacket(PacketSupport.removeEntity((bukkitEntity.entityId)))
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

    override fun playAnimation(action: Int) {
        trackers.sendServerPacketAll(PacketSupport.entityAnimation(bukkitEntity.entityId, action))
    }

    override fun playAnimation(action: AnimationType) {
        trackers.sendServerPacketAll(PacketSupport.entityAnimation(bukkitEntity.entityId, action))
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