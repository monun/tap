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

package io.github.monun.tap.protocol

import io.github.monun.tap.loader.LibraryLoader
import org.bukkit.FireworkEffect
import org.bukkit.Location
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.entity.LivingEntity
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
    companion object : PacketSupport by LibraryLoader.loadNMS(PacketSupport::class.java)

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

    fun entityMetadata(entity: Entity): PacketContainer

    fun entityEquipment(entityId: Int, equipments: Map<EquipmentSlot, ItemStack>): PacketContainer

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

    fun entityHeadLook(entityId: Int, yaw: Float): PacketContainer

    fun entityStatus(entityId: Int, data: Byte): PacketContainer

    fun entityAnimation(entityId: Int, action: Int): PacketContainer

    fun entityAnimation(entityId: Int, action: AnimationType): PacketContainer {
        return entityAnimation(entityId, action.ordinal)
    }

    fun mount(entityId: Int, mountEntityIds: IntArray): PacketContainer

    fun removeEntity(entityId: Int): PacketContainer

    fun removeEntities(vararg entityIds: Int): PacketContainer

    fun spawnFireworkParticles(x: Double, y: Double, z: Double, effect: FireworkEffect): List<PacketContainer>
}