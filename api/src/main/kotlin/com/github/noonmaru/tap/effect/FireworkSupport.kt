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

package com.github.noonmaru.tap.effect

import com.github.noonmaru.tap.protocol.Packet
import com.github.noonmaru.tap.protocol.sendServerPacket
import org.bukkit.FireworkEffect
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.util.BoundingBox

fun Player.playFirework(loc: Location, effect: FireworkEffect) {
    Packet.spawnFireworkParticles(loc, effect).forEach { packet ->
        sendServerPacket(packet)
    }
}

fun World.playFirework(loc: Location, effect: FireworkEffect, distance: Double = 128.0) {
    val packets = Packet.spawnFireworkParticles(loc, effect)

    val world = loc.world
    val box = BoundingBox.of(loc, distance, distance, distance)

    world.players.forEach { player ->
        val playerLoc = player.location
        if (box.contains(playerLoc.x, playerLoc.y, playerLoc.z)) {
            for (packet in packets) {
                player.sendServerPacket(packet)
            }
        }
    }
}