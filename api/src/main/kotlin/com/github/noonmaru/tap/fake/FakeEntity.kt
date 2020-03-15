/*
 *
 *  * Copyright (c) 2020 Noonmaru
 *  *
 *  * Licensed under the General Public License, Version 3.0 (the "License");
 *  *  you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  * https://opensource.org/licenses/gpl-3.0
 *  *
 *  *  Unless required by applicable law or agreed to in writing, software
 *  *  distributed under the License is distributed on an "AS IS" BASIS,
 *  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  *  See the License for the specific language governing permissions and
 *  *  limitations under the License.
 *
 */

package com.github.noonmaru.tap.fake

import com.comphenix.protocol.events.PacketContainer
import com.github.noonmaru.tap.protocol.EntityPacket
import com.github.noonmaru.tap.protocol.sendServerPacket
import com.github.noonmaru.tap.protocol.sendServerPacketAll
import com.google.common.base.Preconditions
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.util.BoundingBox
import org.bukkit.util.Vector
import java.util.*
import kotlin.collections.HashSet

/**
 * @author Nemo
 */
open class FakeEntity internal constructor(private val entity: Entity) {

    internal lateinit var manager: FakeManager

    var glowing
        get() = entity.isGlowing
        set(value) {
            entity.isGlowing = value
            updateMeta = true
            enqueue()
        }

    var invisible
        get() = entity.invisible
        set(value) {
            entity.invisible = value
            updateMeta = true
            enqueue()
        }

    var customNameVisible
        get() = entity.isCustomNameVisible
        set(value) {
            entity.isCustomNameVisible = value
            updateMeta = true
            enqueue()
        }

    var customName
        get() = entity.customName
        set(value) {
            entity.customName = value
            updateMeta = true
            enqueue()
        }

    val boundingBox
        get() = entity.boundingBox

    private val lastUpdateLoc = entity.location

    private val _prevLoc: Location = lastUpdateLoc.clone()

    val prevLocation
        get() = _prevLoc.clone()

    private val _loc: Location = _prevLoc.clone()

    val location
        get() = _loc.clone()

    private val passengers = HashSet<FakeEntity>()

    private var vehicle: FakeEntity? = null

    internal val trackers = HashSet<Player>()

    var show = true
        set(value) {
            checkState()

            if (field != value) {
                field = value

                if (value) { //show
                    updateTrackers()
                } else {
                    trackers.run {
                        sendServerPacketAll(createDestroyPacket())
                        clear()
                    }
                }
            }
        }

    private val ignores = Collections.newSetFromMap(WeakHashMap<Player, Boolean>())

    var trackingRange = 128.0

    private var updateLoc = false

    protected var updateMeta = false

    private var queued = false

    var valid = true
        private set

    protected fun enqueue() {
        if (queued)
            return

        queued = true
        manager.enqueue(this)
    }

    fun setPosition(
        world: World = _prevLoc.world,
        x: Double = _prevLoc.x,
        y: Double = _prevLoc.y,
        z: Double = _prevLoc.z,
        yaw: Float = _prevLoc.yaw,
        pitch: Float = _prevLoc.pitch
    ) {
        vehicle?.removePassenger(this)

        _loc.apply {
            this.world = world
            this.x = x
            this.y = y
            this.z = z
            this.yaw = yaw
            this.pitch = pitch
        }

        passengers.forEach {
            updatePassengerPos(it)
        }

        updateLoc = true
        enqueue()
    }

    fun setPosition(loc: Location) {
        loc.run {
            setPosition(world, x, y, z, yaw, pitch)
        }
    }

    fun move(
        moveX: Double = 0.0,
        moveY: Double = 0.0,
        moveZ: Double = 0.0,
        yaw: Float = _prevLoc.yaw,
        pitch: Float = _prevLoc.pitch
    ) {
        _prevLoc.run {
            setPosition(_prevLoc.world, x + moveX, y + moveY, z + moveZ, yaw, pitch)
        }
    }

    fun move(v: Vector, yaw: Float = _prevLoc.yaw, pitch: Float = _prevLoc.pitch) {
        move(v.x, v.y, v.z, yaw, pitch)
    }

    open fun onUpdate() {
        queued = false

        if (updateLoc) {
            updateLoc = false
            updateLocation()
        }
        if (updateMeta) {
            updateMeta = false
            createMetaPacket()?.let { packet ->
                trackers.sendServerPacketAll(packet)
            }
        }
    }

