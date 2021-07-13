/*
 * Tap
 * Copyright (C) 2021 Monun
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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