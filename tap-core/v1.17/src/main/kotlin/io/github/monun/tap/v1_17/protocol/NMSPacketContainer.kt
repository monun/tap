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

import io.github.monun.tap.protocol.PacketContainer
import net.minecraft.network.protocol.Packet
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer
import org.bukkit.entity.Player


class NMSPacketContainer(private val packet: Packet<*>) : PacketContainer {
    override fun sendTo(player: Player) {
        (player as CraftPlayer).handle.connection.send(packet, null)
    }
}

class NMSMultiPacketContainer(private val packets: List<Packet<*>>) : PacketContainer {
    override fun sendTo(player: Player) {
        (player as CraftPlayer).handle.connection.run {
            packets.forEach { send(it) }
        }
    }
}