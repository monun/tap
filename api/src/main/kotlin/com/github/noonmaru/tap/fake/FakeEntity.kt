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

import com.github.noonmaru.tap.packet.EntityPacket
import com.github.noonmaru.tap.packet.sendPacket
import com.github.noonmaru.tap.packet.sendPacketAll
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.util.BoundingBox

/**
 * @author Nemo
 */
abstract class FakeEntity internal constructor(internal open val entity: Entity) {

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

    private var prevUpdateLocation: Location

    var prevLocation: Location
        private set
        get() = field.clone()


    var location: Location
        private set
        get() = field.clone()

    val trackers = HashSet<Player>()

    var trackingRange = 128.0

    private var updateLoc = false

    protected var updateMeta = false

    private var queued = false

    var valid = true
        private set

    init {
        entity.let {
            this.prevLocation = it.location
            this.location = prevLocation.clone()
            this.prevUpdateLocation = prevLocation.clone()
        }
    }

    protected fun enqueue() {
        if (queued)
            return

        queued = true
        manager.enqueue(this)
    }

    fun setPosition(
        x: Double = prevLocation.x,
        y: Double = prevLocation.y,
        z: Double = prevLocation.z,
        yaw: Float = prevLocation.yaw,
        pitch: Float = prevLocation.pitch
    ) {
        location.run {
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
            setPosition(x, y, z, yaw, pitch)
        }
    }

    open fun onUpdate() {
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

        to.run {
            entity.setPositionAndRotation(world, x, y, z, yaw, pitch)
        }

        if (from.world == to.world && (deltaX < -32768L || deltaX > 32767L || deltaY < -32768L || deltaY > 32767L || deltaZ < -32768L || deltaZ > 32767L)) { //Relative
            prevUpdateLocation.run {
                x += deltaX / 4096.0
                y += deltaY / 4096.0
                z += deltaZ / 4096.0
            }

            val yaw = to.yaw
            val pitch = to.pitch

            val packet = if (from.yaw == yaw && from.pitch == pitch)
                EntityPacket.relativeMove(entity.entityId, deltaX.toShort(), deltaY.toShort(), deltaZ.toShort(), false)
            else
                EntityPacket.relativeMoveAndLook(
                    entity.entityId,
                    deltaX.toShort(),
                    deltaY.toShort(),
                    deltaZ.toShort(),
                    yaw,
                    pitch, false
                )

            trackers.sendPacketAll(packet)

        } else {
            prevUpdateLocation.run {
                x = to.x
                y = to.y
                z = to.z
                yaw = to.yaw
                pitch = to.pitch
            }

            val packet = EntityPacket.teleport(entity)

            trackers.sendPacketAll(packet)
        }

        prevLocation.apply {
            x = to.x
            y = to.y
            z = to.z
            yaw = to.yaw
            pitch = to.pitch
        }
    }

    internal fun updateTrackers() {
        val box = (trackingRange / 2).let { r -> BoundingBox.of(prevLocation, r, r, r) }
        removeOutOfRangeTrackers(box.expand(16.0))

        val players = entity.world.getNearbyEntities(box) { entity -> entity is Player && entity.isValid }
        for (player in players) {
            player as Player

            if (trackers.add(player)) {
                spawnTo(player)
            }
        }
    }

    private fun removeOutOfRangeTrackers(box: BoundingBox) {
        trackers.removeIf { player ->
            if (prevLocation.world != player.world || !box.overlaps(player.boundingBox)) {
                val packet = EntityPacket.destroy(intArrayOf(player.entityId))
                player.sendPacket(packet)
                true
            } else
                false
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : Entity> applyMetadata(applier: (entity: T) -> Boolean) {
        if (applier.invoke(entity as T)) {
            updateMeta = true
            enqueue()
        }
    }

    private fun updateMeta() {
        val packet = EntityPacket.metadata(entity)
        trackers.sendPacketAll(packet)
    }

    abstract fun spawnTo(player: Player)

    fun remove() {
        valid = false
    }
}

infix fun Double.delta(to: Double): Long {
    return ((to - this) * 4096).toLong()
}