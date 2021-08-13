/*
 *
 *  * Copyright 2021 Monun
 *  *
 *  * Licensed under the General Public License, Version 3.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *      https://opensource.org/licenses/gpl-3.0
 *  *
 *  *  Unless required by applicable law or agreed to in writing, software
 *  *  distributed under the License is distributed on an "AS IS" BASIS,
 *  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  *  See the License for the specific language governing permissions and
 *  *  limitations under the License.
 *
 */

package io.github.monun.tap.v1_17.protocol

import com.mojang.datafixers.util.Pair
import io.github.monun.tap.fake.createFakeEntity
import io.github.monun.tap.protocol.PacketContainer
import io.github.monun.tap.protocol.PacketSupport
import io.github.monun.tap.protocol.toProtocolDegrees
import io.github.monun.tap.protocol.toProtocolDelta
import io.github.monun.tap.v1_17.fake.NMSEntityTypes
import io.netty.buffer.Unpooled
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.protocol.game.*
import org.bukkit.FireworkEffect
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftEntity
import org.bukkit.craftbukkit.v1_17_R1.inventory.CraftItemStack
import org.bukkit.craftbukkit.v1_17_R1.util.CraftVector
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.entity.Firework
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector
import java.util.*
import net.minecraft.world.entity.EquipmentSlot as NMSEquipmentSlot

