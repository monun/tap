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

import io.github.monun.tap.protocol.PacketContainer
import net.minecraft.network.protocol.Packet
import net.minecraft.server.level.ServerPlayer
import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.craftbukkit.v1_17_R1.CraftServer
import org.bukkit.craftbukkit.v1_17_R1.CraftWorld
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer
import org.bukkit.entity.Player


class NMSPacketContainer(private val packet: Packet<*>) : PacketContainer {
    override fun sendTo(player: Player) {
        player as CraftPlayer

        sendTo(player.handle)
    }

    override fun sendAll() {
        SERVER.players.forEach { player ->
            sendTo(player)
        }
    }

    override fun sendNearBy(world: World, x: Double, y: Double, z: Double, radius: Double) {
        val dimension = (world as CraftWorld).handle.dimension()
        val squareRadius = radius * radius

        for (player in SERVER.players) {
            if (player.level.dimension() == dimension) {
                val dx = x - player.x
                val dy = y - player.y
                val dz = z - player.z

                if (dx * dx + dy * dy + dz * dz < squareRadius) {
                    sendTo(player)
                }
            }
        }
    }

    private fun sendTo(player: ServerPlayer) {
        player.connection?.send(packet, null)
    }

    companion object {
        private val SERVER = (Bukkit.getServer() as CraftServer).handle
    }
}