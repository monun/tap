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

package com.github.noonmaru.tap.protocol

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.events.PacketContainer
import com.comphenix.protocol.wrappers.EnumWrappers
import com.comphenix.protocol.wrappers.WrappedDataWatcher
import com.github.noonmaru.tap.fake.createFakeEntity
import org.bukkit.FireworkEffect
import org.bukkit.Location
import org.bukkit.entity.*
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector
import java.util.*


val EntityPacket = EntityPacketSupport()

val EffectPacket = EffectPacketSupport()

class EntityPacketSupport {

    fun spawnMob(
        entityId: Int,
        uuid: UUID,
        typeId: Int,
        loc: Location,
        headPitch: Float,
        velocity: Vector
    ): PacketContainer {
        return PacketContainer(PacketType.Play.Server.SPAWN_ENTITY_LIVING).apply {
            integers
                .write(0, entityId)
            uuiDs
                .write(0, uuid)
            integers
                .write(1, typeId)
            doubles
                .write(0, loc.x)
                .write(1, loc.y)
                .write(2, loc.z)
            integers
                .write(2, (velocity.x.coerceIn(-3.9, 3.9) * 8000.0).toInt())
                .write(3, (velocity.y.coerceIn(-3.9, 3.9) * 8000.0).toInt())
                .write(4, (velocity.z.coerceIn(-3.9, 3.9) * 8000.0).toInt())
            bytes
                .write(0, (loc.yaw * 256.0F / 360.0F).toByte())
                .write(0, (loc.pitch * 256.0F / 360.0F).toByte())
                .write(0, (headPitch * 256.0F / 360.0F).toByte())
        }
    }

    fun metadata(entityId: Int, dataWatcher: WrappedDataWatcher): PacketContainer {
        return PacketContainer(PacketType.Play.Server.ENTITY_METADATA).apply {
            integers
                .write(0, entityId)
            watchableCollectionModifier
                .write(0, dataWatcher.deepClone().watchableObjects)
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
                .write(0, slot.convertToItemSlot())
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

    fun teleport(entity: Entity, loc: Location, onGround: Boolean = entity.isOnGround): PacketContainer {
        return entity.run {
            teleport(entityId, loc.x, loc.y, loc.z, loc.yaw, loc.pitch, onGround)
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

    fun lookAndRelativeMove(
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

    fun lookAndRelativeMove(entity: Entity) {
        return entity.run {
            val loc = entity.location

            lookAndRelativeMove(entityId, velocity, loc.yaw, loc.pitch, isOnGround)
        }
    }

    fun mount(entityId: Int, mountEntityIds: IntArray): PacketContainer {
        return PacketContainer(PacketType.Play.Server.MOUNT).apply {
            integers.write(0, entityId)
            integerArrays.write(0, mountEntityIds)
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

private fun EquipmentSlot.convertToItemSlot(): EnumWrappers.ItemSlot {
    return when (this) {
        EquipmentSlot.HAND -> EnumWrappers.ItemSlot.MAINHAND
        EquipmentSlot.OFF_HAND -> EnumWrappers.ItemSlot.OFFHAND
        EquipmentSlot.FEET -> EnumWrappers.ItemSlot.FEET
        EquipmentSlot.LEGS -> EnumWrappers.ItemSlot.LEGS
        EquipmentSlot.CHEST -> EnumWrappers.ItemSlot.CHEST
        EquipmentSlot.HEAD -> EnumWrappers.ItemSlot.HEAD
    }
}

class EffectPacketSupport {
    fun firework(loc: Location, effect: FireworkEffect): List<PacketContainer> {
        return Firework::class.java.createFakeEntity()!!.run {
            fireworkMeta = fireworkMeta.apply { addEffect(effect) }
            listOf(
                PacketContainer(PacketType.Play.Server.SPAWN_ENTITY).apply {
                    integers
                        .write(0, entityId)
                    uuiDs
                        .write(0, uniqueId)
                    doubles
                        .write(0, loc.x)
                        .write(1, loc.y)
                        .write(2, loc.z)
                    entityTypeModifier
                        .write(0, EntityType.FIREWORK)
                    integers
                        .write(6, 76)
                },
                EntityPacket.metadata(this),
                PacketContainer(PacketType.Play.Server.ENTITY_STATUS).apply {
                    integers
                        .write(0, entityId)
                    bytes
                        .write(0, 17.toByte())
                },
                EntityPacket.destroy(intArrayOf(entityId))
            )
        }
    }
}