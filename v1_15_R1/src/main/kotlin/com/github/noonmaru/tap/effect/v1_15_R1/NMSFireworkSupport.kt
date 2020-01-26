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

package com.github.noonmaru.tap.effect.v1_15_R1

import com.github.noonmaru.tap.effect.FireworkSupport
import net.minecraft.server.v1_15_R1.*
import org.bukkit.Bukkit
import org.bukkit.FireworkEffect
import org.bukkit.Location
import org.bukkit.craftbukkit.v1_15_R1.CraftWorld
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftPlayer
import org.bukkit.entity.Firework
import org.bukkit.entity.Player
import org.bukkit.World as BukkitWorld

/**
 * @author Nemo
 */
class NMSFireworkSupport : FireworkSupport() {

    private val nms: EntityFireworks =
        EntityFireworks(
            (Bukkit.getWorlds()[0] as CraftWorld).handle,
            0.0,
            0.0,
            0.0,
            ItemStack(Items.FIREWORK_ROCKET)
        )
    private val bukkit: Firework = nms.bukkitEntity as Firework

    override fun playFirework(player: Player, loc: Location, effect: FireworkEffect) {
        player as CraftPlayer
        val fireworks = EntityFireworks(player.handle.world, loc.x, loc.y, loc.z, ItemStack(Items.FIREWORK_ROCKET))
        val bukkit = fireworks.bukkitEntity as Firework
        val meta = bukkit.fireworkMeta
        meta.addEffect(effect)
        bukkit.fireworkMeta = meta

        player.handle.playerConnection.run {
            sendPacket(PacketPlayOutSpawnEntity(fireworks, 76))
            sendPacket(PacketPlayOutEntityMetadata(fireworks.id, fireworks.dataWatcher, true))
            sendPacket(PacketPlayOutEntityStatus(fireworks, 17.toByte()))
            sendPacket(PacketPlayOutEntityDestroy(fireworks.id))
        }

    }

    override fun playFirework(world: BukkitWorld, loc: Location, effect: FireworkEffect, distance: Double) {
        world as CraftWorld
        val x = loc.x
        val y = loc.y
        val z = loc.z

        val fireworks = EntityFireworks(world.handle, x, y, z, ItemStack(Items.FIREWORK_ROCKET))
        val bukkit = fireworks.bukkitEntity as Firework
        val meta = bukkit.fireworkMeta
        meta.addEffect(effect)
        bukkit.fireworkMeta = meta


        val spawnPacket = (PacketPlayOutSpawnEntity(fireworks, 76))
        val metaPacket = (PacketPlayOutEntityMetadata(fireworks.id, fireworks.dataWatcher, true))
        val statusPacket = (PacketPlayOutEntityStatus(fireworks, 17.toByte()))
        val destroyPacket = (PacketPlayOutEntityDestroy(fireworks.id))

        world.handle.sendPacketNearBy(x, y, z, distance) { player ->
            run {
                player.playerConnection.run {
                    sendPacket(spawnPacket)
                    sendPacket(metaPacket)
                    sendPacket(statusPacket)
                    sendPacket(destroyPacket)
                }
            }
        }
    }

}

internal inline fun WorldServer.sendPacketNearBy(
    x: Double,
    y: Double,
    z: Double,
    radius: Double,
    sender: (player: EntityPlayer) -> Unit
) {
    val squareRadius = radius * radius
    for (player in minecraftServer.playerList.players) {
        if (player.world === this) {
            val dx = x - player.locX()
            val dy = y - player.locY()
            val dz = z - player.locZ()
            if (dx * dx + dy * dy + dz * dz < squareRadius) {
                sender.invoke(player)
            }
        }
    }
}