    private fun updateLocation() {
        if (vehicle != null) return

        val from = lastUpdateLoc
        val to = _loc

        val deltaX = from.x delta to.x
        val deltaY = from.y delta to.y
        val deltaZ = from.z delta to.z
        val move = Vector(deltaX / 4096.0, deltaY / 4096.0, deltaZ / 4096.0)

        entity.setPositionAndRotation(to)

        if (from.world != to.world || (deltaX < -32768L || deltaX > 32767L || deltaY < -32768L || deltaY > 32767L || deltaZ < -32768L || deltaZ > 32767L)) { // Teleport
            from.set(to)
            trackers.sendServerPacketAll(EntityPacket.teleport(entity, to))
        } else { //Relative
            from.apply {
                world = to.world
                add(move)
                yaw = to.yaw
                pitch = to.pitch
            }

            val yaw = to.yaw
            val pitch = to.pitch

            val packet = if (from.yaw == yaw && from.pitch == pitch)
                EntityPacket.relativeMove(entity.entityId, move, false)
            else
                EntityPacket.lookAndRelativeMove(entity.entityId, move, yaw, pitch, false)

            trackers.sendServerPacketAll(packet)
        }

        _prevLoc.set(to)
        //Passenger update
        passengers.forEach { passenger ->
            updatePassengerPos(passenger)
        }
    }

    private fun updatePassengerPos(passenger: FakeEntity) {
        val loc = _loc

        passenger._prevLoc.set(passenger._loc)
        passenger.lastUpdateLoc.set(passenger._loc)
        passenger._loc.apply {
            world = loc.world
            x = loc.x
            y = loc.y + entity.mountedYOffset + entity.yOffset - 0.01
            z = loc.z
            yaw = loc.yaw
            pitch = loc.pitch
        }
        passenger.entity.setPositionAndRotation(passenger._loc)
    }

    internal fun updateTrackers() {
        if (!show) return

        val box = trackingRange.let { r -> BoundingBox.of(_prevLoc, r, r, r) }
        removeOutOfRangeTrackers(box.expand(16.0))

        val players = getNearbyPlayers(box)

        for (player in players) {
            if (player !in ignores && trackers.add(player)) {
                spawnTo(player)
            }
        }
    }

    protected open fun spawnTo(player: Player) {
        player.sendServerPacket(entity.createSpawnPacket())
        createMetaPacket()?.let { player.sendServerPacket(it) }
        vehicle?.let { player.sendServerPacket(it.createMountPacket()) }
        if (passengers.isNotEmpty())
            player.sendServerPacket(createMountPacket())
    }

    private fun getNearbyPlayers(box: BoundingBox): List<Player> {
        return manager.players.filter { player -> player.isValid && player.world == _loc.world && box.overlaps(player.boundingBox) }
    }

    private fun removeOutOfRangeTrackers(box: BoundingBox) {
        trackers.removeIf { player ->
            if (!player.isValid || player.world != _loc.world || !box.overlaps(player.boundingBox)) {
                player.sendServerPacket(createDestroyPacket())
                true
            } else
                false
        }
    }

    fun removeTracker(player: Player) {
        trackers.remove(player)
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : Entity> applyMetadata(applier: (entity: T) -> Unit) {
        applier.invoke(entity as T)
        updateMeta = true
        enqueue()
    }

    private fun checkPassenger(passenger: FakeEntity) {
        checkState()
        passenger.checkState()
        require(this != passenger) { "entity == passenger" }
        require(this.manager == passenger.manager) { "Different FakeManager" }
    }

    fun addPassenger(passenger: FakeEntity) {
        checkPassenger(passenger)

        passenger.vehicle?.let { vehicle ->
            vehicle.removePassenger(vehicle)
        }

        if (this.passengers.add(passenger)) {
            passenger.vehicle = this
            updatePassengerPos(passenger)
            notifyPassengers()
        }
    }

    fun removePassenger(passenger: FakeEntity) {
        checkPassenger(passenger)

        if (this.passengers.remove(passenger)) {
            passenger.vehicle = null
            notifyPassengers()
        }
    }

    private fun notifyPassengers() {
        trackers.sendServerPacketAll(createMountPacket())
    }

    fun showTo(player: Player) {
        ignores.remove(player)
    }

    fun hideTo(player: Player) {
        if (ignores.add(player) && trackers.remove(player)) {
            player.sendServerPacket(createDestroyPacket())
        }
    }

    fun canSeeTo(player: Player): Boolean {
        return player in ignores
    }

    protected open fun createMetaPacket(): PacketContainer? {
        return null
    }

    private fun createMountPacket(): PacketContainer {
        return EntityPacket.mount(entity.entityId, passengers.map { it.entity.entityId }.toIntArray())
    }

    private fun createDestroyPacket(): PacketContainer {
        return EntityPacket.destroy(intArrayOf(entity.entityId))
    }

    fun checkState() {
        Preconditions.checkState(valid, "Invalid $this")
    }

    fun remove() {
        vehicle?.removePassenger(this)

        passengers.apply {
            forEach {
                it.vehicle = null
            }
            clear()
        }

        val packet = EntityPacket.destroy(intArrayOf(entity.entityId))
        trackers.sendServerPacketAll(packet)
        valid = false
    }
}

infix fun Double.delta(to: Double): Long {
    return ((to - this) * 4096).toLong()
}

private fun Location.set(loc: Location) {
    world = loc.world
    x = loc.x
    y = loc.y
    z = loc.z
    yaw = loc.yaw
    pitch = loc.pitch
}