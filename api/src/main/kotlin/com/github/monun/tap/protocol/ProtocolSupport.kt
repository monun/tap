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

package com.github.monun.tap.protocol

import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.events.PacketContainer
import com.github.monun.tap.loader.LibraryLoader
import org.bukkit.entity.Player

val Packet = LibraryLoader.load(PacketSupport::class.java)

private val protocolManager
    get() = ProtocolLibrary.getProtocolManager()

fun Player.sendServerPacket(packet: PacketContainer) {
    protocolManager.sendServerPacket(this, packet)
}

fun Iterable<Player>.sendServerPacketAll(packet: PacketContainer) {
    protocolManager.let { pm ->
        for (player in this) {
            pm.sendServerPacket(player, packet)
        }
    }
}