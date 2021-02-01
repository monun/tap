/*
 * Copyright 2021 Monun
 *
 * Licensed under the Apache License, Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-3.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.monun.tap.v1_16_R1.protocol

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.events.PacketContainer
import com.comphenix.protocol.wrappers.EnumWrappers
import com.comphenix.protocol.wrappers.Pair
import com.comphenix.protocol.wrappers.WrappedDataWatcher
import com.github.monun.tap.fake.createFakeEntity
import com.github.monun.tap.protocol.PacketSupport
import org.bukkit.FireworkEffect
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.EntityType
import org.bukkit.entity.Firework
import org.bukkit.entity.LivingEntity
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector
import java.util.*

class NMSPacketSupport : PacketSupport {
    override fun spawnEntity(
        entityId: Int,
        uuid: UUID,
        x: Double,
        y: Double,
        z: Double,
        type: EntityType,
        objectId: Int
    ): PacketContainer {
        return PacketContainer(PacketType.Play.Server.SPAWN_ENTITY).apply {
            integers
                .write(0, entityId)
            uuiDs
                .write(0, uuid)
            doubles
                .write(0, x)
                .write(1, y)
                .write(2, z)
            entityTypeModifier
                .write(0, type)
            integers
                .write(6, objectId)
        }
    }

    override fun spawnEntityLiving(
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
                .write(0, (loc.yaw * 256.0F / 360.0F).toInt().toByte())
                .write(0, (loc.pitch * 256.0F / 360.0F).toInt().toByte())
                .write(0, (headPitch * 256.0F / 360.0F).toInt().toByte())
        }
    }

    override fun entityMetadata(entityId: Int, dataWatcher: WrappedDataWatcher): PacketContainer {
        return PacketContainer(PacketType.Play.Server.ENTITY_METADATA).apply {
            integers
                .write(0, entityId)
            watchableCollectionModifier
                .write(0, dataWatcher.deepClone().watchableObjects)
        }
    }

    override fun entityEquipment(
        entityId: Int,
        slot: EquipmentSlot,
        item: ItemStack
    ): PacketContainer {
        return PacketContainer(PacketType.Play.Server.ENTITY_EQUIPMENT).apply {
            integers
                .write(0, entityId)
            slotStackPairLists
                .write(0, Collections.singletonList(Pair(slot.convertToItemSlot(), item)))
        }
    }

    override fun entityEquipment(living: LivingEntity): List<PacketContainer> {
        val list = arrayListOf<Pair<EnumWrappers.ItemSlot, ItemStack>>()

        for (slot in EquipmentSlot.values()) {
            list.add(Pair(slot.convertToItemSlot(), living.equipment?.getItem(slot)?: ItemStack(Material.AIR)))
        }

        return Collections.singletonList(
            PacketContainer(PacketType.Play.Server.ENTITY_EQUIPMENT).apply {
                integers
                    .write(0, living.entityId)
                slotStackPairLists
                    .write(0, list)
            }
        )
    }

    override fun entityTeleport(
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
                .write(0, (yaw * 256.0F / 360.0F).toInt().toByte())
                .write(0, (pitch * 256.0F / 360.0F).toInt().toByte())
            booleans
                .write(0, onGround)
        }
    }

    override fun relEntityMove(
        entityId: Int,
        deltaX: Short,
        deltaY: Short,
        deltaZ: Short,
        onGround: Boolean
    ): PacketContainer {
        return PacketContainer(PacketType.Play.Server.REL_ENTITY_MOVE).apply {
            integers
                .write(0, entityId)
            shorts
                .write(0, deltaX)
                .write(1, deltaY)
                .write(2, deltaZ)
            booleans
                .write(0, onGround)
        }
    }

    override fun relEntityMoveLook(
        entityId: Int,
        deltaX: Short,
        deltaY: Short,
        deltaZ: Short,
        yaw: Float,
        pitch: Float,
        onGround: Boolean
    ): PacketContainer {
        return PacketContainer(PacketType.Play.Server.REL_ENTITY_MOVE_LOOK).apply {
            integers
                .write(0, entityId)
            shorts
                .write(0, deltaX)
                .write(1, deltaY)
                .write(2, deltaZ)
            bytes
                .write(0, (yaw * 256.0F / 360.0F).toInt().toByte())
                .write(1, (pitch * 256.0F / 360.0F).toInt().toByte())
            booleans
                .write(0, onGround)
        }
    }

    override fun mount(entityId: Int, mountEntityIds: IntArray): PacketContainer {
        return PacketContainer(PacketType.Play.Server.MOUNT).apply {
            integers
                .write(0, entityId)
            integerArrays
                .write(0, mountEntityIds)
        }
    }

    override fun entityDestroy(entityIds: IntArray): PacketContainer {
        return PacketContainer(PacketType.Play.Server.ENTITY_DESTROY).apply {
            integerArrays
                .write(0, entityIds)
        }
    }

    override fun spawnFireworkParticles(loc: Location, effect: FireworkEffect): List<PacketContainer> {
        val world = requireNotNull(loc.world) { "World cannot be null" }
        val firework = requireNotNull(Firework::class.java.createFakeEntity(world)) { "Failed to create Firework" }

        return firework.run {
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
                entityMetadata(this),
                PacketContainer(PacketType.Play.Server.ENTITY_STATUS).apply {
                    integers
                        .write(0, entityId)
                    bytes
                        .write(0, 17.toByte())
                },
                entityDestroy(intArrayOf(entityId))
            )
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
}