/*
 * Copyright (c) 2020 Noonmaru
 *
 * Licensed under the General Public License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://opensource.org/licenses/gpl-2.0.php
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.github.noonmaru.tap.fake

import com.comphenix.protocol.wrappers.WrappedDataWatcher
import com.github.noonmaru.tap.protocol.EntityPacket
import com.github.noonmaru.tap.protocol.sendServerPacket
import com.github.noonmaru.tap.protocol.sendServerPacketAll
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
abstract class FakeEntity internal constructor(private val entity: Entity) {

    internal lateinit var manager: FakeEntityManager

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

    val world: World
        get() = entity.world

    val boundingBox
        get() = entity.boundingBox

    private var prevUpdateLocation: Location = entity.location

    private val prevLocation: Location = prevUpdateLocation.clone()

    private val location: Location = prevLocation.clone()

    val trackers = HashSet<Player>()

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

    fun getLocation(): Location {
        return prevLocation.clone()
    }

    fun getToLocation(): Location {
        return location.clone()
    }

    fun setPosition(
        world: World = prevLocation.world,
        x: Double = prevLocation.x,
        y: Double = prevLocation.y,
        z: Double = prevLocation.z,
        yaw: Float = prevLocation.yaw,
        pitch: Float = prevLocation.pitch
    ) {
        location.run {
            this.world = world
            this.x = x
            this.y = y
            this.z = z
            this.yaw = yaw
            this.pitch = pitch
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
        yaw: Float = prevLocation.yaw,
        pitch: Float = prevLocation.pitch
    ) {
        prevLocation.run {
            setPosition(prevLocation.world, x + moveX, y + moveY, z + moveZ, yaw, pitch)
        }
    }

    fun move(v: Vector, yaw: Float = prevLocation.yaw, pitch: Float = prevLocation.pitch) {
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
            updateMeta()
        }
    }

    private fun updateLocation() {
        val from = prevUpdateLocation
        val to = location

        val deltaX = from.x delta to.x
        val deltaY = from.y delta to.y
        val deltaZ = from.z delta to.z
        val move = Vector(deltaX / 4096.0, deltaY / 4096.0, deltaZ / 4096.0)

        entity.setPositionAndRotation(to)

        if (from.world == to.world && (deltaX < -32768L || deltaX > 32767L || deltaY < -32768L || deltaY > 32767L || deltaZ < -32768L || deltaZ > 32767L)) { //Relative
            prevUpdateLocation.run {
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

        } else {
            prevUpdateLocation.run {
                world = to.world
                x = to.x
                y = to.y
                z = to.z
                yaw = to.yaw
                pitch = to.pitch
            }

            val packet = to.run { EntityPacket.teleport(entity) }

            trackers.sendServerPacketAll(packet)
        }

        prevLocation.apply {
            this.world = to.world
            this.x = to.x
            this.y = to.y
            this.z = to.z
            this.yaw = to.yaw
            this.pitch = to.pitch
        }
    }

    internal fun updateTrackers() {
        val box = trackingRange.let { r -> BoundingBox.of(prevLocation, r, r, r) }
        removeOutOfRangeTrackers(box.expand(16.0))

        val players = getNearbyPlayers(box)

        for (player in players) {
            if (player !in ignores && trackers.add(player)) {
                spawnTo(player)
            }
        }
    }

    private fun getNearbyPlayers(box: BoundingBox): List<Player> {
        return manager.players.filter { player -> player.isValid && player.world == world && box.overlaps(player.boundingBox) }
    }

    private fun removeOutOfRangeTrackers(box: BoundingBox) {
        trackers.removeIf { player ->
            if (!player.isValid || world != player.world || !box.overlaps(player.boundingBox)) {
                destroyTo(player)
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

    private fun updateMeta() {
        val packet = EntityPacket.metadata(entity.entityId, WrappedDataWatcher.getEntityWatcher(entity))
        trackers.sendServerPacketAll(packet)
    }

    abstract fun spawnTo(player: Player)

    fun showTo(player: Player) {
        ignores.remove(player)
    }

    fun hideTo(player: Player) {
        if (ignores.add(player) && trackers.remove(player)) {
            destroyTo(player)
        }
    }

    fun canSeeTo(player: Player): Boolean {
        return player in ignores
    }

    private fun destroyTo(player: Player) {
        val packet = EntityPacket.destroy(intArrayOf(entity.entityId))

        player.sendServerPacket(packet)
    }

    fun remove() {
        valid = false
        val packet = EntityPacket.destroy(intArrayOf(entity.entityId))
        trackers.sendServerPacketAll(packet)
    }
}

infix fun Double.delta(to: Double): Long {
    return ((to - this) * 4096).toLong()
}