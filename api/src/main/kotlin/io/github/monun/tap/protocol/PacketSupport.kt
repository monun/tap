/*
 * Copyright 2021 Monun
 *
 * Licensed under the General Public License, Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://opensource.org/licenses/gpl-3.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package io.github.monun.tap.protocol

import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.events.PacketContainer
import com.comphenix.protocol.wrappers.WrappedDataWatcher
import io.github.monun.tap.loader.LibraryLoader
import org.bukkit.FireworkEffect
import org.bukkit.Location
import org.bukkit.Server
import org.bukkit.World
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector
import java.util.*

fun Double.toProtocolDelta(): Int {
    return (this.coerceIn(-3.9, 3.9) * 8000.0).toInt()
}

fun Float.toProtocolDegrees(): Int {
    val i = (this * 256.0F / 360.0F).toInt()
    return if (i < i.toFloat()) i - 1 else i
}

interface PacketSupport {
    companion object {
        val INSTANCE = LibraryLoader.load(PacketSupport::class.java)
    }

    fun spawnEntity(
        entityId: Int,
        uuid: UUID,
        x: Double,
        y: Double,
        z: Double,
        yaw: Float,
        pitch: Float,
        type: EntityType,
        objectId: Int,
        velocity: Vector = Vector()
    ): PacketContainer

    fun spawnEntityLiving(
        entityId: Int,
        uuid: UUID,
        typeId: Int,
        x: Double,
        y: Double,
        z: Double,
        yaw: Float,
        pitch: Float,
        roll: Float,
        velocity: Vector = Vector()
    ): PacketContainer

    fun entityMetadata(entityId: Int, dataWatcher: WrappedDataWatcher): PacketContainer

    fun entityMetadata(entity: Entity): PacketContainer {
        return entityMetadata(entity.entityId, WrappedDataWatcher.getEntityWatcher(entity))
    }

    fun entityEquipment(
        entityId: Int,
        equipments: Map<EquipmentSlot, ItemStack>
    ): PacketContainer

    fun entityEquipment(living: LivingEntity) = entityEquipment(living.entityId, living.equipment?.let { equipment ->
        EquipmentSlot.values().associateWith { equipment.getItem(it) }
    } ?: emptyMap())

    fun entityTeleport(
        entityId: Int,
        x: Double,
        y: Double,
        z: Double,
        yaw: Float,
        pitch: Float,
        onGround: Boolean
    ): PacketContainer

    fun entityTeleport(entity: Entity, loc: Location, onGround: Boolean = entity.isOnGround): PacketContainer {
        return entity.run {
            entityTeleport(entityId, loc.x, loc.y, loc.z, loc.yaw, loc.pitch, onGround)
        }
    }

    fun relEntityMove(entityId: Int, deltaX: Short, deltaY: Short, deltaZ: Short, onGround: Boolean): PacketContainer

    fun relEntityMove(entityId: Int, move: Vector, onGround: Boolean): PacketContainer {
        return relEntityMove(
            entityId,
            move.x.toProtocolDelta().toShort(),
            move.y.toProtocolDelta().toShort(),
            move.z.toProtocolDelta().toShort(),
            onGround
        )
    }

    fun relEntityMoveLook(
        entityId: Int,
        deltaX: Short,
        deltaY: Short,
        deltaZ: Short,
        yaw: Float,
        pitch: Float,
        onGround: Boolean
    ): PacketContainer

    fun relEntityMoveLook(
        entityId: Int,
        delta: Vector,
        yaw: Float,
        pitch: Float,
        onGround: Boolean
    ): PacketContainer {
        return relEntityMoveLook(
            entityId,
            delta.x.toProtocolDelta().toShort(),
            delta.y.toProtocolDelta().toShort(),
            delta.z.toProtocolDelta().toShort(),
            yaw, pitch, onGround
        )
    }

    fun entityStatus(entityId: Int, data: Byte): PacketContainer

    fun mount(entityId: Int, mountEntityIds: IntArray): PacketContainer

    fun entityDestroy(entityId: Int): PacketContainer

    fun spawnFireworkParticles(x: Double, y: Double, z: Double, effect: FireworkEffect): List<PacketContainer>
}

private val protocolManager = ProtocolLibrary.getProtocolManager()

fun Player.sendServerPacket(packet: PacketContainer) {
    protocolManager.sendServerPacket(this, packet)
}

fun Iterable<Player>.sendPacketAll(packet: PacketContainer) = forEach {
    it.sendServerPacket(packet)
}

fun World.sendServerPacket(packet: PacketContainer) {
    players.forEach { it.sendServerPacket(packet) }
}

fun Server.sendServerPacket(packet: PacketContainer) {
    onlinePlayers.forEach { it.sendServerPacket(packet) }
}