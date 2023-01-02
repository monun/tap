/*
 * Copyright (C) 2022 Monun
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package io.github.monun.tap.v1_19_3.protocol

import io.github.monun.tap.fake.PlayerInfoAction
import io.github.monun.tap.protocol.PacketContainer
import io.github.monun.tap.protocol.PacketSupport
import io.github.monun.tap.protocol.toProtocolDegrees
import io.netty.buffer.Unpooled
import it.unimi.dsi.fastutil.ints.IntArrayList
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.protocol.game.*
import net.minecraft.world.phys.Vec3
import org.bukkit.craftbukkit.v1_19_R2.entity.CraftEntity
import org.bukkit.craftbukkit.v1_19_R2.entity.CraftPlayer
import org.bukkit.craftbukkit.v1_19_R2.inventory.CraftItemStack
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector
import net.minecraft.world.entity.EquipmentSlot as NMSEquipmentSlot

class NMSPacketSupport : PacketSupport {
    companion object {
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

        private fun PlayerInfoAction.toNMS(): ClientboundPlayerInfoUpdatePacket.Action {
            return when (this) {
                PlayerInfoAction.ADD -> ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER
                PlayerInfoAction.GAME_MODE -> ClientboundPlayerInfoUpdatePacket.Action.UPDATE_GAME_MODE
                PlayerInfoAction.LATENCY -> ClientboundPlayerInfoUpdatePacket.Action.UPDATE_LATENCY
                PlayerInfoAction.DISPLAY_NAME -> ClientboundPlayerInfoUpdatePacket.Action.UPDATE_DISPLAY_NAME
                PlayerInfoAction.INITIALIZE_CHAT -> ClientboundPlayerInfoUpdatePacket.Action.INITIALIZE_CHAT
                PlayerInfoAction.LISTED -> ClientboundPlayerInfoUpdatePacket.Action.UPDATE_LISTED
                PlayerInfoAction.REMOVE -> throw IllegalArgumentException()
            }
        }
    }

    override fun entityMetadata(entity: Entity): NMSPacketContainer {
        entity as CraftEntity

        val entityId = entity.entityId
        val entityData = entity.handle.entityData

        val packet = ClientboundSetEntityDataPacket(entityId, entityData.nonDefaultValues!!)
        return NMSPacketContainer(packet)
    }


    override fun entityEquipment(entityId: Int, equipments: Map<EquipmentSlot, ItemStack>): NMSPacketContainer {
        val packet = ClientboundSetEquipmentPacket(entityId, equipments.map { entry ->
            com.mojang.datafixers.util.Pair(entry.key.toNMS(), CraftItemStack.asNMSCopy(entry.value))
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

    override fun entityRotation(entityId: Int, yaw: Float, pitch: Float, onGround: Boolean): PacketContainer {
        return NMSPacketContainer(
            ClientboundMoveEntityPacket.Rot(
                entityId,
                180.toByte(),
                pitch.toProtocolDegrees().toByte(),
                onGround
            )
        )
    }

    override fun entityHeadLook(entityId: Int, yaw: Float): NMSPacketContainer {
        val byteBuf = FriendlyByteBuf(Unpooled.buffer())
        byteBuf.writeVarInt(entityId)
        byteBuf.writeByte(yaw.toProtocolDegrees())

        return NMSPacketContainer(ClientboundRotateHeadPacket(byteBuf))
    }

    override fun entityVelocity(entityId: Int, vector: Vector): NMSPacketContainer {
        val packet = ClientboundSetEntityMotionPacket(entityId, Vec3(vector.x, vector.y, vector.z))

        return NMSPacketContainer(packet)
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

    override fun takeItem(
        entityId: Int,
        collectorId: Int,
        stackAmount: Int
    ): NMSPacketContainer {
        val packet = ClientboundTakeItemEntityPacket(entityId, collectorId, stackAmount)
        return NMSPacketContainer(packet)
    }

    override fun removeEntity(
        entityId: Int
    ): NMSPacketContainer {
        val packet = ClientboundRemoveEntitiesPacket(entityId)
        return NMSPacketContainer(packet)
    }

    override fun removeEntities(vararg entityIds: Int): PacketContainer {
        return NMSPacketContainer(ClientboundRemoveEntitiesPacket(IntArrayList((entityIds))))
    }

    override fun containerSetSlot(containerId: Int, stateId: Int, slot: Int, item: ItemStack?): NMSPacketContainer {
        return NMSPacketContainer(
            ClientboundContainerSetSlotPacket(
                containerId,
                stateId,
                slot,
                CraftItemStack.asNMSCopy(item)
            )
        )
    }

    override fun playerInfoAction(action: PlayerInfoAction, player: Player): PacketContainer {
        return NMSPacketContainer(
            if (action == PlayerInfoAction.REMOVE) ClientboundPlayerInfoRemovePacket(listOf(player.uniqueId))
            else ClientboundPlayerInfoUpdatePacket(action.toNMS(), (player as CraftPlayer).handle)
        )
    }
}