private fun EquipmentSlot.toNMS(): NMSEquipmentSlot {
    return when (this) {
        EquipmentSlot.HAND -> NMSEquipmentSlot.MAINHAND
        EquipmentSlot.OFF_HAND -> NMSEquipmentSlot.OFFHAND
        EquipmentSlot.FEET -> NMSEquipmentSlot.FEET
        EquipmentSlot.LEGS -> NMSEquipmentSlot.LEGS
        EquipmentSlot.CHEST -> NMSEquipmentSlot.CHEST
        EquipmentSlot.HEAD -> NMSEquipmentSlot.HEAD
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
    ): NMSPacketContainer {
        val entityClass = type.entityClass ?: throw IllegalArgumentException("Unknown EntityType: ${type.name}")

        val packet = ClientboundAddEntityPacket(
            entityId,
            uuid,
            x,
            y,
            z,
            yaw,
            pitch,
            NMSEntityTypes.findType(entityClass),
            objectId,
            CraftVector.toNMS(velocity)
        )

        return NMSPacketContainer(packet)
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
    ): NMSPacketContainer {
        val byteBuf = FriendlyByteBuf(Unpooled.buffer())

        byteBuf.writeVarInt(entityId)
        byteBuf.writeUUID(uuid)
        byteBuf.writeVarInt(typeId)
        byteBuf.writeDouble(x)
        byteBuf.writeDouble(y)
        byteBuf.writeDouble(z)
        byteBuf.writeByte(yaw.toProtocolDegrees())
        byteBuf.writeByte(pitch.toProtocolDegrees())
        byteBuf.writeByte(roll.toProtocolDegrees())
        byteBuf.writeShort(velocity.x.toProtocolDelta())
        byteBuf.writeShort(velocity.y.toProtocolDelta())
        byteBuf.writeShort(velocity.z.toProtocolDelta())

        val packet = ClientboundAddMobPacket(byteBuf)
        return NMSPacketContainer(packet)
    }

    override fun entityMetadata(entity: Entity): NMSPacketContainer {
        entity as CraftEntity

        val entityId = entity.entityId
        val entityData = entity.handle.entityData

        val packet = ClientboundSetEntityDataPacket(entityId, entityData, true)
        return NMSPacketContainer(packet)
    }


    override fun entityEquipment(entityId: Int, equipments: Map<EquipmentSlot, ItemStack>): NMSPacketContainer {
        val packet = ClientboundSetEquipmentPacket(entityId, equipments.map { entry ->
            Pair(entry.key.toNMS(), CraftItemStack.asNMSCopy(entry.value))
        })
        return NMSPacketContainer(packet)
    }

    override fun entityTeleport(
        entityId: Int,
        x: Double,
        y: Double,
        z: Double,
        yaw: Float,
        pitch: Float,
        onGround: Boolean
    ): NMSPacketContainer {
        val byteBuf = FriendlyByteBuf(Unpooled.buffer())

        byteBuf.writeVarInt(entityId)
        byteBuf.writeDouble(x)
        byteBuf.writeDouble(y)
        byteBuf.writeDouble(z)
        byteBuf.writeByte(yaw.toProtocolDegrees())
        byteBuf.writeByte(pitch.toProtocolDegrees())
        byteBuf.writeBoolean(onGround)

        val packet = ClientboundTeleportEntityPacket(byteBuf)
        return NMSPacketContainer(packet)
    }

    override fun relEntityMove(
        entityId: Int,
        deltaX: Short,
        deltaY: Short,
        deltaZ: Short,
        onGround: Boolean
    ): NMSPacketContainer {
        val packet = ClientboundMoveEntityPacket.Pos(entityId, deltaX, deltaY, deltaZ, onGround)
        return NMSPacketContainer(packet)
    }

    override fun relEntityMoveLook(
        entityId: Int,
        deltaX: Short,
        deltaY: Short,
        deltaZ: Short,
        yaw: Float,
        pitch: Float,
        onGround: Boolean
    ): NMSPacketContainer {
        val packet = ClientboundMoveEntityPacket.PosRot(
            entityId,
            deltaX,
            deltaY,
            deltaZ,
            yaw.toProtocolDegrees().toByte(),
            pitch.toProtocolDegrees().toByte(),
            onGround
        )
        return NMSPacketContainer(packet)
    }

    override fun entityHeadLook(entityId: Int, yaw: Float): NMSPacketContainer {
        val byteBuf = FriendlyByteBuf(Unpooled.buffer())
        byteBuf.writeVarInt(entityId)
        byteBuf.writeByte(yaw.toProtocolDegrees())

        return NMSPacketContainer(ClientboundRotateHeadPacket(byteBuf))
    }

    override fun entityStatus(
        entityId: Int,
        data: Byte
    ): NMSPacketContainer {
        val byteBuf = FriendlyByteBuf(Unpooled.buffer())

        byteBuf.writeInt(entityId)
        byteBuf.writeByte(data.toInt())

        val packet = ClientboundEntityEventPacket(byteBuf)
        return NMSPacketContainer(packet)
    }

    override fun entityAnimation(
        entityId: Int,
        action: Int
    ): NMSPacketContainer {
        val byteBuf = FriendlyByteBuf(Unpooled.buffer())

        byteBuf.writeVarInt(entityId)
        byteBuf.writeByte(action)

        return NMSPacketContainer(ClientboundAnimatePacket(byteBuf))
    }

    override fun mount(
        entityId: Int,
        mountEntityIds: IntArray
    ): NMSPacketContainer {
        val byteBuf = FriendlyByteBuf(Unpooled.buffer())

        byteBuf.writeVarInt(entityId)
        byteBuf.writeVarIntArray(mountEntityIds)

        val packet = ClientboundSetPassengersPacket(byteBuf)
        return NMSPacketContainer(packet)
    }

    override fun removeEntity(
        entityId: Int
    ): NMSPacketContainer {
        val packet = ClientboundRemoveEntityPacket(entityId)
        return NMSPacketContainer(packet)
    }

    override fun removeEntities(vararg entityIds: Int): PacketContainer {
        return entityIds.map { id ->
            ClientboundRemoveEntityPacket(id)
        }.let { packets ->
            NMSMultiPacketContainer(packets)
        }
    }

    override fun spawnFireworkParticles(
        x: Double,
        y: Double,
        z: Double,
        effect: FireworkEffect
    ): List<NMSPacketContainer> {
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
            removeEntity(firework.entityId)
        )
    }
}