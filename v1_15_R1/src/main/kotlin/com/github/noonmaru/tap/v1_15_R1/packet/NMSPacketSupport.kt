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

import com.github.noonmaru.tap.packet.PacketContainer
import com.github.noonmaru.tap.packet.PacketSupport
import net.minecraft.server.v1_15_R1.Packet
import org.bukkit.craftbukkit.v1_15_R1.CraftWorld
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftPlayer
import org.bukkit.entity.Player
import org.bukkit.World as BukkitWorld

/**
 * @author Nemo
 */
class NMSPacketSupport : PacketSupport {
    override fun sendPacket(player: Player, packet: PacketContainer) {
        player as CraftPlayer
        player.handle.playerConnection.sendPacket((packet as NMSPacketContainer).packet)
    }

    override fun sendPacketNearBy(
        world: BukkitWorld,
        x: Double,
        y: Double,
        z: Double,
        radius: Double,
        packet: PacketContainer
    ) {
        world as CraftWorld
        val nmsWorld = world.handle
        val squaredRadius = radius.square()
        val nmsPacket = (packet as NMSPacketContainer).packet
        nmsWorld.players.forEach { player ->
            val distX = x - player.locX()
            val distY = y - player.locY()
            val distZ = z - player.locZ()

            if (distX.square() + distY.square() + distZ.square() < squaredRadius) {
                player.playerConnection.sendPacket(nmsPacket)
            }
        }
    }
}

private fun Double.square(): Double {
    return this * this
}

fun Packet<*>.wrap(): PacketContainer {
    return NMSPacketContainer(this)
}