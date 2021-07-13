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

package io.github.monun.tap.effect

import io.github.monun.tap.protocol.PacketSupport
import io.github.monun.tap.protocol.sendPacket
import org.bukkit.FireworkEffect
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.util.BoundingBox
import org.bukkit.util.Vector

fun Player.playFirework(x: Double, y: Double, z: Double, effect: FireworkEffect) {
    PacketSupport.spawnFireworkParticles(x, y, z, effect).forEach { packet ->
        sendPacket(packet)
    }
}

fun World.playFirework(x: Double, y: Double, z: Double, effect: FireworkEffect, distance: Double = 128.0) {
    val packets = PacketSupport.spawnFireworkParticles(x, y, z, effect)

    val box = BoundingBox(
        x - distance,
        y - distance,
        z - distance,
        x + distance,
        y + distance,
        z + distance
    )

    players.forEach { player ->
        val playerLoc = player.location
        if (box.contains(playerLoc.x, playerLoc.y, playerLoc.z)) {
            for (packet in packets) {
                player.sendPacket(packet)
            }
        }
    }
}

fun World.playFirework(pos: Vector, effect: FireworkEffect, distance: Double = 128.0) =
    playFirework(pos.x, pos.y, pos.z, effect, distance)

fun World.playFirework(loc: Location, effect: FireworkEffect, distance: Double = 128.0) =
    playFirework(loc.x, loc.y, loc.z, effect, distance)