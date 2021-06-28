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

package io.github.monun.tap.protocol

import org.bukkit.Location
import org.bukkit.Server
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.util.BoundingBox

@Suppress("NOTHING_TO_INLINE")
private inline fun Double.square() = this * this

interface PacketContainer {
    fun sendTo(player: Player)
}

fun Player.sendPacket(packet: PacketContainer) = packet.sendTo(this)

fun World.sendPacket(packet: PacketContainer) = players.forEach { packet.sendTo(it) }

fun World.sendPacketNearBy(
    box: BoundingBox,
    packet: PacketContainer,
    predicate: ((player: Player) -> Boolean)? = null
) {
    players.asSequence().filter {
        val loc = it.location
        box.contains(loc.x, loc.y, loc.z)
    }.let { players ->
        if (predicate == null) players else players.filter(predicate)
    }.forEach {
        packet.sendTo(it)
    }
}

fun World.sendPacketNearBy(
    x: Double,
    y: Double,
    z: Double,
    xRadius: Double,
    yRadius: Double,
    zRadius: Double,
    packet: PacketContainer,
    predicate: ((player: Player) -> Boolean)? = null
) = sendPacketNearBy(
    BoundingBox(
        x - xRadius, y - yRadius, z - zRadius,
        x + xRadius, y + yRadius, z + zRadius
    ), packet, predicate
)

fun World.sendPacketNearBy(
    x: Double,
    y: Double,
    z: Double,
    xzRadius: Double,
    yRadius: Double,
    packet: PacketContainer,
    predicate: ((player: Player) -> Boolean)? = null
) = sendPacketNearBy(
    BoundingBox(
        x - xzRadius, y - yRadius, z - xzRadius,
        x + xzRadius, y + yRadius, z + xzRadius
    ), packet, predicate
)

fun World.sendPacketNearBy(
    loc: Location,
    xRadius: Double,
    yRadius: Double,
    zRadius: Double,
    packet: PacketContainer,
    predicate: ((player: Player) -> Boolean)? = null
) = sendPacketNearBy(loc.x, loc.y, loc.z, xRadius, yRadius, zRadius, packet, predicate)

fun World.sendPacketNearBy(
    loc: Location,
    xzRadius: Double,
    yRadius: Double,
    packet: PacketContainer,
    predicate: ((player: Player) -> Boolean)? = null
) = sendPacketNearBy(loc.x, loc.y, loc.z, xzRadius, yRadius, packet, predicate)

fun Server.sendPacket(
    packet: PacketContainer,
    predicate: ((player: Player) -> Boolean)? = null
) = onlinePlayers.asSequence().let {
    if (predicate == null) it else it.filter(predicate)
}.forEach { packet.sendTo(it) }