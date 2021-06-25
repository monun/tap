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

package io.github.monun.tap.effect

import io.github.monun.tap.protocol.PacketSupport
import io.github.monun.tap.protocol.sendServerPacket
import org.bukkit.FireworkEffect
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.util.BoundingBox
import org.bukkit.util.Vector

fun Player.playFirework(x: Double, y: Double, z: Double, effect: FireworkEffect) {
    PacketSupport.INSTANCE.spawnFireworkParticles(x, y, z, effect).forEach { packet ->
        sendServerPacket(packet)
    }
}

fun World.playFirework(x: Double, y: Double, z: Double, effect: FireworkEffect, distance: Double = 128.0) {
    val packets = PacketSupport.INSTANCE.spawnFireworkParticles(x, y, z, effect)

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
                player.sendServerPacket(packet)
            }
        }
    }
}

fun World.playFirework(pos: Vector, effect: FireworkEffect, distance: Double = 128.0) =
    playFirework(pos.x, pos.y, pos.z, effect, distance)

fun World.playFirework(loc: Location, effect: FireworkEffect, distance: Double = 128.0) =
    playFirework(loc.x, loc.y, loc.z, effect, distance)