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

package com.github.noonmaru.tap.packet

import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack

/**
 * @author Nemo
 */
interface EntityPacketSupport {

    fun mobSpawn(entity: LivingEntity): PacketContainer

    fun relativeMove(id: Int, deltaX: Short, deltaY: Short, deltaZ: Short, onGround: Boolean): PacketContainer

    fun relativeMove(id: Int, moveX: Double, moveY: Double, moveZ: Double, onGround: Boolean): PacketContainer {
        val deltaX = moveX * 4096
        val deltaY = moveY * 4096
        val deltaZ = moveZ * 4096

        return relativeMove(id, deltaX, deltaY, deltaZ, onGround)
    }

    fun relativeMoveAndLook(
        id: Int,
        deltaX: Short,
        deltaY: Short,
        deltaZ: Short,
        yaw: Float,
        pitch: Float,
        onGround: Boolean
    ): PacketContainer

    fun relativeMoveAndLook(
        id: Int,
        moveX: Double,
        moveY: Double,
        moveZ: Double,
        yaw: Float,
        pitch: Float,
        onGround: Boolean
    ): PacketContainer {
        val deltaX = moveX * 4096
        val deltaY = moveY * 4096
        val deltaZ = moveZ * 4096

        return relativeMoveAndLook(id, deltaX, deltaY, deltaZ, yaw, pitch, onGround)
    }

    fun teleport(entity: Entity): PacketContainer

    fun teleport(entity: Entity, x: Double, y: Double, z: Double, yaw: Float, pitch: Float): PacketContainer

    fun destroy(entityIds: IntArray): PacketContainer

    fun metadata(entity: Entity): PacketContainer

    fun equipment(id: Int, slot: EquipmentSlot, item: ItemStack): PacketContainer
}