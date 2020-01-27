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

package com.github.noonmaru.tap.v1_15_R1.packet

import com.github.noonmaru.tap.packet.EntityPacketSupport
import com.github.noonmaru.tap.packet.PacketContainer
import net.minecraft.server.v1_15_R1.*
import org.bukkit.craftbukkit.v1_15_R1.CraftEquipmentSlot
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftEntity
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftLivingEntity
import org.bukkit.craftbukkit.v1_15_R1.inventory.CraftItemStack
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack

/**
 * @author Nemo
 */
class NMSEntityPacketSupport : EntityPacketSupport {

    override fun mobSpawn(entity: LivingEntity): PacketContainer {
        entity as CraftLivingEntity
        return PacketPlayOutSpawnEntityLiving(entity.handle).wrap()
    }

    override fun relativeMove(
        id: Int,
        deltaX: Short,
        deltaY: Short,
        deltaZ: Short,
        onGround: Boolean
    ): PacketContainer {
        return PacketPlayOutEntity.PacketPlayOutRelEntityMove(id, deltaX, deltaY, deltaZ, onGround).wrap()
    }

    override fun relativeMoveAndLook(
        id: Int,
        deltaX: Short,
        deltaY: Short,
        deltaZ: Short,
        yaw: Float,
        pitch: Float,
        onGround: Boolean
    ): PacketContainer {
        return PacketPlayOutEntity.PacketPlayOutRelEntityMoveLook(
            id, deltaX, deltaY, deltaZ,
            (yaw * 255.0 / 360.0).toByte(),
            (pitch * 255.0 / 360.0).toByte(), onGround
        ).wrap()
    }

    override fun teleport(
        entity: Entity
    ): PacketContainer {
        entity as CraftEntity
        return PacketPlayOutEntityTeleport(entity.handle).wrap()
    }

    override fun teleport(
        entity: Entity,
        x: Double,
        y: Double,
        z: Double,
        yaw: Float,
        pitch: Float
    ): PacketContainer {
        entity as CraftEntity
        val nmsEntity = entity.handle
        val originX = nmsEntity.locX()
        val originY = nmsEntity.locY()
        val originZ = nmsEntity.locZ()
        val originYaw = nmsEntity.yaw
        val originPitch = nmsEntity.pitch

        nmsEntity.setPositionRaw(x, y, z)
        nmsEntity.yaw = yaw
        nmsEntity.pitch = pitch

        val packet = teleport(entity, x, y, z, yaw, pitch)

        nmsEntity.setPositionRaw(originX, originY, originZ)
        nmsEntity.yaw = originYaw
        nmsEntity.pitch = originPitch

        return packet
    }

    override fun metadata(entity: Entity): PacketContainer {
        entity as CraftEntity
        val nmsEntity = entity.handle

        return PacketPlayOutEntityMetadata(nmsEntity.id, nmsEntity.dataWatcher, true).wrap()
    }

    override fun equipment(id: Int, slot: EquipmentSlot, item: ItemStack): PacketContainer {
        return PacketPlayOutEntityEquipment(id, CraftEquipmentSlot.getNMS(slot), CraftItemStack.asNMSCopy(item)).wrap()
    }

    override fun destroy(entityIds: IntArray): PacketContainer {
        return PacketPlayOutEntityDestroy(*entityIds).wrap()
    }
}