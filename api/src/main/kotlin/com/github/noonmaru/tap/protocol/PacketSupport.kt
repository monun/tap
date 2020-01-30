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

package com.github.noonmaru.tap.protocol

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.events.PacketContainer
import com.comphenix.protocol.wrappers.EnumWrappers
import com.comphenix.protocol.wrappers.WrappedDataWatcher
import org.bukkit.Location
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.entity.LivingEntity
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector
import java.util.*

class EntityPacketSupport {

    fun mobSpawn(
        entityId: Int,
        uuid: UUID,
        type: EntityType,
        loc: Location,
        headPitch: Float,
        velocity: Vector
    ): PacketContainer {

        return PacketContainer(PacketType.Play.Server.SPAWN_ENTITY_LIVING).apply {
            integers
                .write(0, entityId)
            uuiDs
                .write(0, uuid)
            entityTypeModifier
                .write(1, type)
            doubles
                .write(0, loc.x)
                .write(1, loc.y)
                .write(2, loc.z)
            float
                .write(0, loc.yaw * 256.0F / 360.0F)
                .write(0, loc.pitch * 256.0F / 360.0F)
                .write(0, headPitch * 256.0F / 360.0F)
            integers
                .write(2, (velocity.x.coerceIn(-3.9, 3.9) * 8000.0).toInt())
                .write(3, (velocity.y.coerceIn(-3.9, 3.9) * 8000.0).toInt())
                .write(4, (velocity.z.coerceIn(-3.9, 3.9) * 8000.0).toInt())
        }
    }

    fun mobSpawn(living: LivingEntity): PacketContainer {
        return living.run {

            mobSpawn(entityId, uniqueId, living.type, location, location.yaw, living.velocity)
        }
    }

    fun metadata(entityId: Int, dataWatcher: WrappedDataWatcher): PacketContainer {

        return PacketContainer(PacketType.Play.Server.ENTITY_METADATA).apply {
            integers
                .write(0, entityId)
            dataWatcherModifier
                .write(0, dataWatcher.deepClone())
        }
    }

    fun metadata(entity: Entity): PacketContainer {

        return entity.run {
            metadata(entityId, WrappedDataWatcher.getEntityWatcher(entity))
        }
    }

    fun equipment(entityId: Int, slot: EquipmentSlot, item: ItemStack): PacketContainer {

        return PacketContainer(PacketType.Play.Server.ENTITY_EQUIPMENT).apply {
            integers
                .write(0, entityId)
            itemSlots
                .write(0, EnumWrappers.getItemSlotConverter().getSpecific(slot))
            itemModifier
                .write(0, item)
        }
    }

    fun equipment(living: LivingEntity): List<PacketContainer> {

        return living.run {
            val slots = EquipmentSlot.values()
            val packets = ArrayList<PacketContainer>(slots.count())

            if (this is ArmorStand) {
                for (slot in slots) {
                    packets += equipment(entityId, slot, getItem(slot))
                }
            } else {
                living.equipment?.also { equipment ->
                    for (slot in slots) {
                        val item = when (slot) {
                            EquipmentSlot.HAND -> equipment.itemInMainHand
                            EquipmentSlot.OFF_HAND -> equipment.itemInOffHand
                            EquipmentSlot.FEET -> equipment.boots
                            EquipmentSlot.LEGS -> equipment.leggings
                            EquipmentSlot.CHEST -> equipment.chestplate
                            EquipmentSlot.HEAD -> equipment.helmet
                        }

                        if (item != null)
                            packets += equipment(entityId, slot, item)
                    }
                }

            }

            packets
        }
    }

    fun teleport(
        entityId: Int,
        x: Double,
        y: Double,
        z: Double,
        yaw: Float,
        pitch: Float,
        onGround: Boolean
    ): PacketContainer {

        return PacketContainer(PacketType.Play.Server.ENTITY_TELEPORT).apply {
            integers
                .write(0, entityId)
            doubles
                .write(0, x)
                .write(1, y)
                .write(2, z)
            bytes
                .write(0, (yaw * 256.0F / 360.0F).toByte())
                .write(1, (pitch * 256.0F / 360.0F).toByte())
            booleans
                .write(0, onGround)
        }
    }

    fun teleport(entity: Entity): PacketContainer {

        return entity.run {
            val loc = entity.location
            teleport(entityId, loc.x, loc.y, loc.z, loc.yaw, loc.pitch, entity.isOnGround)
        }
    }

    fun relativeMove(entityId: Int, move: Vector, onGround: Boolean): PacketContainer {

        return PacketContainer(PacketType.Play.Server.REL_ENTITY_MOVE).apply {
            integers
                .write(0, entityId)
            shorts
                .write(0, (move.x * 4096.0).toShort())
                .write(1, (move.y * 4096.0).toShort())
                .write(2, (move.z * 4096.0).toShort())
            booleans
                .write(0, onGround)
        }
    }

    fun relativeMove(entity: Entity) {

        return entity.run {

            relativeMove(entityId, velocity, isOnGround)
        }
    }

    fun relativeMoveAndLook(
        entityId: Int,
        move: Vector,
        yaw: Float, pitch: Float,
        onGround: Boolean
    ): PacketContainer {

        return PacketContainer(PacketType.Play.Server.REL_ENTITY_MOVE_LOOK).apply {
            integers
                .write(0, entityId)
            shorts
                .write(0, (move.x * 4096.0).toShort())
                .write(1, (move.y * 4096.0).toShort())
                .write(2, (move.z * 4096.0).toShort())
            bytes
                .write(0, (yaw * 256.0 / 360.0).toByte())
                .write(1, (pitch * 256.0 / 360.0).toByte())
            booleans
                .write(0, onGround)
        }
    }

    fun relativeMoveAndLook(entity: Entity) {

        return entity.run {
            val loc = entity.location

            relativeMoveAndLook(entityId, velocity, loc.yaw, loc.pitch, isOnGround)
        }
    }

    fun destroy(entityIds: IntArray): PacketContainer {

        return PacketContainer(PacketType.Play.Server.ENTITY_DESTROY).apply {
            integerArrays
                .write(0, entityIds)
        }
    }

    fun destroy(entities: Array<out Entity>): PacketContainer {

        return destroy(IntArray(entities.size) { entities[it].entityId })
    }
}

val EntityPacket = EntityPacketSupport()