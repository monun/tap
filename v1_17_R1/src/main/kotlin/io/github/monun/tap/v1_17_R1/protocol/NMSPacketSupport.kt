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

package io.github.monun.tap.v1_17_R1.protocol

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.events.PacketContainer
import com.comphenix.protocol.wrappers.EnumWrappers
import com.comphenix.protocol.wrappers.Pair
import com.comphenix.protocol.wrappers.WrappedDataWatcher
import io.github.monun.tap.fake.createFakeEntity
import io.github.monun.tap.protocol.PacketSupport
import io.github.monun.tap.protocol.toProtocolDegrees
import io.github.monun.tap.protocol.toProtocolDelta
import org.bukkit.FireworkEffect
import org.bukkit.entity.EntityType
import org.bukkit.entity.Firework
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector
import java.util.*

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

class NMSPacketSupport : PacketSupport {
    override fun spawnEntity(
        entityId: Int,
        uuid: UUID,
        x: Double,
        y: Double,
        z: Double,
        yaw: Float,
        pitch: Float,
        type: EntityType,
        objectId: Int,
        velocity: Vector
    ) = PacketContainer(PacketType.Play.Server.SPAWN_ENTITY).apply {
        integers
            .write(0, entityId)
        uuiDs
            .write(0, uuid)
        doubles
            .write(0, x)
            .write(1, y)
            .write(2, z)
        integers
            .write(1, velocity.x.toProtocolDelta())
            .write(2, velocity.y.toProtocolDelta())
            .write(3, velocity.z.toProtocolDelta())
            .write(4, yaw.toProtocolDegrees())
            .write(5, pitch.toProtocolDegrees())
        entityTypeModifier
            .write(0, type)
        integers
            .write(6, objectId)
    }

    override fun spawnEntityLiving(
        entityId: Int,
        uuid: UUID,
        typeId: Int,
        x: Double,
        y: Double,
        z: Double,
        yaw: Float,
        pitch: Float,
        roll: Float,
        velocity: Vector
    ) = PacketContainer(PacketType.Play.Server.SPAWN_ENTITY_LIVING).apply {
        integers
            .write(0, entityId)
        uuiDs
            .write(0, uuid)
        integers
            .write(1, typeId)
        doubles
            .write(0, x)
            .write(1, y)
            .write(2, z)
        integers
            .write(2, velocity.x.toProtocolDelta())
            .write(3, velocity.y.toProtocolDelta())
            .write(4, velocity.z.toProtocolDelta())
        bytes
            .write(0, yaw.toProtocolDegrees().toByte())
            .write(1, pitch.toProtocolDegrees().toByte())
            .write(2, roll.toProtocolDegrees().toByte())
    }

    override fun entityMetadata(
        entityId: Int,
        dataWatcher: WrappedDataWatcher
    ) = PacketContainer(PacketType.Play.Server.ENTITY_METADATA).apply {
        integers
            .write(0, entityId)
        watchableCollectionModifier
            .write(0, dataWatcher.deepClone().watchableObjects)
    }


    override fun entityEquipment(
        entityId: Int,
        equipments: Map<EquipmentSlot, ItemStack>
    ) = PacketContainer(PacketType.Play.Server.ENTITY_EQUIPMENT).apply {
        integers
            .write(0, entityId)
        slotStackPairLists
            .write(0, equipments.toList().map { Pair(it.first.convertToItemSlot(), it.second) })
    }

    override fun entityTeleport(
        entityId: Int,
        x: Double,
        y: Double,
        z: Double,
        yaw: Float,
        pitch: Float,
        onGround: Boolean
    ) = PacketContainer(PacketType.Play.Server.ENTITY_TELEPORT).apply {
        integers
            .write(0, entityId)
        doubles
            .write(0, x)
            .write(1, y)
            .write(2, z)
        bytes
            .write(0, yaw.toProtocolDegrees().toByte())
            .write(0, pitch.toProtocolDegrees().toByte())
        booleans
            .write(0, onGround)
    }

    override fun relEntityMove(
        entityId: Int,
        deltaX: Short,
        deltaY: Short,
        deltaZ: Short,
        onGround: Boolean
    ) = PacketContainer(PacketType.Play.Server.REL_ENTITY_MOVE).apply {
        integers
            .write(0, entityId)
        shorts
            .write(0, deltaX)
            .write(1, deltaY)
            .write(2, deltaZ)
        bytes
            .write(0, 0)
            .write(1, 0)
        booleans
            .write(0, onGround)
            .write(1, true)
            .write(2, false)
    }

    override fun relEntityMoveLook(
        entityId: Int,
        deltaX: Short,
        deltaY: Short,
        deltaZ: Short,
        yaw: Float,
        pitch: Float,
        onGround: Boolean
    ) = PacketContainer(PacketType.Play.Server.REL_ENTITY_MOVE_LOOK).apply {
        integers
            .write(0, entityId)
        shorts
            .write(0, deltaX)
            .write(1, deltaY)
            .write(2, deltaZ)
        bytes
            .write(0, yaw.toProtocolDegrees().toByte())
            .write(1, pitch.toProtocolDegrees().toByte())
        booleans
            .write(0, onGround)
            .write(1, true)
            .write(2, true)
    }

    override fun entityStatus(
        entityId: Int,
        data: Byte
    ) = PacketContainer(PacketType.Play.Server.ENTITY_STATUS).apply {
        integers
            .write(0, entityId)
        bytes
            .write(0, data)
    }

    override fun mount(
        entityId: Int,
        mountEntityIds: IntArray
    ) = PacketContainer(PacketType.Play.Server.MOUNT).apply {
        integers
            .write(0, entityId)
        integerArrays
            .write(0, mountEntityIds)
    }

    override fun entityDestroy(
        entityId: Int
    ) = PacketContainer(PacketType.Play.Server.ENTITY_DESTROY).apply {
        integers
            .write(0, entityId)
    }

    override fun spawnFireworkParticles(
        x: Double,
        y: Double,
        z: Double,
        effect: FireworkEffect
    ): List<PacketContainer> {
        val firework = requireNotNull(Firework::class.java.createFakeEntity()).apply {
            fireworkMeta = fireworkMeta.apply { addEffect(effect) }
        }

        return listOf(
            spawnEntity(
                firework.entityId,
                firework.uniqueId,
                x, y, z,
                0.0F, 0.0F,
                EntityType.FIREWORK,
                76
            ),
            entityMetadata(firework),
            entityStatus(firework.entityId, 17),
            entityDestroy(firework.entityId)
        )
    }
}