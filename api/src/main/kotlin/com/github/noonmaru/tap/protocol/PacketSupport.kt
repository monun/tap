/*
 * Copyright (c) 2020 Noonmaru
 *
 *  Licensed under the General Public License, Version 3.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://opensource.org/licenses/gpl-3.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.noonmaru.tap.protocol

import com.comphenix.protocol.events.PacketContainer
import com.comphenix.protocol.wrappers.WrappedDataWatcher
import org.bukkit.FireworkEffect
import org.bukkit.Location
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.entity.LivingEntity
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector
import java.util.*

interface PacketSupport {
    fun spawnEntity(
        entityId: Int,
        uuid: UUID,
        x: Double,
        y: Double,
        z: Double,
        type: EntityType,
        objectId: Int

    ): PacketContainer

    fun spawnEntityLiving(
        entityId: Int,
        uuid: UUID,
        typeId: Int,
        loc: Location,
        headPitch: Float,
        velocity: Vector
    ): PacketContainer

    fun entityMetadata(entityId: Int, dataWatcher: WrappedDataWatcher): PacketContainer

    fun entityMetadata(entity: Entity): PacketContainer {
        return entityMetadata(entity.entityId, WrappedDataWatcher.getEntityWatcher(entity))
    }

    fun entityEquipment(entityId: Int, slot: EquipmentSlot, item: ItemStack): PacketContainer

    fun entityEquipment(living: LivingEntity): List<PacketContainer>

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
            (move.x * 4096.0).toInt().toShort(),
            (move.y * 4096.0).toInt().toShort(),
            (move.z * 4096.0).toInt().toShort(),
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
        yaw: Float, pitch: Float,
        onGround: Boolean
    ): PacketContainer {
        return relEntityMoveLook(
            entityId,
            (delta.x * 4096.0).toInt().toShort(),
            (delta.y * 4096.0).toInt().toShort(),
            (delta.z * 4096.0).toInt().toShort(),
            yaw,
            pitch,
            onGround
        )
    }

    fun mount(entityId: Int, mountEntityIds: IntArray): PacketContainer

    fun entityDestroy(entityIds: IntArray): PacketContainer

    fun spawnFireworkParticles(loc: Location, effect: FireworkEffect): List<PacketContainer>
}